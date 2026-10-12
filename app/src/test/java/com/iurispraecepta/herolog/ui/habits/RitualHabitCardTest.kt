package com.iurispraecepta.herolog.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bloco C (spec-rituais-cards): card de Hábito.
 * "Hoje" é string injetada (vem de `rememberTodayDateString(habits)` na tela).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RitualHabitCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun habit(
        up: Boolean = true,
        down: Boolean = true,
        lastTriggeredDate: String? = null,
        streak: Int = 0,
        notes: String = "ler 10 páginas",
        difficulty: Difficulty = Difficulty.Easy
    ) = Habit(
        id = "h1",
        title = "Ler",
        notes = notes,
        up = up,
        down = down,
        difficulty = difficulty,
        upCount = 3,
        downCount = 1,
        streak = streak,
        tags = emptyList(),
        lastTriggeredDate = lastTriggeredDate
    )

    private fun setCard(
        habit: Habit,
        today: String = "2026-10-12",
        onTrigger: (String, Boolean) -> Unit = { _, _ -> },
        onEdit: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    RitualHabitCard(
                        habit = habit,
                        todayDateStr = today,
                        onTriggerHabit = onTrigger,
                        onOpenEdit = onEdit
                    )
                }
            }
        }
    }

    @Test
    fun pending_showsPendingStatus_xp_counter_andBadge() {
        // Arrange/Act: hábito Easy (12 XP) não registrado hoje
        // Assert: campo a campo
        setCard(habit())
        composeTestRule.onNodeWithText("Pendente hoje").assertIsDisplayed()
        composeTestRule.onNodeWithText("Concluído hoje").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("+12 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("(+3 | -1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("FÁCIL").assertIsDisplayed()
        composeTestRule.onNodeWithText("ler 10 páginas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ler").assertIsDisplayed()
    }

    @Test
    fun recorded_showsDoneStatus_andStreak() {
        // Arrange/Act: registrado hoje + streak 5
        // Assert: campo a campo
        setCard(habit(lastTriggeredDate = "2026-10-12", streak = 5))
        composeTestRule.onNodeWithText("Concluído hoje").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pendente hoje").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("5d streak").assertIsDisplayed()
    }

    @Test
    fun noStreak_hidesStreakSegment() {
        // Arrange/Act: streak 0
        // Assert: sem segmento de streak
        setCard(habit(streak = 0))
        composeTestRule.onNodeWithText("0d streak").assertDoesNotExist()
    }

    @Test
    fun noNotes_hidesNotesLine() {
        // Arrange/Act: sem notas
        // Assert: título presente, linha de notas ausente
        setCard(habit(notes = ""))
        composeTestRule.onNodeWithText("Ler").assertIsDisplayed()
        composeTestRule.onNodeWithText("ler 10 páginas").assertDoesNotExist()
    }

    @Test
    fun plusButton_triggersUp_withoutOpeningEdit() {
        // Arrange/Act: toque no +
        // Assert: trigger(true) e edição NÃO abre (sem propagação)
        var triggered: Pair<String, Boolean>? = null
        var edits = 0
        setCard(
            habit(),
            onTrigger = { id, isUp -> triggered = id to isUp },
            onEdit = { edits++ }
        )
        composeTestRule.onNodeWithContentDescription("Registrar hábito Ler").performClick()
        composeTestRule.runOnIdle {
            assertEquals("h1" to true, triggered)
            assertEquals(0, edits)
        }
    }

    @Test
    fun minusButton_triggersDown_withoutOpeningEdit() {
        // Arrange/Act: toque no −
        // Assert: trigger(false) e edição NÃO abre
        var triggered: Pair<String, Boolean>? = null
        var edits = 0
        setCard(
            habit(),
            onTrigger = { id, isUp -> triggered = id to isUp },
            onEdit = { edits++ }
        )
        composeTestRule.onNodeWithContentDescription("Registrar desvio negativo de Ler").performClick()
        composeTestRule.runOnIdle {
            assertEquals("h1" to false, triggered)
            assertEquals(0, edits)
        }
    }

    @Test
    fun noUp_showsDot_insteadOfPlusButton() {
        // Arrange/Act: hábito só negativo
        // Assert: sem botão +, com slot do ponto (layout não colapsa)
        var triggered: Pair<String, Boolean>? = null
        setCard(habit(up = false), onTrigger = { id, isUp -> triggered = id to isUp })
        composeTestRule.onNodeWithContentDescription("Registrar hábito Ler").assertDoesNotExist()
        composeTestRule.onNodeWithTag("habitDot", useUnmergedTree = true).assertIsDisplayed()
        assertNull(triggered)
    }

    @Test
    fun noDown_hidesMinusButton() {
        // Arrange/Act: hábito só positivo
        // Assert: sem botão −
        setCard(habit(down = false))
        composeTestRule
            .onNodeWithContentDescription("Registrar desvio negativo de Ler")
            .assertDoesNotExist()
    }

    @Test
    fun narrowCard_wrapsStatusLine_keepingCounterVisible() {
        // Regressão (Bloco F): height(IntrinsicSize.Min) travava o FlowRow em 1 linha
        // e as quebradas eram descartadas. Em 280dp o contador precisa quebrar linha.
        // Arrange/Act: dados longos (streak + contador) em largura estreita
        // Assert: contador visível (quebrado, não descartado)
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    Box(modifier = Modifier.width(280.dp)) {
                        RitualHabitCard(
                            habit = habit(
                                lastTriggeredDate = "2026-10-12",
                                streak = 12,
                                difficulty = Difficulty.Easy
                            ),
                            todayDateStr = "2026-10-12",
                            onTriggerHabit = { _, _ -> },
                            onOpenEdit = {}
                        )
                    }
                }
            }
        }
        composeTestRule.onNodeWithText("Concluído hoje").assertIsDisplayed()
        composeTestRule.onNodeWithText("12d streak").assertIsDisplayed()
        composeTestRule.onNodeWithText("(+3 | -1)").assertIsDisplayed()
    }

    @Test
    fun bodyClick_opensEdit() {
        // Arrange/Act: toque no título (corpo)
        // Assert: abre edição
        var edits = 0
        setCard(habit(), onEdit = { edits++ })
        composeTestRule.onNodeWithText("Ler").performClick()
        composeTestRule.runOnIdle { assertEquals(1, edits) }
    }
}
