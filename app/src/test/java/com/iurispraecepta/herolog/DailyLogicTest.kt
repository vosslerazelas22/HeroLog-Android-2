package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.quests.ChecklistItem
import com.iurispraecepta.herolog.logic.quests.Daily
import com.iurispraecepta.herolog.logic.quests.DailyLogic
import com.iurispraecepta.herolog.logic.quests.Difficulty
import com.iurispraecepta.herolog.logic.quests.RepeatFrequency
import com.iurispraecepta.herolog.model.CharClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class DailyLogicTest {

    private val testDate = Date()

    private fun sampleDaily(
        difficulty: Difficulty = Difficulty.MEDIUM,
        completed: Boolean = false,
        streak: Int = 0,
        value: Int? = null,
        checklist: List<ChecklistItem> = emptyList()
    ) = Daily(
        id = "d-test-1",
        title = "Estudar Código",
        notes = "Notas diárias",
        difficulty = difficulty,
        completed = completed,
        streak = streak,
        repeats = RepeatFrequency.DAILY,
        every = 1,
        checklist = checklist,
        value = value
    )

    @Test
    fun toggle_completing_incrementsStreakAndValue_appliesRewards() {
        // Medium rewards: XP = 28, Gold = 14 (Ranger has no bonus)
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 50,
            combatLevel = 1,
            combatXP = 0
        )
        val daily = sampleDaily(streak = 2, value = 2, completed = false)

        val result = DailyLogic.toggle(daily, state)

        assertTrue(result.updatedDaily.completed)
        assertEquals(3, result.updatedDaily.streak)
        assertEquals(3, result.updatedDaily.value)
        assertEquals(114, result.updatedState.gold)
        assertEquals(114, result.updatedState.totalGoldEarned)
        assertEquals(78, result.updatedState.totalXP)
        assertEquals(28, result.updatedState.combatXP)
    }

    @Test
    fun toggle_uncompleting_decrementsStreakClampedAtZero_andValueUnclamped() {
        // Streak clampa em 0, mas value pode ser negativo (não clampa)
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 100,
            combatLevel = 1,
            combatXP = 50
        )
        val daily = sampleDaily(streak = 0, value = 0, completed = true)

        val result = DailyLogic.toggle(daily, state)

        assertFalse(result.updatedDaily.completed)
        assertEquals(0, result.updatedDaily.streak)
        assertEquals(-1, result.updatedDaily.value)
    }

    @Test
    fun toggle_completing_mageClass_appliesXpBonus() {
        // Medium rewards: XP 28 -> Mage receives floor(28 * 1.2) = 33 XP
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Mage,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0
        )
        val daily = sampleDaily(difficulty = Difficulty.MEDIUM, completed = false)

        val result = DailyLogic.toggle(daily, state)

        assertEquals(33, result.updatedState.totalXP)
        assertEquals(33, result.updatedState.combatXP)
        assertEquals(114, result.updatedState.gold)
    }

    @Test
    fun toggle_completing_warriorClass_appliesGoldBonus() {
        // Medium rewards: Gold 14 -> Warrior receives floor(14 * 1.2) = 16 Gold
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Warrior,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0
        )
        val daily = sampleDaily(difficulty = Difficulty.MEDIUM, completed = false)

        val result = DailyLogic.toggle(daily, state)

        assertEquals(28, result.updatedState.totalXP)
        assertEquals(116, result.updatedState.gold)
        assertEquals(116, result.updatedState.totalGoldEarned)
    }

    @Test
    fun toggle_completing_levelUp_restoresHpToMax() {
        // Combat Level 1 requires 100 XP. Current combatXP = 80.
        // Hard rewards: 60 XP. 80 + 60 = 140 >= 100 -> Level 2 (remaining 40 XP).
        // HP is at 10/50 -> on level up it should be restored to 50.
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            combatLevel = 1,
            combatXP = 80,
            hp = 10,
            maxHp = 50
        )
        val daily = sampleDaily(difficulty = Difficulty.HARD, completed = false)

        val result = DailyLogic.toggle(daily, state)

        assertEquals(2, result.updatedState.combatLevel)
        assertEquals(40, result.updatedState.combatXP)
        assertEquals(50, result.updatedState.hp)
    }

    @Test
    fun toggle_uncompleting_neverModifiesHp_evenOnLevelDown() {
        // Level down: combatLevel 2, combatXP 10. Uncompleting Medium (28 XP) causes combatXP to become -18.
        // Level down to 1: combatXP becomes -18 + 100 = 82.
        // HP is at 25/50 -> HP must NOT be touched.
        val state = createInitialCharacterState(testDate).copy(
            charClass = CharClass.Ranger,
            combatLevel = 2,
            combatXP = 10,
            hp = 25,
            maxHp = 50
        )
        val daily = sampleDaily(difficulty = Difficulty.MEDIUM, completed = true)

        val result = DailyLogic.toggle(daily, state)

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
        val daily = sampleDaily(difficulty = Difficulty.HARD, completed = true) // Hard: XP 60, Gold 25

        val result = DailyLogic.toggle(daily, state)

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
        val daily = sampleDaily(difficulty = Difficulty.MEDIUM, completed = false) // Medium: XP 28

        val result = DailyLogic.toggle(daily, state)

        assertEquals(28, result.updatedState.combatXP)
    }

    @Test
    fun toggleChecklistItem_flipsCompletedState_withoutAffectingRewards() {
        val checklist = listOf(
            ChecklistItem(id = "item-1", text = "Subtarefa 1", completed = false),
            ChecklistItem(id = "item-2", text = "Subtarefa 2", completed = true)
        )
        val daily = sampleDaily(checklist = checklist)

        val updated = DailyLogic.toggleChecklistItem(daily, "item-1")

        assertTrue(updated.checklist[0].completed)
        assertTrue(updated.checklist[1].completed)

        val toggledBack = DailyLogic.toggleChecklistItem(updated, "item-1")
        assertFalse(toggledBack.checklist[0].completed)
    }
}
