package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.ChecklistItem
import com.iurispraecepta.herolog.logic.quests.Daily
import com.iurispraecepta.herolog.logic.quests.Difficulty
import com.iurispraecepta.herolog.logic.quests.DifficultyRewards
import com.iurispraecepta.herolog.logic.quests.Habit
import com.iurispraecepta.herolog.logic.quests.RepeatFrequency
import com.iurispraecepta.herolog.logic.quests.Todo
import com.iurispraecepta.herolog.logic.quests.getDifficultyRewards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestTypesTest {

    @Test
    fun getDifficultyRewards_trivial_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.TRIVIAL)
        assertEquals(DifficultyRewards(xp = 4, gold = 2, damage = 1), rewards)
    }

    @Test
    fun getDifficultyRewards_easy_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.EASY)
        assertEquals(DifficultyRewards(xp = 12, gold = 6, damage = 3), rewards)
    }

    @Test
    fun getDifficultyRewards_medium_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.MEDIUM)
        assertEquals(DifficultyRewards(xp = 28, gold = 14, damage = 7), rewards)
    }

    @Test
    fun getDifficultyRewards_hard_returnsCorrectValues() {
        val rewards = getDifficultyRewards(Difficulty.HARD)
        assertEquals(DifficultyRewards(xp = 60, gold = 25, damage = 15), rewards)
    }

    @Test
    fun habit_defaultValues_areSetCorrectly() {
        val habit = Habit(
            id = "h-123456",
            title = "Drink Water",
            notes = "Stay hydrated",
            up = true,
            down = false,
            difficulty = Difficulty.EASY
        )

        assertEquals("h-123456", habit.id)
        assertEquals("Drink Water", habit.title)
        assertEquals("Stay hydrated", habit.notes)
        assertTrue(habit.up)
        assertFalse(habit.down)
        assertEquals(Difficulty.EASY, habit.difficulty)
        assertEquals(0, habit.upCount)
        assertEquals(0, habit.downCount)
        assertEquals(0, habit.streak)
        assertTrue(habit.tags.isEmpty())
        assertNull(habit.lastTriggeredDate)
    }

    @Test
    fun daily_defaultValues_areSetCorrectly() {
        val daily = Daily(
            id = "d-123456",
            title = "Morning Workout",
            notes = "Stretch and cardio",
            difficulty = Difficulty.MEDIUM,
            repeats = RepeatFrequency.DAILY,
            every = 1
        )

        assertEquals("d-123456", daily.id)
        assertEquals("Morning Workout", daily.title)
        assertEquals("Stretch and cardio", daily.notes)
        assertEquals(Difficulty.MEDIUM, daily.difficulty)
        assertFalse(daily.completed)
        assertEquals(0, daily.streak)
        assertEquals(RepeatFrequency.DAILY, daily.repeats)
        assertEquals(1, daily.every)
        assertTrue(daily.tags.isEmpty())
        assertTrue(daily.checklist.isEmpty())
        assertNull(daily.value)
        assertNull(daily.createdAt)
    }

    @Test
    fun todo_defaultValues_areSetCorrectly() {
        val todo = Todo(
            id = "t-123456",
            title = "File Taxes",
            notes = "Deadline coming up",
            difficulty = Difficulty.HARD
        )

        assertEquals("t-123456", todo.id)
        assertEquals("File Taxes", todo.title)
        assertEquals("Deadline coming up", todo.notes)
        assertEquals(Difficulty.HARD, todo.difficulty)
        assertFalse(todo.completed)
        assertTrue(todo.tags.isEmpty())
        assertTrue(todo.checklist.isEmpty())
        assertNull(todo.createdAt)
        assertNull(todo.completedAt)
    }

    @Test
    fun checklistItem_defaultValues_areSetCorrectly() {
        val item = ChecklistItem(
            id = "c-1",
            text = "Subtask 1"
        )

        assertEquals("c-1", item.id)
        assertEquals("Subtask 1", item.text)
        assertFalse(item.completed)
    }
}
