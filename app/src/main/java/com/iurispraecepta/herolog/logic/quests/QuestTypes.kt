package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.model.Difficulty

data class DifficultyRewards(
    val xp: Int,
    val gold: Int,
    val damage: Int
)

fun getDifficultyRewards(difficulty: Difficulty): DifficultyRewards = when (difficulty) {
    Difficulty.Trivial -> DifficultyRewards(xp = 4, gold = 2, damage = 1)
    Difficulty.Easy -> DifficultyRewards(xp = 12, gold = 6, damage = 3)
    Difficulty.Medium -> DifficultyRewards(xp = 28, gold = 14, damage = 7)
    Difficulty.Hard -> DifficultyRewards(xp = 60, gold = 25, damage = 15)
}

