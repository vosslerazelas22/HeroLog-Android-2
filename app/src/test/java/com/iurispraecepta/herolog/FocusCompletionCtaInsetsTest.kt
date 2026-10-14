package com.iurispraecepta.herolog

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import com.iurispraecepta.herolog.ui.focus.PendingCelebrationOverlay
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regressão do bug do CTA "Continuar" fora da tela (PR #41, commit 73aacf0).
 *
 * Causa raiz: `navigationBarsPadding()` + `padding(bottom = 16.dp)` estavam
 * DEPOIS do `height(56.dp)` no modifier do botão (`CompletionShell`), então o
 * inset era consumido DENTRO dos 56dp fixos — altura dourada visível de
 * `56 − inset − 16` (0dp com navegação de 3 botões, ~16dp com gestos).
 * Correção: inset/padding movidos para ANTES do `height(56.dp)` (fora do
 * tamanho fixo, como o footer `p-4 pb-[calc(1rem+safe)]` do React
 * `FocusCompletionFlow.tsx:103`) + coluna com `bottom = 0.dp`.
 *
 * Usa o [PendingCelebrationOverlay] REAL com celebração SINGLE (2 steps:
 * summary → notes, botão "Continuar" na primeira tela).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class FocusCompletionCtaInsetsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun entity(id: String, completedAt: Long, historyId: String? = null) =
        PendingRewardCelebrationEntity(
            id = id,
            sessionCompletedAt = completedAt,
            skillName = "Kotlin",
            durationMinutes = 25,
            xpGained = 60,
            goldGained = 90,
            historyId = historyId
        )

    private fun singleSession(): AggregatedCelebrationSummary = aggregateCelebrations(
        listOf(entity("s1", completedAt = 1_000L, historyId = "h-cta")),
        JsonConfig.default
    )

    /**
     * Injeta um inset inferior de barra de navegação falso direto na ComposeView
     * (desvia do consumo do `fitsSystemWindows` do decorView). Disparado DEPOIS
     * do setContent: o holder de insets do Compose reage ao dispatch e recompõe.
     */
    private fun setNavigationBarsBottomInset(bottom: Dp) {
        val bottomPx = with(composeTestRule.density) { bottom.roundToPx() }
        composeTestRule.runOnUiThread {
            val insets = WindowInsetsCompat.Builder()
                .setInsets(
                    WindowInsetsCompat.Type.navigationBars(),
                    Insets.of(0, 0, 0, bottomPx)
                )
                .build()
            val content = composeTestRule.activity
                .findViewById<ViewGroup>(android.R.id.content)
            ViewCompat.dispatchApplyWindowInsets(content.getChildAt(0), insets)
        }
        composeTestRule.waitForIdle()
    }

    private fun renderOverlay() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                PendingCelebrationOverlay(
                    summary = singleSession(),
                    exitFocusModeOnDismiss = false,
                    onExitFocusMode = {},
                    onConfirmSingle = { _, _, _ -> },
                    onConfirmAggregated = {},
                    skills = emptyList(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun assertCtaGeometry(navInset: Dp) {
        val density = composeTestRule.density
        composeTestRule.onNodeWithTag("completion_cta").assertIsDisplayed()
        val cta = composeTestRule.onNodeWithTag("completion_cta").fetchSemanticsNode()
        val root = composeTestRule.onRoot().fetchSemanticsNode()

        // O inset não pode mais espremer o botão: altura dourada sempre 56dp.
        val expectedHeightPx = with(density) { 56.dp.roundToPx() }
        assertEquals(
            "altura do CTA com inset de $navInset (era ${56.dp - navInset - 16.dp} no bug)",
            expectedHeightPx,
            cta.size.height
        )

        // Espelho do footer React (`p-4 pb-[calc(1rem+safe)]`): abaixo do botão
        // ficam 16dp + inset, nunca atrás/abaixo da borda da janela.
        val maxBottom = root.size.height -
            with(density) { navInset.roundToPx() } -
            with(density) { 16.dp.roundToPx() }
        assertTrue(
            "fundo do CTA (${cta.boundsInRoot.bottom}) deve ficar em/acima de $maxBottom " +
                "(janela ${root.size.height} − inset $navInset − 16dp)",
            cta.boundsInRoot.bottom <= maxBottom
        )
    }

    @Test
    fun cta_with48dpNavInset_stays56dpTall_andAboveSystemBar() {
        renderOverlay()
        setNavigationBarsBottomInset(48.dp)
        assertCtaGeometry(48.dp)
    }

    @Test
    fun cta_with24dpNavInset_stays56dpTall_andAboveSystemBar() {
        renderOverlay()
        setNavigationBarsBottomInset(24.dp)
        assertCtaGeometry(24.dp)
    }
}
