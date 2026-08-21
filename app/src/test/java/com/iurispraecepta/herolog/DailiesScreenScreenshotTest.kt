package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.ui.dailies.DailiesScreen
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
class DailiesScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dailiesScreen_emptyList_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = emptyList(),
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_empty.png")
    }

    @Test
    fun dailiesScreen_incompleteItemWithoutChecklist_screenshot() {
        val sampleDailies = listOf(
            Daily(
                id = "d1",
                title = "Treino Matinal de Calistenia",
                notes = "Alongamento + 4 séries completas antes do café",
                difficulty = Difficulty.Medium,
                completed = false,
                streak = 7,
                repeats = RepeatInterval.Daily,
                every = 1,
                tags = listOf("saude", "treino"),
                checklist = emptyList(),
                value = 5
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = sampleDailies,
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_incomplete_no_checklist.png")
    }

    @Test
    fun dailiesScreen_completedItemWithoutChecklist_screenshot() {
        val sampleDailies = listOf(
            Daily(
                id = "d2",
                title = "Meditação e Respiração Consciente",
                notes = "15 minutos de foco absoluto",
                difficulty = Difficulty.Easy,
                completed = true,
                streak = 14,
                repeats = RepeatInterval.Daily,
                every = 1,
                tags = listOf("mindfulness"),
                checklist = emptyList(),
                value = 12
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = sampleDailies,
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_completed_no_checklist.png")
    }

    @Test
    fun dailiesScreen_incompleteItemWithChecklistCollapsed_screenshot() {
        val sampleDailies = listOf(
            Daily(
                id = "d3",
                title = "Rotina de Encerramento do Dia",
                notes = "Revisar tarefas, organizar mesa e desconectar telas",
                difficulty = Difficulty.Hard,
                completed = false,
                streak = 3,
                repeats = RepeatInterval.Daily,
                every = 1,
                tags = listOf("noite", "foco"),
                checklist = listOf(
                    ChecklistItem(id = "c1", text = "Guardar cadernos e limpar mesa", completed = true),
                    ChecklistItem(id = "c2", text = "Planejar as 3 prioridades de amanhã", completed = false),
                    ChecklistItem(id = "c3", text = "Desligar telas às 22h", completed = false)
                ),
                value = 2
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = sampleDailies,
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {},
                    initialExpandedDailyId = null
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_incomplete_checklist_collapsed.png")
    }

    @Test
    fun dailiesScreen_incompleteItemWithChecklistExpandedManually_screenshot() {
        val sampleDailies = listOf(
            Daily(
                id = "d4",
                title = "Rotina de Encerramento do Dia",
                notes = "Revisar tarefas, organizar mesa e desconectar telas",
                difficulty = Difficulty.Hard,
                completed = false,
                streak = 3,
                repeats = RepeatInterval.Daily,
                every = 1,
                tags = listOf("noite", "foco"),
                checklist = listOf(
                    ChecklistItem(id = "c1", text = "Guardar cadernos e limpar mesa", completed = true),
                    ChecklistItem(id = "c2", text = "Planejar as 3 prioridades de amanhã", completed = false),
                    ChecklistItem(id = "c3", text = "Desligar telas às 22h", completed = false)
                ),
                value = 2
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = sampleDailies,
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {},
                    initialExpandedDailyId = "d4"
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_incomplete_checklist_expanded.png")
    }

    @Test
    fun dailiesScreen_completedItemWithChecklist_screenshot() {
        val sampleDailies = listOf(
            Daily(
                id = "d5",
                title = "Manutenção Semanal do Grimório",
                notes = "Backup dos dados e organização de notas de estudo",
                difficulty = Difficulty.Medium,
                completed = true,
                streak = 5,
                repeats = RepeatInterval.Weekly,
                every = 1,
                tags = listOf("organizacao"),
                checklist = listOf(
                    ChecklistItem(id = "c1", text = "Sincronizar repositório Git", completed = true),
                    ChecklistItem(id = "c2", text = "Limpar downloads temporários", completed = true),
                    ChecklistItem(id = "c3", text = "Revisar metas semanais", completed = true)
                ),
                value = 8
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = sampleDailies,
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {},
                    initialExpandedDailyId = null // Deve expandir automaticamente pois completed == true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_completed_checklist_auto_expanded.png")
    }

    @Test
    fun dailiesScreen_createModalWithChecklistBeingBuilt_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = emptyList(),
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {},
                    initialIsCreating = true,
                    initialChecklistItems = listOf("Fase 1: Preparação mental", "Fase 2: Execução de 25m", "Fase 3: Registro")
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_create_modal_with_checklist.png")
    }

    @Test
    fun dailiesScreen_editModalWithoutChecklistBuilder_screenshot() {
        val existingDaily = Daily(
            id = "d6",
            title = "Leitura Filosófica Diária",
            notes = "Meditações de Marco Aurélio",
            difficulty = Difficulty.Medium,
            completed = false,
            streak = 10,
            repeats = RepeatInterval.Daily,
            every = 1,
            tags = listOf("estoicismo", "leitura"),
            checklist = listOf(
                ChecklistItem(id = "c1", text = "Ler 2 capítulos", completed = false)
            ),
            value = 6
        )

        composeTestRule.setContent {
            HeroLogTheme {
                DailiesScreen(
                    dailies = listOf(existingDaily),
                    onToggleDaily = {},
                    onToggleChecklistItem = { _, _ -> },
                    onAddDaily = { _, _, _, _, _, _, _, _ -> },
                    onEditDaily = {},
                    onDeleteDaily = {},
                    initialEditingDaily = existingDaily
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dailies_screen_edit_modal_no_checklist.png")
    }
}
