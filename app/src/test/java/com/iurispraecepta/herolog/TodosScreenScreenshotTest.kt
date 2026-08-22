package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Todo
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.todos.TodoFilter
import com.iurispraecepta.herolog.ui.todos.TodosScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class TodosScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun todosScreen_emptyList_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = emptyList(),
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_empty.png")
    }

    @Test
    fun todosScreen_incompleteItemWithoutChecklist_screenshot() {
        val sampleTodos = listOf(
            Todo(
                id = "t1",
                title = "Resolver 3 problemas de Árvores Binárias",
                notes = "Foco em DFS e BFS com complexidade O(n)",
                difficulty = Difficulty.Medium,
                completed = false,
                tags = listOf("algoritmos", "estudo"),
                checklist = emptyList(),
                createdAt = "2026-08-20T10:00:00Z"
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_incomplete_no_checklist.png")
    }

    @Test
    fun todosScreen_completedItemWithoutChecklist_screenshot() {
        val sampleTodos = listOf(
            Todo(
                id = "t2",
                title = "Enviar declaração de rendimentos",
                notes = "Documentação enviada para a contabilidade",
                difficulty = Difficulty.Easy,
                completed = true,
                tags = listOf("financas"),
                checklist = emptyList(),
                createdAt = "2026-08-18T10:00:00Z",
                completedAt = "2026-08-19T14:00:00Z"
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    initialFilter = TodoFilter.Completed,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_completed_no_checklist.png")
    }

    @Test
    fun todosScreen_incompleteItemWithChecklistCollapsed_screenshot() {
        val sampleTodos = listOf(
            Todo(
                id = "t3",
                title = "Deploy do Backend em Produção",
                notes = "Configurar containers e variáveis de ambiente no cluster",
                difficulty = Difficulty.Hard,
                completed = false,
                tags = listOf("devops", "cloud"),
                checklist = listOf(
                    ChecklistItem(id = "c1", text = "Rodar migrações do banco", completed = true),
                    ChecklistItem(id = "c2", text = "Verificar certificados TLS/SSL", completed = false),
                    ChecklistItem(id = "c3", text = "Configurar health check endpoints", completed = false)
                ),
                createdAt = "2026-08-15T12:00:00Z"
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {},
                    initialExpandedTodoId = null
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_incomplete_checklist_collapsed.png")
    }

    @Test
    fun todosScreen_incompleteItemWithChecklistExpandedManually_screenshot() {
        val sampleTodos = listOf(
            Todo(
                id = "t4",
                title = "Deploy do Backend em Produção",
                notes = "Configurar containers e variáveis de ambiente no cluster",
                difficulty = Difficulty.Hard,
                completed = false,
                tags = listOf("devops", "cloud"),
                checklist = listOf(
                    ChecklistItem(id = "c1", text = "Rodar migrações do banco", completed = true),
                    ChecklistItem(id = "c2", text = "Verificar certificados TLS/SSL", completed = false),
                    ChecklistItem(id = "c3", text = "Configurar health check endpoints", completed = false)
                ),
                createdAt = "2026-08-15T12:00:00Z"
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {},
                    initialExpandedTodoId = "t4"
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_incomplete_checklist_expanded.png")
    }

    @Test
    fun todosScreen_completedItemWithChecklist_screenshot() {
        val sampleTodos = listOf(
            Todo(
                id = "t5",
                title = "Refatoração de Módulos Legados",
                notes = "Extração de interfaces e cobertura de testes",
                difficulty = Difficulty.Medium,
                completed = true,
                tags = listOf("refactor"),
                checklist = listOf(
                    ChecklistItem(id = "c1", text = "Isolar lógica de negócio", completed = true),
                    ChecklistItem(id = "c2", text = "Escrever 20 testes unitários", completed = true)
                ),
                createdAt = "2026-08-10T12:00:00Z",
                completedAt = "2026-08-18T16:00:00Z"
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    initialFilter = TodoFilter.Completed,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {},
                    initialExpandedTodoId = null // Deve permanecer colapsado (NÃO expande sozinho)
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_completed_checklist_collapsed.png")
    }

    @Test
    fun todosScreen_createModalWithChecklistBeingBuilt_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = emptyList(),
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {},
                    initialIsCreating = true,
                    initialChecklistItems = listOf("Passo 1: Esboçar arquitetura", "Passo 2: Definir contratos de API", "Passo 3: Testar integração")
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_create_modal_with_checklist.png")
    }

    @Test
    fun todosScreen_editModalWithoutChecklistBuilder_screenshot() {
        val existingTodo = Todo(
            id = "t6",
            title = "Escrever Artigo sobre Kotlin Multiplatform",
            notes = "Publicar no Dev.to e Medium",
            difficulty = Difficulty.Hard,
            completed = false,
            tags = listOf("escrita", "kotlin"),
            checklist = listOf(
                ChecklistItem(id = "c1", text = "Introdução e contexto", completed = true)
            ),
            createdAt = "2026-08-19T08:00:00Z"
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = listOf(existingTodo),
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {},
                    initialEditingTodo = existingTodo
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_edit_modal_no_checklist.png")
    }

    @Test
    fun todosScreen_filterPending_screenshot() {
        val sampleTodos = listOf(
            Todo(id = "t1", title = "Tarefa Pendente 1", notes = "", difficulty = Difficulty.Easy, completed = false, tags = emptyList(), checklist = emptyList()),
            Todo(id = "t2", title = "Tarefa Concluída 1", notes = "", difficulty = Difficulty.Medium, completed = true, tags = emptyList(), checklist = emptyList())
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    initialFilter = TodoFilter.Pending,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_filter_pending.png")
    }

    @Test
    fun todosScreen_filterCompleted_screenshot() {
        val sampleTodos = listOf(
            Todo(id = "t1", title = "Tarefa Pendente 1", notes = "", difficulty = Difficulty.Easy, completed = false, tags = emptyList(), checklist = emptyList()),
            Todo(id = "t2", title = "Tarefa Concluída 1", notes = "", difficulty = Difficulty.Medium, completed = true, tags = emptyList(), checklist = emptyList())
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    initialFilter = TodoFilter.Completed,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_filter_completed.png")
    }

    @Test
    fun todosScreen_filterAll_screenshot() {
        val sampleTodos = listOf(
            Todo(id = "t1", title = "Tarefa Pendente 1", notes = "", difficulty = Difficulty.Easy, completed = false, tags = emptyList(), checklist = emptyList()),
            Todo(id = "t2", title = "Tarefa Concluída 1", notes = "", difficulty = Difficulty.Medium, completed = true, tags = emptyList(), checklist = emptyList())
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    initialFilter = TodoFilter.All,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_filter_all.png")
    }

    @Test
    fun todosScreen_filtersDisabledWithModalOpen_screenshot() {
        val sampleTodos = listOf(
            Todo(id = "t1", title = "Tarefa Ativa", notes = "", difficulty = Difficulty.Easy, completed = false, tags = emptyList(), checklist = emptyList())
        )

        composeTestRule.setContent {
            HeroLogTheme {
                TodosScreen(
                    todos = sampleTodos,
                    initialIsCreating = true,
                    onToggleTodo = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddTodo = { _, _, _, _, _ -> },
                    onEditTodo = {},
                    onDeleteTodo = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/todos_screen_filters_disabled_modal_open.png")
    }
}
