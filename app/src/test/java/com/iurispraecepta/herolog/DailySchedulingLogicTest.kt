package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.DailySchedulingLogic
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.RepeatInterval
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailySchedulingLogicTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateBase = dateFormat.parse("2026-08-01")!!

    private fun sampleDaily(
        repeats: RepeatInterval = RepeatInterval.Daily,
        every: Int = 1,
        createdAt: String? = "2026-08-01"
    ) = Daily(
        id = "daily-test-1",
        title = "Revisar Flashcards",
        notes = "Notas",
        difficulty = Difficulty.Medium,
        completed = false,
        streak = 0,
        repeats = repeats,
        every = every,
        tags = emptyList(),
        checklist = emptyList(),
        createdAt = createdAt
    )

    private fun addDays(date: Date, days: Int): Date {
        return Date(date.time + days * 86400000L)
    }

    @Test
    fun wasDailyScheduledForDate_dailyRepeats_every1_sameDayAsCreatedAt_returnsTrue() {
        val daily = sampleDaily(repeats = RepeatInterval.Daily, every = 1, createdAt = "2026-08-01")
        val currentDate = dateFormat.parse("2026-08-01")!!

        val isScheduled = DailySchedulingLogic.wasDailyScheduledForDate(daily, currentDate)
        assertTrue(isScheduled)
    }

    @Test
    fun wasDailyScheduledForDate_dailyRepeats_every2_oddDiffDaysReturnsFalse_evenDiffDaysReturnsTrue() {
        val daily = sampleDaily(repeats = RepeatInterval.Daily, every = 2, createdAt = "2026-08-01")

        val day0 = dateFormat.parse("2026-08-01")!! // diffDays = 0 (even)
        val day1 = dateFormat.parse("2026-08-02")!! // diffDays = 1 (odd)
        val day2 = dateFormat.parse("2026-08-03")!! // diffDays = 2 (even)
        val day3 = dateFormat.parse("2026-08-04")!! // diffDays = 3 (odd)

        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, day0))
        assertFalse(DailySchedulingLogic.wasDailyScheduledForDate(daily, day1))
        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, day2))
        assertFalse(DailySchedulingLogic.wasDailyScheduledForDate(daily, day3))
    }

    @Test
    fun wasDailyScheduledForDate_dailyRepeats_every0_treatedAs1Fallback() {
        val daily = sampleDaily(repeats = RepeatInterval.Daily, every = 0, createdAt = "2026-08-01")

        val day1 = dateFormat.parse("2026-08-02")!! // diffDays = 1 % 1 == 0
        val day2 = dateFormat.parse("2026-08-03")!! // diffDays = 2 % 1 == 0

        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, day1))
        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, day2))
    }

    @Test
    fun wasDailyScheduledForDate_dailyRepeats_createdAtNull_returnsTrue() {
        val daily = sampleDaily(repeats = RepeatInterval.Daily, every = 2, createdAt = null)
        val anyDate = dateFormat.parse("2026-08-15")!!

        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, anyDate))
    }

    @Test
    fun wasDailyScheduledForDate_weeklyRepeats_returnsTrueUnconditionally() {
        val daily = sampleDaily(repeats = RepeatInterval.Weekly, every = 3, createdAt = "2026-08-01")
        val oddDay = dateFormat.parse("2026-08-02")!!

        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, oddDay))
    }

    @Test
    fun wasDailyScheduledForDate_monthlyRepeats_returnsTrueUnconditionally() {
        val daily = sampleDaily(repeats = RepeatInterval.Monthly, every = 5, createdAt = "2026-08-01")
        val oddDay = dateFormat.parse("2026-08-04")!!

        assertTrue(DailySchedulingLogic.wasDailyScheduledForDate(daily, oddDay))
    }

    @Test
    fun wasDailyScheduledForDate_dailyRepeats_dateBeforeCreatedAt_returnsFalse() {
        val daily = sampleDaily(repeats = RepeatInterval.Daily, every = 1, createdAt = "2026-08-05")
        val dateBefore = dateFormat.parse("2026-08-01")!! // diffDays = -4 < 0

        assertFalse(DailySchedulingLogic.wasDailyScheduledForDate(daily, dateBefore))
    }
}
