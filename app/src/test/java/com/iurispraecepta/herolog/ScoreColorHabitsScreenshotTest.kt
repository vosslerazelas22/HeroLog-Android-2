package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.ui.habits.HabitsScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ScoreColorHabitsScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun habitsScreen_scoreColor_allTiers_screenshot() {
        val habits = listOf(
            Habit(
                id = "h_tier1", title = "Score 15 (Tier 1 — Sky Bright)", notes = "",
                up = true, down = true, difficulty = Difficulty.Medium,
                upCount = 20, downCount = 5, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            ),
            Habit(
                id = "h_tier2", title = "Score 7 (Tier 2 — Sky Dim)", notes = "",
                up = true, down = true, difficulty = Difficulty.Medium,
                upCount = 10, downCount = 3, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            ),
            Habit(
                id = "h_tier3", title = "Score 3 (Tier 3 — Emerald)", notes = "",
                up = true, down = true, difficulty = Difficulty.Medium,
                upCount = 5, downCount = 2, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            ),
            Habit(
                id = "h_tier4", title = "Score 0 (Tier 4 — Amber)", notes = "",
                up = true, down = true, difficulty = Difficulty.Medium,
                upCount = 3, downCount = 3, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            ),
            Habit(
                id = "h_tier5", title = "Score -5 (Tier 5 — Orange)", notes = "",
                up = true, down = true, difficulty = Difficulty.Medium,
                upCount = 1, downCount = 6, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            ),
            Habit(
                id = "h_tier6", title = "Score -15 (Tier 6 — Red)", notes = "",
                up = false, down = true, difficulty = Difficulty.Medium,
                upCount = 0, downCount = 15, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            ),
            Habit(
                id = "h_tier7", title = "Score -25 (Tier 7 — Dark Red)", notes = "",
                up = false, down = true, difficulty = Difficulty.Medium,
                upCount = 0, downCount = 25, streak = 0,
                tags = emptyList(), lastTriggeredDate = null
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                Box(modifier = Modifier.fillMaxSize().background(QuestPanel)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x332E1065),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    HabitsScreen(
                        habits = habits,
                        onTriggerHabit = { _, _ -> },
                        onAddHabit = { _, _, _, _, _, _ -> },
                        onEditHabit = {},
                        onDeleteHabit = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/score_color_habits_all_tiers.png")
    }
}
