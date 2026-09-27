package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusUseCase
import com.iurispraecepta.herolog.model.Skill
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FocusUseCaseTest {

    private val config = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    private fun stateWithSkill(level: Int = 1, xp: Int = 0) =
        createInitialCharacterState(Date(1_700_000_000_000L)).copy(
            skills = listOf(Skill(id = "sk_1", name = "Kotlin", level = level, xp = xp))
        )

    private data class Ctx(
        val useCase: FocusUseCase,
        val charRepo: CharacterRepository,
        val sessionRepo: FocusSessionRepository,
        val pendingRepo: PendingRewardRepository
    )

    private fun setUp(): Ctx {
        val charRepo = CharacterRepository(FakeCharacterStateDao())
        val sessionRepo = FocusSessionRepository(FakeActiveFocusSessionDao())
        val pendingRepo = PendingRewardRepository(FakePendingRewardCelebrationDao())
        return Ctx(FocusUseCase(charRepo, sessionRepo, pendingRepo), charRepo, sessionRepo, pendingRepo)
    }

    @Test
    fun calculateRewards_noCharacterState_returnsNullAndPersistsNothing() = runTest {
        val (useCase, _, sessionRepo, _) = setUp()

        val calc = useCase.calculateRewards(config, studiedMinutes = 25)

        assertNull(calc)
        assertNull(sessionRepo.getSession())
    }

    @Test
    fun calculateRewards_withState_persistsFrozenCalculation() = runTest {
        val (useCase, charRepo, sessionRepo, _) = setUp()
        charRepo.saveCharacterState(stateWithSkill())

        val calc = useCase.calculateRewards(
            config,
            studiedMinutes = 25,
            endTimeMillis = 1_700_000_150_000L
        )

        assertNotNull(calc)
        assertEquals("Kotlin", calc!!.skillName)
        assertEquals(25, calc.durationMins)
        assertEquals(60, calc.xpEarned)
        assertEquals(75, calc.goldEarned)
        val persisted = sessionRepo.getSession()
        assertNotNull(persisted)
        assertEquals(25, persisted!!.durationMinutes)
        assertEquals(1_700_000_150_000L, persisted.endTimeMillis)
        assertEquals(calc, persisted.pendingCalculation)
    }

    @Test
    fun applyRewards_defaults_appliesWithEmptyNotesAndNullTagAndQueuesCelebration() = runTest {
        val (useCase, charRepo, _, pendingRepo) = setUp()
        charRepo.saveCharacterState(stateWithSkill())
        val calc = useCase.calculateRewards(config, studiedMinutes = 25)!!

        val newState = useCase.applyRewards(calc)

        assertNotNull(newState)
        assertEquals(60, newState!!.totalXP)
        assertEquals(75, newState.gold - 200)
        val historyEntry = newState.history.first()
        assertEquals("", historyEntry.notes)
        assertNull(historyEntry.subskillTag)
        assertEquals(25, historyEntry.duration)
        val pending = pendingRepo.getPending()
        assertEquals(1, pending.size)
        assertEquals("Kotlin", pending.first().skillName)
        assertEquals(60, pending.first().xpGained)
        assertEquals(75, pending.first().goldGained)
        assertEquals(false, pending.first().consumed)
    }

    @Test
    fun applyRewards_twoSessions_accumulatesTwoPending() = runTest {
        val (useCase, charRepo, _, pendingRepo) = setUp()
        charRepo.saveCharacterState(stateWithSkill())
        val calc = useCase.calculateRewards(config, studiedMinutes = 25)!!

        useCase.applyRewards(calc)
        useCase.applyRewards(calc)

        assertEquals(2, pendingRepo.countPending())
    }

    @Test
    fun applyRewards_skillLevelUp_recordsPreviousAndNewLevel() = runTest {
        val (useCase, charRepo, _, pendingRepo) = setUp()
        // requiredXpForLevel(1) = 80 → 79 + 60 = 139 cruza para o nível 2.
        charRepo.saveCharacterState(stateWithSkill(level = 1, xp = 79))
        val calc = useCase.calculateRewards(config, studiedMinutes = 25)!!

        val newState = useCase.applyRewards(calc)

        assertEquals(2, newState!!.skills.first().level)
        val pending = pendingRepo.getPending().first()
        assertTrue(pending.leveledUp)
        assertEquals(1, pending.previousLevel)
        assertEquals(2, pending.newLevel)
    }

    @Test
    fun applyRewards_noLevelUp_recordsNullLevels() = runTest {
        val (useCase, charRepo, _, pendingRepo) = setUp()
        charRepo.saveCharacterState(stateWithSkill(level = 1, xp = 0))
        val calc = useCase.calculateRewards(config, studiedMinutes = 25)!!

        useCase.applyRewards(calc)

        val pending = pendingRepo.getPending().first()
        assertEquals(false, pending.leveledUp)
        assertNull(pending.previousLevel)
        assertNull(pending.newLevel)
    }
}
