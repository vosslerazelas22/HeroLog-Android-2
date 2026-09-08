package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.DailyReportData
import com.iurispraecepta.herolog.ui.daily.DailyReportModal
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
class DailyReportModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dailyReport_streakKept_noMissedDailies() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 100,
                        currentStreak = 7,
                        streakLost = false,
                        streakProtected = false,
                        missedDailiesCount = 0,
                        damageTaken = 0,
                        allDailiesCompleted = true
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_streak_kept.png")
    }

    @Test
    fun dailyReport_streakLost_missedDailies() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 100,
                        currentStreak = 0,
                        streakLost = true,
                        streakProtected = false,
                        missedDailiesCount = 3,
                        damageTaken = 8,
                        allDailiesCompleted = false
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_streak_lost.png")
    }

    @Test
    fun dailyReport_streakProtected_byShield() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 100,
                        currentStreak = 10,
                        streakLost = false,
                        streakProtected = true,
                        missedDailiesCount = 2,
                        damageTaken = 0,
                        allDailiesCompleted = false
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_streak_protected.png")
    }

    @Test
    fun dailyReport_warriorBonus_120gp() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 120,
                        currentStreak = 3,
                        streakLost = false,
                        streakProtected = false,
                        missedDailiesCount = 0,
                        damageTaken = 0,
                        allDailiesCompleted = false
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_warrior_bonus.png")
    }

    @Test
    fun dailyReport_allDailiesCompleted() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 100,
                        currentStreak = 4,
                        streakLost = false,
                        streakProtected = false,
                        missedDailiesCount = 0,
                        damageTaken = 0,
                        allDailiesCompleted = true
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_all_completed.png")
    }

    @Test
    fun dailyReport_noDailies() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 100,
                        currentStreak = 0,
                        streakLost = false,
                        streakProtected = false,
                        missedDailiesCount = 0,
                        damageTaken = 0,
                        allDailiesCompleted = false
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_no_dailies.png")
    }

    @Test
    fun dailyReport_rangerReducedDamage() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailyReportModal(
                    data = DailyReportData(
                        rewardAmount = 100,
                        currentStreak = 0,
                        streakLost = false,
                        streakProtected = false,
                        missedDailiesCount = 3,
                        damageTaken = 7,
                        allDailiesCompleted = false
                    ),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/daily_report_ranger_damage.png")
    }
}
