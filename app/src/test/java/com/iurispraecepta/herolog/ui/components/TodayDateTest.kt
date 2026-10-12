package com.iurispraecepta.herolog.ui.components

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.TimeZone

class TodayDateTest {

    private lateinit var originalZone: TimeZone

    @Before
    fun setUp() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun formatTodayDate_usesLocalDay_beforeMidnight() {
        // 2026-10-03T02:30:00Z = 2026-10-02 23:30 em America/Sao_Paulo (UTC-3)
        assertEquals("2026-10-02", formatTodayDate(Date(1790994600000L)))
    }

    @Test
    fun formatTodayDate_usesLocalDay_afterMidnight() {
        // 2026-10-03T03:30:00Z = 2026-10-03 00:30 em America/Sao_Paulo (UTC-3)
        assertEquals("2026-10-03", formatTodayDate(Date(1790998200000L)))
    }
}
