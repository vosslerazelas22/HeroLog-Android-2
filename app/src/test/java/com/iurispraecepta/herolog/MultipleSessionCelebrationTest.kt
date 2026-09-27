package com.iurispraecepta.herolog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import com.iurispraecepta.herolog.ui.focus.MultipleSessionCelebration
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
class MultipleSessionCelebrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun entity(
        id: String,
        skillName: String = "Kotlin",
        xp: Int = 60,
        gold: Int = 90,
        completedAt: Long = 1_000L,
        leveledUp: Boolean = false,
        previousLevel: Int? = null,
        newLevel: Int? = null
    ) = PendingRewardCelebrationEntity(
        id = id,
        sessionCompletedAt = completedAt,
        skillName = skillName,
        durationMinutes = 25,
        xpGained = xp,
        goldGained = gold,
        leveledUp = leveledUp,
        previousLevel = previousLevel,
        newLevel = newLevel
    )

    private fun threeSessions(): AggregatedCelebrationSummary = aggregateCelebrations(
        listOf(
            entity("s1", skillName = "Kotlin", xp = 60, gold = 90, completedAt = 1_000L),
            entity(
                "s2",
                skillName = "Matemática",
                xp = 50,
                gold = 75,
                completedAt = 2_000L,
                leveledUp = true,
                previousLevel = 4,
                newLevel = 5
            ),
            entity("s3", skillName = "Inglês", xp = 70, gold = 80, completedAt = 3_000L)
        ),
        JsonConfig.default
    )

    private fun setContent(summary: AggregatedCelebrationSummary, onDismiss: () -> Unit = {}) {
        composeTestRule.setContent {
            HeroLogTheme {
                MultipleSessionCelebration(summary = summary, onDismiss = onDismiss)
            }
        }
    }

    @Test
    fun aggregated_showsTotalsAndSingleLevelUpIndicator() {
        setContent(threeSessions())

        composeTestRule.onNodeWithText("3 sessões concluídas enquanto você estava fora").assertIsDisplayed()
        composeTestRule.onNodeWithText("+180 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("+245 Ouro").assertIsDisplayed()
        composeTestRule.onNodeWithText("🎉 Level Up! Nv. 4 → 5").assertIsDisplayed()
    }

    @Test
    fun aggregated_noLevelUp_hidesIndicator() {
        setContent(aggregateCelebrations(listOf(entity("s1"), entity("s2")), JsonConfig.default))

        composeTestRule.onNodeWithText("+120 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("🎉 Level Up! Nv.", substring = true).assertDoesNotExist()
    }

    @Test
    fun aggregated_breakdown_expandsAndCollapses() {
        setContent(threeSessions())

        composeTestRule.onNodeWithText("▾ Ver detalhes por sessão").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matemática").assertDoesNotExist()

        composeTestRule.onNodeWithText("▾ Ver detalhes por sessão").performClick()
        composeTestRule.mainClock.advanceTimeBy(500)

        composeTestRule.onNodeWithText("▾ Ocultar detalhes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matemática").assertExists()
        composeTestRule.onNodeWithText("+50 XP").assertExists()

        composeTestRule.onNodeWithText("▾ Ocultar detalhes").performClick()
        composeTestRule.mainClock.advanceTimeBy(500)

        composeTestRule.onNodeWithText("▾ Ver detalhes por sessão").assertIsDisplayed()
        composeTestRule.onNodeWithText("Matemática").assertDoesNotExist()
    }
}
