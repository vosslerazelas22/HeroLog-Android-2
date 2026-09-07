package com.iurispraecepta.herolog.data.export

import android.content.Context
import android.net.Uri
import com.iurispraecepta.herolog.logic.SaveImportResult
import com.iurispraecepta.herolog.logic.SaveMigrationLogic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Funções puras de import de save do HeroLog Android.
 *
 * Suporta 2 mídias: arquivo `.json` (lê via `contentResolver.openInputStream`)
 * e string JSON crua (já carregada). Em ambos os casos, detecta se o JSON
 * é um [ExportPayload] versionado (produzido pelo `GameStateExporter`) ou
 * um `CharacterState` cru (formato herdado do `RestoreSaveDialog` original,
 * exportado via `localStorage` no React) — e delega a normalização final
 * pro [SaveMigrationLogic.normalizeGameState] que já existe e já está
 * auditado.
 */
object GameStateImporter {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Lê o conteúdo de um arquivo apontado por [uri] usando o `ContentResolver`
     * do Android. Retorna string vazia se a Uri for nula.
     */
    fun readFromUri(context: Context, uri: Uri?): String {
        if (uri == null) return ""
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: ""
    }

    /**
     * Decide se o JSON é um wrapper [ExportPayload] (com `version` e
     * `character`) ou um `CharacterState` cru, extrai o sub-JSON do
     * personagem, e delega ao [SaveMigrationLogic.normalizeGameState] pra
     * produzir o [SaveImportResult] final (com migrações de save antigo
     * como `pomodoroSettings`/`skills[].id`).
     *
     * Heurística: se a chave `version` existir no JSON de topo, é wrapper
     * — `version` é exclusivo do [ExportPayload] (o `CharacterState` cru
     * não tem esse campo). Se for wrapper, `character` é obrigatório (sem
     * ele, retorna `InvalidJson`); se for cru, o objeto inteiro é o
     * personagem.
     *
     * `version` > 1 retorna `InvalidJson` — sem migrations conhecidas, é
     * mais seguro rejeitar do que aceitar dados em formato desconhecido.
     */
    fun parsePayload(rawJson: String): SaveImportResult {
        if (rawJson.isBlank()) {
            return SaveImportResult.InvalidJson("Conteúdo vazio.")
        }

        val parsedElement = try {
            json.parseToJsonElement(rawJson)
        } catch (e: Exception) {
            return SaveImportResult.InvalidJson("JSON malformado: ${e.message}")
        }

        val parsedObj = parsedElement as? JsonObject
            ?: return SaveImportResult.InvalidJson("O save precisa ser um objeto JSON (recebi outro tipo).")

        return if (parsedObj.containsKey("version")) {
            val version = parsedObj["version"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            if (version == null || version < 1) {
                return SaveImportResult.InvalidJson("Versão do save ausente ou inválida: $version")
            }
            if (version > ExportPayload.EXPORT_VERSION) {
                return SaveImportResult.InvalidJson(
                    "Versão do save não suportada: $version (suportado: ${ExportPayload.EXPORT_VERSION})"
                )
            }
            val characterElement = parsedObj["character"]
                ?: return SaveImportResult.InvalidJson("ExportPayload sem campo 'character'.")
            val characterJson = json.encodeToString(JsonObject.serializer(), characterElement.jsonObject)
            SaveMigrationLogic.normalizeGameState(characterJson)
        } else {
            SaveMigrationLogic.normalizeGameState(rawJson)
        }
    }
}
