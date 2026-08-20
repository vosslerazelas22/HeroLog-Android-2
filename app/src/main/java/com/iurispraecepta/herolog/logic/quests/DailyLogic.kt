package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.CharClass
import kotlin.math.floor
import kotlin.math.max

data class DailyToggleResult(
    val updatedDaily: Daily,
    val updatedState: CharacterState
)

object DailyLogic {

    fun toggle(daily: Daily, state: CharacterState): DailyToggleResult {
        val rewards = getDifficultyRewards(daily.difficulty)
        val xpMul = if (state.charClass == CharClass.Mage) 1.2 else 1.0
        val goldMul = if (state.charClass == CharClass.Warrior) 1.2 else 1.0
        val finalXP = floor(rewards.xp * xpMul).toInt()
        val finalGold = floor(rewards.gold * goldMul).toInt()

        return if (!daily.completed) completeDaily(daily, state, finalXP, finalGold)
        else uncompleteDaily(daily, state, finalXP, finalGold)
    }

    private fun completeDaily(
        daily: Daily, state: CharacterState, finalXP: Int, finalGold: Int
    ): DailyToggleResult {
        var combatXPApplied = state.combatXP + finalXP
        var currentCombatLevel = state.combatLevel
        var requirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
        var didLevelUp = false
        while (combatXPApplied >= requirement) {
            combatXPApplied -= requirement
            currentCombatLevel += 1
            requirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
            didLevelUp = true
        }
        val nextHp = if (didLevelUp) state.maxHp else state.hp

        val updatedDaily = daily.copy(
            completed = true,
            streak = daily.streak + 1,
            value = (daily.value ?: 0) + 1
        )
        val updatedState = state.copy(
            gold = state.gold + finalGold,
            totalGoldEarned = state.totalGoldEarned + finalGold,
            totalXP = state.totalXP + finalXP,
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied,
            hp = nextHp
        )
        return DailyToggleResult(updatedDaily, updatedState)
    }

    private fun uncompleteDaily(
        daily: Daily, state: CharacterState, finalXP: Int, finalGold: Int
    ): DailyToggleResult {
        var combatXPApplied = state.combatXP - finalXP
        var currentCombatLevel = state.combatLevel
        while (combatXPApplied < 0 && currentCombatLevel > 1) {
            currentCombatLevel -= 1
            val requirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
            combatXPApplied += requirement
        }
        if (combatXPApplied < 0) combatXPApplied = 0

        val updatedDaily = daily.copy(
            completed = false,
            streak = max(0, daily.streak - 1),
            value = (daily.value ?: 0) - 1
        )
        val updatedState = state.copy(
            gold = max(0, state.gold - finalGold),
            totalGoldEarned = max(0, state.totalGoldEarned - finalGold),
            totalXP = max(0, state.totalXP - finalXP),
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied
            // hp: propositalmente NÃO tocado aqui — fiel à fonte
        )
        return DailyToggleResult(updatedDaily, updatedState)
    }

    fun toggleChecklistItem(daily: Daily, itemId: String): Daily {
        val updatedChecklist = daily.checklist.map {
            if (it.id == itemId) it.copy(completed = !it.completed) else it
        }
        return daily.copy(checklist = updatedChecklist)
    }
}
