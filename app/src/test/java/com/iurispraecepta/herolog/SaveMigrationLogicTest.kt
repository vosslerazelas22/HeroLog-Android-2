package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.SaveImportResult
import com.iurispraecepta.herolog.logic.SaveMigrationLogic
import com.iurispraecepta.herolog.model.CharClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Date

/**
 * Testes de `SaveMigrationLogic.normalizeGameState` — porte de `normalizeGameState`
 * (`useGameState.ts`). `now` é sempre injetado fixo para determinismo dos fallbacks de
 * `createdAt`.
 */
class SaveMigrationLogicTest {

    private val fixedNow = Date(1_724_500_000_000L) // timestamp fixo qualquer

    private fun asSuccess(result: SaveImportResult) =
        (result as? SaveImportResult.Success)?.characterState
            ?: fail("Esperava SaveImportResult.Success, recebi $result").let { null }

    // ---------------------------------------------------------------------
    // Save moderno e completo — deve passar direto, sem depender de nenhum fallback
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_completeModernSave_decodesValuesAsIs() {
        val rawJson = """
        {
          "gold": 4820,
          "totalXP": 91234,
          "totalGoldEarned": 15320,
          "totalSessions": 210,
          "totalMinutes": 8830,
          "combatLevel": 12,
          "combatXP": 3410,
          "skills": [
            { "id": "sk-1", "name": "Código Sagrado", "level": 8, "xp": 940, "emoji": "💻", "prestige": 1, "tags": ["React"] }
          ],
          "history": [],
          "inventory": [],
          "streak": 14,
          "bestStreak": 30,
          "lastStudyDate": "25/08/2026",
          "wildernessWins": 3,
          "combo": 2,
          "dungeonProgress": 1,
          "isDungeonMode": false,
          "dungeonSessions": 0,
          "achievements": ["first_quest", "streak_7"],
          "charName": "Bruno",
          "charClass": "Warrior",
          "todayXP": 120,
          "todayMinutes": 45,
          "todayDate": "Tue Aug 25 2026",
          "hasClaimedLogin": true,
          "hp": 44,
          "maxHp": 50,
          "habits": [],
          "dailies": [],
          "todos": [],
          "equippedTitle": "sage_novice",
          "ownedTitles": ["sage_novice"],
          "equippedEquipment": [null, null, null],
          "pomodoroSettings": {
            "focusDuration": 50,
            "shortBreakDuration": 10,
            "longBreakDuration": 20,
            "autoStartBreak": true,
            "autoStartFocus": false
          }
        }
        """.trimIndent()

        val result = SaveMigrationLogic.normalizeGameState(rawJson, fixedNow)
        val state = asSuccess(result)!!

        assertEquals(4820, state.gold)
        assertEquals("Bruno", state.charName)
        assertEquals(CharClass.Warrior, state.charClass)
        assertEquals(14, state.streak)
        assertEquals(50, state.pomodoroSettings.focusDuration)
        assertEquals(20, state.pomodoroSettings.longBreakDuration)
        assertEquals(1, state.skills.size)
        assertEquals("sk-1", state.skills[0].id)
        assertEquals(listOf("first_quest", "streak_7"), state.achievements)
    }

    // ---------------------------------------------------------------------
    // pomodoroSettings ausente + campo morto longBreakMinutes (save bem antigo)
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_missingPomodoroSettings_withLegacyLongBreakMinutes_migratesCorrectly() {
        val rawJson = """{ "charName": "Save Antigo", "longBreakMinutes": 12 }"""

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(25, state.pomodoroSettings.focusDuration) // default
        assertEquals(5, state.pomodoroSettings.shortBreakDuration) // default
        assertEquals(12, state.pomodoroSettings.longBreakDuration) // migrado do campo morto
        assertEquals(false, state.pomodoroSettings.autoStartBreak)
    }

    @Test
    fun normalizeGameState_missingPomodoroSettings_noLegacyField_usesFullDefaults() {
        val rawJson = """{ "charName": "Save Vazio" }"""

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(25, state.pomodoroSettings.focusDuration)
        assertEquals(5, state.pomodoroSettings.shortBreakDuration)
        assertEquals(15, state.pomodoroSettings.longBreakDuration) // default 15, sem campo morto
        assertEquals(false, state.pomodoroSettings.autoStartFocus)
    }

