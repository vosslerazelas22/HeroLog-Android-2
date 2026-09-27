package com.iurispraecepta.herolog.logic

import com.iurispraecepta.herolog.model.ActivityEntry
import com.iurispraecepta.herolog.model.ActivityType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

/**
 * Funções puras da tela Taverna — Spec B (`TavernScreen`).
 *
 * Fonte: `TavernFeat c89af8f`, `src/modules/tavern/TavernScreen.tsx` (linhas 60-80 para
 * `formatRelativeTime`/`formatHoursMinutes`; linhas 84-86 para `xpNeeded`/`xpPercentage`;
 * linha 229 para o ouro via `toLocaleString('pt-BR')`).
 *
 * Regras:
 * - [formatHoursMinutes] é a **regra da Taverna** (`0h` omitido → `"25m"`). A versão da
 *   CharacterScreen (sempre `XhYYm`) é da Spec C — não trocar as duas (TAVERN_DELTA §3.6).
 * - [streakLabel] pluraliza `dia/dias` (D4 — desvio consciente do React, que escreve sempre
 *   "dias" no tile da Taverna).
 * - [formatRelativeTime] recebe o ISO do feed (Spec F) e formata no **fuso local**, como o
 *   `toLocaleDateString`/`new Date()` do React. `parseEntryDate` do React (para o formato
 *   `"04/08/2026, 14:23"` do `history.date`) **não** é portado: o feed já traz ISO.
 */
object TavernLogic {

    /** Fuso/idioma fixos para as formatações pt-BR da tela (paridade com `toLocaleString('pt-BR')`). */
    private val LocalePtBr = Locale("pt", "BR")

    // ------------------------------------------------------------------
    // XP de combate (TAV-3)
    // ------------------------------------------------------------------

    /** `Math.max(100, combatLevel * 100)` — React linha 84. */
    fun xpNeeded(combatLevel: Int): Int = max(100, combatLevel * 100)

    /** `Math.min(100, Math.round(combatXP / xpNeeded * 100))` — React linha 85. */
    fun xpPercent(combatXP: Int, combatLevel: Int): Int {
        val needed = xpNeeded(combatLevel)
        val percent = (combatXP.toDouble() / needed * 100).let { Math.round(it) }
        return min(100, percent.toInt())
    }

    // ------------------------------------------------------------------
    // Formatação
    // ------------------------------------------------------------------

    /**
     * Regra da **Taverna** (React linhas 68-72): `h == 0 → "{m}m"`, senão `"{h}h{mm}m"`
     * com 2 dígitos nos minutos (`25 → "25m"`, `125 → "2h05m"`).
     */
    fun formatHoursMinutes(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return if (h == 0) "${m}m" else String.format(Locale.US, "%dh%02dm", h, m)
    }

    /** `gold.toLocaleString('pt-BR')` — agrupamento pt-BR **sem** símbolo de moeda (`1234 → "1.234"`). */
    fun formatGold(gold: Int): String = NumberFormat.getIntegerInstance(LocalePtBr).format(gold.toLong())

    /**
     * Tile "Sequência" da Taverna (D4 — desvio consciente do React, que escreve sempre "dias"):
     * `1 → "1 dia"`, `N → "N dias"`.
     */
    fun streakLabel(streak: Int): String = if (streak == 1) "1 dia" else "$streak dias"

    /**
     * Tempo relativo de uma entrada do feed — literal do React (linhas 50-66):
     *
     * ```
     * sem at → "recente"; diff < 0 ou < 1min → "agora"; < 60min → "há {m}m";
     * < 24h → "há {h}h"; 1d → "ontem"; < 7d → "há {d}d"; senão dd/MM (pt-BR)
     * ```
     *
     * @param at ISO-8601 do [ActivityEntry.at] (UTC com milissegundos).
     * @param now relógio injetável (por padrão o instante atual).
     * @return a string original de [at] quando ela não parseia — mesmo comportamento do React
     *   (`if (!d) return dateStr`).
     */
    fun formatRelativeTime(at: String?, now: Date = Date()): String {
        if (at == null) return "recente"
        val date = parseIso(at) ?: return at
        val diffMs = now.time - date.time
        if (diffMs < 0) return "agora"
        val diffMinutes = diffMs / (1000L * 60L)
        if (diffMinutes < 1) return "agora"
        if (diffMinutes < 60) return "há ${diffMinutes}m"
        val diffHours = diffMinutes / 60L
        if (diffHours < 24) return "há ${diffHours}h"
        val diffDays = diffHours / 24L
        if (diffDays == 1L) return "ontem"
        if (diffDays < 7) return "há ${diffDays}d"
        return SimpleDateFormat("dd/MM", LocalePtBr).format(date)
    }

    // ------------------------------------------------------------------
    // Rótulos por tipo de entrada (TAV-7)
    // ------------------------------------------------------------------

    /** Título do item de Recentes por tipo — React: "Foco concluído" / "Missão concluído" / "Ritual concluído". */
    fun entryTitle(type: ActivityType): String = when (type) {
        ActivityType.Focus -> "Foco concluído"
        ActivityType.Mission -> "Missão concluída"
        ActivityType.Ritual -> "Ritual concluído"
    }

    /**
     * Detalhe do item de Recentes — React (linhas 108-131):
     * focus → `"{formatHoursMinutes(minutes)} · {title}"`; mission/ritual → `{title}`.
     */
    fun entryDetail(entry: ActivityEntry): String = when (entry.type) {
        ActivityType.Focus -> "${formatHoursMinutes(entry.minutes)} · ${entry.title}"
        else -> entry.title
    }

    /**
     * Recompensa do item — só para foco (D2), React: `"+{xp} XP · +{gold} GP"`.
     * `null` para mission/ritual (o React omite a linha).
     */
    fun entryReward(entry: ActivityEntry): String? = when (entry.type) {
        ActivityType.Focus -> "+${entry.xp} XP · +${entry.gold} GP"
        else -> null
    }

    /** Parse do ISO-8601 em UTC do feed (`minSdk` 24 → `SimpleDateFormat`, sem `java.time`). */
    private fun parseIso(value: String): Date? = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(value)
    } catch (_: Exception) {
        null
    }
}
