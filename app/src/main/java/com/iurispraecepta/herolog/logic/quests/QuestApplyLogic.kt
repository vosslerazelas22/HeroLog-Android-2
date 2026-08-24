package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.model.CharacterState
import java.util.Date
import kotlin.math.floor

object QuestApplyLogic {

    fun claimQuestReward(
        state: CharacterState,
        questId: String,
        goldReward: Int,
        xpReward: Int,
        referenceDate: Date = Date()
    ): CharacterState {
        val claimId = QuestLogic.getQuestClaimId(questId, referenceDate)

        // Idempotência: se já resgatado, retorna o estado sem modificações
        if (state.achievements.contains(claimId)) {
            return state
        }

        // 1. Combat XP & Level Up
        var nextXP = state.combatXP + floor(xpReward * 0.4).toInt()
        var nextCombatLevel = state.combatLevel
        var requiredXP = CombatLogic.requiredXpForCombatLevel(nextCombatLevel)

        while (nextXP >= requiredXP) {
            nextXP -= requiredXP
            nextCombatLevel += 1
            requiredXP = CombatLogic.requiredXpForCombatLevel(nextCombatLevel)
        }

        // 2. Atualização dos atributos do herói
        return state.copy(
            gold = state.gold + goldReward,
            totalGoldEarned = state.totalGoldEarned + goldReward,
            totalXP = state.totalXP + xpReward,
            combatLevel = nextCombatLevel,
            combatXP = nextXP,
            achievements = state.achievements + claimId
        )
    }
}
