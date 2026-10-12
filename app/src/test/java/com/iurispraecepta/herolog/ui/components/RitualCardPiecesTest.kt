package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.ritualDifficultyStyle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bloco B (spec-rituais-cards): peças compartilhadas dos cards de Rituais.
 * Padrão de teste: `HeroLogTheme(darkTheme = true)` + fundo `QuestPanel`
 * (AGENTS.md regra 10 — stubs claros sobre fundo branco do Robolectric saem
 * invisíveis no PNG; aqui o fundo escuro garante assert legível).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RitualCardPiecesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setPieces(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    content()
                }
            }
        }
    }

    @Test
    fun badge_showsUppercasePtLabel_perDifficulty() {
        // Arrange/Act/Assert: chave interna em inglês, label PT em uppercase
        setPieces {
            Column {
                RitualDifficultyBadge(Difficulty.Trivial)
                RitualDifficultyBadge(Difficulty.Easy)
                RitualDifficultyBadge(Difficulty.Medium)
                RitualDifficultyBadge(Difficulty.Hard)
            }
        }
        composeTestRule.onNodeWithText("TRIVIAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("FÁCIL").assertIsDisplayed()
        composeTestRule.onNodeWithText("MÉDIO").assertIsDisplayed()
        composeTestRule.onNodeWithText("DIFÍCIL").assertIsDisplayed()
    }

    @Test
    fun progressChip_showsCountOrOneZeroStates() {
        // Arrange/Act: c/t com checklist; 1/1 concluído; 0/1 pendente
        // Assert: campo a campo
        setPieces {
            Column {
                RitualProgressChip(completedCount = 2, totalCount = 3, done = false, hasChecklist = true)
                RitualProgressChip(completedCount = 0, totalCount = 1, done = true, hasChecklist = false)
                RitualProgressChip(completedCount = 0, totalCount = 1, done = false, hasChecklist = false)
            }
        }
        composeTestRule.onNodeWithText("2/3").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/1").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/1").assertIsDisplayed()
    }

    @Test
    fun drawer_togglesImmediately_andItemClickReportsId() {
        // Arrange: gaveta com 2 itens, estado como nos cards (useState do React)
        var toggledId: String? = null
        setPieces {
            var expanded by remember { mutableStateOf(false) }
            Column {
                RitualChevronButton(expanded = expanded, onToggle = { expanded = !expanded })
                if (expanded) {
                    RitualChecklistDrawer(
                        items = listOf(
                            ChecklistItem(id = "a", text = "Primeiro item", completed = true),
                            ChecklistItem(id = "b", text = "Segundo item", completed = false)
                        ),
                        onToggleItem = { toggledId = it }
                    )
                }
            }
        }
        // Act: gaveta fechada não mostra itens; chevron abre sem animação
        composeTestRule.onNodeWithContentDescription("Expandir checklist").performClick()
        // Assert: itens visíveis + clique propaga o id (não abre edição — sem callback de edição aqui)
        composeTestRule.onNodeWithText("Primeiro item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Segundo item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Segundo item").performClick()
        assertEquals("b", toggledId)
        // Act: chevron fecha de imediato
        composeTestRule.onNodeWithContentDescription("Recolher checklist").performClick()
        composeTestRule.onNodeWithText("Primeiro item").assertDoesNotExist()
    }

    @Test
    fun pressable_forwardsClick() {
        // Arrange/Act: modificador de pressão repassa o clique (sem ripple)
        // Assert: exatamente 1 chamada
        var clicks = 0
        setPieces {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .testTag("pressTarget")
                    .ritualPressable { clicks++ }
            )
        }
        composeTestRule.onNodeWithTag("pressTarget").performClick()
        composeTestRule.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun difficultyEdge_andGlow_composeWithoutCrash() {
        // Arrange/Act: barra desenhada + glow radial compõem nas 4 dificuldades
        // Assert: smoke test de composição (cores validadas no Bloco A)
        setPieces {
            Column {
                Difficulty.entries.forEach { difficulty ->
                    Box(
                        modifier = Modifier
                            .height(88.dp)
                            .ritualDifficultyEdge(ritualDifficultyStyle(difficulty).pastel)
                    ) {
                        Box(modifier = Modifier.ritualGlow())
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
