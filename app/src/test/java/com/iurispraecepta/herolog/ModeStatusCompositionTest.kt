package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.ui.focus.ModeStatus
import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.theme
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * SC-004 (D3): pausa e urgência (timeLeft ≤ 60s) mudam só o orb — header e
 * status mantêm a cor do modo. O [ModeStatus] recebe apenas [RaidMode.theme()];
 * este teste fixa o contrato do composable: o título exibido segue o modo e
 * os pips seguem o progresso da sessão, sem entrada de pausa/urgência.
 * O wiring (MainActivity deriva o tema só de RaidMode, nunca de FocusMode/
 * isPaused/isUrgent — FR-004) é estrutural e validado por inspeção + device.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ModeStatusCompositionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setStatus(
        mode: RaidMode,
        completed: Int,
        dungeonOnCooldown: Boolean = false,
        cooldownText: String = "00:00:00",
    ) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    ModeStatus(
                        mode = mode,
                        completed = completed,
                        dungeonOnCooldown = dungeonOnCooldown,
                        cooldownText = cooldownText,
                        lootChancePercent = 25,
                        theme = mode.theme()
                    )
                }
            }
        }
    }

    @Test
    fun modeStatus_pausedSession_showsModeTitle_notPauseState() {
        // Arrange: sessão de Masmorra pausada (completed vem do config — FR-010)
        // Act
        setStatus(mode = RaidMode.MASMORRA, completed = 1)
        // Assert: título do modo + pips da sessão atual, nada de estado de pausa
        composeTestRule.onNodeWithTag("modeStatusMain").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sessão 2 de 4").assertIsDisplayed()
    }

    @Test
    fun modeStatus_urgentSession_showsModeTitle_notUrgentState() {
        // Arrange: mesma sessão com timeLeft ≤ 60s — as entradas do status não mudam
        // Act
        setStatus(mode = RaidMode.MASMORRA, completed = 1)
        // Assert: idêntico ao caso pausado (SC-004)
        composeTestRule.onNodeWithTag("modeStatusMain").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sessão 2 de 4").assertIsDisplayed()
    }

    @Test
    fun modeStatus_postFourthRestWithCooldown_showsTimerInsteadOfPips() {
        // Arrange: descanso pós-4ª com cooldown (FR-011)
        // Act
        setStatus(
            mode = RaidMode.MASMORRA,
            completed = 4,
            dungeonOnCooldown = true,
            cooldownText = "01:59:59"
        )
        // Assert: ⏳ no lugar dos pips
        composeTestRule.onNodeWithText("01:59:59", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sessão 4 de 4").assertDoesNotExist()
    }

    @Test
    fun modeStatus_postFourthRestExpired_showsFullPips() {
        // Arrange: descanso pós-4ª com cooldown já expirado (FR-011)
        // Act
        setStatus(mode = RaidMode.MASMORRA, completed = 4, dungeonOnCooldown = false)
        // Assert: ●●●●
        composeTestRule.onNodeWithContentDescription("Sessão 4 de 4").assertIsDisplayed()
    }
}
