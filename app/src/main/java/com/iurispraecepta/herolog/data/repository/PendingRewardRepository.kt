package com.iurispraecepta.herolog.data.repository

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.dao.PendingRewardCelebrationDao
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import kotlinx.serialization.json.Json

/**
 * Fila de celebrações pendentes (spec-008, FR-10/FR-12).
 *
 * O Service insere um registro por sessão concluída em background; a UI consome
 * tudo de uma vez ao reabrir o app ([consumePendingCelebrations], atômico).
 */
class PendingRewardRepository(
    private val dao: PendingRewardCelebrationDao,
    private val json: Json = JsonConfig.default
) {
    suspend fun queue(entity: PendingRewardCelebrationEntity) {
        dao.insert(entity)
    }

    suspend fun getPending(): List<PendingRewardCelebrationEntity> {
        return dao.getAllUnconsumed()
    }

    suspend fun countPending(): Int {
        return dao.countUnconsumed()
    }

    /**
     * Agrega os pendentes e os marca como consumidos numa única operação atômica.
     * Retorna `null` quando não há nada pendente.
     */
    suspend fun consumePendingCelebrations(): AggregatedCelebrationSummary? {
        val pending = dao.getAllUnconsumed()
        if (pending.isEmpty()) return null
        val summary = aggregateCelebrations(pending, json)
        dao.markAllConsumed(pending.map { it.id })
        return summary
    }
}
