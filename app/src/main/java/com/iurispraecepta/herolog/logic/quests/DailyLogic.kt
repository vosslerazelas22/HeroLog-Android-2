package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.logic.ActivityFeedLogic
import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.model.ActivitySource
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import java.util.Date
import kotlin.math.floor
import kotlin.math.max

data class DailyToggleResult(
    val updatedDaily: Daily,
    val updatedState: CharacterState
)

object DailyLogic {

    /**
     * @param referenceDate relógio injetável (Spec F / E7 do TAVERN_DELTA): alimenta o `at` da
     *   entrada do feed quando a daily é concluída. Default `Date()` — não quebra chamadas existentes.
     */
    fun toggle(daily: Daily, state: CharacterState, referenceDate: Date = Date()): DailyToggleResult {
        val rewards = getDifficultyRewards(daily.difficulty)
        val xpMul = if (state.charClass == CharClass.Mage) 1.2 else 1.0
        val goldMul = if (state.charClass == CharClass.Warrior) 1.2 else 1.0
        val finalXP = floor(rewards.xp * xpMul).toInt()
        val finalGold = floor(rewards.gold * goldMul).toInt()

        return if (!daily.completed) completeDaily(daily, state, finalXP, finalGold, referenceDate)
        else uncompleteDaily(daily, state, finalXP, finalGold)
    }

    private fun completeDaily(
        daily: Daily, state: CharacterState, finalXP: Int, finalGold: Int, referenceDate: Date
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
        val rewardedState = state.copy(
            gold = state.gold + finalGold,
            totalGoldEarned = state.totalGoldEarned + finalGold,
            totalXP = state.totalXP + finalXP,
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied,
            hp = nextHp
        )
        // Spec F (FEED-4): a entrada no feed nasce junto com a recompensa (atomicidade).
        val updatedState = ActivityFeedLogic.record(
            state = rewardedState,
            source = ActivitySource.Daily,
            refId = daily.id,
            title = daily.title,
            xp = finalXP,
            gold = finalGold,
            at = referenceDate
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
        val rewardedState = state.copy(
            gold = max(0, state.gold - finalGold),
            totalGoldEarned = max(0, state.totalGoldEarned - finalGold),
            totalXP = max(0, state.totalXP - finalXP),
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied
            // hp: propositalmente NÃO tocado aqui — fiel à fonte
        )
        // Spec F (FEED-5 / D3): desfazer remove a entrada do feed; se não houver, no-op.
        val updatedState = ActivityFeedLogic.revoke(rewardedState, ActivitySource.Daily, daily.id)
        return DailyToggleResult(updatedDaily, updatedState)
    }

    fun toggleChecklistItem(daily: Daily, itemId: String): Daily {
        val updatedChecklist = daily.checklist.map {
            if (it.id == itemId) it.copy(completed = !it.completed) else it
        }
        return daily.copy(checklist = updatedChecklist)
    }
}
