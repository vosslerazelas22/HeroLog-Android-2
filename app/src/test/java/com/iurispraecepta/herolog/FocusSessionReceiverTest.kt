package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.service.completeSessionFromBackup
import com.iurispraecepta.herolog.service.shouldHandleSessionBackup
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FocusSessionReceiverTest {

    private val config = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    private fun session(endTimeMillis: Long, pending: Boolean = false) =
        PersistedFocusSession(
            config = config,
            durationMinutes = 25,
            endTimeMillis = endTimeMillis,
            pendingCalculation = if (pending) {
                com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation(
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
            } else {
                null
            }
        )

    @Test
    fun shouldHandleSessionBackup_nullSession_returnsFalse() {
        assertFalse(shouldHandleSessionBackup(null, now = 9_000L))
    }

    @Test
    fun shouldHandleSessionBackup_alreadyCalculated_returnsFalse() {
        assertFalse(shouldHandleSessionBackup(session(endTimeMillis = 5_000L, pending = true), now = 9_000L))
    }

    @Test
    fun shouldHandleSessionBackup_notYetExpired_returnsFalse() {
        assertFalse(shouldHandleSessionBackup(session(endTimeMillis = 9_000L), now = 5_000L))
    }

    @Test
    fun shouldHandleSessionBackup_expiredAndUncalculated_returnsTrue() {
        assertTrue(shouldHandleSessionBackup(session(endTimeMillis = 5_000L), now = 9_000L))
    }

    @Test
    fun completeSessionFromBackup_noCharacterState_returnsFalseWithoutEffects() = runTest {
        val charRepo = CharacterRepository(FakeCharacterStateDao())
        val sessionRepo = FocusSessionRepository(FakeActiveFocusSessionDao())
        val pendingRepo = PendingRewardRepository(FakePendingRewardCelebrationDao())

        val completed = completeSessionFromBackup(charRepo, sessionRepo, pendingRepo, config, 25)

        assertFalse(completed)
        assertEquals(0, pendingRepo.countPending())
    }

    @Test
    fun completeSessionFromBackup_expiredSession_appliesQueuesAndClears() = runTest {
        val charRepo = CharacterRepository(FakeCharacterStateDao())
        val sessionRepo = FocusSessionRepository(FakeActiveFocusSessionDao())
        val pendingRepo = PendingRewardRepository(FakePendingRewardCelebrationDao())
        charRepo.saveCharacterState(
            createInitialCharacterState(Date(1_700_000_000_000L)).copy(
                skills = listOf(Skill(id = "sk_1", name = "Kotlin", level = 1, xp = 0))
            )
        )
        sessionRepo.saveSession(session(endTimeMillis = 1_000L))

        val completed = completeSessionFromBackup(
            charRepo, sessionRepo, pendingRepo, config, 25,
            referenceDate = Date(1_700_000_000_000L)
        )

        assertTrue(completed)
        assertEquals(60, charRepo.getCharacterState()!!.totalXP)
        assertEquals(1, pendingRepo.countPending())
        assertNull(sessionRepo.getSession())
    }
}
