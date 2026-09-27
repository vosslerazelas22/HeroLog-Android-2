package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.focus.FocusApplyLogic
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.quests.DailyLogic
import com.iurispraecepta.herolog.logic.quests.HabitLogic
import com.iurispraecepta.herolog.logic.quests.TodoLogic
import com.iurispraecepta.herolog.model.ActivitySource
import com.iurispraecepta.herolog.model.ActivityType
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Testes de integração de lógica da Spec F (T4/T5): `record` ligado em
 * `FocusApplyLogic.apply`, `TodoLogic.completeTodo`, `HabitLogic.triggerUp` e
 * `DailyLogic.completeDaily`; `revoke` ligado em `TodoLogic.uncompleteTodo` e
 * `DailyLogic.uncompleteDaily`.
 *
 * Cenários = critérios de aceitação da spec: sessão de 25 min, todo concluída e desfeita,
 * hábito 2×, hábito negativo fora do feed (D1) e 25 conclusões → 20 no feed (D9).
 * Comparação campo a campo, nunca igualdade de objeto inteiro.
 */
class ActivityFeedIntegrationTest {

    // 2025-09-16T05:20:00.000Z — timestamp fixo pra determinismo do `at`.
    private val baseDate = Date(1_758_000_000_000L)

    private fun baseState(): CharacterState = createInitialCharacterState(baseDate)

    private fun todo(id: String, title: String, completed: Boolean = false) = Todo(
        id = id,
        title = title,
        notes = "",
        difficulty = Difficulty.Medium,
        completed = completed,
        tags = emptyList(),
        checklist = emptyList()
    )

    private fun habit(id: String, title: String) = Habit(
        id = id,
        title = title,
        notes = "",
        up = true,
        down = true,
        difficulty = Difficulty.Medium,
        upCount = 0,
        downCount = 0,
        streak = 0,
        tags = emptyList()
    )

    private fun daily(id: String, title: String, completed: Boolean = false) = Daily(
        id = id,
        title = title,
        notes = "",
        difficulty = Difficulty.Medium,
        completed = completed,
        streak = 0,
        repeats = RepeatInterval.Daily,
        every = 1,
        tags = emptyList(),
        checklist = emptyList()
    )

    private fun calc(
        skillName: String = "Direito Civil",
        xpEarned: Int = 100,
        goldEarned: Int = 40,
        durationMins: Int = 25
    ) = FocusRewardsCalculation(
        skillIdx = 0,
        skillName = skillName,
        xpEarned = xpEarned,
        goldEarned = goldEarned,
        durationMins = durationMins,
        dungeonClearGoldBonus = 0,
        hasUsedDoubleLoot = false,
        hasUsedFocusElixir = false,
        hasUsedRuneFortune = false,
        hasUsedCrystalClarity = false,
        usedEquipmentIndicesAndCharges = emptyList(),
        lootedItems = emptyList(),
        droppedTitle = null,
        isWildernessChecked = false,
        isDungeonMode = false,
        comboBonusPercent = 0
    )

    // ------------------------------------------------------------------
    // FEED-1 — sessão de foco
    // ------------------------------------------------------------------

    @Test
    fun focusSession_25min_recordsFocusEntry_matchingHistoryEntry() {
        val state = baseState().copy(
            skills = listOf(Skill(id = "sk_1", name = "Direito Civil", level = 1, xp = 0))
        )

        val next = FocusApplyLogic.apply(
            state = state,
            calc = calc(),
            editedNotes = "Revisão",
            selectedTag = null,
            referenceDate = baseDate
        )

        assertEquals(1, next.recentActivity.size)
        val entry = next.recentActivity[0]

        assertEquals(ActivityType.Focus, entry.type)
        assertEquals(ActivitySource.Session, entry.source)
        assertEquals("Direito Civil", entry.title)
        assertEquals(25, entry.minutes)
        assertEquals("2025-09-16T05:20:00.000Z", entry.at)

        // refId/xp/gold batem com o HistoryEntry gravado na mesma sessão
        assertEquals(1, next.history.size)
        assertEquals(next.history[0].id, entry.refId)
        assertEquals(next.history[0].xp, entry.xp)
        assertEquals(next.history[0].gold, entry.gold)
        assertEquals(100, entry.xp)
        assertEquals(40, entry.gold)
    }

    @Test
    fun focusSession_blankSkillName_fallsBackToEstudo() {
        val state = baseState().copy(
            skills = listOf(Skill(id = "sk_1", name = "Sem Nome", level = 1, xp = 0))
        )

        val next = FocusApplyLogic.apply(
            state = state,
            calc = calc(skillName = "   "),
            editedNotes = "",
            selectedTag = null,
            referenceDate = baseDate
        )

        assertEquals("Estudo", next.recentActivity[0].title)
    }

    // ------------------------------------------------------------------
    // FEED-2 / FEED-5 — todo concluída e desfeita (D3)
    // ------------------------------------------------------------------

