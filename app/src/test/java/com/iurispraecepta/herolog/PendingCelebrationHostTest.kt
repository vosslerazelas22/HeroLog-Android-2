package com.iurispraecepta.herolog

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.focus.CelebrationMode
import com.iurispraecepta.herolog.ui.focus.selectCelebrationMode
import com.iurispraecepta.herolog.ui.focus.PendingCelebrationHost
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
class PendingCelebrationHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun entity(
        id: String,
        xp: Int = 60,
        gold: Int = 90,
        completedAt: Long = 1_000L,
        historyId: String? = null,
        streakAfter: Int = 0,
        showStreakCelebration: Boolean = false,
        pauseCount: Int = 0
    ) = PendingRewardCelebrationEntity(
        id = id,
        sessionCompletedAt = completedAt,
        skillName = "Kotlin",
        durationMinutes = 25,
        xpGained = xp,
        goldGained = gold,
        historyId = historyId,
        streakAfter = streakAfter,
        showStreakCelebration = showStreakCelebration,
        pauseCount = pauseCount
    )

    private fun setContent(
        summary: AggregatedCelebrationSummary,
        onConfirmSingle: (String?, String, String) -> Unit = { _, _, _ -> },
        onDismissAggregated: () -> Unit = {},
        skills: List<Skill> = emptyList()
    ) {
        composeTestRule.setContent {
            HeroLogTheme {
                PendingCelebrationHost(
                    summary = summary,
                    onConfirmSingle = onConfirmSingle,
                    onDismissAggregated = onDismissAggregated,
                    skills = skills
                )
            }
        }
    }

    @Test
    fun selectCelebrationMode_single_returnsSingle() {
        assertEquals(CelebrationMode.SINGLE, selectCelebrationMode(1))
    }

    @Test
    fun selectCelebrationMode_multiple_returnsAggregated() {
        assertEquals(CelebrationMode.AGGREGATED, selectCelebrationMode(2))
        assertEquals(CelebrationMode.AGGREGATED, selectCelebrationMode(5))
    }

    @Test
    fun singleSession_routesToCompletionFlowWithoutModalChrome() {
        setContent(aggregateCelebrations(listOf(entity("s1")), JsonConfig.default))

        // Título do fluxo existe 1x — o título do HeroLogModal ("Sessão Concluída"
        // como header de modal) sumiu junto com o chrome.
        composeTestRule.onAllNodesWithText("SESSÃO CONCLUÍDA").assertCountEquals(1)
        composeTestRule.onNodeWithText("⚡ +60 XP").assertExists()
        composeTestRule.onNodeWithText("Continuar").assertIsDisplayed()
    }

    @Test
    fun singleSession_confirmSingle_forwardsHistoryIdNotesAndTag() {
        var capturedHistoryId: String? = "unset"
        var capturedNotes = ""
        var capturedTag = ""
        val skills = listOf(
            Skill(id = "sk_1", name = "Kotlin", level = 1, xp = 0, tags = listOf("foco-profundo"))
        )
        setContent(
            aggregateCelebrations(
                listOf(entity("s1", historyId = "h-123")),
                JsonConfig.default
            ),
            onConfirmSingle = { historyId, notes, tag ->
                capturedHistoryId = historyId
                capturedNotes = notes
                capturedTag = tag
            },
            skills = skills
        )

        // summary (sem loot) → "Continuar" leva à etapa de notas.
        composeTestRule.onNodeWithText("Continuar").performClick()
        composeTestRule.onNodeWithText("📜 CRÔNICA DA MISSÃO").assertIsDisplayed()
        // Tag da skill real aparece (entidade não guarda tags — resolve via skills).
        composeTestRule.onNodeWithText("foco-profundo").assertIsDisplayed()
        composeTestRule.onNodeWithText("O que você aprendeu ou fez nesta sessão?")
            .performTextInput("revisei coroutines")
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").performClick()

        assertEquals("h-123", capturedHistoryId)
        assertEquals("revisei coroutines", capturedNotes)
        assertEquals("", capturedTag)
    }

    @Test
    fun multiSession_routesToAggregatedWithoutModalChrome() {
        var dismissed = false
        setContent(
            aggregateCelebrations(
                listOf(
                    entity("s1", xp = 60, gold = 90, completedAt = 1_000L),
                    entity("s2", xp = 50, gold = 75, completedAt = 2_000L),
                    entity("s3", xp = 70, gold = 80, completedAt = 3_000L)
                ),
                JsonConfig.default
            ),
            onDismissAggregated = { dismissed = true }
        )

        // Título do HeroLogModal ("3 SESSÕES CONCLUÍDAS") não existe mais.
        composeTestRule.onNodeWithText("3 SESSÕES CONCLUÍDAS").assertDoesNotExist()
        composeTestRule.onNodeWithText("3 sessões concluídas enquanto você estava fora").assertIsDisplayed()
        composeTestRule.onNodeWithText("+180 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("+245 GP").assertIsDisplayed()
        // Botão agora é o do CompletionShell compartilhado.
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").assertIsDisplayed()

        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun singleSession_nullHistoryId_forwardsNull() {
        var capturedHistoryId: String? = "unset"
        setContent(
            aggregateCelebrations(listOf(entity("s1", historyId = null)), JsonConfig.default),
            onConfirmSingle = { historyId, _, _ -> capturedHistoryId = historyId }
        )

        composeTestRule.onNodeWithText("Continuar").performClick()
        composeTestRule.onNodeWithText("RECEBER RECOMPENSAS").performClick()

        assertNull(capturedHistoryId)
    }
}
