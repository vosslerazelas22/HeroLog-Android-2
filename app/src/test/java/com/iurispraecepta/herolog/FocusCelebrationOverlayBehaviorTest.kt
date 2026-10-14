package com.iurispraecepta.herolog

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.unit.dp
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.focus.PendingCelebrationOverlay
import com.iurispraecepta.herolog.ui.focus.shouldExitFocusOnDismiss
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Testes de comportamento do overlay de celebração de foco (issue #39).
 *
 * Usam o [PendingCelebrationOverlay] REAL (extraído da MainActivity) — mesmo
 * modifier de bloqueio de input, mesmo BackHandler e mesmos callbacks.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class FocusCelebrationOverlayBehaviorTest {

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

    private fun twoSessions(): AggregatedCelebrationSummary = aggregateCelebrations(
        listOf(entity("s1", completedAt = 1_000L), entity("s2", completedAt = 2_000L)),
        JsonConfig.default
    )

    private fun singleSession(historyId: String? = "h-1"): AggregatedCelebrationSummary =
        aggregateCelebrations(
            listOf(entity("s1", completedAt = 1_000L, historyId = historyId)),
            JsonConfig.default
        )

    /**
     * Correção 1 (a): toque numa área vazia do overlay — a mesma faixa da base onde
     * ficaria um item do HeroLogBottomNav — NÃO aciona o controle do Scaffold por baixo.
     * O detectTapGestures vazio do container consome o toque antes do clickable de baixo.
     */
    @Test
    fun overlay_visible_tapOnEmptyBottomArea_doesNotTriggerScaffoldControl() {
        var scaffoldClicks = 0
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Camada de baixo: simula o Scaffold com controle na base (HeroLogBottomNav)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(QuestPanel)
                            .testTag("scaffold-control")
                            .clickable(onClick = { scaffoldClicks++ })
                    ) {
                        Text(text = "BOTTOM_NAV_ITEM", color = Color.White)
                    }

                    PendingCelebrationOverlay(
                        summary = twoSessions(),
                        exitFocusModeOnDismiss = false,
                        onExitFocusMode = {},
                        onConfirmSingle = { _, _, _ -> },
                        onConfirmAggregated = {},
                        skills = emptyList()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Overlay visível: o botão da celebração aparece por cima do Scaffold
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertIsDisplayed()
        // O controle do Scaffold continua na árvore (por baixo, encoberto)
        composeTestRule.onNodeWithTag("scaffold-control").assertExists()

        // Toque na faixa vazia da base: o botão da celebração agora termina 16dp
        // acima da borda (coluna com bottom 0 + padding 16dp do botão, inset 0 no
        // Robolectric) e ocupa quase toda a faixa do controle do Scaffold (56dp),
        // então o clique usa posição explícita nos últimos 8dp — área vazia do overlay.
        // (Antes do fix do CTA o botão terminava ~40dp acima da borda e o centro do
        // controle caía na área vazia; com o botão em 56dp o centro atingiria o ouro.)
        val controlNode = composeTestRule.onNodeWithTag("scaffold-control").fetchSemanticsNode()
        val clickY = controlNode.size.height - with(composeTestRule.density) { 8.dp.roundToPx() }.toFloat()
        composeTestRule.onNodeWithTag("scaffold-control").performTouchInput {
            click(Offset(controlNode.size.width / 2f, clickY))
        }
        composeTestRule.waitForIdle()

        // O toque foi consumido pelo overlay: o controle do Scaffold NÃO foi acionado
        assertEquals(0, scaffoldClicks)
        // E o overlay continua intacto (o toque não "passou through")
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertIsDisplayed()
    }

    /**
     * Correção 3 (b): isFocusMode = true + celebração pendente → o overlay aparece por
     * cima do FocusModeScreen; ao dispensar, isFocusMode vira false (sai do focus mode)
     * e some com o overlay.
     */
    @Test
    fun focusMode_withPendingCelebration_overlayAppears_andDismissExitsFocusMode() {
        var confirmAggregatedCount = 0
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                var focusMode by remember { mutableStateOf(true) }
                // Simula a fila do ViewModel: confirm limpa a celebração pendente
                var summary by remember { mutableStateOf<AggregatedCelebrationSummary?>(twoSessions()) }
                Box(modifier = Modifier.fillMaxSize()) {
                    if (focusMode) {
                        Text(
                            text = "FOCUS_MODE_SCREEN",
                            color = Color.White,
                            modifier = Modifier.fillMaxSize().testTag("focus-mode")
                        )
                    }
                    PendingCelebrationOverlay(
                        summary = summary,
                        exitFocusModeOnDismiss = true,
                        onExitFocusMode = { focusMode = false },
                        onConfirmSingle = { _, _, _ -> summary = null },
                        onConfirmAggregated = {
                            confirmAggregatedCount++
                            summary = null
                        },
                        skills = emptyList()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // isFocusMode = true: FocusModeScreen por baixo E overlay por cima
        composeTestRule.onNodeWithTag("focus-mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertIsDisplayed()

        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").performClick()
        composeTestRule.waitForIdle()

        // Overlay some e o exit do focus mode foi disparado (FOCUS_MODE_SCREEN saiu)
        assertEquals(1, confirmAggregatedCount)
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertDoesNotExist()
        composeTestRule.onNodeWithTag("focus-mode").assertDoesNotExist()
    }

    /**
     * Descanso em andamento (exitFocusModeOnDismiss = false, vindo de
     * shouldExitFocusOnDismiss com isBreakActive = true): o dismiss NÃO deve sair
     * da tela de foco/descanso — só fecha a celebração.
     */
    @Test
    fun breakActive_dismissDoesNotExitFocusMode_overlayGone_confirmCalledOnce() {
        var exitFocusCount = 0
        var confirmAggregatedCount = 0
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                var summary by remember { mutableStateOf<AggregatedCelebrationSummary?>(twoSessions()) }
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "FOCUS_MODE_SCREEN",
                        color = Color.White,
                        modifier = Modifier.fillMaxSize().testTag("focus-mode")
                    )
                    PendingCelebrationOverlay(
                        summary = summary,
                        exitFocusModeOnDismiss = false,
                        onExitFocusMode = { exitFocusCount++ },
                        onConfirmSingle = { _, _, _ -> summary = null },
                        onConfirmAggregated = {
                            confirmAggregatedCount++
                            summary = null
                        },
                        skills = emptyList()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").performClick()
        composeTestRule.waitForIdle()

        // Saiu da tela de foco? NÃO. Confirm chamado 1×. Overlay some.
        assertEquals(0, exitFocusCount)
        assertEquals(1, confirmAggregatedCount)
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertDoesNotExist()
        composeTestRule.onNodeWithTag("focus-mode").assertIsDisplayed()
    }

    /**
     * Notas no back (modo SINGLE): digitar nota e tag, apertar voltar → onConfirmSingle
     * recebe os dois valores (mesmo caminho do botão) e o overlay some.
     */
    @Test
    fun singleMode_backPress_confirmsWithTypedNotesAndTag() {
        data class Confirm(val historyId: String?, val notes: String, val tag: String)
        val confirms = mutableListOf<Confirm>()

        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                var summary by remember { mutableStateOf<AggregatedCelebrationSummary?>(singleSession(historyId = "h-1")) }
                PendingCelebrationOverlay(
                    summary = summary,
                    exitFocusModeOnDismiss = false,
                    onExitFocusMode = {},
                    onConfirmSingle = { historyId, notes, tag ->
                        confirms += Confirm(historyId, notes, tag)
                        summary = null
                    },
                    onConfirmAggregated = { summary = null },
                    skills = listOf(
                        Skill(name = "Kotlin", level = 1, xp = 0, tags = listOf("Backend"))
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        // Passo summary → avança para notes (sem streak/loot/conquista: 2 steps)
        composeTestRule.onNodeWithText("SESSÃO CONCLUÍDA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar").performClick()
        composeTestRule.waitForIdle()

        // Digita nota e seleciona tag
        composeTestRule.onNode(hasSetTextAction()).performTextInput("anotação de teste")
        composeTestRule.onNodeWithText("Backend").performClick()
        composeTestRule.waitForIdle()

        // Aperta voltar (BackHandler do overlay)
        composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressed() }
        composeTestRule.waitForIdle()

        // onConfirmSingle recebeu historyId + nota + tag; overlay some
        assertEquals(1, confirms.size)
        assertEquals("h-1", confirms.single().historyId)
        assertEquals("anotação de teste", confirms.single().notes)
        assertEquals("Backend", confirms.single().tag)
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertDoesNotExist()
        composeTestRule.onNodeWithText("Continuar").assertDoesNotExist()
    }

    /**
     * Contrato do keying por historyId: uma troca de celebração A→B SEM passar por
     * summary == null (possível no app via refreshPendingCelebration em
     * handleServiceCompleted, HeroLogViewModel.kt:539 — fila crescendo sem null)
     * não pode vazar as notas/tag de A para o confirm de B.
     */
    @Test
    fun singleMode_summarySwapWithoutNull_notesOfADoNotLeakToB() {
        data class Confirm(val historyId: String?, val notes: String, val tag: String)
        val confirms = mutableListOf<Confirm>()
        // State fora da composição para o teste trocar A→B sem passar por null
        val summaryState = mutableStateOf<AggregatedCelebrationSummary?>(singleSession(historyId = "h-1"))

        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                PendingCelebrationOverlay(
                    summary = summaryState.value,
                    exitFocusModeOnDismiss = false,
                    onExitFocusMode = {},
                    onConfirmSingle = { historyId, notes, tag ->
                        confirms += Confirm(historyId, notes, tag)
                        summaryState.value = null
                    },
                    onConfirmAggregated = { summaryState.value = null },
                    skills = listOf(
                        Skill(name = "Kotlin", level = 1, xp = 0, tags = listOf("Backend"))
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        // Celebração A (h-1): avança até notes, digita nota e seleciona tag
        composeTestRule.onNodeWithText("Continuar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("nota da sessao A")
        composeTestRule.onNodeWithText("Backend").performClick()
        composeTestRule.waitForIdle()

        // Troca direta A→B (h-2) sem passar por summary == null
        composeTestRule.runOnUiThread {
            summaryState.value = singleSession(historyId = "h-2")
        }
        composeTestRule.waitForIdle()

        // Volta: onConfirmSingle recebe B (h-2) com nota/tag VAZIAS — nada de A vazou
        composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressed() }
        composeTestRule.waitForIdle()

        assertEquals(1, confirms.size)
        assertEquals("h-2", confirms.single().historyId)
        assertEquals("", confirms.single().notes)
        assertEquals("", confirms.single().tag)
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertDoesNotExist()
    }

    /**
     * shouldExitFocusOnDismiss — função pura que decide se o dismiss tira o usuário
     * do FocusModeScreen. 3 combinações relevantes.
     */
    @Test
    fun shouldExitFocusOnDismiss_focusModeWithoutBreak_exits() {
        assertTrue(shouldExitFocusOnDismiss(isFocusMode = true, isBreakActive = false))
    }

    @Test
    fun shouldExitFocusOnDismiss_focusModeWithBreak_doesNotExit() {
        assertFalse(shouldExitFocusOnDismiss(isFocusMode = true, isBreakActive = true))
    }

    @Test
    fun shouldExitFocusOnDismiss_notInFocusMode_doesNotExit() {
        assertFalse(shouldExitFocusOnDismiss(isFocusMode = false, isBreakActive = false))
    }
}
