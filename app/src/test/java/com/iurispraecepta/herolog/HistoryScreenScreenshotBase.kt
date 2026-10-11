package com.iurispraecepta.herolog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.ui.history.HistoryScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test

/**
 * Classe base dos screenshots de [HistoryScreen] (Missões → Crônicas Diárias).
 * NÃO rodar diretamente: as subclasses [HistoryScreenScreenshotTest] (390×844) e
 * [HistoryScreenScreenshotTest375] (375×667) herdam os 9 @Test e fornecem o
 * qualifier estático do viewport (padrão `BottomNavDiagnosticTest`).
 *
 * Montagem espelha a produção: `Scaffold(containerColor = QuestPanel)` sem topBar/
 * bottomBar — o fundo vem do Scaffold (exatamente o que o patch Trello #72 mudou;
 * NÃO pintar `background(QuestPanel)` no root, mascararia a mudança).
 * `LocalBottomBarInset` fica no default (0.dp, `BottomBarInset.kt:6`).
 *
 * Datas fixas (nunca relógio) para baseline determinístico.
 *
 * Interações: o estado interno (`viewMode`, `expandedChronicleId`) só é acessível
 * por clique — toggle de modo via texto renderizado ("COMPILADO DE NOTAS") e toggle
 * da crônica via "REVELAR CRÔNICA MÍSTICA" (nenhum testTag foi adicionado a produção).
 *
 * Nota: `subskillTag` existe no model mas NÃO é renderizado por HistoryScreen
 * (grep: zero usos no arquivo) — os dados o preenchem mesmo assim; o campo
 * simplesmente não aparece no PNG.
 */
abstract class HistoryScreenScreenshotBase {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Dados de teste (IDs estáveis = keys do LazyColumn) ──

    private val h1 = HistoryEntry(
        id = "h1",
        skillName = "Direito Processual Civil",
        date = "05/01/2026, 09:05:00",
        duration = 25,
        xp = 50,
        gold = 10,
        notes = "Revisão dos capítulos de tutela provisória",
        wilderness = false,
        aiChronicle = null,
        subskillTag = null
    )

    private val h2 = HistoryEntry(
        id = "h2",
        skillName = "Inglês",
        date = "12/03/2026, 21:40:10",
        duration = 5,
        xp = 8,
        gold = 2,
        notes = "Listening: podcast sobre phrasal verbs",
        wilderness = false,
        aiChronicle = "A noite caiu sobre a biblioteca antiga, e as velas tremeluziam " +
                "enquanto as páginas do grimório sussurravam segredos esquecidos. " +
                "Cada phrasal verb era uma chave; cada idiom, um portal para mundos que " +
                "a mente comum não alcança. O estudante, com os olhos cansados mas a alma " +
                "desperta, bebia aquelas palavras como quem bebe água no deserto. " +
                "'To give up' não era desistir — era entregar-se ao fluxo. 'To carry on' " +
                "não era continuar — era carregar a própria luz. Quando o amanhecer tingiu " +
                "os vitrais de dourado, o grimório se fechou sozinho, satisfeito. A " +
                "verdadeira fluência não mora em decorar regras, mas em ouvir o que as " +
                "palavras não dizem em voz alta.",
        subskillTag = "Listening"
    )

    private val h3 = HistoryEntry(
        id = "h3",
        skillName = "Kotlin",
        date = "24/12/2025, 14:30:00",
        duration = 125,
        xp = 1250,
        gold = 340,
        notes = "Corrotinas e Flow: o modelo de concorrência estruturada.\n" +
                "Diferença entre launch e async, escopo de corrotina e cancelamento.\n" +
                "Operadores de Flow: map, filter, combine, flatMapLatest.\n" +
                "Testes de corrotinas com runTest e TestDispatcher.\n" +
                "Integração com ViewModel e lifecycleScope em produção.",
        wilderness = false,
        aiChronicle = null,
        subskillTag = null
    )

