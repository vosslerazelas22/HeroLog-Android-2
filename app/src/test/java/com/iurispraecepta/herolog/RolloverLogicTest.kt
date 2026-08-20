package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.DailySchedulingLogic
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.logic.quests.RolloverLogic
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.RepeatInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RolloverLogicTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val jsDateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.US)

    private fun sampleShieldItem(id: String = "shield-1") = InventoryItem(
        id = id,
        name = "Streak Shield",
        emoji = "🛡️",
        buff = BuffType.StreakShield,
        price = 100,
        desc = "Protects your streak"
    )

    private fun sampleDaily(
        id: String = "daily-1",
        difficulty: Difficulty = Difficulty.Medium,
        completed: Boolean = false,
        streak: Int = 3,
        value: Int? = 5,
        repeats: RepeatInterval = RepeatInterval.Daily,
        every: Int = 1,
        createdAt: String? = "2026-08-01"
    ) = Daily(
        id = id,
        title = "Estudar Kotlin",
        notes = "Foco diário",
        difficulty = difficulty,
        completed = completed,
        streak = streak,
        repeats = repeats,
        every = every,
        tags = emptyList(),
        checklist = emptyList(),
        value = value,
        createdAt = createdAt
    )

    private fun sampleState(
        todayDate: String = "Wed Aug 05 2026",
        lastStudyDate: String? = "Wed Aug 05 2026",
        streak: Int = 5,
        hp: Int = 50,
        maxHp: Int = 50,
        charClass: CharClass = CharClass.Mage,
        inventory: List<InventoryItem> = emptyList(),
        achievements: List<String> = emptyList(),
        hasClaimedLogin: Boolean = true
    ) = CharacterState(
        gold = 100,
        totalXP = 500,
        totalGoldEarned = 100,
        totalSessions = 10,
        totalMinutes = 250,
        combatLevel = 2,
        combatXP = 50,
        skills = emptyList(),
        history = emptyList(),
        inventory = inventory,
        streak = streak,
        bestStreak = maxOf(streak, 10),
        lastStudyDate = lastStudyDate,
        wildernessWins = 0,
        combo = 0,
        dungeonProgress = 0,
        isDungeonMode = false,
        dungeonSessions = 0,
        achievements = achievements,
        charName = "Hero",
        charClass = charClass,
        todayXP = 120,
        todayMinutes = 50,
        todayDate = todayDate,
        hasClaimedLogin = hasClaimedLogin,
        hp = hp,
        maxHp = maxHp,
        habits = emptyList(),
        dailies = emptyList(),
        todos = emptyList(),
        pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
    )

    @Test
    fun rollover_whenTodayDateIsSameDay_doesNothingAndReturnsUnmodifiedState() {
        val refDate = jsDateFormat.parse("Wed Aug 05 2026")!!
        val state = sampleState(todayDate = "Wed Aug 05 2026", streak = 5, hp = 45)
        val daily = sampleDaily(completed = false)

        val result = RolloverLogic.applyRollover(state, listOf(daily), refDate)

        assertEquals(state, result.updatedState)
        assertEquals(listOf(daily), result.updatedDailies)
        assertEquals(0, result.missedCount)
        assertFalse(result.shieldConsumed)
        assertFalse(result.died)
    }

    @Test
    fun rollover_whenDiffDaysIsOne_maintainsGlobalStreak() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!! // 1 dia de diferenca
        val state = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr, streak = 7)
        val daily = sampleDaily(completed = true) // Daily completa, sem faltas

        val result = RolloverLogic.applyRollover(state, listOf(daily), refDate)

        assertEquals(7, result.updatedState.streak)
        assertFalse(result.shieldConsumed)
        assertEquals(0, result.missedCount)
    }

    @Test
    fun rollover_whenDiffDaysGreaterThanOne_withoutShield_missedCountZero_resetsStreakWithoutDamage() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Fri Aug 07 2026")!! // 2 dias de diferenca (diffDays = 2)
        val state = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr, streak = 10, hp = 50)
        val daily = sampleDaily(completed = true) // Sem faltas

        val result = RolloverLogic.applyRollover(state, listOf(daily), refDate)

        assertEquals(0, result.updatedState.streak)
        assertEquals(50, result.updatedState.hp)
        assertFalse(result.shieldConsumed)
        assertEquals(0, result.missedCount)
        assertFalse(result.died)
    }

    @Test
    fun rollover_whenDiffDaysGreaterThanOne_withShield_missedCountZero_protectsStreakAndConsumesShield() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Fri Aug 07 2026")!! // 2 dias de diferenca
        val state = sampleState(
            todayDate = lastDateStr,
            lastStudyDate = lastDateStr,
            streak = 10,
            hp = 50,
            inventory = listOf(sampleShieldItem())
        )
        val daily = sampleDaily(completed = true)

        val result = RolloverLogic.applyRollover(state, listOf(daily), refDate)

        assertEquals(10, result.updatedState.streak) // Streak protegida!
        assertEquals(50, result.updatedState.hp)
        assertTrue(result.shieldConsumed)
        assertTrue(result.updatedState.inventory.isEmpty()) // Consumiu do inventario
        assertEquals(0, result.missedCount)
    }

    @Test
    fun rollover_whenDiffDaysLessThanOrEqualToOne_missedCountPositive_withShield_protectsDamageAndDailyStreak() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!! // 1 dia de diferenca (streak segura)
        val state = sampleState(
            todayDate = lastDateStr,
            lastStudyDate = lastDateStr,
            streak = 5,
            hp = 50,
            inventory = listOf(sampleShieldItem())
        )
        val missedDaily = sampleDaily(
            difficulty = Difficulty.Hard, // 15 de dano
            completed = false,
            streak = 4,
            value = 2,
            createdAt = "2026-08-01"
        )

        val result = RolloverLogic.applyRollover(state, listOf(missedDaily), refDate)

        assertTrue(result.shieldConsumed)
        assertEquals(1, result.missedCount)
        assertEquals(5, result.updatedState.streak) // Streak global mantida
        assertEquals(50, result.updatedState.hp) // Dano de HP protegido!
        val updatedDaily = result.updatedDailies.first()
        assertEquals(4, updatedDaily.streak) // Daily.streak NAO zerou!
        assertEquals(2, updatedDaily.value) // Daily.value NAO decrementou!
        assertFalse(updatedDaily.completed)
    }

    @Test
    fun rollover_whenDiffDaysGreaterThanOneAndMissedCountPositive_withOneShield_protectsAllThreeAndConsumesOnlyOneShield() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Sat Aug 08 2026")!! // 3 dias de diferenca
        val shield1 = sampleShieldItem("shield-1")
        val shield2 = sampleShieldItem("shield-2")
        val state = sampleState(
            todayDate = lastDateStr,
            lastStudyDate = lastDateStr,
            streak = 15,
            hp = 50,
            inventory = listOf(shield1, shield2)
        )
        val missedDaily = sampleDaily(
            difficulty = Difficulty.Medium,
            completed = false,
            streak = 6,
            value = 4,
            createdAt = "2026-08-01"
        )

        val result = RolloverLogic.applyRollover(state, listOf(missedDaily), refDate)

        assertTrue(result.shieldConsumed)
        assertEquals(1, result.missedCount)
        assertEquals(15, result.updatedState.streak) // 1. Streak global mantida
        assertEquals(50, result.updatedState.hp) // 2. Dano nao aplicado
        val updatedDaily = result.updatedDailies.first()
        assertEquals(6, updatedDaily.streak) // 3. Daily.streak mantida
        assertEquals(listOf(shield2), result.updatedState.inventory) // Consumiu exatamente 1 shield
    }

    @Test
    fun rollover_whenDiffDaysGreaterThanOneAndMissedCountPositive_withoutShield_penalizesAllThree() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Sat Aug 08 2026")!! // 3 dias de diferenca
        val state = sampleState(
            todayDate = lastDateStr,
            lastStudyDate = lastDateStr,
            streak = 15,
            hp = 50,
            charClass = CharClass.Mage,
            inventory = emptyList()
        )
        val missedDaily = sampleDaily(
            difficulty = Difficulty.Medium, // 7 de dano
            completed = false,
            streak = 6,
            value = 4,
            createdAt = "2026-08-01"
        )

        val result = RolloverLogic.applyRollover(state, listOf(missedDaily), refDate)

        assertFalse(result.shieldConsumed)
        assertEquals(1, result.missedCount)
        assertEquals(0, result.updatedState.streak) // 1. Streak global zerada
        assertEquals(43, result.updatedState.hp) // 2. Dano aplicado: 50 - 7 = 43
        val updatedDaily = result.updatedDailies.first()
        assertEquals(0, updatedDaily.streak) // 3. Daily.streak zerada
        assertEquals(3, updatedDaily.value) // Daily.value decrementado (4 - 1 = 3)
    }

    @Test
    fun rollover_completedDaily_alwaysBecomesUncompletedWithOrWithoutShield() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!!
        val state = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr)
        val completedDaily = sampleDaily(completed = true, streak = 5, value = 10)

        val result = RolloverLogic.applyRollover(state, listOf(completedDaily), refDate)

        val updated = result.updatedDailies.first()
        assertFalse(updated.completed)
        assertEquals(5, updated.streak)
        assertEquals(10, updated.value)
    }

    @Test
    fun rollover_uncompletedDailyNotScheduled_hasNoPenaltyEvenWithoutShield() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!! // diffDays = 1
        val state = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr, hp = 50)
        // Daily criada em 2026-08-05 com repeat every 2 -> no dia 2026-08-06 (diff=1) nao esta agendada
        val unscheduledDaily = sampleDaily(
            repeats = RepeatInterval.Daily,
            every = 2,
            createdAt = "2026-08-05",
            completed = false,
            streak = 2,
            value = 3
        )

        val result = RolloverLogic.applyRollover(state, listOf(unscheduledDaily), refDate)

        assertEquals(0, result.missedCount)
        assertEquals(50, result.updatedState.hp)
        val updated = result.updatedDailies.first()
        assertEquals(2, updated.streak)
        assertEquals(3, updated.value)
    }

    @Test
    fun rollover_damageCalculation_rangerGetsThirtyPercentReductionWithMinOne() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!!
        // Ranger com Trivial (1 de dano) -> floor(1 * 0.7) = 0 -> max(1, 0) = 1
        val rangerState = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr, hp = 50, charClass = CharClass.Ranger)
        val trivialDaily = sampleDaily(difficulty = Difficulty.Trivial, completed = false, createdAt = "2026-08-01")

        val resultTrivial = RolloverLogic.applyRollover(rangerState, listOf(trivialDaily), refDate)
        assertEquals(49, resultTrivial.updatedState.hp) // 50 - 1 = 49

        // Ranger com Hard (15 de dano) -> floor(15 * 0.7) = 10
        val hardDaily = sampleDaily(difficulty = Difficulty.Hard, completed = false, createdAt = "2026-08-01")
        val resultHard = RolloverLogic.applyRollover(rangerState, listOf(hardDaily), refDate)
        assertEquals(40, resultHard.updatedState.hp) // 50 - 10 = 40

        // Warrior / Mage sem reducao: 50 - 15 = 35
        val warriorState = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr, hp = 50, charClass = CharClass.Warrior)
        val resultWarrior = RolloverLogic.applyRollover(warriorState, listOf(hardDaily), refDate)
        assertEquals(35, resultWarrior.updatedState.hp)
    }

    @Test
    fun rollover_whenFinalHpReachesZero_diedIsTrue() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!!
        val lowHpState = sampleState(todayDate = lastDateStr, lastStudyDate = lastDateStr, hp = 5, charClass = CharClass.Warrior)
        val hardDaily = sampleDaily(difficulty = Difficulty.Hard, completed = false, createdAt = "2026-08-01") // 15 dano

        val result = RolloverLogic.applyRollover(lowHpState, listOf(hardDaily), refDate)

        assertEquals(0, result.updatedState.hp)
        assertTrue(result.died)
    }

    @Test
    fun rollover_achievements_filtersOutClaimedDailyTagsAndPreservesOthers() {
        val lastDateStr = "Wed Aug 05 2026"
        val refDate = jsDateFormat.parse("Thu Aug 06 2026")!!
        val initialAchievements = listOf(
            "first_focus_session",
            "claimed_daily_quest_1_Wed Aug 05 2026",
            "claimed_daily_quest_2",
            "reach_level_5",
            "claimed_boss_win"
        )
        val state = sampleState(
            todayDate = lastDateStr,
            lastStudyDate = lastDateStr,
            achievements = initialAchievements
        )

        val result = RolloverLogic.applyRollover(state, emptyList(), refDate)

        assertEquals(
            listOf("first_focus_session", "reach_level_5", "claimed_boss_win"),
            result.updatedState.achievements
        )
        assertEquals("Thu Aug 06 2026", result.updatedState.todayDate)
        assertEquals(0, result.updatedState.todayMinutes)
        assertEquals(0, result.updatedState.todayXP)
        assertFalse(result.updatedState.hasClaimedLogin)
    }
}
