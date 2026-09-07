package com.iurispraecepta.herolog.data.export

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class GameStateExporterTest {

    @Test
    fun buildPayload_includesVersionAndExportedAt() {
        val fixedNow = Date(0)
        val payload = GameStateExporter.buildPayload(
            character = createInitialCharacterState(fixedNow),
            activeFocus = null,
            now = fixedNow
        )

        assertEquals(1, payload.version)
        assertEquals(ExportPayload.EXPORT_VERSION, payload.version)
        assertNotNull(payload.exportedAt)
        assertTrue("exportedAt deve ser ISO-8601", payload.exportedAt.contains("T"))
    }

    @Test
    fun buildPayload_includesCharacter() {
        val state = createInitialCharacterState(Date(0))
        val payload = GameStateExporter.buildPayload(state, null, Date(0))

        assertEquals(state.charName, payload.character.charName)
        assertEquals(state.charClass, payload.character.charClass)
        assertEquals(state.gold, payload.character.gold)
    }

    @Test
    fun buildPayload_includesActiveFocus_whenPresent() {
        val state = createInitialCharacterState(Date(0))
        val activeFocus = PersistedFocusSession(
            config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
                selectedSkillIdx = 0,
                isWildernessChecked = false,
                isDungeonMode = false,
                dungeonSessions = 0
            ),
            durationMinutes = 25,
            endTimeMillis = 1_700_000_000_000L
        )

        val payload = GameStateExporter.buildPayload(state, activeFocus, Date(0))

        assertNotNull(payload.activeFocus)
        assertEquals(25, payload.activeFocus?.durationMinutes)
    }

    @Test
    fun buildPayload_includesNullActiveFocus_whenAbsent() {
        val payload = GameStateExporter.buildPayload(
            character = createInitialCharacterState(Date(0)),
            activeFocus = null,
            now = Date(0)
        )

        assertNull(payload.activeFocus)
    }

    @Test
    fun encodePayload_roundTrip_preservesCharacterState() {
        val original = createInitialCharacterState(Date(0)).copy(
            charName = "Thorin",
            gold = 1234
        )
        val payload = GameStateExporter.buildPayload(original, null, Date(0))

        val reparsed = JsonConfig.default.decodeFromString(
            ExportPayload.serializer(),
            GameStateExporter.encodePayload(payload, JsonConfig.default)
        )

        assertEquals(payload.version, reparsed.version)
        assertEquals(payload.exportedAt, reparsed.exportedAt)
        assertEquals(original.charName, reparsed.character.charName)
        assertEquals(original.gold, reparsed.character.gold)
        assertEquals(original.charClass, reparsed.character.charClass)
    }

    @Test
    fun encodePayload_usesPrettyJsonConfig() {
        val payload = GameStateExporter.buildPayload(
            character = createInitialCharacterState(Date(0)),
            activeFocus = null,
            now = Date(0)
        )

        val pretty = GameStateExporter.encodePayload(payload, JsonConfig.pretty)
        val compact = GameStateExporter.encodePayload(payload, JsonConfig.default)

        assertTrue("JSON pretty deve ter indentação (quebra de linha)", pretty.contains("\n"))
        assertTrue("JSON pretty deve ser maior que o compact", pretty.length > compact.length)
    }

    @Test
    fun encodePayload_pretty_includesAllFields_viaEncodeDefaults() {
        val payload = GameStateExporter.buildPayload(
            character = createInitialCharacterState(Date(0)),
            activeFocus = null,
            now = Date(0)
        )

        val json = GameStateExporter.encodePayload(payload, JsonConfig.pretty)
        val obj = Json.parseToJsonElement(json).jsonObject

        assertTrue("wrapper deve ter 'version'", obj.containsKey("version"))
        assertTrue("wrapper deve ter 'exportedAt'", obj.containsKey("exportedAt"))
        assertTrue("wrapper deve ter 'character'", obj.containsKey("character"))
        assertTrue("wrapper deve ter 'activeFocus' (mesmo que null)", obj.containsKey("activeFocus"))
    }

    @Test
    fun encodePayload_injectsFieldNames_consistentWithContract() {
        val payload = GameStateExporter.buildPayload(
            character = createInitialCharacterState(Date(0)),
            activeFocus = null,
            now = Date(0)
        )

        val json = GameStateExporter.encodePayload(payload, JsonConfig.pretty)
        val obj = Json.parseToJsonElement(json).jsonObject

        assertEquals(1, obj["version"]?.jsonPrimitive?.contentOrNull?.toInt())
        assertNotNull(obj["exportedAt"]?.jsonPrimitive?.contentOrNull)
        assertNotNull(obj["character"]?.jsonObject)
        assertNotNull(obj["activeFocus"])

        val characterObj = obj["character"]?.jsonObject
        assertNotNull(characterObj)
        assertNotNull(characterObj!!["charName"]?.jsonPrimitive?.contentOrNull)
        assertNotNull(characterObj["habits"]?.jsonArray)
    }

    @Test
    fun encodePayload_withCustomJson_respectsConfig() {
        val strictJson = Json { prettyPrint = false; encodeDefaults = false }
        val payload = GameStateExporter.buildPayload(
            character = createInitialCharacterState(Date(0)),
            activeFocus = null,
            now = Date(0)
        )

        val json = GameStateExporter.encodePayload(payload, strictJson)
        val obj = Json.parseToJsonElement(json).jsonObject

        assertNotNull(obj["character"])
        val characterObj = obj["character"]?.jsonObject as JsonObject
        assertTrue("sem encodeDefaults, 'lastDungeonClearedTime' (default 0L) não deve aparecer", !characterObj.containsKey("lastDungeonClearedTime"))
    }
}
