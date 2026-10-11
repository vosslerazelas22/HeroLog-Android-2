package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.ui.focus.PipState
import com.iurispraecepta.herolog.ui.focus.dungeonPipStates
import com.iurispraecepta.herolog.ui.focus.dungeonStatusCompleted
import org.junit.Assert.assertEquals
import org.junit.Test

class ModeStatusTest {

    @Test
    fun dungeonPipStates_completed0_firstIsCurrent_restPending() {
        // Arrange/Act
        val states = dungeonPipStates(completed = 0)
        // Assert: campo a campo
        assertEquals(
            listOf(PipState.CURRENT, PipState.PENDING, PipState.PENDING, PipState.PENDING),
            states
        )
    }

    @Test
    fun dungeonPipStates_completed1_oneDoneOneCurrent() {
        // Arrange/Act
        val states = dungeonPipStates(completed = 1)
        // Assert: campo a campo
        assertEquals(
            listOf(PipState.DONE, PipState.CURRENT, PipState.PENDING, PipState.PENDING),
            states
        )
    }

    @Test
    fun dungeonPipStates_completed2_twoDoneOneCurrent() {
        // Arrange/Act
        val states = dungeonPipStates(completed = 2)
        // Assert: campo a campo
        assertEquals(
            listOf(PipState.DONE, PipState.DONE, PipState.CURRENT, PipState.PENDING),
            states
        )
    }

    @Test
    fun dungeonPipStates_completed3_threeDoneOneCurrent() {
        // Arrange/Act
        val states = dungeonPipStates(completed = 3)
        // Assert: campo a campo
        assertEquals(
            listOf(PipState.DONE, PipState.DONE, PipState.DONE, PipState.CURRENT),
            states
        )
    }

    @Test
    fun dungeonPipStates_completed4_allDone() {
        // Arrange/Act
        val states = dungeonPipStates(completed = 4)
        // Assert: campo a campo
        assertEquals(
            listOf(PipState.DONE, PipState.DONE, PipState.DONE, PipState.DONE),
            states
        )
    }

    @Test
    fun dungeonStatusCompleted_idle_usesProgress() {
        // Arrange/Act (FR-010: idle → dungeonSessionsProgress)
        val completed = dungeonStatusCompleted(
            isRunning = false,
            isBreak = false,
            dungeonSessionsProgress = 2,
            configDungeonSessions = 0,
            lastSessionDungeonSessions = 0
        )
        // Assert
        assertEquals(2, completed)
    }

    @Test
    fun dungeonStatusCompleted_running_usesConfig() {
        // Arrange/Act (FR-010: sessão rodando/pausada → config.dungeonSessions)
        val completed = dungeonStatusCompleted(
            isRunning = true,
            isBreak = false,
            dungeonSessionsProgress = 0,
            configDungeonSessions = 1,
            lastSessionDungeonSessions = 0
        )
        // Assert
        assertEquals(1, completed)
    }

    @Test
    fun dungeonStatusCompleted_break_addsOneToLastSession() {
        // Arrange/Act (FR-010: descanso → lastSessionDungeonSessions + 1)
        val completed = dungeonStatusCompleted(
            isRunning = false,
            isBreak = true,
            dungeonSessionsProgress = 0,
            configDungeonSessions = 0,
            lastSessionDungeonSessions = 0
        )
        // Assert: corrige a defasagem (era "0/4" após a 1ª sessão)
        assertEquals(1, completed)
    }

    @Test
    fun dungeonStatusCompleted_breakAfterFourth_capsAtFour() {
        // Arrange/Act (FR-011: descanso pós-4ª → completed = 4)
        val completed = dungeonStatusCompleted(
            isRunning = false,
            isBreak = true,
            dungeonSessionsProgress = 0,
            configDungeonSessions = 0,
            lastSessionDungeonSessions = 3
        )
        // Assert
        assertEquals(4, completed)
    }
}
