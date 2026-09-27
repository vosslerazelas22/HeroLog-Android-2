package com.iurispraecepta.herolog

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import com.iurispraecepta.herolog.ui.focus.PendingCelebrationModal
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class PendingCelebrationModalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun entity(
        id: String,
        xp: Int = 60,
        gold: Int = 90,
        completedAt: Long = 1_000L
    ) = PendingRewardCelebrationEntity(
        id = id,
        sessionCompletedAt = completedAt,
        skillName = "Kotlin",
        durationMinutes = 25,
        xpGained = xp,
        goldGained = gold
    )

    private fun setContent(summary: AggregatedCelebrationSummary, onDismiss: () -> Unit = {}) {
        composeTestRule.setContent {
            HeroLogTheme {
                PendingCelebrationModal(summary = summary, onDismiss = onDismiss)
            }
        }
    }

    @Test
    fun singleSession_routesToExistingCompletionFlow() {
        setContent(aggregateCelebrations(listOf(entity("s1")), JsonConfig.default))

        composeTestRule.onAllNodesWithText("SESSÃO CONCLUÍDA").assertCountEquals(2)
        composeTestRule.onNodeWithText("⚡ +60 XP").assertExists()
        composeTestRule.onNodeWithText("Continuar").assertIsDisplayed()
    }

    @Test
    fun multiSession_routesToAggregatedView() {
        setContent(
            aggregateCelebrations(
                listOf(
                    entity("s1", xp = 60, gold = 90, completedAt = 1_000L),
                    entity("s2", xp = 50, gold = 75, completedAt = 2_000L),
                    entity("s3", xp = 70, gold = 80, completedAt = 3_000L)
                ),
                JsonConfig.default
            )
        )

        composeTestRule.onNodeWithText("3 SESSÕES CONCLUÍDAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 sessões concluídas enquanto você estava fora").assertIsDisplayed()
        composeTestRule.onNodeWithText("+180 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("+245 Ouro").assertIsDisplayed()
    }

    @Test
    fun multiSession_confirmButton_invokesOnDismiss() {
        var dismissed = false
        setContent(
            aggregateCelebrations(listOf(entity("s1"), entity("s2")), JsonConfig.default),
            onDismiss = { dismissed = true }
        )

        composeTestRule.onNodeWithText("Continuar").performClick()

        assertTrue(dismissed)
    }
}
