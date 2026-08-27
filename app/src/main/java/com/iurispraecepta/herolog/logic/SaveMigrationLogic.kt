package com.iurispraecepta.herolog.logic

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Date

/** Resultado de uma tentativa de importar um save do React. */
sealed class SaveImportResult {
    data class Success(val characterState: CharacterState) : SaveImportResult()
    data class InvalidJson(val message: String) : SaveImportResult()
}

/**
 * Porte de `normalizeGameState`/`importGameState` (`src/hooks/useGameState.ts`) — a função real
 * que a fonte usa tanto pra carregar o save do `localStorage` quanto pra importar um save colado
 * manualmente (`importGameState`, que só chama `normalizeGameState` e substitui o estado atual).
 *
 * Auditado direto contra `useGameState.ts` real (clone do repo, commit `52822c2`, mesmo hash já
 * usado nos Blocos A/B/D/E/C/F — sem drift). A fonte guarda o save em
 * `localStorage['quest-of-mind-campaign']` como o `CharacterState` inteiro serializado; o
 * usuário extrai esse JSON manualmente do navegador (ex. console: `copy(localStorage.getItem(
 * 'quest-of-mind-campaign'))`) e cola no app Android.
 *
 * **Fiel à fonte**: replica exatamente a lógica de `{...INITIAL_STATE, ...parsed}` (spread raso —
 * qualquer campo de nível superior presente no save colado sobrescreve o default por completo,
 * sem merge profundo, exceto pelas migrações explícitas abaixo, que a própria fonte também trata
 * à parte) mais as migrações pontuais de saves antigos:
 * - `pomodoroSettings` ausente/parcial → preenchido campo a campo, com fallback pro campo morto
 *   `longBreakMinutes` (formato ainda mais antigo) se presente.
 * - `skills[].id` ausente → `"sk-${idx+1}"` (não UUID aleatório — a fonte usa esse padrão
 *   determinístico específico pra migração, diferente do default de skill nova no Android,
 *   que usa UUID aleatório só pra skills criadas de novo, não pra import).
 * - `skills[].emoji` ausente → 💻/🧪/📚 pros 3 primeiros índices, 🎯 pros demais.
 * - `skills[].prestige` ausente → 0.
 * - `dailies[].value` ausente → 0; `dailies[].createdAt` ausente → agora (ISO-8601).
 * - `todos[].createdAt` ausente → agora (ISO-8601).
 * - `equippedEquipment` ausente/não-array/com menos de 3 posições → `[null, null, null]`.
 *
 * `orbConcept` (campo do `INITIAL_STATE` real sem equivalente em `CharacterState.kt`) é
 * silenciosamente ignorado na decodificação (`ignoreUnknownKeys = true`, mesmo `Json` já usado
 * em `CharacterRepository.kt`) — divergência de modelo já existente no port, não introduzida
 * aqui.
 *
 * @param now injetável para testes determinísticos do fallback de `createdAt`.
 */
object SaveMigrationLogic {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun normalizeGameState(rawJson: String, now: Date = Date()): SaveImportResult {
        val parsedElement = try {
            json.parseToJsonElement(rawJson)
        } catch (e: Exception) {
            return SaveImportResult.InvalidJson("JSON malformado: ${e.message}")
        }

        val parsedObj = parsedElement as? JsonObject
            ?: return SaveImportResult.InvalidJson("O save precisa ser um objeto JSON (recebi outro tipo).")

        val defaultsObj = json.encodeToJsonElement(
            CharacterState.serializer(),
            createInitialCharacterState(now)
        ).jsonObject

        // 1. Spread raso: {...INITIAL_STATE, ...parsed}
        val merged = defaultsObj.toMutableMap()
        merged.putAll(parsedObj)

