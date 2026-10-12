package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bloco F (spec-rituais-cards): chrome das abas (cabeçalho, botão novo, vazio).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RitualTabChromeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setChrome(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    content()
                }
            }
        }
    }

    @Test
    fun header_showsUppercaseTitle_counter_andNewButton() {
        // Arrange/Act: título sentence-case vira uppercase (`.uppercase()`, spec §4)
        // Assert: campo a campo
        var news = 0
        setChrome {
            RitualTabHeader(
                iconRes = LucideR.drawable.lucide_ic_zap,
                title = "Capela de Hábitos",
                counterText = "3 hábitos",
                showNewButton = true,
                newContentDescription = "Novo hábito",
                onNewClick = { news++ }
            )
        }
        composeTestRule.onNodeWithText("CAPELA DE HÁBITOS").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 hábitos").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ NOVO").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ NOVO").performClick()
        composeTestRule.runOnIdle { assertEquals(1, news) }
    }

    @Test
    fun header_hidesNewButton_whenCreating() {
        // Arrange/Act: criando/editando (como no React `{!isCreating && ...}`)
        // Assert: sem botão novo
        setChrome {
            RitualTabHeader(
                iconRes = LucideR.drawable.lucide_ic_calendar_days,
                title = "Tarefas Diárias",
                counterText = "1 tarefa",
                showNewButton = false,
                newContentDescription = "Nova diária",
                onNewClick = {}
            )
        }
        composeTestRule.onNodeWithText("TAREFAS DIÁRIAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 tarefa").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ NOVO").assertDoesNotExist()
    }

    @Test
    fun empty_showsMessage() {
        // Arrange/Act/Assert: texto literal do React, Inter itálico centralizado
        setChrome {
            RitualEmptyState(
                message = "Nenhum afazer místico sob este filtro. Adicione novas aventuras e complete-as!"
            )
        }
        composeTestRule
            .onNodeWithText("Nenhum afazer místico sob este filtro. Adicione novas aventuras e complete-as!")
            .assertIsDisplayed()
    }

    @Test
    fun bottomBorder_modifier_appliesWithoutCrash() {
        // Arrange/Act: modificador de borda inferior compõe
        // Assert: smoke test
        setChrome {
            Box(modifier = Modifier.ritualBottomBorder())
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("+ NOVO").assertIsNotDisplayed()
    }
}
