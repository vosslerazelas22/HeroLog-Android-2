package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.BreakPrepScreen
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
class BreakPrepScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun breakPrepScreen_standardMode_shortSelected_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                BreakPrepScreen(
                    shortBreakMinutes = 5,
                    longBreakMinutes = 15,
                    selectedBreakMinutes = 5,
                    isDungeonMode = false,
                    onSelectDuration = {},
                    onStartBreak = {},
                    onSkipBreak = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/break_prep_screen_standard_short.png")
    }

    @Test
    fun breakPrepScreen_standardMode_longSelected_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                BreakPrepScreen(
                    shortBreakMinutes = 5,
                    longBreakMinutes = 15,
                    selectedBreakMinutes = 15,
                    isDungeonMode = false,
                    onSelectDuration = {},
                    onStartBreak = {},
                    onSkipBreak = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/break_prep_screen_standard_long.png")
    }

    @Test
    fun breakPrepScreen_dungeonMode_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                BreakPrepScreen(
                    shortBreakMinutes = 5,
                    longBreakMinutes = 15,
                    selectedBreakMinutes = 15,
                    isDungeonMode = true,
                    onSelectDuration = {},
                    onStartBreak = {},
                    onSkipBreak = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/break_prep_screen_dungeon.png")
    }
}
