package com.iurispraecepta.herolog.ui.dailies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.RepeatInterval
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
 * Bloco D (spec-rituais-cards): card de Diária + frequência localizada (D5).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RitualDailyCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun daily(
        completed: Boolean = false,
        repeats: RepeatInterval = RepeatInterval.Weekly,
        every: Int = 1,
        streak: Int = 0,
        checklist: List<ChecklistItem> = emptyList(),
        notes: String = "revisar anotações"
    ) = Daily(
        id = "d1",
        title = "Meditar",
        notes = notes,
        difficulty = Difficulty.Medium,
        completed = completed,
        streak = streak,
        repeats = repeats,
        every = every,
        tags = emptyList(),
        checklist = checklist,
        value = null,
        createdAt = null
    )

    private fun setCard(
        daily: Daily,
        isExpanded: Boolean = false,
        onToggleExpand: () -> Unit = {},
        onToggleDaily: () -> Unit = {},
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
                    RitualDailyCard(
                        daily = daily,
                        isExpanded = isExpanded,
                        onToggleExpand = onToggleExpand,
                        onToggleDaily = onToggleDaily,
                        onToggleChecklistItem = onToggleItem,
                        onOpenEdit = onEdit
                    )
                }
            }
        }
    }

    @Test
    fun pending_showsTitle_badge_xp_frequency_andZeroState() {
        // Arrange/Act: Diária Medium (28 XP), semanal, pendente
        // Assert: campo a campo
        setCard(daily())
        composeTestRule.onNodeWithText("Meditar").assertIsDisplayed()
        composeTestRule.onNodeWithText("MÉDIO").assertIsDisplayed()
        composeTestRule.onNodeWithText("28 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText("Semanalmente").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/1").assertIsDisplayed()
        composeTestRule.onNodeWithText("revisar anotações").assertIsDisplayed()
    }

    @Test
    fun frequency_localizesEveryOneAndEveryN() {
        // Arrange/Act/Assert: D5, campo a campo (sem o "Weekly (2x)" do React)
        assertEquals("Diariamente", ritualFrequencyText(RepeatInterval.Daily, 1))
        assertEquals("Semanalmente", ritualFrequencyText(RepeatInterval.Weekly, 1))
        assertEquals("Mensalmente", ritualFrequencyText(RepeatInterval.Monthly, 1))
        assertEquals("A cada 2 dias", ritualFrequencyText(RepeatInterval.Daily, 2))
        assertEquals("A cada 2 semanas", ritualFrequencyText(RepeatInterval.Weekly, 2))
        assertEquals("A cada 3 meses", ritualFrequencyText(RepeatInterval.Monthly, 3))
    }

    @Test
    fun completed_showsOneState_andStreak() {
        // Arrange/Act: concluída com streak 4
        // Assert: campo a campo
        setCard(daily(completed = true, streak = 4))
        composeTestRule.onNodeWithText("1/1").assertIsDisplayed()
        composeTestRule.onNodeWithText("4d").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/1").assertDoesNotExist()
    }

    @Test
    fun completed_doesNotAutoExpandDrawer() {
        // Arrange/Act: concluída COM checklist, gaveta fechada
        // Assert: itens ocultos (removido o `isExpanded || daily.completed`)
        setCard(
            daily(
                completed = true,
                checklist = listOf(ChecklistItem(id = "i1", text = "Item um", completed = true))
            ),
            isExpanded = false
        )
        composeTestRule.onNodeWithText("Item um").assertDoesNotExist()
        composeTestRule.onNodeWithText("1/1").assertIsDisplayed()
    }

    @Test
    fun chevron_togglesState_inSitu() {
        // Arrange: estado vive no chamador, como na tela/React
        // Act: toque no chevron
        // Assert: chevron vira para cima (estado alternou; gaveta aberta coberta abaixo)
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    RitualDailyCard(
                        daily = daily(
                            checklist = listOf(
                                ChecklistItem(id = "i1", text = "Item um", completed = true)
                            )
                        ),
                        isExpanded = expanded.value,
                        onToggleExpand = { expanded.value = !expanded.value },
                        onToggleDaily = {},
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
    fun expandedDrawer_showsItems_andItemClickReportsId() {
        // Arrange/Act: gaveta aberta com 2 itens (1 feito)
        // Assert: itens visíveis + clique propaga o id (sem abrir edição)
        var toggled: String? = null
        var edits = 0
        setCard(
            daily(
                checklist = listOf(
                    ChecklistItem(id = "i1", text = "Item um", completed = true),
                    ChecklistItem(id = "i2", text = "Item dois", completed = false)
                )
            ),
            isExpanded = true,
            onToggleItem = { toggled = it },
            onEdit = { edits++ }
        )
        composeTestRule.onNodeWithTag("ritualDrawer").assertExists()
        composeTestRule.onNodeWithTag("ritualDrawer").assertWidthIsAtLeast(10.dp)
        composeTestRule.onNodeWithTag("ritualDrawer").assertHeightIsAtLeast(10.dp)
        composeTestRule.onNodeWithText("Item um").assertIsDisplayed()
        composeTestRule.onNodeWithText("Item dois").assertIsDisplayed()
        composeTestRule.onNodeWithText("Item dois").performClick()
        composeTestRule.runOnIdle {
            assertEquals("i2", toggled)
            assertEquals(0, edits)
        }
    }

    @Test
    fun checkbox_toggles_withoutOpeningEdit() {
        // Arrange/Act: toque no checkbox
        // Assert: alterna sem abrir edição
        var toggles = 0
        var edits = 0
        setCard(
            daily(),
            onToggleDaily = { toggles++ },
            onEdit = { edits++ }
        )
        composeTestRule.onNodeWithContentDescription("Alternar missão diária Meditar").performClick()
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
        setCard(daily(), onEdit = { edits++ })
        composeTestRule.onNodeWithText("Meditar").performClick()
        composeTestRule.runOnIdle { assertEquals(1, edits) }
    }

    @Test
    fun narrowCard_wrapsMetadata_keepingFrequencyVisible() {
        // Regressão (Bloco F): mesma causa do card de Hábito — metadados longos em
        // 280dp precisam quebrar linha sem perder nenhum segmento.
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    Box(modifier = Modifier.width(280.dp)) {
                        RitualDailyCard(
                            daily = daily(
                                repeats = RepeatInterval.Weekly,
                                every = 2,
                                streak = 9,
                                checklist = listOf(
                                    ChecklistItem(id = "i1", text = "Item um", completed = false)
                                )
                            ),
                            isExpanded = false,
                            onToggleExpand = {},
                            onToggleDaily = {},
                            onToggleChecklistItem = {},
                            onOpenEdit = {}
                        )
                    }
                }
            }
        }
        composeTestRule.onNodeWithText("A cada 2 semanas").assertIsDisplayed()
        composeTestRule.onNodeWithText("9d").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/1").assertIsDisplayed()
    }

    @Test
    fun noChecklist_hidesChevron() {
        // Arrange/Act: sem checklist
        // Assert: sem chevron
        setCard(daily(checklist = emptyList()))
        composeTestRule.onNodeWithContentDescription("Expandir checklist").assertDoesNotExist()
    }
}
