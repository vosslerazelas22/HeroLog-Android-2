package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.ui.focus.IncursionModeModal
import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.RaidModeHelpContent
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class IncursionModeModalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setModal(
        readOnly: Boolean = false,
        onSelectMode: (RaidMode) -> Unit = {},
    ) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    IncursionModeModal(
                        isOpen = true,
                        onClose = {},
                        currentMode = RaidMode.PADRAO,
                        dungeonCooldownRemainingMs = 0L,
                        onSelectMode = onSelectMode,
                        helpBlocksFor = { mode ->
                            when (mode) {
                                RaidMode.MASMORRA -> RaidModeHelpContent.MASMORRA
                                RaidMode.SELVAGEM -> RaidModeHelpContent.SELVAGEM
                                RaidMode.PADRAO -> emptyList()
                            }
                        },
                        readOnly = readOnly
                    )
                }
            }
        }
    }

    @Test
    fun helpToggle_doesNotSelectMode() {
        // Arrange: modal editável, registra seleções
        var selected: RaidMode? = null
        setModal(onSelectMode = { selected = it })
        // Act: toque no "?" da Masmorra (SC-009: não seleciona o modo)
        composeTestRule.onNodeWithTag("modeHelp_MASMORRA").performClick()
        // Assert: nenhuma seleção + ajuda expandida
        assertNull(selected)
        composeTestRule.onNodeWithText("2 horas", substring = true).assertIsDisplayed()
    }

    @Test
    fun expandingOneCard_collapsesPrevious() {
        // Arrange
        setModal()
        // Act: abre Masmorra, depois Selvagem (SC-009: um aberto por vez)
        composeTestRule.onNodeWithTag("modeHelp_MASMORRA").performClick()
        composeTestRule.onNodeWithText("2 horas", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("modeHelp_SELVAGEM").performClick()
        // Assert: anterior fechou (aguarda a animação de saída), novo abriu
        composeTestRule.mainClock.advanceTimeBy(2_000L)
        composeTestRule.onNodeWithText("2 horas", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Sobreviventes", substring = true).assertIsDisplayed()
    }

    @Test
    fun editable_cardClick_selectsMode() {
        // Arrange
        var selected: RaidMode? = null
        setModal(onSelectMode = { selected = it })
        // Act
        composeTestRule.onNodeWithTag("modeCard_SELVAGEM").assertHasClickAction()
        composeTestRule.onNodeWithTag("modeCard_SELVAGEM").performClick()
        // Assert
        assertEquals(RaidMode.SELVAGEM, selected)
    }

    @Test
    fun readOnly_cardHasNoClickAction_butHelpStillWorks() {
        // Arrange: modal somente leitura (sessão rodando)
        var selected: RaidMode? = null
        setModal(readOnly = true, onSelectMode = { selected = it })
        // Assert: toque no card não seleciona (SC-009)
        composeTestRule.onNodeWithTag("modeCard_PADRAO").assertHasNoClickAction()
        assertNull(selected)
        // Act: "?" continua funcionando no readOnly
        composeTestRule.onNodeWithTag("modeHelp_MASMORRA").performClick()
        // Assert
        composeTestRule.onNodeWithText("2 horas", substring = true).assertIsDisplayed()
        assertNull(selected)
    }
}
