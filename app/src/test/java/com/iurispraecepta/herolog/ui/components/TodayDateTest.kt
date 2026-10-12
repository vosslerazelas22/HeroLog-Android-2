package com.iurispraecepta.herolog.ui.components

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    // ---------------------------------------------------------------------
    // millisUntilNextMidnight — timer de virada de dia (item 4)
    // ---------------------------------------------------------------------

    @Test
    fun millisUntilNextMidnight_halfHourBeforeMidnight_returns30Minutes() {
        // 2026-10-03T02:30:00Z = 2026-10-02 23:30 local → 30 min até 00:00
        assertEquals(30 * 60 * 1000L, millisUntilNextMidnight(1790994600000L))
    }

    @Test
    fun millisUntilNextMidnight_halfHourAfterMidnight_returns23h30() {
        // 2026-10-03T03:30:00Z = 2026-10-03 00:30 local → 23h30 até o próximo 00:00
        assertEquals(23 * 60 * 60 * 1000L + 30 * 60 * 1000L, millisUntilNextMidnight(1790998200000L))
    }

    @Test
    fun millisUntilNextMidnight_atMidnight_returnsFullDay() {
        // 2026-10-03T03:00:00Z = 2026-10-03 00:00 local exato → 24h
        assertEquals(24 * 60 * 60 * 1000L, millisUntilNextMidnight(1790996400000L))
    }

    @Test
    fun millisUntilNextMidnight_justBeforeMidnight_returnsOneMillisecond() {
        // 23:59:59.999 local → 1ms
        assertEquals(1L, millisUntilNextMidnight(1790996400000L - 1))
    }

    @Test
    fun millisUntilNextMidnight_alwaysPositive_andNeverMoreThan24h() {
        // Uma amostra cobrindo o dia inteiro: o valor fica em (0, 24h]
        // (em America/Sao_Paulo não há horário de verão desde 2019, então 24h é o teto)
        val base = 1790996400000L // meia-noite local
        for (offset in 0 until 48 step 1) {
            val ms = millisUntilNextMidnight(base + offset * 3_600_000L)
            assertTrue("offset=${offset}h → $ms", ms > 0L)
            assertTrue("offset=${offset}h → $ms", ms <= 24 * 60 * 60 * 1000L)
        }
    }
}
