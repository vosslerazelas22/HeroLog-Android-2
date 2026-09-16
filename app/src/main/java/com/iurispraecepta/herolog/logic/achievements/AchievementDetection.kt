package com.iurispraecepta.herolog.logic.achievements

import com.iurispraecepta.herolog.model.CharacterState

/**
 * Detecção central de conquistas (FR-004).
 *
 * Compara o estado anterior [previous] com o estado candidato [candidate] e retorna
 * somente os achievements cujo threshold foi cruzado pela primeira vez naquela mutação.
 *
 * Regras:
 * - FR-005: IDs já presentes em `previous.achievements` nunca entram na fila.
 * - FR-006: Não emite anúncios retroativos — thresholds já satisfeitos em [previous]
 *   (check(previous) == true) não são anunciados, mesmo sem ID persistido; nesse caso
 *   o achievement continua visível como desbloqueado via `AchievementLogic.isUnlocked`
 *   (que faz OR com `check(state)`), sem nunca entrar na fila.
 * - Ordem estável: retorna na ordem do catálogo.
 * - Função pura: sem efeitos colaterais, sem side-effects.
 */
object AchievementDetection {

    /**
     * Detecta novos achievements desbloqueados na transição [previous] → [candidate].
     * Retorna os Achievement novos em ordem de catálogo, já com os IDs adicionados ao
     * [candidate] (via retorna uma lista de novos IDs para serem persistidos).
     */
    fun detectNewAchievements(
        previous: CharacterState,
        candidate: CharacterState
    ): List<Achievement> {
        val alreadyUnlocked = previous.achievements.toSet()
        return AchievementCatalog.ACHIEVEMENTS_LIST.filter { achievement ->
            achievement.id !in alreadyUnlocked &&
                !achievement.check(previous) &&
                achievement.check(candidate)
        }
    }

    /**
     * Retorna os IDs dos novos achievements desbloqueados.
     */
    fun detectNewAchievementIds(
        previous: CharacterState,
        candidate: CharacterState
    ): List<String> {
        return detectNewAchievements(previous, candidate).map { it.id }
    }

    /**
     * Adiciona os IDs fornecidos à lista de achievements do estado.
     * Idempotente: IDs já presentes não são adicionados novamente.
     */
    fun withAchievements(
        state: CharacterState,
        newIds: List<String>
    ): CharacterState {
        if (newIds.isEmpty()) return state
        val existing = state.achievements.toSet()
        val toAdd = newIds.filter { it !in existing }
        if (toAdd.isEmpty()) return state
        return state.copy(achievements = state.achievements + toAdd)
    }
}
