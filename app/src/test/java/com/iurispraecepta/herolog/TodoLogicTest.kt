package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.quests.Difficulty
import com.iurispraecepta.herolog.logic.quests.Todo
import com.iurispraecepta.herolog.logic.quests.TodoLogic
import com.iurispraecepta.herolog.model.CharClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoLogicTest {

    private val testDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse("2026-08-18 14:00:00")!!

    private fun sampleTodo(
        difficulty: Difficulty = Difficulty.MEDIUM,
        completed: Boolean = false,
        completedAt: String? = null
    ) = Todo(
        id = "t-test-1",
        title = "Finalizar Documento",
        notes = "Notas do afazer",
        difficulty = difficulty,
        completed = completed,
        completedAt = completedAt
    )

    @Test
    fun toggle_completing_setsCompletedAtTimestamp() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 50,
            combatLevel = 1,
            combatXP = 0
        )
        val todo = sampleTodo(completed = false, completedAt = null)

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertTrue(result.updatedTodo.completed)
        assertNotNull(result.updatedTodo.completedAt)
        assertEquals(testDate.toInstant().toString(), result.updatedTodo.completedAt)
        assertEquals(114, result.updatedState.gold)
        assertEquals(114, result.updatedState.totalGoldEarned)
        assertEquals(78, result.updatedState.totalXP)
        assertEquals(28, result.updatedState.combatXP)
    }

    @Test
    fun toggle_uncompleting_clearsCompletedAtToNull() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 100,
            combatLevel = 1,
            combatXP = 50
        )
        val todo = sampleTodo(completed = true, completedAt = "2026-08-18T14:00:00Z")

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertFalse(result.updatedTodo.completed)
        assertNull(result.updatedTodo.completedAt)
    }

    @Test
    fun toggle_completing_mageClass_appliesXpBonus() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Mage,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0
        )
        val todo = sampleTodo(difficulty = Difficulty.MEDIUM, completed = false)

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertEquals(33, result.updatedState.totalXP)
        assertEquals(33, result.updatedState.combatXP)
        assertEquals(114, result.updatedState.gold)
    }

    @Test
    fun toggle_completing_warriorClass_appliesGoldBonus() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Warrior,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0
        )
        val todo = sampleTodo(difficulty = Difficulty.MEDIUM, completed = false)

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertEquals(28, result.updatedState.totalXP)
        assertEquals(116, result.updatedState.gold)
        assertEquals(116, result.updatedState.totalGoldEarned)
    }

    @Test
    fun toggle_completing_levelUp_restoresHpToMax() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            combatLevel = 1,
            combatXP = 80,
            hp = 10,
            maxHp = 50
        )
        val todo = sampleTodo(difficulty = Difficulty.HARD, completed = false) // Hard: 60 XP -> 80+60=140 -> Level 2

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertEquals(2, result.updatedState.combatLevel)
        assertEquals(40, result.updatedState.combatXP)
        assertEquals(50, result.updatedState.hp)
    }

    @Test
    fun toggle_uncompleting_neverModifiesHp_evenOnLevelDown() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            combatLevel = 2,
            combatXP = 10,
            hp = 25,
            maxHp = 50
        )
        val todo = sampleTodo(difficulty = Difficulty.MEDIUM, completed = true) // Medium: 28 XP -> combatXP 10 - 28 = -18 -> Level 1 (82 XP)

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertEquals(1, result.updatedState.combatLevel)
        assertEquals(82, result.updatedState.combatXP)
        assertEquals(25, result.updatedState.hp)
    }

    @Test
    fun toggle_uncompleting_goldAndXpNeverGoBelowZero() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            gold = 5,
            totalGoldEarned = 5,
            totalXP = 10,
            combatLevel = 1,
            combatXP = 5
        )
        val todo = sampleTodo(difficulty = Difficulty.HARD, completed = true)

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertEquals(0, result.updatedState.gold)
        assertEquals(0, result.updatedState.totalGoldEarned)
        assertEquals(0, result.updatedState.totalXP)
        assertEquals(1, result.updatedState.combatLevel)
        assertEquals(0, result.updatedState.combatXP)
    }

    @Test
    fun toggle_combatXpAppliesFullAmount_notFortyPercent() {
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            combatLevel = 1,
            combatXP = 0
        )
        val todo = sampleTodo(difficulty = Difficulty.MEDIUM, completed = false) // Medium: 28 XP

        val result = TodoLogic.toggle(todo, state, referenceDate = testDate)

        assertEquals(28, result.updatedState.combatXP)
    }
}
