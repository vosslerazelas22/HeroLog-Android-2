package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.DifficultyRewards
import com.iurispraecepta.herolog.logic.quests.getDifficultyRewards
import com.iurispraecepta.herolog.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestTypesTest {

    @Test
    fun getDifficultyRewards_trivial_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.Trivial)
        assertEquals(DifficultyRewards(xp = 4, gold = 2, damage = 1), rewards)
    }

    @Test
    fun getDifficultyRewards_easy_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.Easy)
        assertEquals(DifficultyRewards(xp = 12, gold = 6, damage = 3), rewards)
    }

    @Test
    fun getDifficultyRewards_medium_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.Medium)
        assertEquals(DifficultyRewards(xp = 28, gold = 14, damage = 7), rewards)
    }

    @Test
    fun getDifficultyRewards_hard_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.Hard)
        assertEquals(DifficultyRewards(xp = 60, gold = 25, damage = 15), rewards)
    }
}