    @Test
    fun todoComplete_recordsMissionEntry_andUncomplete_removesIt() {
        val t = todo("t-1", "Pagar a conta")

        val done = TodoLogic.toggle(t, baseState(), referenceDate = baseDate)
        val withEntry = done.updatedState.copy(todos = listOf(done.updatedTodo))

        assertEquals(1, withEntry.recentActivity.size)
        val entry = withEntry.recentActivity[0]
        assertEquals(ActivityType.Mission, entry.type)
        assertEquals(ActivitySource.Todo, entry.source)
        assertEquals("t-1", entry.refId)
        assertEquals("Pagar a conta", entry.title)
        assertEquals("2025-09-16T05:20:00.000Z", entry.at)

        val undone = TodoLogic.toggle(done.updatedTodo, withEntry, referenceDate = baseDate)
        val finalState = undone.updatedState.copy(todos = listOf(undone.updatedTodo))

        assertTrue(
            finalState.recentActivity.none { it.source == ActivitySource.Todo && it.refId == "t-1" }
        )
        assertEquals(0, finalState.recentActivity.size)
    }

    @Test
    fun todoUncomplete_keepsOtherFeedEntries() {
        val t = todo("t-1", "Pagar a conta")
        val done = TodoLogic.toggle(t, baseState(), referenceDate = baseDate)
        var state = done.updatedState.copy(todos = listOf(done.updatedTodo))

        val h = habit("h-1", "Ler")
        val habitResult = HabitLogic.trigger(h, state, isUp = true, referenceDate = baseDate)
        state = habitResult.updatedState.copy(habits = listOf(habitResult.updatedHabit))

        val undone = TodoLogic.toggle(done.updatedTodo, state, referenceDate = baseDate)
        val finalState = undone.updatedState.copy(todos = listOf(undone.updatedTodo))

        assertEquals(1, finalState.recentActivity.size)
        assertEquals(ActivitySource.Habit, finalState.recentActivity[0].source)
        assertEquals("h-1", finalState.recentActivity[0].refId)
    }

    // ------------------------------------------------------------------
    // FEED-3 — hábito positivo repetível; negativo fora do feed (D1)
    // ------------------------------------------------------------------

    @Test
    fun habitTriggeredTwice_recordsTwoRitualEntries_sameRefId() {
        var state = baseState()
        var current = habit("h-1", "Ler 10 páginas")

        repeat(2) {
            val result = HabitLogic.trigger(current, state, isUp = true, referenceDate = baseDate)
            state = result.updatedState.copy(habits = listOf(result.updatedHabit))
            current = result.updatedHabit
        }

        assertEquals(2, state.recentActivity.size)
        assertEquals(
            2,
            state.recentActivity.count { it.source == ActivitySource.Habit && it.refId == "h-1" }
        )
        assertTrue(state.recentActivity.all { it.type == ActivityType.Ritual })
        assertEquals("Ler 10 páginas", state.recentActivity[0].title)
    }

    @Test
    fun habitNegative_doesNotRecord() {
        val h = habit("h-1", "Procrastinar")

        val result = HabitLogic.trigger(h, baseState(), isUp = false, referenceDate = baseDate)
        val state = result.updatedState.copy(habits = listOf(result.updatedHabit))

        assertEquals(0, state.recentActivity.size)
        assertEquals(1, state.habits[0].downCount)
    }

    // ------------------------------------------------------------------
    // FEED-4 / FEED-5 — daily concluída e desfeita (D3)
    // ------------------------------------------------------------------

    @Test
    fun dailyComplete_recordsRitualEntry_andUncomplete_removesIt() {
        val d = daily("d-1", "Beber 2L de água")

        val done = DailyLogic.toggle(d, baseState(), referenceDate = baseDate)
        val withEntry = done.updatedState.copy(dailies = listOf(done.updatedDaily))

        assertEquals(1, withEntry.recentActivity.size)
        val entry = withEntry.recentActivity[0]
        assertEquals(ActivityType.Ritual, entry.type)
        assertEquals(ActivitySource.Daily, entry.source)
        assertEquals("d-1", entry.refId)
        assertEquals("Beber 2L de água", entry.title)
        assertEquals("2025-09-16T05:20:00.000Z", entry.at)

        val undone = DailyLogic.toggle(done.updatedDaily, withEntry, referenceDate = baseDate)
        val finalState = undone.updatedState.copy(dailies = listOf(undone.updatedDaily))

        assertTrue(
            finalState.recentActivity.none { it.source == ActivitySource.Daily && it.refId == "d-1" }
        )
        assertEquals(0, finalState.recentActivity.size)
    }

    // ------------------------------------------------------------------
    // FEED-6 — cap de 20 (D9)
    // ------------------------------------------------------------------

    @Test
    fun twentyFiveTodoCompletions_feedKeeps20_oldestSurvivorIsSixth() {
        var state = baseState()

        for (i in 1..25) {
            val done = TodoLogic.toggle(
                todo = todo("t$i", "Tarefa $i"),
                state = state,
                referenceDate = Date(baseDate.time + i * 1_000L)
            )
            state = done.updatedState.copy(todos = state.todos + done.updatedTodo)
        }

        assertEquals(20, state.recentActivity.size)
        assertEquals("t25", state.recentActivity.first().refId)
        assertEquals("t6", state.recentActivity.last().refId)
        assertTrue(state.recentActivity.none { it.refId == "t5" })
    }
}
