package com.iurispraecepta.herolog.ui.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Todo
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bloco E (spec-rituais-cards): card de Tarefa (sem frequência/streak/data).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RitualTodoCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun todo(
        completed: Boolean = false,
        checklist: List<ChecklistItem> = emptyList(),
        notes: String = "comprar pergaminhos"
    ) = Todo(
        id = "t1",
        title = "Limpar o porão",
        notes = notes,
        difficulty = Difficulty.Hard,
        completed = completed,
        tags = emptyList(),
        checklist = checklist,
        createdAt = null,
        completedAt = null
    )

    private fun setCard(
        todo: Todo,
        isExpanded: Boolean = false,
        onToggleExpand: () -> Unit = {},
        onToggleTodo: () -> Unit = {},
        onToggleItem: (String) -> Unit = {},
        onEdit: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    RitualTodoCard(
                        todo = todo,
                        isExpanded = isExpanded,
                        onToggleExpand = onToggleExpand,
                        onToggleTodo = onToggleTodo,
                        onToggleChecklistItem = onToggleItem,
                        onOpenEdit = onEdit
                    )
                }
            }
        }
    }

    @Test
    fun pending_showsTitle_badge_xpOnly_andZeroState() {
        // Arrange/Act: Tarefa Hard (60 XP), pendente
        // Assert: só XP nos metadados (sem frequência/streak/data/rótulo)
        setCard(todo())
        composeTestRule.onNodeWithText("Limpar o porão").assertIsDisplayed()
        composeTestRule.onNodeWithText("DIFÍCIL").assertIsDisplayed()
        composeTestRule.onNodeWithText("60 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tarefa Única").assertDoesNotExist()
        composeTestRule.onNodeWithText("Diariamente").assertDoesNotExist()
    }

    @Test
    fun completed_showsOneState() {
        // Arrange/Act: concluída
        // Assert: campo a campo
        setCard(todo(completed = true))
        composeTestRule.onNodeWithText("1/1").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/1").assertDoesNotExist()
    }

    @Test
    fun expandedDrawer_showsItems_andItemClickReportsId() {
        // Arrange/Act: gaveta aberta (rede de regressão do bug do Bloco D)
        // Assert: gaveta com altura sã + itens + id propagado sem abrir edição
        var toggled: String? = null
        var edits = 0
        setCard(
            todo(
                checklist = listOf(
                    ChecklistItem(id = "i1", text = "Varrer", completed = false),
                    ChecklistItem(id = "i2", text = "Encerar", completed = false)
                )
            ),
            isExpanded = true,
            onToggleItem = { toggled = it },
            onEdit = { edits++ }
        )
        composeTestRule.onNodeWithTag("ritualDrawer").assertExists()
        composeTestRule.onNodeWithTag("ritualDrawer").assertWidthIsAtLeast(10.dp)
        composeTestRule.onNodeWithTag("ritualDrawer").assertHeightIsAtLeast(10.dp)
        composeTestRule.onNodeWithText("0/2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Encerar").performClick()
        composeTestRule.runOnIdle {
            assertEquals("i2", toggled)
            assertEquals(0, edits)
        }
    }

    @Test
    fun chevron_togglesState_inSitu() {
        // Arrange/Act: toque no chevron
        // Assert: estado alterna (chevron vira para cima)
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    RitualTodoCard(
                        todo = todo(
                            checklist = listOf(
                                ChecklistItem(id = "i1", text = "Varrer", completed = false)
                            )
                        ),
                        isExpanded = expanded.value,
                        onToggleExpand = { expanded.value = !expanded.value },
                        onToggleTodo = {},
                        onToggleChecklistItem = {},
                        onOpenEdit = {}
                    )
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Expandir checklist").performClick()
        composeTestRule.onNodeWithContentDescription("Recolher checklist").assertIsDisplayed()
    }

    @Test
    fun checkbox_toggles_withoutOpeningEdit() {
        // Arrange/Act: toque no checkbox (rótulo "Alternar afazer")
        // Assert: alterna sem abrir edição
        var toggles = 0
        var edits = 0
        setCard(
            todo(),
            onToggleTodo = { toggles++ },
            onEdit = { edits++ }
        )
        composeTestRule.onNodeWithContentDescription("Alternar afazer Limpar o porão").performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, toggles)
            assertEquals(0, edits)
        }
    }

    @Test
    fun titleClick_opensEdit() {
        // Arrange/Act: toque no título
        // Assert: abre edição
        var edits = 0
        setCard(todo(), onEdit = { edits++ })
        composeTestRule.onNodeWithText("Limpar o porão").performClick()
        composeTestRule.runOnIdle { assertEquals(1, edits) }
    }
}
