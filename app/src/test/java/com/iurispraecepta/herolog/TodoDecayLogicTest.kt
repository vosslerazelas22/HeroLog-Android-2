package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.TodoDecayLogic
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoDecayLogicTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateBase = dateFormat.parse("2026-08-01")!!

    private fun sampleTodo(
        completed: Boolean = false,
        createdAt: String? = "2026-08-01"
    ) = Todo(
        id = "todo-test-1",
        title = "Tarefa de Estudo",
        notes = "Notas",
        difficulty = Difficulty.Medium,
        completed = completed,
        tags = emptyList(),
        checklist = emptyList(),
        createdAt = createdAt
    )

    private fun addDays(date: Date, days: Int): Date {
        return Date(date.time + days * 86400000L)
    }

    @Test
    fun getTodoDecayValue_whenCompletedIsTrue_returnsZeroEvenWithLargeDiffDays() {
        val todo = sampleTodo(completed = true, createdAt = "2026-08-01")
        val currentDate = addDays(dateBase, 30) // 30 dias depois

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(0, decay)
    }

    @Test
    fun getTodoDecayValue_whenCreatedAtIsNull_returnsZero() {
        val todo = sampleTodo(completed = false, createdAt = null)
        val currentDate = addDays(dateBase, 10)

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(0, decay)
    }

    @Test
    fun getTodoDecayValue_whenDiffDaysIsZero_returnsZero() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01")
        val currentDate = dateFormat.parse("2026-08-01")!!

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(0, decay)
    }

    @Test
    fun getTodoDecayValue_whenDiffDaysIsOne_returnsZero() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01")
        val currentDate = dateFormat.parse("2026-08-02")!! // 1 dia

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(0, decay)
    }

    @Test
    fun getTodoDecayValue_whenDiffDaysIsTwo_returnsMinusOne() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01")
        val currentDate = dateFormat.parse("2026-08-03")!! // 2 dias

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(-1, decay)
    }

    @Test
    fun getTodoDecayValue_whenDiffDaysIsThree_returnsMinusOne() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01")
        val currentDate = dateFormat.parse("2026-08-04")!! // 3 dias -> floor(3/2) * -1 = -1

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(-1, decay)
    }

    @Test
    fun getTodoDecayValue_whenDiffDaysIsFour_returnsMinusTwo() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01")
        val currentDate = dateFormat.parse("2026-08-05")!! // 4 dias -> floor(4/2) * -1 = -2

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(-2, decay)
    }

    @Test
    fun getTodoDecayValue_whenDiffDaysIsLarge_clampsToFloorMinusTwenty() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01")
        val currentDate = addDays(dateBase, 60) // 60 dias -> floor(60/2) * -1 = -30 -> clamped to -20

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(-20, decay)
    }

    @Test
    fun getTodoDecayValue_whenCurrentDateIsBeforeCreatedAt_clampsDiffDaysToZero() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-10")
        val currentDate = dateFormat.parse("2026-08-05")!! // 5 dias antes de createdAt

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(0, decay)
    }

    @Test
    fun getTodoDecayValue_supportsIso8601InstantCreatedAtString() {
        val todo = sampleTodo(completed = false, createdAt = "2026-08-01T15:30:00Z")
        val currentDate = dateFormat.parse("2026-08-03")!! // 2 dias depois

        val decay = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
        assertEquals(-1, decay)
    }
}
