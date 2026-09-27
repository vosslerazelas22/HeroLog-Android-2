package com.iurispraecepta.herolog

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [ShadowFocusSessionService::class])
class FocusSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testContext: android.content.Context
        get() = ApplicationProvider.getApplicationContext()

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
            clock = { testDispatcher.scheduler.currentTime }
        )
    }

    private fun drainStartedServices() {
        val shadow = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        while (shadow.nextStartedService != null) {
        }
    }

    private fun nextStartedService(): android.content.Intent? {
        val shadow = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        return shadow.nextStartedService
    }

    private fun releaseMirrorTicker() {
        FocusSessionService.setStateForTests(ServiceState())
        testDispatcher.scheduler.advanceTimeBy(1_000L)
    }

    private val defaultConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    @Test
    fun startSession_setsMirrorAndPersistsWithoutCalculationAndStartsService() = runTest {
        val db = createInMemoryDatabase()
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        drainStartedServices()
        viewModel.startSession(testContext, defaultConfig, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFocusCompleted)
        assertEquals(1500, state.totalSeconds)
        assertEquals(1500, state.timeLeft)
        assertEquals(25, state.durationMinutes)
        assertEquals(defaultConfig, state.config)
        assertNull(state.pendingRewardsCalculation)

        val persisted = focusRepository.getSession()
        assertNotNull(persisted)
        assertEquals(25, persisted?.durationMinutes)
        assertNull(persisted?.pendingCalculation)

        val startIntent = nextStartedService()
        assertEquals(FocusSessionService.ACTION_START, startIntent?.action)
        assertEquals(
            25,
            startIntent?.getIntExtra(FocusSessionService.EXTRA_DURATION_MINUTES, -1)
        )

        db.close()
    }

    @Test
    fun togglePauseQuest_whenNotRunning_isNoOp() = runTest {
        val db = createInMemoryDatabase()
        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        val initialFocusState = viewModel.focusSessionState.value
        assertFalse(initialFocusState.isRunning)

        drainStartedServices()
        viewModel.togglePauseQuest(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(initialFocusState, viewModel.focusSessionState.value)
        assertNull(nextStartedService())

        db.close()
    }

    @Test
    fun mirror_reflectsInjectedRunningServiceState() = runTest {
        val db = createInMemoryDatabase()
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
        assertEquals(1200, state.totalSeconds)
        assertEquals(20, state.durationMinutes)
        assertEquals(defaultConfig, state.config)
        assertFalse(viewModel.breakTimerState.value.isBreakActive)

        releaseMirrorTicker()
        db.close()
    }

    @Test
    fun mirror_reflectsInjectedPausedServiceState() = runTest {
        val db = createInMemoryDatabase()
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.PAUSED,
                sessionConfig = defaultConfig,
                durationMinutes = 10,
                pausedRemainingMillis = 500_000L,
                pauseCount = 1
            )
        )
        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertTrue(state.isRunning)
        assertTrue(state.isPaused)
        assertEquals(500, state.timeLeft)
        assertEquals(600, state.totalSeconds)
        assertEquals(1, state.pauseCount)
        assertEquals(defaultConfig, state.config)

        releaseMirrorTicker()
        db.close()
    }

    @Test
    fun togglePauseQuest_whenRunning_sendsPauseWithoutLocalMutation() = runTest {
        val db = createInMemoryDatabase()
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

        drainStartedServices()
        viewModel.togglePauseQuest(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(FocusSessionService.ACTION_PAUSE, nextStartedService()?.action)
        val state = viewModel.focusSessionState.value
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)

        releaseMirrorTicker()
        db.close()
    }

    @Test
    fun togglePauseQuest_whenPaused_sendsResumeWithoutLocalMutation() = runTest {
        val db = createInMemoryDatabase()
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.PAUSED,
                sessionConfig = defaultConfig,
                durationMinutes = 10,
                pausedRemainingMillis = 500_000L,
                pauseCount = 1
            )
        )
        val viewModel = createViewModel(db)
        testDispatcher.scheduler.runCurrent()

        drainStartedServices()
        viewModel.togglePauseQuest(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(FocusSessionService.ACTION_RESUME, nextStartedService()?.action)
        val state = viewModel.focusSessionState.value
        assertTrue(state.isRunning)
        assertTrue(state.isPaused)

        releaseMirrorTicker()
        db.close()
    }

    @Test
    fun cancelSession_sendsStopAction() = runTest {
        val db = createInMemoryDatabase()
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

        drainStartedServices()
        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(FocusSessionService.ACTION_STOP, nextStartedService()?.action)

        releaseMirrorTicker()
        db.close()
    }
}
