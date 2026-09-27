package com.iurispraecepta.herolog.logic

import com.iurispraecepta.herolog.model.ActivityEntry
import com.iurispraecepta.herolog.model.ActivitySource
import com.iurispraecepta.herolog.model.ActivityType
import com.iurispraecepta.herolog.model.CharacterState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Feed persistido de atividades recentes (`CharacterState.recentActivity`) — Spec F.
 *
 * Duas funções puras chamadas **dentro** das funções de lógica que já retornam o novo
 * `CharacterState`, para que o feed mude atomicamente com a recompensa (um único
 * `saveCharacterState()`). A Taverna (Spec B) lê apenas o feed.
 *
 * Contrato idêntico ao React (`src/utils/activityFeed.ts`): mesmo nome de campo, mesma forma
 * JSON, mesmo cap de 20 (D8/D9).
 */
object ActivityFeedLogic {

    /** Cap do feed (FEED-6 / D9): ao exceder, descarta as mais antigas. */
    const val MAX_RECENT_ACTIVITY = 20

    /**
     * Prepend da entrada mais recente e corte no cap. A ordem é sempre da mais recente para a
     * mais antiga.
     *
     * @param source origem do evento; o `type` é derivado dela (`session→focus`, `todo→mission`,
     *   `habit→ritual`, `daily→ritual`).
     * @param minutes só preenchido para sessões de foco; `0` nos demais tipos.
     * @param at instante do evento (relógio injetável — testabilidade).
     */
    fun record(
        state: CharacterState,
        source: ActivitySource,
        refId: String,
        title: String,
        minutes: Int = 0,
        xp: Int = 0,
        gold: Int = 0,
        at: Date = Date()
    ): CharacterState {
        val entry = ActivityEntry(
            id = "${source.jsonName()}_${at.time}",
            type = typeOf(source),
            source = source,
            refId = refId,
            title = title,
            minutes = minutes,
            xp = xp,
            gold = gold,
            at = isoInstant(at)
        )
        return state.copy(
            recentActivity = (listOf(entry) + state.recentActivity).take(MAX_RECENT_ACTIVITY)
        )
    }

    /**
     * Remove a entrada mais recente com esse `source`+`refId` (FEED-5 / D3 — desfazer
     * conclusão de Todo/Daily). No-op quando não há match: retorna o mesmo estado.
     *
     * O casamento é por `source`+`refId`, nunca por `id`, então ids repetidos no mesmo milissegundo
     * não são problema.
     */
    fun revoke(state: CharacterState, source: ActivitySource, refId: String): CharacterState {
        val index = state.recentActivity.indexOfFirst { it.source == source && it.refId == refId }
        if (index < 0) return state
        return state.copy(
            recentActivity = state.recentActivity.filterIndexed { i, _ -> i != index }
        )
    }

    /** Mapeamento `source → type` do contrato da Spec F. */
    fun typeOf(source: ActivitySource): ActivityType = when (source) {
        ActivitySource.Session -> ActivityType.Focus
        ActivitySource.Todo -> ActivityType.Mission
        ActivitySource.Habit, ActivitySource.Daily -> ActivityType.Ritual
    }

    /** Valor JSON de [ActivitySource] (garante que o prefixo do `id` é o próprio `source`). */
    private fun ActivitySource.jsonName(): String =
        ActivitySource.serializer().descriptor.getElementName(ordinal)

    /**
     * Instante ISO-8601 em UTC com milissegundos (`2026-09-20T14:23:10.000Z`) — mesmo formato
     * que o React gera com `new Date().toISOString()`, campo a campo de paridade (D8).
     * `SimpleDateFormat` em vez de `java.time` porque o `minSdk` é 24 (sem desugaring).
     */
    private fun isoInstant(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(date)
}