        // 2. Migração de pomodoroSettings (com fallback pro campo morto longBreakMinutes)
        val oldLongBreak = parsedObj["longBreakMinutes"]?.jsonPrimitive?.intOrNull ?: 15
        val parsedPomodoro = parsedObj["pomodoroSettings"] as? JsonObject
        merged["pomodoroSettings"] = buildJsonObject {
            put("focusDuration", parsedPomodoro?.get("focusDuration") ?: JsonPrimitive(25))
            put("shortBreakDuration", parsedPomodoro?.get("shortBreakDuration") ?: JsonPrimitive(5))
            put("longBreakDuration", parsedPomodoro?.get("longBreakDuration") ?: JsonPrimitive(oldLongBreak))
            put("autoStartBreak", parsedPomodoro?.get("autoStartBreak") ?: JsonPrimitive(false))
            put("autoStartFocus", parsedPomodoro?.get("autoStartFocus") ?: JsonPrimitive(false))
        }
        merged.remove("longBreakMinutes")

        // 3. Migração de skills — id/emoji/prestige com defaults determinísticos por posição
        val fallbackEmojis = listOf("💻", "🧪", "📚")
        val skillsArray = (merged["skills"] as? JsonArray) ?: JsonArray(emptyList())
        merged["skills"] = JsonArray(
            skillsArray.mapIndexed { idx, el ->
                val skillObj = el.jsonObject
                buildJsonObject {
                    skillObj.forEach { (k, v) -> put(k, v) }
                    if (skillObj["id"]?.jsonPrimitive?.contentOrNull.isNullOrEmpty()) {
                        put("id", JsonPrimitive("sk-${idx + 1}"))
                    }
                    if (skillObj["emoji"] == null || skillObj["emoji"] is JsonNull) {
                        put("emoji", JsonPrimitive(fallbackEmojis.getOrElse(idx) { "🎯" }))
                    }
                    if (skillObj["prestige"] == null || skillObj["prestige"] is JsonNull) {
                        put("prestige", JsonPrimitive(0))
                    }
                }
            }
        )

        val nowIso = now.toInstant().toString()

        // 4. Migração de dailies — value/createdAt
        val dailiesArray = (merged["dailies"] as? JsonArray) ?: JsonArray(emptyList())
        merged["dailies"] = JsonArray(
            dailiesArray.map { el ->
                val dObj = el.jsonObject
                buildJsonObject {
                    dObj.forEach { (k, v) -> put(k, v) }
                    if (dObj["value"] == null || dObj["value"] is JsonNull) {
                        put("value", JsonPrimitive(0))
                    }
                    if (dObj["createdAt"] == null || dObj["createdAt"] is JsonNull) {
                        put("createdAt", JsonPrimitive(nowIso))
                    }
                }
            }
        )

        // 5. Migração de todos — createdAt
        val todosArray = (merged["todos"] as? JsonArray) ?: JsonArray(emptyList())
        merged["todos"] = JsonArray(
            todosArray.map { el ->
                val tObj = el.jsonObject
                buildJsonObject {
                    tObj.forEach { (k, v) -> put(k, v) }
                    if (tObj["createdAt"] == null || tObj["createdAt"] is JsonNull) {
                        put("createdAt", JsonPrimitive(nowIso))
                    }
                }
            }
        )

        // 6. Guarda de equippedEquipment — precisa ser array com pelo menos 3 posições
        val equippedEquipmentEl = merged["equippedEquipment"]
        val isValidArray = (equippedEquipmentEl as? JsonArray)?.let { it.size >= 3 } ?: false
        if (!isValidArray) {
            merged["equippedEquipment"] = buildJsonArray {
                add(JsonNull); add(JsonNull); add(JsonNull)
            }
        }

        return try {
            val characterState = json.decodeFromJsonElement(CharacterState.serializer(), JsonObject(merged))
            SaveImportResult.Success(characterState)
        } catch (e: Exception) {
            SaveImportResult.InvalidJson("Save não pôde ser convertido em personagem válido: ${e.message}")
        }
    }
}

/** Resultado da tentativa de import, já traduzido pra o que a UI precisa mostrar. */
sealed class SaveImportOutcome {
    data class Restored(val charName: String) : SaveImportOutcome()
    data object Failed : SaveImportOutcome()
}
