package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.logic.character.LevelUpEvent
import com.iurispraecepta.herolog.ui.character.LevelUpOverlay
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
class LevelUpOverlayScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun levelUpOverlay_combat_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                LevelUpOverlay(
                    event = LevelUpEvent.Combat(
                        oldLevel = 4,
                        newLevel = 5,
                        charName = "Thalric",
                        charClass = "Warrior"
                    ),
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/level_up_overlay_combat.png")
    }

    @Test
    fun levelUpOverlay_skill_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                LevelUpOverlay(
                    event = LevelUpEvent.Skill(
                        skillName = "Kotlin",
                        emoji = "\uD83D\uDCBB",
                        oldLevel = 6,
                        newLevel = 7
                    ),
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/level_up_overlay_skill.png")
    }
}
