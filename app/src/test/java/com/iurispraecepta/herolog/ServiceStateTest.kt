package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import com.iurispraecepta.herolog.service.breakRemainingMillis
import com.iurispraecepta.herolog.service.formatRemaining
import com.iurispraecepta.herolog.service.sessionRemainingMillis
import com.iurispraecepta.herolog.service.shouldServiceCompleteSession
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceStateTest {

    @Test
    fun defaultState_isIdleWithZeroedFields() {
        val state = ServiceState()

        assertEquals(ServicePhase.IDLE, state.phase)
        assertEquals(null, state.sessionConfig)
        assertEquals(0, state.pauseCount)
        assertEquals(0L, state.endTimeMillis)
    }

    @Test
    fun sessionRemainingMillis_running_returnsEndMinusNow() {
        val state = ServiceState(phase = ServicePhase.RUNNING, endTimeMillis = 10_000L)

        assertEquals(4_000L, state.sessionRemainingMillis(now = 6_000L))
    }

    @Test
    fun sessionRemainingMillis_expired_neverNegative() {
        val state = ServiceState(phase = ServicePhase.RUNNING, endTimeMillis = 5_000L)

        assertEquals(0L, state.sessionRemainingMillis(now = 9_000L))
    }

    @Test
    fun sessionRemainingMillis_paused_returnsFrozenValue() {
        val state = ServiceState(
            phase = ServicePhase.PAUSED,
            endTimeMillis = 1_000L,
            pausedRemainingMillis = 42_000L
        )

        assertEquals(42_000L, state.sessionRemainingMillis(now = 999_999L))
    }

    @Test
    fun breakRemainingMillis_running_returnsEndMinusNow() {
        val state = ServiceState(
            phase = ServicePhase.BREAK_RUNNING,
            breakEndTimeMillis = 300_000L
        )

        assertEquals(120_000L, state.breakRemainingMillis(now = 180_000L))
    }

    @Test
    fun formatRemaining_formatsMinutesAndPaddedSeconds() {
        assertEquals("24:00", formatRemaining(1_440_000L))
        assertEquals("5:03", formatRemaining(303_000L))
        assertEquals("0:00", formatRemaining(0L))
        assertEquals("0:00", formatRemaining(-5_000L))
    }

    private val testConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    @Test
    fun shouldServiceCompleteSession_liveUncalculated_returnsTrue() {
        val session = PersistedFocusSession(
            config = testConfig,
            durationMinutes = 25,
            endTimeMillis = 5_000L,
            pendingCalculation = null
        )

        assertTrue(shouldServiceCompleteSession(session))
    }

    @Test
    fun shouldServiceCompleteSession_missingSession_returnsFalse() {
        // Backup completou e limpou, ou dados externos apagados: nunca reaplicar.
        assertFalse(shouldServiceCompleteSession(null))
    }

    @Test
    fun shouldServiceCompleteSession_alreadyCalculated_returnsFalse() {
        val session = PersistedFocusSession(
            config = testConfig,
            durationMinutes = 25,
            endTimeMillis = 5_000L,
            pendingCalculation = com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation(
                skillIdx = 0,
                skillName = "Kotlin",
                xpEarned = 60,
                goldEarned = 75,
                durationMins = 25,
                dungeonClearGoldBonus = 0,
                hasUsedDoubleLoot = false,
                hasUsedFocusElixir = false,
                hasUsedRuneFortune = false,
                hasUsedCrystalClarity = false,
                usedEquipmentIndicesAndCharges = emptyList(),
                lootedItems = emptyList(),
                droppedTitle = null,
                isWildernessChecked = false,
                isDungeonMode = false,
                comboBonusPercent = 0
            )
        )

        assertFalse(shouldServiceCompleteSession(session))
    }
}
