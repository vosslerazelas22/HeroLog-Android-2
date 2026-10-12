package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServiceState
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.sfx.SfxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [ShadowFocusSessionService::class])
class DungeonCooldownViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var nowMs = 10_000_000_000L

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

    private fun createBaseState(): CharacterState = CharacterState(
        gold = 0,
        totalXP = 0,
        totalGoldEarned = 0,
        totalSessions = 0,
        totalMinutes = 0,
        combatLevel = 1,
        combatXP = 0,
        skills = emptyList(),
        history = emptyList(),
        inventory = emptyList(),
        streak = 0,
        bestStreak = 0,
        lastStudyDate = null,
        wildernessWins = 0,
        combo = 0,
        dungeonProgress = 0,
        isDungeonMode = false,
        dungeonSessions = 0,
        achievements = emptyList(),
        charName = "Hero",
        charClass = CharClass.Warrior,
        todayXP = 0,
        todayMinutes = 0,
        todayDate = QuestLogic.toDateStringJs(),
        hasClaimedLogin = true,
        hp = 100,
        maxHp = 100,
        habits = emptyList(),
        dailies = emptyList(),
        todos = emptyList(),
        pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
    )

    private fun createViewModel(
        repository: CharacterRepository,
        focusRepository: FocusSessionRepository
    ): HeroLogViewModel {
        val vm = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            clock = { nowMs }
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    /**
     * Drena o ticker de 1s do cooldown: sem isso o `runTest` nunca termina,
     * pois ele aguarda coroutines de fundo em `advanceUntilIdle` ao final.
     */
    private fun stopCooldownTicker(viewModel: HeroLogViewModel) {
        viewModel.saveCharacterState(createBaseState().copy(lastDungeonClearedTime = 0L))
        testDispatcher.scheduler.runCurrent()
    }

    @Test
    fun dungeonCooldown_afterFourthSessionClear_remainingIsNearTwoHours() = runTest {
        // Arrange: estado com Masmorra recém-limpa (lastDungeonClearedTime = agora)
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = createViewModel(repository, focusRepository)
        // Act: grava clear no instante atual
        // (runCurrent, não advanceUntilIdle: o ticker de 1s do cooldown
        // reagendaria delay para sempre no tempo virtual e travaria o teste)
        viewModel.saveCharacterState(createBaseState().copy(lastDungeonClearedTime = nowMs))
        testDispatcher.scheduler.runCurrent()
        // Assert: restante ≈ 2h (SC-006)
        val remaining = viewModel.dungeonCooldownRemainingMs.value
        assertTrue(remaining > 0L)
        assertTrue(remaining <= 2 * 60 * 60 * 1000L)
        stopCooldownTicker(viewModel)
        db.close()
    }

    @Test
    fun dungeonCooldown_afterTwoHours_tickerStopsAtZero() = runTest {
        // Arrange: clear no instante atual
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = createViewModel(repository, focusRepository)
        viewModel.saveCharacterState(createBaseState().copy(lastDungeonClearedTime = nowMs))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.dungeonCooldownRemainingMs.value > 0L)
        // Act: avança o relógio 2h e deixa o ticker disparar uma vez
        nowMs += 2 * 60 * 60 * 1000L
        testDispatcher.scheduler.advanceTimeBy(1_500L)
        testDispatcher.scheduler.runCurrent()
        // Assert: zerou e o ticker parou (SC-006)
        assertEquals(0L, viewModel.dungeonCooldownRemainingMs.value)
        db.close()
    }

    @Test
    fun startSession_dungeonDuringCooldown_leavesFocusStateUntouched() = runTest {
        // Arrange: cooldown ativo
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = createViewModel(repository, focusRepository)
        viewModel.saveCharacterState(createBaseState().copy(lastDungeonClearedTime = nowMs))
        testDispatcher.scheduler.runCurrent()
        // Act: tenta iniciar Masmorra em cooldown (SC-007)
        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()
        // Assert: estado intacto + log de sistema
        assertFalse(viewModel.focusSessionState.value.isRunning)
        assertTrue(viewModel.systemLogs.value.any { it.text.contains("recarga") })
        stopCooldownTicker(viewModel)
        db.close()
    }

    @Test
    fun dungeonCooldown_clockMovedBackwards_neverExceedsTwoHours() = runTest {
        // Arrange: relógio retrocedido — clear 1h no futuro (SC-008)
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = createViewModel(repository, focusRepository)
        // Act
        viewModel.saveCharacterState(
            createBaseState().copy(lastDungeonClearedTime = nowMs + 3_600_000L)
        )
        testDispatcher.scheduler.runCurrent()
        // Assert: teto de 2h (FR-014)
        assertEquals(2 * 60 * 60 * 1000L, viewModel.dungeonCooldownRemainingMs.value)
        stopCooldownTicker(viewModel)
        db.close()
    }

    @Test
    fun startSession_dungeonImmediatelyAfterSaveDuringCooldown_isRefusedWithoutAdvancingScheduler() = runTest {
        // Arrange: cooldown ativo
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = createViewModel(repository, focusRepository)
        // Act: save com clear + start imediato, SEM avançar o scheduler
        // (item 2 do review: publish síncrono — o guard vê o timestamp novo na hora)
        viewModel.saveCharacterState(createBaseState().copy(lastDungeonClearedTime = nowMs))
        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, config, durationMinutes = 25)
        // Assert: recusado com estado intacto + log (falharia com publish assíncrono)
        assertFalse(viewModel.focusSessionState.value.isRunning)
        assertTrue(viewModel.systemLogs.value.any { it.text.contains("recarga") })
        stopCooldownTicker(viewModel)
        db.close()
    }

    @Test
    fun recover_expiredFourthDungeonSession_cooldownActiveImmediately() = runTest {
        // Arrange: personagem + 4ª sessão de Masmorra expirada, app reaberto
        // (item 4 do review: recovery aplica e deve recarregar o cooldown)
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepo = PendingRewardRepository(db.pendingRewardCelebrationDao())
        repository.saveCharacterState(
            createBaseState().copy(
                skills = listOf(Skill(id = "sk_1", name = "Kotlin", level = 1, xp = 0))
            )
        )
        focusRepository.saveSession(
            PersistedFocusSession(
                config = FocusSessionConfig(
                    selectedSkillIdx = 0,
                    isWildernessChecked = false,
                    isDungeonMode = true,
                    dungeonSessions = 3
                ),
                durationMinutes = 25,
                endTimeMillis = nowMs - 1_000L,
                pendingCalculation = null
            )
        )
        // Act: cria o VM (init roda o recovery); runCurrent sem avançar o ticker
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            clock = { nowMs },
            pendingRewardRepository = pendingRepo
        )
        testDispatcher.scheduler.runCurrent()
        // Assert: cooldown ativo + guard recusa Masmorra de imediato
        assertTrue(viewModel.dungeonCooldownRemainingMs.value > 0L)
        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, config, durationMinutes = 25)
        assertFalse(viewModel.focusSessionState.value.isRunning)
        stopCooldownTicker(viewModel)
        db.close()
    }
}
