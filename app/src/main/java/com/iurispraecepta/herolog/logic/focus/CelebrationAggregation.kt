package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import kotlinx.serialization.json.Json

/**
 * Resumo agregado de N celebrações pendentes (spec-008, FR-11).
 *
 * Exibição: total fixo no topo + breakdown colapsável por sessão, nível final único
 * (sem repetir animação de level-up por sessão), achievements deduplicados por ID.
 * O caso `sessionCount == 1` continua usando o FocusCompletionFlow existente
 * (ui.focus); só `sessionCount >= 2` usa a view agregada.
 */
data class AggregatedCelebrationSummary(
    val sessionCount: Int,
    val totalXp: Int,
    val totalGold: Int,
    val leveledUp: Boolean,
    val initialLevel: Int?,
    val finalLevel: Int?,
    val allAchievements: List<String>,
    val allLootItems: List<LootItem>,
    val sessions: List<PendingRewardCelebrationEntity>
)

/**
 * Agrega celebrações pendentes (ordenadas por conclusão) num resumo único.
 *
 * Função pura e determinística (dado o mesmo [json] para decode): não toca em banco,
 * relógio ou estado global. `initialLevel` = nível anterior da primeira sessão com
 * level-up; `finalLevel` = nível novo da última sessão com level-up.
 */
fun aggregateCelebrations(
    sessions: List<PendingRewardCelebrationEntity>,
    json: Json = JsonConfig.default
): AggregatedCelebrationSummary {
    val leveledSessions = sessions.filter { it.leveledUp }
    return AggregatedCelebrationSummary(
        sessionCount = sessions.size,
        totalXp = sessions.sumOf { it.xpGained },
        totalGold = sessions.sumOf { it.goldGained },
        leveledUp = leveledSessions.isNotEmpty(),
        initialLevel = leveledSessions.firstOrNull()?.previousLevel,
        finalLevel = leveledSessions.lastOrNull()?.newLevel,
        allAchievements = sessions
            .flatMap { runCatching { json.decodeFromString<List<String>>(it.achievementsUnlocked) }.getOrDefault(emptyList()) }
            .distinct(),
        allLootItems = sessions.flatMap {
            runCatching { json.decodeFromString<List<LootItem>>(it.lootedItems) }.getOrDefault(emptyList())
        },
        sessions = sessions
    )
}
