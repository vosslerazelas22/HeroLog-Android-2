package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.ui.focus.SessionSummaryScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * OQ-2 (spec-tema-por-modo-foco): a tela de recompensa exibe os pips da
 * Masmorra a partir do índice gravado na celebração.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class DungeonRewardPipsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun calc() = FocusRewardsCalculation(
        skillIdx = 0,
        skillName = "Kotlin",
        xpEarned = 60,
        goldEarned = 75,
        durationMins = 25,
        dungeonClearGoldBonus = 0,
        hasUsedDoubleLoot = false,
        hasUsedFocusElixir = false,
        hasUsedRuneFortune = false,
        hasUsedCrystalClarity = false,
        usedEquipmentIndicesAndCharges = emptyList(),
        lootedItems = emptyList(),
        droppedTitle = null,
        isWildernessChecked = false,
        isDungeonMode = true,
        comboBonusPercent = 0
    )

    private fun setSummary(dungeonSessionsCompleted: Int) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    SessionSummaryScreen(
                        rewardsCalculation = calc(),
                        pauseCount = 0,
                        streak = 1,
                        dungeonSessionsCompleted = dungeonSessionsCompleted
                    )
                }
            }
        }
    }

    @Test
    fun summary_fourthDungeonSession_showsFullPips() {
        // Arrange/Act: 4ª sessão concluída
        setSummary(dungeonSessionsCompleted = 4)
        // Assert: ●●●● + rótulo
        composeTestRule.onNodeWithContentDescription("Sessão 4 de 4").assertIsDisplayed()
        composeTestRule.onNodeWithText("SESSÃO 4 DE 4", substring = true).assertIsDisplayed()
    }

    @Test
    fun summary_standardSession_showsNoPips() {
        // Arrange/Act: sessão não-masmorra (0)
        setSummary(dungeonSessionsCompleted = 0)
        // Assert: nenhum pip
        composeTestRule.onNodeWithContentDescription("Sessão 1 de 4").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Sessão 4 de 4").assertDoesNotExist()
    }
}
