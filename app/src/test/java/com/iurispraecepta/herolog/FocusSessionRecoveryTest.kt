package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.sfx.SfxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusSessionRecoveryTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        FocusSessionService.setStateForTests(ServiceState())
    }

    @After
    fun tearDown() {
        FocusSessionService.setStateForTests(ServiceState())
        Dispatchers.resetMain()
    }

    private fun createInMemoryDatabase(): HeroLogDatabase {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return Room.inMemoryDatabaseBuilder(context, HeroLogDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
    }

    private fun createViewModel(db: HeroLogDatabase): HeroLogViewModel {
        return HeroLogViewModel(
            CharacterRepository(db.characterStateDao()),
            FocusSessionRepository(db.activeFocusSessionDao()),
            SfxManager.noOp(),
            clock = { testDispatcher.scheduler.currentTime },
            pendingRewardRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        )
    }

    private val defaultConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    @Test
    fun recover_case1_holderRunning_mirrorsServiceWithoutTouchingRoom() = runTest {
        val db = createInMemoryDatabase()
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = defaultConfig,
                durationMinutes = 20,
                endTimeMillis = 1_200_000L
            )
        )

        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFocusCompleted)
        assertEquals(1200, state.timeLeft)
        assertEquals(20, state.durationMinutes)
        assertEquals(defaultConfig, state.config)

        assertNull(focusRepository.getSession())
        assertNull(viewModel.pendingServiceResume.value)
        assertNull(viewModel.pendingCelebration.value)

        FocusSessionService.setStateForTests(ServiceState())
        testDispatcher.scheduler.advanceTimeBy(1_000L)

        db.close()
    }

    @Test
    fun recover_case2_storedCalculation_appliesExactlyOnceClearsAndQueuesCelebration() = runTest {
        val db = createInMemoryDatabase()
        val characterRepository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())

        val dummyCalc = FocusRewardsCalculation(
            skillIdx = 0,
            skillName = "Arcane Logic",
            xpEarned = 999999,
            goldEarned = 888888,
            durationMins = 15,
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
        focusRepository.saveSession(
            PersistedFocusSession(
                config = defaultConfig,
                durationMinutes = 15,
                endTimeMillis = testDispatcher.scheduler.currentTime - 1000L,
                pendingCalculation = dummyCalc
            )
        )

        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        val finalCharState = characterRepository.getCharacterState()!!
        assertEquals(999999, finalCharState.totalXP)
        assertEquals(200 + 100 + 888888, finalCharState.gold)

        assertNull(focusRepository.getSession())

        assertEquals(1, pendingRepository.countPending())
        val queued = pendingRepository.getPending().single()
        assertEquals(999999, queued.xpGained)
        assertEquals(888888, queued.goldGained)
        assertEquals("Arcane Logic", queued.skillName)
        assertEquals(15, queued.durationMinutes)

        val celebration = viewModel.pendingCelebration.value
        assertEquals(1, celebration?.sessionCount)
        assertEquals(999999, celebration?.totalXp)
        assertEquals(888888, celebration?.totalGold)

        assertFalse(viewModel.focusSessionState.value.isRunning)
        assertNull(viewModel.pendingServiceResume.value)

        db.close()
    }

    @Test
    fun recover_case3_futureSessionWithoutCalculation_exposesServiceResume() = runTest {
        val db = createInMemoryDatabase()
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        focusRepository.saveSession(
            PersistedFocusSession(
                config = defaultConfig,
                durationMinutes = 10,
                endTimeMillis = testDispatcher.scheduler.currentTime + 600_000L,
                pendingCalculation = null
            )
        )

        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        val resume = viewModel.pendingServiceResume.value
        assertEquals(600_000L, resume?.endTimeMillis)
        assertEquals(10, resume?.durationMinutes)
        assertEquals(defaultConfig, resume?.config)

        assertFalse(viewModel.focusSessionState.value.isRunning)
        assertNull(viewModel.pendingCelebration.value)

        db.close()
    }

    @Test
    fun recover_case4_expiredWithoutCalculation_completesViaBackupAndQueuesCelebration() = runTest {
        val db = createInMemoryDatabase()
        val characterRepository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        focusRepository.saveSession(
            PersistedFocusSession(
                config = defaultConfig,
                durationMinutes = 10,
                endTimeMillis = testDispatcher.scheduler.currentTime - 1000L,
                pendingCalculation = null
            )
        )

        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        assertNull(focusRepository.getSession())

        assertEquals(1, pendingRepository.countPending())

        val finalCharState = characterRepository.getCharacterState()!!
        assertTrue(finalCharState.totalXP > 0)
        assertTrue(finalCharState.gold > 200)

        assertEquals(1, viewModel.pendingCelebration.value?.sessionCount)

        assertFalse(viewModel.focusSessionState.value.isRunning)
        assertNull(viewModel.pendingServiceResume.value)

        db.close()
    }
}
