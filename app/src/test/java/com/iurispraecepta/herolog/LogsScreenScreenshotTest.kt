package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.LogEntry
import com.iurispraecepta.herolog.ui.kingdom.LogsScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LogsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun logsScreen_empty_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                LogsScreen(logs = emptyList())
            }
        }

        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/logs_screen_empty.png")
    }

    @Test
    fun logsScreen_populated_screenshot() {
        val sampleLogs = listOf(
            LogEntry(
                id = "1",
                time = "14:32:05",
                text = "✨ Prática Virtuosa: Completou o hábito positivo \"Leitura Matinal\"! Ganhou +10 GP e +25 XP.",
                highlighted = true
            ),
            LogEntry(
                id = "2",
                time = "14:30:12",
                text = "📅 Voto Diário Cumprido: Concluiu \"Treino de Força\"! (+20 GP, +50 XP, Streak: 4 dias)",
                highlighted = true
            ),
            LogEntry(
                id = "3",
                time = "14:15:00",
                text = "✔️ Afazer Cumprido: Concluiu aventura \"Revisar Código do Sistema\"! (+15 GP, +30 XP!)",
                highlighted = true
            ),
            LogEntry(
                id = "4",
                time = "12:00:00",
                text = "💎 Proclamação Diária: Recebeste +100 GP por adentrar hoje ao Santuário Sagrado!",
                highlighted = true
            ),
            LogEntry(
                id = "5",
                time = "11:55:22",
                text = "⚙️ Diária Modificada: \"Meditação Vespertina\" atualizada.",
                highlighted = false
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                LogsScreen(logs = sampleLogs)
            }
        }

        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/logs_screen_populated.png")
    }
}
