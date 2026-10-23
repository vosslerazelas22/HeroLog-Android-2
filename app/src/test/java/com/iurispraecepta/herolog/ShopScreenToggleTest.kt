package com.iurispraecepta.herolog

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.ui.kingdom.ShopScreen
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
 * Regressão do ajuste visual das sub-abas do Bazar (issue #50, pós-escolha V2).
 *
 * Bug original: os dois botões do toggle têm labels com nº de linhas diferente
 * ("Consumíveis & Equipamentos" quebra em 2 linhas, "Títulos" em 1) e o `Row`
 * alinhava ao topo — o pill de "Títulos" ficava menor que a célula e o texto,
 * deslocado. Correção: `heightIn(min = 40.dp)` em cada botão (mesmo padrão do
 * `RaidModeButton`) + `Alignment.CenterVertically` no `Row`.
 *
 * Cobre também o rename "Selos & Títulos Reais" → "Títulos".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ShopScreenToggleTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun renderShop() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                ShopScreen(
                    gold = 250,
                    inventory = emptyList(),
                    state = createInitialCharacterState(),
                    onBuyItem = {},
                    onBuyTitle = { _, _ -> },
                    onClaimAchievementTitle = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun subTabButtons_equalHeight_bothPillsFillCell() {
        renderShop()
        val density = composeTestRule.density
        val items = composeTestRule.onNodeWithTag("shop_subtab_items").fetchSemanticsNode()
        val titles = composeTestRule.onNodeWithTag("shop_subtab_titles").fetchSemanticsNode()
        val min40Px = with(density) { 40.dp.roundToPx() }

        assertTrue(
            "botão Consumíveis deve ter >= 40dp (${items.size.height}px)",
            items.size.height >= min40Px
        )
        assertTrue(
            "botão Títulos deve ter >= 40dp (${titles.size.height}px)",
            titles.size.height >= min40Px
        )
        assertEquals(
            "pills com alturas diferentes — o pill de Títulos não preenchia a célula",
            items.size.height,
            titles.size.height
        )
    }

    @Test
    fun subTabLabel_titulos_renamedFromSelosETitulosReais() {
        renderShop()
        composeTestRule.onNodeWithText("TÍTULOS").assertExists()
        composeTestRule.onNodeWithText("SELOS & TÍTULOS REAIS").assertDoesNotExist()
    }
}
