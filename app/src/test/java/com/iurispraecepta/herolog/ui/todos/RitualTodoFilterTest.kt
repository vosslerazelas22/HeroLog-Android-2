package com.iurispraecepta.herolog.ui.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Todo
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bloco F (spec-rituais-cards): cabeçalho, filtros e vazio da tela de Tarefas.
 * Lógica de filtro mantida (`TodoFilter`, padrão `Pending`); só o visual mudou.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RitualTodoFilterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun todo(id: String, title: String, completed: Boolean) = Todo(
        id = id,
        title = title,
        notes = "",
        difficulty = Difficulty.Easy,
        completed = completed,
        tags = emptyList(),
        checklist = emptyList(),
        createdAt = null,
        completedAt = null
    )

    private fun setScreen(todos: List<Todo>) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    TodosScreen(
                        todos = todos,
                        onToggleTodo = {},
                        onToggleChecklistItem = { _, _ -> },
                        onAddTodo = { _, _, _, _, _ -> },
                        onEditTodo = {},
                        onDeleteTodo = {}
                    )
                }
            }
        }
    }

    @Test
    fun header_showsTitle_filteredCounter_andNewButton() {
        // Arrange/Act: 1 pendente + 1 concluída, filtro padrão Pendentes
        // Assert: contador é das FILTRADAS ("1 missão")
        setScreen(
            listOf(
                todo("p1", "Varrer o sótão", false),
                todo("c1", "Encerar o corrimão", true)
            )
        )
        composeTestRule.onNodeWithText("MISSÕES AVULSAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 missão").assertIsDisplayed()
        composeTestRule.onNodeWithText("NOVO").assertIsDisplayed()
        composeTestRule.onNodeWithText("Varrer o sótão").assertIsDisplayed()
        composeTestRule.onNodeWithText("Encerar o corrimão").assertDoesNotExist()
    }

    @Test
    fun filters_switchBetweenPendingCompletedAndAll() {
        // Arrange/Act: alterna pelos 3 chips
        // Assert: lista e contador seguem o filtro
        setScreen(
            listOf(
                todo("p1", "Varrer o sótão", false),
                todo("c1", "Encerar o corrimão", true)
            )
        )
        composeTestRule.onNodeWithText("Concluídos").performClick()
        composeTestRule.onNodeWithText("Encerar o corrimão").assertIsDisplayed()
        composeTestRule.onNodeWithText("Varrer o sótão").assertDoesNotExist()
        composeTestRule.onNodeWithText("1 missão").assertIsDisplayed()

        composeTestRule.onNodeWithText("Todos").performClick()
        composeTestRule.onNodeWithText("Varrer o sótão").assertIsDisplayed()
        composeTestRule.onNodeWithText("Encerar o corrimão").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 missões").assertIsDisplayed()

        composeTestRule.onNodeWithText("Pendentes").performClick()
        composeTestRule.onNodeWithText("Varrer o sótão").assertIsDisplayed()
        composeTestRule.onNodeWithText("Encerar o corrimão").assertDoesNotExist()
    }

    @Test
    fun empty_pendingFilter_showsPendingMessage() {
        // Arrange/Act: sem tarefas, filtro padrão Pendentes
        // Assert: contador zerado + mensagem do filtro (spec-rebranding item 2)
        setScreen(emptyList())
        composeTestRule.onNodeWithText("0 missões").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Nenhuma missão pendente. Crie uma.")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Nenhuma missão concluída ainda.")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Nenhuma missão ainda. Crie a primeira.")
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("NOVO").assertIsDisplayed()
    }

    @Test
    fun empty_completedFilter_showsCompletedMessage() {
        // Arrange/Act: sem tarefas, filtro Concluídos
        // Assert: mensagem própria do filtro
        setScreen(emptyList())
        composeTestRule.onNodeWithText("Concluídos").performClick()
        composeTestRule
            .onNodeWithText("Nenhuma missão concluída ainda.")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Nenhuma missão pendente. Crie uma.")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Nenhuma missão ainda. Crie a primeira.")
            .assertDoesNotExist()
    }

    @Test
    fun empty_allFilter_showsAllMessage() {
        // Arrange/Act: sem tarefas, filtro Todos
        // Assert: mensagem própria do filtro
        setScreen(emptyList())
        composeTestRule.onNodeWithText("Todos").performClick()
        composeTestRule
            .onNodeWithText("Nenhuma missão ainda. Crie a primeira.")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Nenhuma missão pendente. Crie uma.")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Nenhuma missão concluída ainda.")
            .assertDoesNotExist()
    }
}
