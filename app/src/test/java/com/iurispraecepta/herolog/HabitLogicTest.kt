package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.quests.HabitLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class HabitLogicTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateDay1 = dateFormat.parse("2026-08-18")!!
    private val dateDay2 = dateFormat.parse("2026-08-19")!!

    private fun sampleHabit(
        difficulty: Difficulty = Difficulty.Medium,
        upCount: Int = 0,
        downCount: Int = 0,
        streak: Int = 0,
        lastTriggeredDate: String? = null,
        lastUpDate: String? = null,
        lastDownDate: String? = null
    ) = Habit(
        id = "h-test-1",
        title = "Exercício",
        notes = "Fazer caminhada",
        up = true,
        down = true,
        difficulty = difficulty,
        upCount = upCount,
        downCount = downCount,
        streak = streak,
        tags = emptyList(),
        lastTriggeredDate = lastTriggeredDate,
        lastUpDate = lastUpDate,
        lastDownDate = lastDownDate
    )

    @Test
    fun triggerUp_incrementsStreakAndUpCount_evenOnMultipleTriggersSameDay() {
        val initialState = createInitialCharacterState(dateDay1).copy(charClass = CharClass.Ranger)
        val habit = sampleHabit(upCount = 1, streak = 5, lastTriggeredDate = "2026-08-01")

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(2, result.updatedHabit.upCount)
        assertEquals(6, result.updatedHabit.streak)
    }

    // ---------------------------------------------------------------------
    // SPEC-Rebranding item 4: datas de "hoje" por botão (+ e − independentes)
    // ---------------------------------------------------------------------

    @Test
    fun triggerUp_writesOnlyLastUpDate_andKeepsLegacyFieldUntouched() {
        val initialState = createInitialCharacterState(dateDay1)
        val habit = sampleHabit(lastTriggeredDate = "2026-08-01")

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals("2026-08-18", result.updatedHabit.lastUpDate)
        assertNull(result.updatedHabit.lastDownDate)
        // lastTriggeredDate é legado de save: não é mais gravada nem relida
        assertEquals("2026-08-01", result.updatedHabit.lastTriggeredDate)
    }

    @Test
    fun triggerDown_writesOnlyLastDownDate_andDoesNotTouchUpDate() {
        val initialState = createInitialCharacterState(dateDay1).copy(hp = 50)
        val habit = sampleHabit(lastTriggeredDate = "2026-08-01", lastUpDate = "2026-08-18")

        val result = HabitLogic.trigger(habit, initialState, isUp = false, referenceDate = dateDay1)

        assertEquals("2026-08-18", result.updatedHabit.lastDownDate)
        assertEquals("2026-08-18", result.updatedHabit.lastUpDate)
        assertEquals("2026-08-01", result.updatedHabit.lastTriggeredDate)
    }

    @Test
    fun triggerUpAndDown_sameDay_keepsBothDates_andBothAreToday() {
        val initialState = createInitialCharacterState(dateDay1).copy(hp = 50)
        val habit = sampleHabit()

        val afterDown = HabitLogic.trigger(habit, initialState, isUp = false, referenceDate = dateDay1).updatedHabit
        val afterBoth = HabitLogic.trigger(afterDown, initialState, isUp = true, referenceDate = dateDay1).updatedHabit

        assertEquals("2026-08-18", afterBoth.lastDownDate)
        assertEquals("2026-08-18", afterBoth.lastUpDate)
        // Os dois botões ficam acesos de forma independente no mesmo dia
        assertTrue(afterBoth.isUpToday("2026-08-18"))
        assertTrue(afterBoth.isDownToday("2026-08-18"))
    }

    @Test
    fun nextDay_comparison_clearsBothMarkers_withoutMutatingDates() {
        val initialState = createInitialCharacterState(dateDay1).copy(hp = 50)
        val afterUp = HabitLogic.trigger(sampleHabit(), initialState, isUp = true, referenceDate = dateDay1).updatedHabit
        val afterBoth = HabitLogic.trigger(afterUp, initialState, isUp = false, referenceDate = dateDay1).updatedHabit

        // Dia seguinte: a COMPARAÇÃO zera os dois (os campos continuam na data antiga)
        assertFalse(afterBoth.isUpToday("2026-08-19"))
        assertFalse(afterBoth.isDownToday("2026-08-19"))
        assertEquals("2026-08-18", afterBoth.lastUpDate)
        assertEquals("2026-08-18", afterBoth.lastDownDate)
        // E no dia da marcação continuam acesos
        assertTrue(afterBoth.isUpToday("2026-08-18"))
        assertTrue(afterBoth.isDownToday("2026-08-18"))
    }

    @Test
    fun neverTriggered_bothDatesNull_markersNeverLit() {
        val initialState = createInitialCharacterState(dateDay1)
        val habit = sampleHabit()

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertNull(result.updatedHabit.lastDownDate)
        assertFalse(result.updatedHabit.isDownToday("2026-08-18"))
    }

    @Test
    fun triggerUp_mageClass_appliesXpBonus() {
        // Medium rewards: xp = 28, gold = 14. Mage receives xp * 1.2 = floor(28 * 1.2) = 33. Gold remains 14.
        val initialState = createInitialCharacterState(dateDay1).copy(
            charClass = CharClass.Mage,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0
        )
        val habit = sampleHabit(difficulty = Difficulty.Medium)

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(33, result.updatedState.totalXP)
        assertEquals(33, result.updatedState.combatXP)
        assertEquals(114, result.updatedState.gold)
        assertEquals(114, result.updatedState.totalGoldEarned)
    }

    @Test
    fun triggerUp_warriorClass_appliesGoldBonus() {
        // Medium rewards: xp = 28, gold = 14. Warrior receives gold * 1.2 = floor(14 * 1.2) = 16. XP remains 28.
        val initialState = createInitialCharacterState(dateDay1).copy(
            charClass = CharClass.Warrior,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0
        )
        val habit = sampleHabit(difficulty = Difficulty.Medium)

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(28, result.updatedState.totalXP)
        assertEquals(116, result.updatedState.gold)
        assertEquals(116, result.updatedState.totalGoldEarned)
    }

    @Test
    fun triggerUp_nonBonusClass_appliesBaseValues() {
        // Ranger has no XP/Gold bonus on up: receives base xp = 28, base gold = 14.
        val initialState = createInitialCharacterState(dateDay1).copy(
            charClass = CharClass.Ranger,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0
        )
        val habit = sampleHabit(difficulty = Difficulty.Medium)

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(28, result.updatedState.totalXP)
        assertEquals(114, result.updatedState.gold)
        assertEquals(114, result.updatedState.totalGoldEarned)
    }

    @Test
    fun triggerUp_appliesFullCombatXp_100Percent() {
        // Combat level 1 requires 100 XP.
        // Hard rewards: base XP = 60. Full 100% applied = 60 XP.
        // 90 + 60 = 150 >= 100 -> Combat Level 2, remaining combatXP = 50.
        val initialState = createInitialCharacterState(dateDay1).copy(
            charClass = CharClass.Ranger,
            combatLevel = 1,
            combatXP = 90
        )
        val habit = sampleHabit(difficulty = Difficulty.Hard)

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(2, result.updatedState.combatLevel)
        assertEquals(50, result.updatedState.combatXP)
    }

    @Test
    fun triggerUp_levelUp_restoresHpToMax() {
        // HP is 10/50. Leveling up restores HP to 50.
        val initialState = createInitialCharacterState(dateDay1).copy(
            charClass = CharClass.Ranger,
            combatLevel = 1,
            combatXP = 90,
            hp = 10,
            maxHp = 50
        )
        val habit = sampleHabit(difficulty = Difficulty.Hard) // 60 XP -> level up

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(2, result.updatedState.combatLevel)
        assertEquals(50, result.updatedState.hp)
    }

    @Test
    fun triggerUp_noLevelUp_preservesCurrentHp() {
        val initialState = createInitialCharacterState(dateDay1).copy(
            charClass = CharClass.Ranger,
            combatLevel = 1,
            combatXP = 0,
            hp = 25,
            maxHp = 50
        )
        val habit = sampleHabit(difficulty = Difficulty.Medium) // 28 XP -> no level up

        val result = HabitLogic.trigger(habit, initialState, isUp = true, referenceDate = dateDay1)

        assertEquals(1, result.updatedState.combatLevel)
        assertEquals(25, result.updatedState.hp)
    }

    @Test
    fun triggerDown_decrementsStreakClampedAtZero() {
        val initialState = createInitialCharacterState(dateDay1).copy(hp = 50)
        val habitWithStreak = sampleHabit(downCount = 3, streak = 7)

        val resultWithStreak = HabitLogic.trigger(habitWithStreak, initialState, isUp = false, referenceDate = dateDay1)
        assertEquals(4, resultWithStreak.updatedHabit.downCount)
        assertEquals(6, resultWithStreak.updatedHabit.streak)

        val habitZeroStreak = sampleHabit(downCount = 3, streak = 0)
        val resultZeroStreak = HabitLogic.trigger(habitZeroStreak, initialState, isUp = false, referenceDate = dateDay1)
        assertEquals(4, resultZeroStreak.updatedHabit.downCount)
        assertEquals(0, resultZeroStreak.updatedHabit.streak)
    }

    @Test
    fun triggerDown_rangerClass_appliesDamageReduction_roundedDownMinimumOne() {
        // Medium damage = 7. Ranger receives floor(7 * 0.7) = 4. HP 50 -> 46.
        val rangerState = createInitialCharacterState(dateDay1).copy(charClass = CharClass.Ranger, hp = 50)
        val habitMedium = sampleHabit(difficulty = Difficulty.Medium)

        val resultMedium = HabitLogic.trigger(habitMedium, rangerState, isUp = false, referenceDate = dateDay1)
        assertEquals(46, resultMedium.updatedState.hp)

        // Trivial damage = 1. Ranger receives max(1, floor(1 * 0.7)) = max(1, 0) = 1. HP 50 -> 49.
        val habitTrivial = sampleHabit(difficulty = Difficulty.Trivial)
        val resultTrivial = HabitLogic.trigger(habitTrivial, rangerState, isUp = false, referenceDate = dateDay1)
        assertEquals(49, resultTrivial.updatedState.hp)
    }

    @Test
    fun triggerDown_nonRangerClass_appliesBaseDamage() {
        // Medium damage = 7. Warrior receives full 7 damage. HP 50 -> 43.
        val warriorState = createInitialCharacterState(dateDay1).copy(charClass = CharClass.Warrior, hp = 50)
        val habitMedium = sampleHabit(difficulty = Difficulty.Medium)

        val result = HabitLogic.trigger(habitMedium, warriorState, isUp = false, referenceDate = dateDay1)
        assertEquals(43, result.updatedState.hp)
    }

    @Test
    fun triggerDown_fatalDamage_setsIsPlayerDeadToTrue() {
        val lowHpState = createInitialCharacterState(dateDay1).copy(charClass = CharClass.Warrior, hp = 5, isPlayerDead = false)
        val habitHard = sampleHabit(difficulty = Difficulty.Hard) // Hard damage = 15 -> hp 0 -> isPlayerDead true

        val result = HabitLogic.trigger(habitHard, lowHpState, isUp = false, referenceDate = dateDay1)
        assertEquals(0, result.updatedState.hp)
        org.junit.Assert.assertTrue(result.updatedState.isPlayerDead)
    }

    @Test
    fun triggerDown_nonFatalDamage_preservesIsPlayerDeadFalse() {
        val healthyState = createInitialCharacterState(dateDay1).copy(charClass = CharClass.Warrior, hp = 50, isPlayerDead = false)
        val habitMedium = sampleHabit(difficulty = Difficulty.Medium) // Medium damage = 7 -> hp 43 -> isPlayerDead false

        val result = HabitLogic.trigger(habitMedium, healthyState, isUp = false, referenceDate = dateDay1)
        assertEquals(43, result.updatedState.hp)
        org.junit.Assert.assertFalse(result.updatedState.isPlayerDead)
    }

    @Test
    fun triggerDown_hpNeverGoesBelowZero() {
        val lowHpState = createInitialCharacterState(dateDay1).copy(charClass = CharClass.Warrior, hp = 5)
        val habitHard = sampleHabit(difficulty = Difficulty.Hard) // Hard damage = 15

        val result = HabitLogic.trigger(habitHard, lowHpState, isUp = false, referenceDate = dateDay1)
        assertEquals(0, result.updatedState.hp)
    }

    @Test
    fun triggerDown_doesNotModifyXpGoldOrCombatLevel() {
        val state = createInitialCharacterState(dateDay1).copy(
            gold = 500,
            totalGoldEarned = 1000,
            totalXP = 850,
            combatLevel = 3,
            combatXP = 45,
            hp = 50
        )
        val habit = sampleHabit(difficulty = Difficulty.Medium)

        val result = HabitLogic.trigger(habit, state, isUp = false, referenceDate = dateDay1)

        assertEquals(500, result.updatedState.gold)
        assertEquals(1000, result.updatedState.totalGoldEarned)
        assertEquals(850, result.updatedState.totalXP)
        assertEquals(3, result.updatedState.combatLevel)
        assertEquals(45, result.updatedState.combatXP)
        assertEquals(43, result.updatedState.hp)
    }
}
