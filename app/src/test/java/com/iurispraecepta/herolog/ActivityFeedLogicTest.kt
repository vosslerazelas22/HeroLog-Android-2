package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.ActivityFeedLogic
import com.iurispraecepta.herolog.model.ActivitySource
import com.iurispraecepta.herolog.model.ActivityType
import com.iurispraecepta.herolog.model.CharacterState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Testes unitários de [ActivityFeedLogic] — Spec F (feed de atividades recentes persistido).
 *
 * Feed: mais recente primeiro, máx. 20 entradas (D9); `revoke` remove só a entrada mais
 * recente com `source`+`refId` (D3) e é no-op quando não há match.
 * Comparação campo a campo, nunca igualdade de objeto inteiro.
 */
class ActivityFeedLogicTest {

    // 2025-09-16T05:20:00.000Z — timestamp fixo pra determinismo do `at` e do id.
    private val baseInstant = Date(1_758_000_000_000L)

    private fun baseState(): CharacterState = createInitialCharacterState(baseInstant)

    // ------------------------------------------------------------------
    // record — prepend
    // ------------------------------------------------------------------

    @Test
    fun record_prependsNewestFirst_andKeepsEveryField() {
        var state = baseState()

        state = ActivityFeedLogic.record(
            state = state,
            source = ActivitySource.Todo,
            refId = "todo_1",
            title = "Estudar Kotlin",
            minutes = 0,
            xp = 28,
            gold = 14,
            at = baseInstant
        )
        state = ActivityFeedLogic.record(
            state = state,
            source = ActivitySource.Session,
            refId = "hist_9",
            title = "Direito Civil",
            minutes = 25,
            xp = 100,
            gold = 40,
            at = Date(baseInstant.time + 1_000)
        )

        assertEquals(2, state.recentActivity.size)

        val newest = state.recentActivity[0]
        assertEquals("session_1758000001000", newest.id)
        assertEquals(ActivityType.Focus, newest.type)
        assertEquals(ActivitySource.Session, newest.source)
        assertEquals("hist_9", newest.refId)
        assertEquals("Direito Civil", newest.title)
        assertEquals(25, newest.minutes)
        assertEquals(100, newest.xp)
        assertEquals(40, newest.gold)
        assertEquals("2025-09-16T05:20:01.000Z", newest.at)

        val older = state.recentActivity[1]
        assertEquals("todo_1758000000000", older.id)
        assertEquals(ActivityType.Mission, older.type)
        assertEquals(ActivitySource.Todo, older.source)
        assertEquals("todo_1", older.refId)
        assertEquals("Estudar Kotlin", older.title)
        assertEquals(0, older.minutes)
        assertEquals(28, older.xp)
        assertEquals(14, older.gold)
        assertEquals("2025-09-16T05:20:00.000Z", older.at)
    }

    @Test
    fun record_mapsSourceToType_sessionFocus_todoMission_habitAndDailyRitual() {
        var state = baseState()

        state = ActivityFeedLogic.record(state, ActivitySource.Session, "s1", "Foco", 0, 1, 2, baseInstant)
        state = ActivityFeedLogic.record(state, ActivitySource.Todo, "t1", "Missão", 0, 1, 2, baseInstant)
        state = ActivityFeedLogic.record(state, ActivitySource.Habit, "h1", "Ritual H", 0, 1, 2, baseInstant)
        state = ActivityFeedLogic.record(state, ActivitySource.Daily, "d1", "Ritual D", 0, 1, 2, baseInstant)

        assertEquals(ActivityType.Ritual, state.recentActivity[0].type)
        assertEquals(ActivityType.Ritual, state.recentActivity[1].type)
        assertEquals(ActivityType.Mission, state.recentActivity[2].type)
        assertEquals(ActivityType.Focus, state.recentActivity[3].type)
    }

    @Test
    fun record_atIsIsoInstantUtcWithMilliseconds() {
        val state = ActivityFeedLogic.record(
            state = baseState(),
            source = ActivitySource.Habit,
            refId = "h1",
            title = "Ler",
            at = baseInstant
        )

        val at = state.recentActivity[0].at
        assertTrue("at deveria ser ISO com millis: $at", at.matches(Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z""")))
    }

    // ------------------------------------------------------------------
    // record — cap (FEED-6 / D9)
    // ------------------------------------------------------------------

    @Test
    fun record_after25Entries_keeps20Newest_andOldestSurvivorIsSixth() {
        var state = baseState()

        for (i in 1..25) {
            state = ActivityFeedLogic.record(
                state = state,
                source = ActivitySource.Todo,
                refId = "t$i",
                title = "Tarefa $i",
                xp = 1,
                gold = 1,
                at = Date(baseInstant.time + i * 1_000L)
            )
        }

        assertEquals(ActivityFeedLogic.MAX_RECENT_ACTIVITY, state.recentActivity.size)
        assertEquals(20, state.recentActivity.size)
        assertEquals("t25", state.recentActivity.first().refId)
        assertEquals("t6", state.recentActivity.last().refId)
    }

    @Test
    fun record_onEmptyFeed_startsWithSingleEntry() {
        val state = ActivityFeedLogic.record(
            state = baseState(),
            source = ActivitySource.Daily,
            refId = "d1",
            title = "Beber água",
            xp = 28,
            gold = 14,
            at = baseInstant
        )

        assertEquals(1, state.recentActivity.size)
        assertEquals("d1", state.recentActivity[0].refId)
    }

    // ------------------------------------------------------------------
    // revoke (FEED-5 / D3)
    // ------------------------------------------------------------------

    @Test
    fun revoke_removesOnlyMostRecentMatchingEntry_olderMatchSurvives() {
        var state = baseState()

        state = ActivityFeedLogic.record(state, ActivitySource.Todo, "t1", "Antiga", 0, 28, 14, baseInstant)
        state = ActivityFeedLogic.record(state, ActivitySource.Habit, "h1", "Hábito", 0, 28, 14, baseInstant)
        state = ActivityFeedLogic.record(
            state, ActivitySource.Todo, "t1", "Nova", 0, 28, 14, Date(baseInstant.time + 5_000)
        )

        val after = ActivityFeedLogic.revoke(state, ActivitySource.Todo, "t1")

        assertEquals(2, after.recentActivity.size)
        assertEquals("h1", after.recentActivity[0].refId)
        assertEquals("t1", after.recentActivity[1].refId)
        assertEquals("Antiga", after.recentActivity[1].title)
    }

    @Test
    fun revoke_withoutMatch_returnsFeedUnchanged() {
        val state = ActivityFeedLogic.record(
            state = baseState(),
            source = ActivitySource.Todo,
            refId = "t1",
            title = "Tarefa",
            xp = 28,
            gold = 14,
            at = baseInstant
        )

        val afterNoSource = ActivityFeedLogic.revoke(state, ActivitySource.Daily, "t1")
        val afterNoRef = ActivityFeedLogic.revoke(state, ActivitySource.Todo, "inexistente")

        assertEquals(state.recentActivity, afterNoSource.recentActivity)
        assertEquals(state.recentActivity, afterNoRef.recentActivity)
        assertEquals(1, afterNoSource.recentActivity.size)
        assertEquals(1, afterNoRef.recentActivity.size)
    }

    @Test
    fun revoke_doesNotAffectOtherFieldsOfTheState() {
        val state = baseState().copy(gold = 500, totalXP = 123)
        val after = ActivityFeedLogic.revoke(state, ActivitySource.Todo, "nada")

        assertEquals(500, after.gold)
        assertEquals(123, after.totalXP)
        assertEquals(state.todos, after.todos)
        assertEquals(state.history, after.history)
    }
}
