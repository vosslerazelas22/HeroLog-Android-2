package com.iurispraecepta.herolog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Fila de recompensas concluídas em background aguardando celebração (spec-008, FR-10).
 *
 * Feature Android-first: no React não existe conclusão de sessão com o app fechado
 * (o timer morre com a aba — ver `useFocusSession.ts`, `localStorage herolog_active_session`),
 * então nunca há mais de uma recompensa pendente por vez. No Android, o Foreground
 * Service pode concluir N sessões sem o app ser reaberto — por isso isto é uma tabela
 * (INSERT acumula) e não um campo único (que sobrescreveria a sessão anterior).
 *
 * Campos JSON (`lootedItems`, `droppedTitle`, `achievementsUnlocked`) usam o mesmo
 * [com.iurispraecepta.herolog.data.JsonConfig.default] dos blobs existentes.
 */
@Entity(tableName = "pending_reward_celebrations")
data class PendingRewardCelebrationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionCompletedAt: Long = System.currentTimeMillis(),
    val skillName: String,
    val durationMinutes: Int,
    val xpGained: Int,
    val goldGained: Int,
    val lootedItems: String = "[]",
    val droppedTitle: String? = null,
    val leveledUp: Boolean = false,
    val previousLevel: Int? = null,
    val newLevel: Int? = null,
    val achievementsUnlocked: String = "[]",
    val consumed: Boolean = false
)
