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
 * Spec-bottomnav-minimalista (T11): cobre os 5 estados da BottomNav (Foco, Rituais, Taverna,
 * Herói, Reino ativos) nos 2 viewports + fonte em escala 1.3 (Taverna ativa e uma lateral
 * ativa, 390x844). Uso do `HeroLogBottomNav` igual ao da MainActivity: só `fillMaxWidth()`,
 * altura vem do conteúdo. Critério: exatamente um item dourado por estado (conferência
 * humana nos PNGs contra o mockup — Roborazzi é baseline, não juiz).
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

    private fun renderAndCapture(activeTab: String, fileName: String) {
        composeTestRule.setContent { Screen(activeTab) }
        composeTestRule.waitForIdle()
        assertFiveLabels()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/$fileName")
    }

    @Test
    fun bottomNav_focoAtivo_390x844() {
        renderAndCapture("focus", "bottomnav_diag_focus_390x844.png")
    }

    @Test
    fun bottomNav_rituaisAtivo_390x844() {
        renderAndCapture("habits", "bottomnav_diag_habits_390x844.png")
    }

    @Test
    fun bottomNav_tavernaAtiva_390x844() {
        renderAndCapture("tavern", "bottomnav_diag_tavern_390x844.png")
    }

    @Test
    fun bottomNav_heroiAtivo_390x844() {
        renderAndCapture("character", "bottomnav_diag_heroi_390x844.png")
    }

    @Test
    fun bottomNav_reinoAtivo_390x844() {
        renderAndCapture("shop", "bottomnav_diag_reino_390x844.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun bottomNav_focoAtivo_375x667() {
        renderAndCapture("focus", "bottomnav_diag_focus_375x667.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun bottomNav_rituaisAtivo_375x667() {
        renderAndCapture("habits", "bottomnav_diag_habits_375x667.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun bottomNav_tavernaAtiva_375x667() {
        renderAndCapture("tavern", "bottomnav_diag_tavern_375x667.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun bottomNav_heroiAtivo_375x667() {
        renderAndCapture("character", "bottomnav_diag_heroi_375x667.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun bottomNav_reinoAtivo_375x667() {
        renderAndCapture("shop", "bottomnav_diag_reino_375x667.png")
    }

    @Test
    @Config(fontScale = 1.3f)
    fun bottomNav_tavernaAtiva_fontScale13_390x844() {
        renderAndCapture("tavern", "bottomnav_diag_tavern_font13_390x844.png")
    }

    @Test
    @Config(fontScale = 1.3f)
    fun bottomNav_focoAtivo_fontScale13_390x844() {
        renderAndCapture("focus", "bottomnav_diag_focus_font13_390x844.png")
    }
}
