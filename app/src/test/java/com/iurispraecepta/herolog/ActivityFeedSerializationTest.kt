package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.logic.ActivityFeedLogic
import com.iurispraecepta.herolog.logic.SaveImportResult
import com.iurispraecepta.herolog.logic.SaveMigrationLogic
import com.iurispraecepta.herolog.model.ActivitySource
import com.iurispraecepta.herolog.model.ActivityType
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Testes de serialização do feed da Spec F (T7 / FEED-8 / FEED-9):
 * save sem o campo decodifica com lista vazia, round-trip preserva os 20 itens e a ordem,
 * importação de save React com o campo preserva o feed, e o feed sobrevive à persistência
 * em Room (blob JSON) — incluindo um save antigo gravado sem o campo.
 */
class ActivityFeedSerializationTest {

    // 2025-09-16T05:20:00.000Z — timestamp fixo pra determinismo do `at`.
    private val baseDate = Date(1_758_000_000_000L)
    private val json = JsonConfig.default

    private fun stateWithFeed(size: Int): CharacterState {
        var state = createInitialCharacterState(baseDate)
        for (i in 1..size) {
            state = ActivityFeedLogic.record(
                state = state,
                source = ActivitySource.Todo,
                refId = "t$i",
                title = "Tarefa $i",
                minutes = 0,
                xp = 28,
                gold = 14,
                at = Date(baseDate.time + i * 1_000L)
            )
        }
        return state
    }

    private fun encodedWithoutFeedKey(state: CharacterState): String {
        val encoded = json.encodeToString(CharacterState.serializer(), state)
        val obj = json.parseToJsonElement(encoded).jsonObject.toMutableMap()
        obj.remove("recentActivity")
        return JsonObject(obj).toString()
    }

    // ------------------------------------------------------------------
    // FEED-8 — save antigo (sem o campo) carrega vazio, sem erro
    // ------------------------------------------------------------------

    @Test
    fun decode_saveWithoutRecentActivity_fallsBackToEmptyList() {
        val encoded = json.encodeToString(CharacterState.serializer(), stateWithFeed(3))
        val obj = json.parseToJsonElement(encoded).jsonObject
        assertTrue("esperava o campo no JSON com feed", obj.containsKey("recentActivity"))

        val decoded = json.decodeFromString<CharacterState>(encodedWithoutFeedKey(stateWithFeed(3)))

        assertEquals(0, decoded.recentActivity.size)
        assertEquals(createInitialCharacterState(baseDate).charName, decoded.charName)
    }

    @Test
    fun normalizeGameState_oldSaveWithoutRecentActivity_decodesEmptyFeed() {
        val rawJson = encodedWithoutFeedKey(stateWithFeed(2))

        val result = SaveMigrationLogic.normalizeGameState(rawJson, now = baseDate)

        assertTrue("esperava Success, recebi $result", result is SaveImportResult.Success)
        val state = (result as SaveImportResult.Success).characterState
        assertEquals(0, state.recentActivity.size)
    }

    @Test
    fun repository_oldBlobWithoutRecentActivity_loadsWithEmptyFeed_noError() = runTest {
        val dao = FakeCharacterStateDao()
        val repository = CharacterRepository(dao)
        dao.saveState(
            CharacterStateEntity(
                id = 0,
                jsonPayload = encodedWithoutFeedKey(createInitialCharacterState(baseDate)),
                updatedAt = 0L
            )
        )

        val loaded = repository.getCharacterState()

        assertNotNull("save antigo deveria carregar sem erro", loaded)
        assertEquals(0, loaded!!.recentActivity.size)
    }

    // ------------------------------------------------------------------
    // FEED-9 — round-trip preserva os 20 itens e a ordem
    // ------------------------------------------------------------------

    @Test
    fun roundTrip_preservesTwentyEntriesAndOrder_fieldByField() {
        val original = stateWithFeed(ActivityFeedLogic.MAX_RECENT_ACTIVITY)
        assertEquals(20, original.recentActivity.size)

        val encoded = json.encodeToString(CharacterState.serializer(), original)
        val decoded = json.decodeFromString<CharacterState>(encoded)

        assertEquals(20, decoded.recentActivity.size)
        assertEquals(
            original.recentActivity.map { it.id },
            decoded.recentActivity.map { it.id }
        )

        val first = decoded.recentActivity.first()
        assertEquals("todo_1758000020000", first.id)
        assertEquals(ActivityType.Mission, first.type)
        assertEquals(ActivitySource.Todo, first.source)
        assertEquals("t20", first.refId)
        assertEquals("Tarefa 20", first.title)
        assertEquals(0, first.minutes)
        assertEquals(28, first.xp)
        assertEquals(14, first.gold)
        assertEquals("2025-09-16T05:20:20.000Z", first.at)

        val last = decoded.recentActivity.last()
        assertEquals("todo_1758000001000", last.id)
        assertEquals("t1", last.refId)
        assertEquals("Tarefa 1", last.title)
        assertEquals("2025-09-16T05:20:01.000Z", last.at)
    }

