package com.iurispraecepta.herolog.data.export

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.SaveImportResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class GameStateImporterTest {

    @Test
    fun parsePayload_emptyString_returnsInvalidJson() {
        val result = GameStateImporter.parsePayload("")
        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun parsePayload_blankString_returnsInvalidJson() {
        val result = GameStateImporter.parsePayload("   \n  ")
        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun parsePayload_malformedJson_returnsInvalidJson() {
        val result = GameStateImporter.parsePayload("{isso não é json}")
        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun parsePayload_nonObjectJson_returnsInvalidJson() {
        val result = GameStateImporter.parsePayload("[1, 2, 3]")
        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun parsePayload_rawCharacterState_returnsSuccess() {
        val state = createInitialCharacterState(Date(0))
        val rawJson = JsonConfig.default.encodeToString(
            com.iurispraecepta.herolog.model.CharacterState.serializer(),
            state
        )

        val result = GameStateImporter.parsePayload(rawJson)

        assertTrue("esperava Success, recebi $result", result is SaveImportResult.Success)
        val success = result as SaveImportResult.Success
        assertEquals(state.charName, success.characterState.charName)
    }

    @Test
    fun parsePayload_exportPayloadV1_returnsSuccess() {
        val state = createInitialCharacterState(Date(0))
        val payload = GameStateExporter.buildPayload(state, null, Date(0))
        val json = GameStateExporter.encodePayload(payload, JsonConfig.pretty)

        val result = GameStateImporter.parsePayload(json)

        assertTrue("esperava Success, recebi $result", result is SaveImportResult.Success)
        val success = result as SaveImportResult.Success
        assertEquals(state.charName, success.characterState.charName)
    }

    @Test
    fun parsePayload_exportPayload_unsupportedVersion_returnsInvalidJson() {
        val state = createInitialCharacterState(Date(0))
        val payload = ExportPayload(
            version = ExportPayload.EXPORT_VERSION + 99,
            exportedAt = "2099-01-01T00:00:00Z",
            character = state,
            activeFocus = null
        )
        val json = JsonConfig.pretty.encodeToString(ExportPayload.serializer(), payload)

        val result = GameStateImporter.parsePayload(json)

        assertTrue(result is SaveImportResult.InvalidJson)
        val invalid = result as SaveImportResult.InvalidJson
        assertTrue(
            "mensagem deveria mencionar versão não suportada, recebi: ${invalid.message}",
            invalid.message.contains("não suportada")
        )
    }

    @Test
    fun parsePayload_exportPayload_invalidVersion_returnsInvalidJson() {
        val rawJson = """{ "version": -1, "exportedAt": "x", "character": {} }"""
        val result = GameStateImporter.parsePayload(rawJson)

        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun parsePayload_exportPayload_missingCharacter_returnsInvalidJson() {
        val rawJson = """{ "version": 1, "exportedAt": "x" }"""
        val result = GameStateImporter.parsePayload(rawJson)

        assertTrue(result is SaveImportResult.InvalidJson)
    }

    @Test
    fun parsePayload_exportPayloadV1WithEmptyCharacter_succeedsViaMigrationFallback() {
        val rawJson = """{ "version": 1, "exportedAt": "x", "character": {} }"""
        val result = GameStateImporter.parsePayload(rawJson)

        assertTrue(
            "Spread raso do normalizeGameState aceita character vazio e preenche defaults — esperado Success, recebi $result",
            result is SaveImportResult.Success
        )
    }
}
