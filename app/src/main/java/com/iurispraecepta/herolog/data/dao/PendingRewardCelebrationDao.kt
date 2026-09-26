package com.iurispraecepta.herolog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity

@Dao
interface PendingRewardCelebrationDao {
    @Insert
    suspend fun insert(entity: PendingRewardCelebrationEntity)

    @Query(
        "SELECT * FROM pending_reward_celebrations " +
            "WHERE consumed = 0 ORDER BY sessionCompletedAt ASC"
    )
    suspend fun getAllUnconsumed(): List<PendingRewardCelebrationEntity>

    @Query("SELECT COUNT(*) FROM pending_reward_celebrations WHERE consumed = 0")
    suspend fun countUnconsumed(): Int

    @Query("UPDATE pending_reward_celebrations SET consumed = 1 WHERE id IN (:ids)")
    suspend fun markConsumed(ids: List<String>)

    /**
     * Consumo atômico (spec-008, FR-12): marca todos os registros exibidos juntos de
     * uma vez, para nunca deixar estado parcialmente consumido se o processo morrer
     * no meio da operação.
     */
    @Transaction
    suspend fun markAllConsumed(ids: List<String>) {
        if (ids.isNotEmpty()) {
            markConsumed(ids)
        }
    }
}