    @Test
    fun normalizeGameState_saveWithRecentActivity_preservesFeedAndOrder() {
        val original = stateWithFeed(5)
        val rawJson = json.encodeToString(CharacterState.serializer(), original)

        val result = SaveMigrationLogic.normalizeGameState(rawJson, now = baseDate)

        assertTrue("esperava Success, recebi $result", result is SaveImportResult.Success)
        val state = (result as SaveImportResult.Success).characterState
        assertEquals(5, state.recentActivity.size)
        assertEquals(
            original.recentActivity.map { it.id },
            state.recentActivity.map { it.id }
        )
        assertEquals("Tarefa 5", state.recentActivity.first().title)
        assertEquals("Tarefa 1", state.recentActivity.last().title)
    }

    @Test
    fun decode_reactShapedRecentActivity_parsesContractFields() {
        // Forma JSON exata do contrato da Spec F (§2 Plan), como o React escreveria:
        // entrada de foco com todos os campos; entrada de daily sem `minutes` (default 0).
        val reactFeed = """
        [
          {
            "id": "session_1789923456789",
            "type": "focus",
            "source": "session",
            "refId": "hist_1",
            "title": "Direito Civil",
            "minutes": 25,
            "xp": 100,
            "gold": 40,
            "at": "2026-09-20T14:23:10.000Z"
          },
          {
            "id": "daily_1789923000000",
            "type": "ritual",
            "source": "daily",
            "refId": "d1",
            "title": "Beber 2L de água",
            "xp": 28,
            "gold": 14,
            "at": "2026-09-20T13:00:00.000Z"
          }
        ]
        """.trimIndent()

        val base = json.parseToJsonElement(
            json.encodeToString(CharacterState.serializer(), createInitialCharacterState(baseDate))
        ).jsonObject.toMutableMap()
        base["recentActivity"] = json.parseToJsonElement(reactFeed)

        val decoded = json.decodeFromString<CharacterState>(JsonObject(base).toString())

        assertEquals(2, decoded.recentActivity.size)
        assertEquals(
            listOf("session_1789923456789", "daily_1789923000000"),
            decoded.recentActivity.map { it.id }
        )

        val focus = decoded.recentActivity[0]
        assertEquals(ActivityType.Focus, focus.type)
        assertEquals(ActivitySource.Session, focus.source)
        assertEquals("hist_1", focus.refId)
        assertEquals("Direito Civil", focus.title)
        assertEquals(25, focus.minutes)
        assertEquals(100, focus.xp)
        assertEquals(40, focus.gold)
        assertEquals("2026-09-20T14:23:10.000Z", focus.at)

        val ritual = decoded.recentActivity[1]
        assertEquals(ActivityType.Ritual, ritual.type)
        assertEquals(ActivitySource.Daily, ritual.source)
        assertEquals("d1", ritual.refId)
        assertEquals("Beber 2L de água", ritual.title)
        assertEquals(0, ritual.minutes)
        assertEquals(28, ritual.xp)
        assertEquals(14, ritual.gold)
        assertEquals("2026-09-20T13:00:00.000Z", ritual.at)
    }

    // ------------------------------------------------------------------
    // FEED-8 — o feed sobrevive à persistência (reinício do app)
    // ------------------------------------------------------------------

    @Test
    fun repository_saveAndReload_preservesFeed() = runTest {
        val repository = CharacterRepository(FakeCharacterStateDao())
        val original = stateWithFeed(3)

        repository.saveCharacterState(original)
        val reloaded = repository.getCharacterState()

        assertNotNull(reloaded)
        assertEquals(3, reloaded!!.recentActivity.size)
        assertEquals(
            original.recentActivity.map { it.id },
            reloaded.recentActivity.map { it.id }
        )
        assertEquals(
            original.recentActivity.first().at,
            reloaded.recentActivity.first().at
        )
    }
}
