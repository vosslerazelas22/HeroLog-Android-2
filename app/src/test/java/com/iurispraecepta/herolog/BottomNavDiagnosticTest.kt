package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.navigation.HeroLogBottomNav
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import dev.chrisbanes.haze.HazeState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Diagnóstico Spec A: renderiza a nova barra inferior em 390x844 e 375x667 e confere que os
 * 5 rótulos estão visíveis ao mesmo tempo (critério de aceitação da Spec A). Uso do
 * `HeroLogBottomNav` igual ao da MainActivity: só `fillMaxWidth()`, altura vem do conteúdo.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w390dp-h844dp", sdk = [36])
class BottomNavDiagnosticTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun Screen(activeTab: String) {
        val hazeState = remember { HazeState() }
        HeroLogTheme(darkTheme = true) {
            Column(modifier = Modifier.fillMaxSize().background(QuestPanel)) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth().background(QuestPanel)) {
                    Text(text = "conteudo-$activeTab", color = Color.White)
                }
                HeroLogBottomNav(
                    activeTab = activeTab,
                    onChangeTab = {},
                    hazeState = hazeState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    private fun assertFiveLabels() {
        listOf("FOCO", "RITUAIS", "TAVERNA", "HERÓI", "REINO").forEach { label ->
            composeTestRule.onNodeWithText(label).assertIsDisplayed()
        }
    }

    @Test
    fun bottomNav_tavernaAtiva_390x844() {
        composeTestRule.setContent { Screen("tavern") }
        composeTestRule.waitForIdle()
        assertFiveLabels()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/bottomnav_diag_tavern_390x844.png")
        println("DIAG-NAV 390x844 rotulos=5 OK")
    }

    @Test
    fun bottomNav_rituaisAtivo_390x844() {
        composeTestRule.setContent { Screen("habits") }
        composeTestRule.waitForIdle()
        assertFiveLabels()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/bottomnav_diag_habits_390x844.png")
        println("DIAG-NAV habits 390x844 rotulos=5 OK")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun bottomNav_tavernaAtiva_375x667() {
        composeTestRule.setContent { Screen("tavern") }
        composeTestRule.waitForIdle()
        assertFiveLabels()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/bottomnav_diag_tavern_375x667.png")
        println("DIAG-NAV 375x667 rotulos=5 OK")
    }
}
