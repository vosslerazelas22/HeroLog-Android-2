package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.ui.habits.HabitsScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class HabitsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    @Test
    fun habitsScreen_emptyList_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = emptyList(),
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_empty.png")
    }

    @Test
    fun habitsScreen_upAndDownActive_screenshot() {
        val sampleHabits = listOf(
            Habit(
                id = "h1",
                title = "Alimentação Equilibrada",
                notes = "Evitar ultraprocessados e priorizar comida de verdade",
                up = true,
                down = true,
                difficulty = Difficulty.Medium,
                upCount = 8,
                downCount = 2,
                streak = 5,
                tags = listOf("saude", "nutricao"),
                lastTriggeredDate = todayStr
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = sampleHabits,
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_up_down.png")
    }

    @Test
    fun habitsScreen_onlyUp_screenshot() {
        val sampleHabits = listOf(
            Habit(
                id = "h2",
                title = "Leitura de Livros Técnicos",
                notes = "Mínimo de 1 capítulo focado com anotações",
                up = true,
                down = false,
                difficulty = Difficulty.Easy,
                upCount = 14,
                downCount = 0,
                streak = 12,
                tags = listOf("estudo", "livros"),
                lastTriggeredDate = "2026-08-01"
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = sampleHabits,
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_only_up.png")
    }

    @Test
    fun habitsScreen_onlyDown_screenshot() {
        val sampleHabits = listOf(
            Habit(
                id = "h3",
                title = "Procrastinar em Redes Sociais",
                notes = "Scroll infinito sem propósito no meio do bloco de trabalho",
                up = false,
                down = true,
                difficulty = Difficulty.Hard,
                upCount = 0,
                downCount = 5,
                streak = 0,
                tags = listOf("vicio", "atencao"),
                lastTriggeredDate = null
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = sampleHabits,
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_only_down.png")
    }

    @Test
    fun habitsScreen_createModalOpen_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = emptyList(),
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    initialIsCreating = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_create_modal.png")
    }

    @Test
    fun habitsScreen_editModalOpen_screenshot() {
        val sampleHabit = Habit(
            id = "h1",
            title = "Treino de Força e Mobilidade",
            notes = "Aquecimento articular + séries principais na academia",
            up = true,
            down = true,
            difficulty = Difficulty.Hard,
            upCount = 6,
            downCount = 1,
            streak = 4,
            tags = listOf("treino", "fitness"),
            lastTriggeredDate = todayStr
        )

        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = listOf(sampleHabit),
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    initialEditingHabit = sampleHabit
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_edit_modal.png")
    }

    @Test
    fun habitsScreen_confirmDeleteState_screenshot() {
        val sampleHabit = Habit(
            id = "h1",
            title = "Hábito a ser excluído",
            notes = "Testando o fluxo de exclusão",
            up = true,
            down = false,
            difficulty = Difficulty.Trivial,
            upCount = 1,
            downCount = 0,
            streak = 1,
            tags = listOf("temporario"),
            lastTriggeredDate = todayStr
        )

        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = listOf(sampleHabit),
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    initialEditingHabit = sampleHabit,
                    initialConfirmDelete = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_confirm_delete.png")
    }

    @Test
    fun habitsScreen_confirmCancelState_screenshot() {
        val sampleHabit = Habit(
            id = "h1",
            title = "Hábito em edição",
            notes = "Modificações que podem ser descartadas",
            up = true,
            down = true,
            difficulty = Difficulty.Medium,
            upCount = 3,
            downCount = 0,
            streak = 2,
            tags = listOf("foco"),
            lastTriggeredDate = todayStr
        )

        composeTestRule.setContent {
            HeroLogTheme {
                HabitsScreen(
                    habits = listOf(sampleHabit),
                    onTriggerHabit = { _, _ -> },
                    onAddHabit = { _, _, _, _, _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    initialEditingHabit = sampleHabit,
                    initialConfirmCancel = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habits_screen_confirm_cancel.png")
    }
}
