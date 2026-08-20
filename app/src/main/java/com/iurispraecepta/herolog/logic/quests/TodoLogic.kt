package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.CharClass
import java.util.Date
import kotlin.math.floor
import kotlin.math.max

data class TodoToggleResult(
    val updatedTodo: Todo,
    val updatedState: CharacterState
)

object TodoLogic {

    fun toggle(todo: Todo, state: CharacterState, referenceDate: Date = Date()): TodoToggleResult {
        val rewards = getDifficultyRewards(todo.difficulty)
        val xpMul = if (state.charClass == CharClass.Mage) 1.2 else 1.0
        val goldMul = if (state.charClass == CharClass.Warrior) 1.2 else 1.0
        val finalXP = floor(rewards.xp * xpMul).toInt()
        val finalGold = floor(rewards.gold * goldMul).toInt()

        return if (!todo.completed) completeTodo(todo, state, finalXP, finalGold, referenceDate)
        else uncompleteTodo(todo, state, finalXP, finalGold)
    }

    private fun completeTodo(
        todo: Todo, state: CharacterState, finalXP: Int, finalGold: Int, referenceDate: Date
    ): TodoToggleResult {
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

        val updatedTodo = todo.copy(
            completed = true,
            completedAt = referenceDate.toInstant().toString()
        )
        val updatedState = state.copy(
            gold = state.gold + finalGold,
            totalGoldEarned = state.totalGoldEarned + finalGold,
            totalXP = state.totalXP + finalXP,
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied,
            hp = nextHp
        )
        return TodoToggleResult(updatedTodo, updatedState)
    }

    private fun uncompleteTodo(
        todo: Todo, state: CharacterState, finalXP: Int, finalGold: Int
    ): TodoToggleResult {
        var combatXPApplied = state.combatXP - finalXP
        var currentCombatLevel = state.combatLevel
        while (combatXPApplied < 0 && currentCombatLevel > 1) {
            currentCombatLevel -= 1
            val requirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
            combatXPApplied += requirement
        }
        if (combatXPApplied < 0) combatXPApplied = 0

        val updatedTodo = todo.copy(completed = false, completedAt = null)
        val updatedState = state.copy(
            gold = max(0, state.gold - finalGold),
            totalGoldEarned = max(0, state.totalGoldEarned - finalGold),
            totalXP = max(0, state.totalXP - finalXP),
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied
            // hp: propositalmente NÃO tocado, mesma fidelidade do Daily
        )
        return TodoToggleResult(updatedTodo, updatedState)
    }
}
