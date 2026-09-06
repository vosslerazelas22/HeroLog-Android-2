package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.OrbConcept
import com.iurispraecepta.herolog.ui.focus.FocusOrb
import com.iurispraecepta.herolog.ui.focus.FocusOrbSize
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class FocusOrbScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun focusOrb_running_highProgress() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_running_high_progress.png")
    }

    @Test
    fun focusOrb_running_urgent() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 45,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_running_urgent.png")
    }

    @Test
    fun focusOrb_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 800,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_paused.png")
    }

    @Test
    fun focusOrb_breakActive() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 200,
                    totalSeconds = 300,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_break_active.png")
    }

    @Test
    fun focusOrb_fullscreenSize() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1000,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.FULLSCREEN
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_fullscreen_size.png")
    }

    @Test
    fun focusOrb_conceptA_running_highProgress() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.A,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptA_running.png")
    }

    @Test
    fun focusOrb_conceptB_running_highProgress() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.B,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptB_running.png")
    }

    @Test
    fun focusOrb_conceptC_running_highProgress() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.C,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptC_running.png")
    }

    @Test
    fun focusOrb_conceptD_running_highProgress() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.D,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptD_running.png")
    }

    @Test
    fun focusOrb_conceptA_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 800,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    isBreakActive = false,
                    orbConcept = OrbConcept.A,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptA_paused.png")
    }

    @Test
    fun focusOrb_conceptB_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 800,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    isBreakActive = false,
                    orbConcept = OrbConcept.B,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptB_paused.png")
    }

    @Test
    fun focusOrb_conceptC_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 800,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    isBreakActive = false,
                    orbConcept = OrbConcept.C,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptC_paused.png")
    }

    @Test
    fun focusOrb_conceptD_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 800,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    isBreakActive = false,
                    orbConcept = OrbConcept.D,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptD_paused.png")
    }

    // =========================================================================
    // Dungeon mode — 4 concepts
    // =========================================================================

    @Test
    fun focusOrb_conceptA_dungeon() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isDungeonMode = true,
                    orbConcept = OrbConcept.A,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptA_dungeon.png")
    }

    @Test
    fun focusOrb_conceptB_dungeon() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isDungeonMode = true,
                    orbConcept = OrbConcept.B,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptB_dungeon.png")
    }

    @Test
    fun focusOrb_conceptC_dungeon() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isDungeonMode = true,
                    orbConcept = OrbConcept.C,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptC_dungeon.png")
    }

    @Test
    fun focusOrb_conceptD_dungeon() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isDungeonMode = true,
                    orbConcept = OrbConcept.D,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptD_dungeon.png")
    }

    // =========================================================================
    // Wilderness mode — 4 concepts
    // =========================================================================

    @Test
    fun focusOrb_conceptA_wilderness() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isWildernessMode = true,
                    orbConcept = OrbConcept.A,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptA_wilderness.png")
    }

    @Test
    fun focusOrb_conceptB_wilderness() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isWildernessMode = true,
                    orbConcept = OrbConcept.B,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptB_wilderness.png")
    }

    @Test
    fun focusOrb_conceptC_wilderness() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isWildernessMode = true,
                    orbConcept = OrbConcept.C,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptC_wilderness.png")
    }

    @Test
    fun focusOrb_conceptD_wilderness() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    isWildernessMode = true,
                    orbConcept = OrbConcept.D,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptD_wilderness.png")
    }

    // =========================================================================
    // Urgent mode (timeLeft <= 60) — 4 concepts
    // =========================================================================

    @Test
    fun focusOrb_conceptA_urgent() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 45,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.A,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptA_urgent.png")
    }

    @Test
    fun focusOrb_conceptB_urgent() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 45,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.B,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptB_urgent.png")
    }

    @Test
    fun focusOrb_conceptC_urgent() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 45,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.C,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptC_urgent.png")
    }

    @Test
    fun focusOrb_conceptD_urgent() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 45,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    orbConcept = OrbConcept.D,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptD_urgent.png")
    }

    // =========================================================================
    // Break mode — 4 concepts
    // =========================================================================

    @Test
    fun focusOrb_conceptA_break() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 200,
                    totalSeconds = 300,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    orbConcept = OrbConcept.A,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptA_break.png")
    }

    @Test
    fun focusOrb_conceptB_break() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 200,
                    totalSeconds = 300,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    orbConcept = OrbConcept.B,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptB_break.png")
    }

    @Test
    fun focusOrb_conceptC_break() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 200,
                    totalSeconds = 300,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    orbConcept = OrbConcept.C,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptC_break.png")
    }

    @Test
    fun focusOrb_conceptD_break() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 200,
                    totalSeconds = 300,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    orbConcept = OrbConcept.D,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_conceptD_break.png")
    }

    // =========================================================================
    // Compact size — concept D (default)
    // =========================================================================

    @Test
    fun focusOrb_compactSize() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.COMPACT
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_compact_size.png")
    }
}
