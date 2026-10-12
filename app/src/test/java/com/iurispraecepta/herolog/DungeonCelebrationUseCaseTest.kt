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
import org.junit.Test
import java.util.Date

/**
 * OQ-2 (spec-tema-por-modo-foco): o índice da sessão de Masmorra concluída é
 * gravado na fila de celebrações via applyRewards/queueCelebration.
 */
class DungeonCelebrationUseCaseTest {

    private fun stateWithSkill() =
        createInitialCharacterState(Date(1_700_000_000_000L)).copy(
            skills = listOf(Skill(id = "sk_1", name = "Kotlin", level = 1, xp = 0))
        )

    @Test
    fun applyRewards_dungeonFourthSession_queuesCelebrationWithIndex4() = runTest {
        // Arrange
        val charRepo = CharacterRepository(FakeCharacterStateDao())
        val sessionRepo = FocusSessionRepository(FakeActiveFocusSessionDao())
        val pendingRepo = PendingRewardRepository(FakePendingRewardCelebrationDao())
        val useCase = FocusUseCase(charRepo, sessionRepo, pendingRepo)
        charRepo.saveCharacterState(stateWithSkill())
        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 3
        )
        val calc = useCase.calculateRewards(config, studiedMinutes = 25)!!
        // Act: 4ª sessão (3 concluídas antes + esta)
        useCase.applyRewards(calc, dungeonSessionsCompleted = config.dungeonSessions + 1)
        // Assert: campo a campo
        val pending = pendingRepo.getPending()
        assertEquals(1, pending.size)
        assertEquals(4, pending.first().dungeonSessionsCompleted)
    }

    @Test
    fun applyRewards_standardSession_queuesCelebrationWithIndexZero() = runTest {
        // Arrange
        val charRepo = CharacterRepository(FakeCharacterStateDao())
        val sessionRepo = FocusSessionRepository(FakeActiveFocusSessionDao())
        val pendingRepo = PendingRewardRepository(FakePendingRewardCelebrationDao())
        val useCase = FocusUseCase(charRepo, sessionRepo, pendingRepo)
        charRepo.saveCharacterState(stateWithSkill())
        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        val calc = useCase.calculateRewards(config, studiedMinutes = 25)!!
        // Act (default 0 = não-masmorra)
        useCase.applyRewards(calc)
        // Assert: campo a campo
        val pending = pendingRepo.getPending()
        assertEquals(1, pending.size)
        assertEquals(0, pending.first().dungeonSessionsCompleted)
    }
}