    // ---------------------------------------------------------------------
    // Skills sem id/emoji/prestige — defaults determinísticos por posição
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_skillsMissingIdEmojiPrestige_fillsPositionalDefaults() {
        val rawJson = """
        {
          "skills": [
            { "name": "Programação", "level": 3, "xp": 50 },
            { "name": "Leitura", "level": 2, "xp": 20 },
            { "name": "Meditação", "level": 1, "xp": 0 },
            { "name": "Extra", "level": 1, "xp": 0 }
          ]
        }
        """.trimIndent()

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(4, state.skills.size)
        assertEquals("sk-1", state.skills[0].id)
        assertEquals("💻", state.skills[0].emoji)
        assertEquals(0, state.skills[0].prestige)
        assertEquals("sk-2", state.skills[1].id)
        assertEquals("🧪", state.skills[1].emoji)
        assertEquals("sk-3", state.skills[2].id)
        assertEquals("📚", state.skills[2].emoji)
        assertEquals("sk-4", state.skills[3].id)
        assertEquals("🎯", state.skills[3].emoji) // fallback genérico pro 4º índice em diante
    }

    @Test
    fun normalizeGameState_skillsWithExistingIdEmojiPrestige_preservesThem() {
        val rawJson = """
        {
          "skills": [
            { "id": "custom-id", "name": "Programação", "level": 3, "xp": 50, "emoji": "🔥", "prestige": 2 }
          ]
        }
        """.trimIndent()

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals("custom-id", state.skills[0].id)
        assertEquals("🔥", state.skills[0].emoji)
        assertEquals(2, state.skills[0].prestige)
    }

    // ---------------------------------------------------------------------
    // Dailies sem value/createdAt
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_dailiesMissingValueAndCreatedAt_fillsWithZeroAndNow() {
        val rawJson = """
        {
          "dailies": [
            { "id": "d-1", "title": "Duolingo", "notes": "", "difficulty": "Easy", "completed": false,
              "streak": 5, "repeats": "Daily", "every": 1, "tags": [], "checklist": [] }
          ]
        }
        """.trimIndent()

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(0, state.dailies[0].value)
        assertEquals(fixedNow.toInstant().toString(), state.dailies[0].createdAt)
    }

    // ---------------------------------------------------------------------
    // Todos sem createdAt
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_todosMissingCreatedAt_fillsWithNow() {
        val rawJson = """
        {
          "todos": [
            { "id": "t-1", "title": "Ler", "notes": "", "difficulty": "Medium", "completed": false,
              "tags": [], "checklist": [] }
          ]
        }
        """.trimIndent()

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(fixedNow.toInstant().toString(), state.todos[0].createdAt)
    }

    // ---------------------------------------------------------------------
    // equippedEquipment ausente/malformado — vira [null, null, null]
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_missingEquippedEquipment_defaultsToThreeNulls() {
        val rawJson = """{ "charName": "Sem Equipamento" }"""

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(listOf(null, null, null), state.equippedEquipment)
    }

    @Test
    fun normalizeGameState_equippedEquipmentTooShort_defaultsToThreeNulls() {
        val rawJson = """{ "equippedEquipment": [null] }"""

        val state = asSuccess(SaveMigrationLogic.normalizeGameState(rawJson, fixedNow))!!

        assertEquals(3, state.equippedEquipment?.size)
    }

    // ---------------------------------------------------------------------
    // Entradas inválidas
    // ---------------------------------------------------------------------

    @Test
    fun normalizeGameState_malformedJson_returnsInvalidJson() {
        val result = SaveMigrationLogic.normalizeGameState("{ isso não é json válido", fixedNow)
        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun normalizeGameState_jsonArrayInsteadOfObject_returnsInvalidJson() {
        val result = SaveMigrationLogic.normalizeGameState("[1, 2, 3]", fixedNow)
        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun normalizeGameState_emptyObject_stillProducesValidCharacterState() {
        // Save vazio de verdade (ex: "{}") deve cair 100% nos defaults do createInitialCharacterState.
        val result = SaveMigrationLogic.normalizeGameState("{}", fixedNow)
        val state = asSuccess(result)!!

        assertEquals(200, state.gold) // default de createInitialCharacterState
        assertEquals("Aventureiro do Foco", state.charName)
        assertNotNull(state.pomodoroSettings)
        assertEquals(listOf(null, null, null), state.equippedEquipment)
    }
}
