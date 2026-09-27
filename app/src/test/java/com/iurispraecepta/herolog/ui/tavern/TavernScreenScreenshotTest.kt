package com.iurispraecepta.herolog.ui.tavern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.ActivityEntry
import com.iurispraecepta.herolog.model.ActivitySource
import com.iurispraecepta.herolog.model.ActivityType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Screenshots da tela Taverna (Spec B T8) nos viewports 375x667 e 390x844.
 *
 * Cenários: **vazio** (TAV-8), **4 itens** (TAV-7) e **nome longo** (TAV-2, 1 linha com
 * ellipsis). O wrapper reproduz o painel do `MainActivity` (QuestPanel, borda White10 1dp,
 * cantos 8dp, padding 12dp).
 *
 * Regra AGENTS.md §6.10: tema escuro explícito + fundo `QuestPanel` no root — sem isso os
 * stubs saem invisíveis no PNG do Robolectric.
 *
 * **Baseline, não substitui inspeção humana** (AGENTS.md §6.9): comparar com o React antes
 * de regravar qualquer PNG (`_compare.png` em `app/build/outputs/roborazzi/`).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w390dp-h844dp", sdk = [36])
class TavernScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Idade das entradas em milissegundos — fixas para os rótulos relativos não oscilarem. */
    private fun ago(millis: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return format.format(Date(System.currentTimeMillis() - millis))
    }

    private val feedQuatroItens = listOf(
        ActivityEntry(
            id = "session_1",
            type = ActivityType.Focus,
            source = ActivitySource.Session,
            refId = "hist_1",
            title = "Direito Civil",
            minutes = 25,
            xp = 120,
            gold = 45,
            at = ago(5 * 60_000L) // há 5m
        ),
        ActivityEntry(
            id = "todo_1",
            type = ActivityType.Mission,
            source = ActivitySource.Todo,
            refId = "todo_1",
            title = "Revisar contrato de compra e venda",
            at = ago(3 * 3_600_000L) // há 3h
        ),
        ActivityEntry(
            id = "habit_1",
            type = ActivityType.Ritual,
            source = ActivitySource.Habit,
            refId = "habit_1",
            title = "Meditar 10 minutos",
            at = ago(2 * 86_400_000L) // há 2d
        ),
        ActivityEntry(
            id = "session_2",
            type = ActivityType.Focus,
            source = ActivitySource.Session,
            refId = "hist_2",
            title = "Filosofia do Direito",
            minutes = 125,
            xp = 240,
            gold = 90,
            at = ago(5 * 86_400_000L) // há 5d
        )
    )

    @Composable
    private fun TavernWrapper(
        charName: String,
        charClass: CharClass,
        recentActivity: List<ActivityEntry>
    ) {
        HeroLogTheme(darkTheme = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuestPanel)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(QuestPanel)
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    TavernScreen(
                        charName = charName,
                        charClass = charClass,
                        combatLevel = 3,
                        combatXP = 150,
                        streak = 1,
                        bestStreak = 12,
                        totalMinutes = 125,
                        gold = 1234,
                        recentActivity = recentActivity,
                        onNavigate = {}
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 390x844 (viewport padrão desta classe)
    // ------------------------------------------------------------------

    @Test
    fun tavern_vazio_390x844() {
        composeTestRule.setContent {
            TavernWrapper(
                charName = "Aethelgard",
                charClass = CharClass.Mage,
                recentActivity = emptyList()
            )
        }
        composeTestRule.onNodeWithText("TAVERNA DE MYSTARA").assertIsDisplayed()
        composeTestRule.onNodeWithText("150 / 300 (50%)").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 dia").assertExists()
        composeTestRule.onNodeWithText("2h05m").assertExists()
        composeTestRule.onNodeWithText("1.234").assertExists()
        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/tavern_screen_vazio_390x844.png")
    }

    @Test
    fun tavern_4itens_390x844() {
        composeTestRule.setContent {
            TavernWrapper(
                charName = "Aethelgard",
                charClass = CharClass.Mage,
                recentActivity = feedQuatroItens
            )
        }
        composeTestRule.onNodeWithText("25m · Direito Civil").assertExists() // foco (2 no feed)
        composeTestRule.onNodeWithText("2h05m · Filosofia do Direito").assertExists()
        composeTestRule.onNodeWithText("Missão concluída").assertExists()
        composeTestRule.onNodeWithText("Ritual concluído").assertExists()
        composeTestRule.onNodeWithText("+120 XP · +45 GP").assertExists()
        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/tavern_screen_4itens_390x844.png")
    }

    @Test
    fun tavern_nomeLongo_390x844() {
        composeTestRule.setContent {
            TavernWrapper(
                charName = "Maximiliano Augusto de Villarinho",
                charClass = CharClass.Ranger,
                recentActivity = feedQuatroItens
            )
        }
        composeTestRule.onNodeWithText("Maximiliano Augusto de Villarinho").assertExists()
        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/tavern_screen_nome_longo_390x844.png")
    }

    // ------------------------------------------------------------------
    // 375x667
    // ------------------------------------------------------------------

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun tavern_vazio_375x667() {
        composeTestRule.setContent {
            TavernWrapper(
                charName = "Aethelgard",
                charClass = CharClass.Mage,
                recentActivity = emptyList()
            )
        }
        composeTestRule.onNodeWithText("GRIMÓRIO DE SKILLS").assertExists()
        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/tavern_screen_vazio_375x667.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun tavern_4itens_375x667() {
        composeTestRule.setContent {
            TavernWrapper(
                charName = "Aethelgard",
                charClass = CharClass.Mage,
                recentActivity = feedQuatroItens
            )
        }
        composeTestRule.onNodeWithText("GRIMÓRIO DE SKILLS").assertExists()
        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/tavern_screen_4itens_375x667.png")
    }

    @Test
    @Config(qualifiers = "w375dp-h667dp")
    fun tavern_nomeLongo_375x667() {
        composeTestRule.setContent {
            TavernWrapper(
                charName = "Maximiliano Augusto de Villarinho",
                charClass = CharClass.Ranger,
                recentActivity = feedQuatroItens
            )
        }
        composeTestRule.onNodeWithText("Maximiliano Augusto de Villarinho").assertExists()
        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/tavern_screen_nome_longo_375x667.png")
    }
}
