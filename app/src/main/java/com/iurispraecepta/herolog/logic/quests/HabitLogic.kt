package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max

data class HabitTriggerResult(
    val updatedHabit: Habit,
    val updatedState: CharacterState
)

object HabitLogic {

    fun trigger(
        habit: Habit,
        state: CharacterState,
        isUp: Boolean,
        referenceDate: Date = Date()
    ): HabitTriggerResult {
        val rewards = getDifficultyRewards(habit.difficulty)
        return if (isUp) triggerUp(habit, state, rewards, referenceDate)
        else triggerDown(habit, state, rewards)
    }

    private fun triggerUp(
        habit: Habit,
        state: CharacterState,
        rewards: DifficultyRewards,
        referenceDate: Date
    ): HabitTriggerResult {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(referenceDate)

        val xpEarned = if (state.charClass == CharClass.Mage) {
            floor(rewards.xp * 1.2).toInt()
        } else rewards.xp

        val goldEarned = if (state.charClass == CharClass.Warrior) {
            floor(rewards.gold * 1.2).toInt()
        } else rewards.gold

        var combatXPApplied = state.combatXP + xpEarned
        var currentCombatLevel = state.combatLevel
        var combatXPRequirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
        var didLevelUp = false
        while (combatXPApplied >= combatXPRequirement) {
            combatXPApplied -= combatXPRequirement
            currentCombatLevel += 1
            combatXPRequirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
            didLevelUp = true
        }
        val nextHp = if (didLevelUp) state.maxHp else state.hp

        val updatedState = state.copy(
            gold = state.gold + goldEarned,
            totalGoldEarned = state.totalGoldEarned + goldEarned,
            totalXP = state.totalXP + xpEarned,
            combatXP = combatXPApplied,
            combatLevel = currentCombatLevel,
            hp = nextHp
        )

        val updatedHabit = habit.copy(
            upCount = habit.upCount + 1,
            streak = habit.streak + 1,
            lastTriggeredDate = todayStr
        )

        return HabitTriggerResult(updatedHabit, updatedState)
    }

    private fun triggerDown(
        habit: Habit,
        state: CharacterState,
        rewards: DifficultyRewards
    ): HabitTriggerResult {
        val finalDamage = if (state.charClass == CharClass.Ranger) {
            max(1, floor(rewards.damage * 0.7).toInt())
        } else rewards.damage

        val nextHp = max(0, state.hp - finalDamage)
        val isDead = nextHp <= 0

        val updatedState = state.copy(
            hp = nextHp,
            isPlayerDead = if (isDead) true else state.isPlayerDead
        )

        val updatedHabit = habit.copy(
            downCount = habit.downCount + 1,
            streak = max(0, habit.streak - 1)
        )

        return HabitTriggerResult(updatedHabit, updatedState)
    }
}
