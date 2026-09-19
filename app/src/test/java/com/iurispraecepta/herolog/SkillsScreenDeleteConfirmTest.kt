package com.iurispraecepta.herolog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.skills.SkillsScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regressão da issue #18: o modal de confirmação de ESQUECER era aninhado dentro do
 * conteúdo do modal de criação, então só compunha com o modal de criação aberto
 * (ESQUECER parecia morto; clicar em NOVA "ressuscitava" a confirmação).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class SkillsScreenDeleteConfirmTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSkills = listOf(
        Skill(
            name = "Estudos",
            level = 10,
            xp = 320,
            emoji = "📚",
            tags = listOf("Direito")
        ),
        Skill(
            name = "Foco Profundo",
            level = 5,
            xp = 100,
            emoji = "🧠",
            tags = emptyList()
        )
    )

    private fun setContent(
        onDeleteSkill: (Int) -> Unit = {},
        isCreateModalOpen: Boolean = false,
    ) {
        composeTestRule.setContent {
            HeroLogTheme {
                SkillsScreen(
                    skills = sampleSkills,
                    onAddTagToSkill = { _, _ -> },
                    onRemoveTagFromSkill = { _, _ -> },
                    onAddCustomSkill = { _, _ -> },
                    onDeleteSkill = onDeleteSkill,
                    onPrestigeSkill = {},
                    onRenameSkill = { _, _ -> },
                    isCreateModalOpen = isCreateModalOpen,
                    onCreateModalOpenChange = {}
                )
            }
        }
    }

    @Test
    fun forgetButton_opensConfirmImmediately_withoutCreateModal() {
        setContent(isCreateModalOpen = false)

        composeTestRule.onNodeWithTag("forgetSkill_0").performClick()

        // HeroLogModal exibe o título em uppercase.
        composeTestRule.onNodeWithText("ESQUECER HABILIDADE?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("forgetConfirmButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("forgetCancelButton").assertIsDisplayed()
    }

    @Test
    fun forgetConfirm_cancel_doesNotDeleteSkill() {
        val deleted = mutableListOf<Int>()
        setContent(onDeleteSkill = { deleted.add(it) })

        composeTestRule.onNodeWithTag("forgetSkill_1").performClick()
        composeTestRule.onNodeWithText("ESQUECER HABILIDADE?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("forgetCancelButton").performClick()
        composeTestRule.mainClock.advanceTimeBy(500)

        composeTestRule.onNodeWithText("ESQUECER HABILIDADE?").assertDoesNotExist()
        assertTrue(deleted.isEmpty())
    }

    @Test
    fun forgetConfirm_confirm_deletesSkill() {
        val deleted = mutableListOf<Int>()
        setContent(onDeleteSkill = { deleted.add(it) })

        composeTestRule.onNodeWithTag("forgetSkill_1").performClick()
        composeTestRule.onNodeWithTag("forgetConfirmButton").performClick()
        composeTestRule.mainClock.advanceTimeBy(500)

        assertEquals(listOf(1), deleted)
        composeTestRule.onNodeWithText("ESQUECER HABILIDADE?").assertDoesNotExist()
    }
}