    private val h4 = HistoryEntry(
        id = "h4",
        skillName = "Academia",
        date = "30/11/2025, 06:15:42",
        duration = 45,
        xp = 90,
        gold = 20,
        notes = "",
        wilderness = true,
        aiChronicle = "O ferro frio encontrou a vontade de fogo. Cada repetição, um pacto " +
                "entre carne e propósito. O suor não traía fraqueza — era a prova do " +
                "compromisso selado antes do amanhecer.",
        subskillTag = null
    )

    private val h5 = HistoryEntry(
        id = "h5",
        skillName = "Revisão de Jurisprudência: Responsabilidade Civil e Contratos",
        date = "01/10/2026, 18:00:00",
        duration = 90,
        xp = 2400,
        gold = 1180,
        notes = "Análise do REsp 1.895.672/SP e do AREsp 2.145.331/RJ. Dano moral " +
                "coletivo e cláusulas abusivas em contratos de adesão.",
        wilderness = false,
        aiChronicle = null,
        subskillTag = "Contratos de adesão e cláusulas abusivas"
    )

    private val h6 = HistoryEntry(
        id = "h6",
        skillName = "Leitura: Doutrina de Família",
        date = "15/09/2026, 10:20:00",
        duration = 40,
        xp = 80,
        gold = 15,
        notes = "Capítulo sobre regimes de bens: comunhão parcial, universal, " +
                "participação final nos aquestos e separação total. Destaque para a " +
                "Súmula 377 do STF.",
        wilderness = false,
        aiChronicle = null,
        subskillTag = null
    )

    @Composable
    private fun HistoryScreenWrapper(history: List<HistoryEntry>) {
        Scaffold(containerColor = QuestPanel) { innerPadding ->
            HistoryScreen(
                history = history,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

    @Test
    fun historyScreen_emptyAll_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(emptyList())
            }
        }
        composeTestRule.waitForIdle()
        capture("empty_all")
    }

    @Test
    fun historyScreen_emptyNotes_screenshot() {
        // Divergência do prompt (reportada): com a lista VAZIA o toggle de modo nem
        // renderiza (React `if (history.length === 0) return <empty>` — sem toggle) e
        // o estado exibido é o empty geral, não o de notas. O EmptyNotesState só é
        // alcançável com histórico não vazio cujas entradas têm notes em branco
        // (h4 tem notes = "").
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h4))
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("COMPILADO DE NOTAS").performClick()
        composeTestRule.waitForIdle()
        capture("empty_notes")
    }

    @Test
    fun historyScreen_allSingleCard_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h1))
            }
        }
        composeTestRule.waitForIdle()
        capture("all_single_card")
    }

    @Test
    fun historyScreen_allChronicleCollapsed_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h2))
            }
        }
        composeTestRule.waitForIdle()
        capture("all_chronicle_collapsed")
    }

    @Test
    fun historyScreen_allChronicleExpanded_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h2))
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("REVELAR CRÔNICA MÍSTICA").performClick()
        composeTestRule.waitForIdle()
        capture("all_chronicle_expanded")
    }

    @Test
    fun historyScreen_allWilderness_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h4))
            }
        }
        composeTestRule.waitForIdle()
        capture("all_wilderness")
    }

    @Test
    fun historyScreen_allStress_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h3, h5))
            }
        }
        composeTestRule.waitForIdle()
        capture("all_stress")
    }

    @Test
    fun historyScreen_allMixedList_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h1, h2, h3, h4, h5))
            }
        }
        composeTestRule.waitForIdle()
        capture("all_mixed_list")
    }

    @Test
    fun historyScreen_notesSingle_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                HistoryScreenWrapper(listOf(h6))
            }
        }
        composeTestRule.waitForIdle()
        // O modo notas é estado interno (viewMode inicia em "all"); sem parâmetro
        // público — a entrada é o clique no próprio toggle.
        composeTestRule.onNodeWithText("COMPILADO DE NOTAS").performClick()
        composeTestRule.waitForIdle()
        capture("notes_single")
    }

    private fun capture(suffix: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/history_screen_${suffix}_$viewportSuffix.png"
        )
    }

    protected abstract val viewportSuffix: String
}
