package com.iurispraecepta.herolog.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Funções puras de export de save do HeroLog Android.
 *
 * Suporta 2 mídias: arquivo `.json` (compartilhável via `Intent.ACTION_SEND`
 * + `FileProvider`) e string JSON crua (compartilhável via clipboard).
 * Ambas produzem o mesmo [ExportPayload] versionado, garantindo
 * consistência entre os dois caminhos.
 *
 * O `Json` é injetado — o caller decide se usa [com.iurispraecepta.herolog.data.JsonConfig.pretty]
 * (default recomendado — gera arquivo legível com todos os campos,
 * `encodeDefaults = true`) ou outra config.
 *
 * Função deliberadamente separada de `SaveMigrationLogic` (que é de import)
 * pra que cada lado tenha responsabilidade única e testes unitários isolados.
 */
object GameStateExporter {

    private const val EXPORTS_DIR = "exports"
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    /**
     * Monta o [ExportPayload] a partir do estado atual. `exportedAt` é
     * gerado em ISO-8601 (`Instant.toString()`) usando [now] injetável
     * pra testes determinísticos.
     */
    fun buildPayload(
        character: CharacterState,
        activeFocus: PersistedFocusSession?,
        now: Date = Date()
    ): ExportPayload {
        return ExportPayload(
            exportedAt = now.toInstant().toString(),
            character = character,
            activeFocus = activeFocus
        )
    }

    /**
     * Serializa o payload pra string JSON usando a config de [json] injetada.
     */
    fun encodePayload(payload: ExportPayload, json: Json): String {
        return json.encodeToString(payload)
    }

    /**
     * Escreve o payload em `cacheDir/exports/herolog-save-<timestamp>.json`
     * e retorna o `Uri` (via `FileProvider`) pronto pra `Intent.ACTION_SEND`.
     *
     * O diretório `cacheDir/exports/` precisa estar declarado em
     * `res/xml/file_paths.xml` como `<cache-path name="exports" path="exports/" />`
     * — o provider authority usado é `<applicationId>.fileprovider`.
     */
    fun writeToCache(
        context: Context,
        payload: ExportPayload,
        json: Json
    ): Uri {
        val jsonText = encodePayload(payload, json)
        val exportsDir = File(context.cacheDir, EXPORTS_DIR).apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val outFile = File(exportsDir, "herolog-save-$timestamp.json")
        outFile.writeText(jsonText, Charsets.UTF_8)
        return FileProvider.getUriForFile(
            context,
            context.packageName + FILE_PROVIDER_SUFFIX,
            outFile
        )
    }
}
