package com.iurispraecepta.herolog

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.model.Todo
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.sfx.SfxManager
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.logic.SkillError
import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
import kotlinx.coroutines.Dispatchers
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
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [ShadowFocusSessionService::class])
class HeroLogViewModelTest {

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
        todayDate = com.iurispraecepta.herolog.logic.quests.QuestLogic.toDateStringJs(),
        hasClaimedLogin = true,
        hp = 100,
        maxHp = 100,
        habits = emptyList(),
        dailies = emptyList(),
        todos = emptyList(),
        pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
    )

    @Test
    fun viewModel_createsAndPersistsInitialState_whenNoneExists() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.characterState.value
        // 200 inicial + 100 login reward para Mage
        assertEquals(300, state?.gold)
        assertEquals("Aventureiro do Foco", state?.charName)
        assertTrue(state?.hasClaimedLogin == true)

        // Confirma que persistiu de verdade - novo ViewModel no mesmo banco nao recria, so recarrega
        val secondViewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(state, secondViewModel.characterState.value)

        db.close()
    }

    @Test
    fun viewModel_unequipItem_movesItemFromEquipmentToInventory() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val equippedItem = com.iurispraecepta.herolog.model.InventoryItem(
            "eq1", "Espada", "⚔️", com.iurispraecepta.herolog.model.BuffType.UnwaveringSword, 100, "desc", isEquipment = true
        )
        val stateWithEquipment = createBaseState().copy(equippedEquipment = listOf(equippedItem, null, null))
        viewModel.saveCharacterState(stateWithEquipment)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.unequipItem(0)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.characterState.value
        assertNull(result?.equippedEquipment?.get(0))
        assertTrue(result?.inventory?.contains(equippedItem) == true)

        db.close()
    }

    @Test
    fun viewModel_sellItem_addsGoldToState_regressionForPreviousLoggingOnlyBug() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val item = com.iurispraecepta.herolog.model.InventoryItem(
            "sell1", "Relíquia", "🔮", com.iurispraecepta.herolog.model.BuffType.ArcaneRelic, 100, "desc", isEquipment = false
        )
        val stateWithItem = createBaseState().copy(gold = 100, inventory = listOf(item))
        viewModel.saveCharacterState(stateWithItem)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sellItem(item)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.characterState.value
        assertEquals(150, result?.gold) // 100 + 50 (nao-equipamento vende por 50 fixo)
        assertTrue(result?.inventory?.isEmpty() == true)

        db.close()
    }

    @Test
    fun viewModel_savesAndReloadsState_persistsAcrossNewViewModelInstance() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = createBaseState().copy(charName = "Aethelgard", gold = 500)
        viewModel.saveCharacterState(state)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, viewModel.characterState.value)

        // Novo ViewModel, mesmo banco - prova persistência real, não só estado em memória do primeiro objeto
        val secondViewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, secondViewModel.characterState.value)
        db.close()
    }

    @Test
    fun viewModel_addCustomSkill_success_persistsSkills() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val baseState = createBaseState().copy(skills = emptyList())
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.addCustomSkill("Android Dev", "🤖")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result is SkillOperationResult.Success)
        val finalSkills = viewModel.characterState.value?.skills ?: emptyList()
        assertEquals(1, finalSkills.size)
        assertEquals("Android Dev", finalSkills[0].name)
        assertEquals("🤖", finalSkills[0].emoji)

        db.close()
    }

    @Test
    fun viewModel_addCustomSkill_validationError_doesNotPersist() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val baseState = createBaseState().copy(skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖")
        ))
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Duplicate
        val duplicateResult = viewModel.addCustomSkill("Android Dev", "🤖")
        assertTrue(duplicateResult is SkillOperationResult.Error)
        assertEquals(SkillError.DuplicateName, (duplicateResult as SkillOperationResult.Error).reason)

        // 2. Empty/Blank
        val blankResult = viewModel.addCustomSkill("   ", "🤖")
        assertTrue(blankResult is SkillOperationResult.Error)
        assertEquals(SkillError.BlankName, (blankResult as SkillOperationResult.Error).reason)

        testDispatcher.scheduler.advanceUntilIdle()
        // verify skills hasn't changed from original size 1
        assertEquals(1, viewModel.characterState.value?.skills?.size)

        db.close()
    }

    @Test
    fun viewModel_deleteSkill_blockedDuringActiveFocusSession() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖"),
            com.iurispraecepta.herolog.model.Skill(id = "s2", name = "Kotlin", level = 1, xp = 0, emoji = "☕")
        )
        val baseState = createBaseState().copy(skills = skills)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        // Simulate focus session running
        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, false, false, 0)
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isRunning)

        // Try to delete skill while running
        val eligibility = viewModel.deleteSkill(0)
        testDispatcher.scheduler.runCurrent()

        assertEquals(DeleteSkillEligibility.Blocked, eligibility)
        assertEquals(2, viewModel.characterState.value?.skills?.size) // Still 2

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_deleteSkill_eligibleAndDeletesSuccessfully() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖"),
            com.iurispraecepta.herolog.model.Skill(id = "s2", name = "Kotlin", level = 1, xp = 0, emoji = "☕")
        )
        val baseState = createBaseState().copy(skills = skills)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        val eligibility = viewModel.deleteSkill(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DeleteSkillEligibility.Eligible, eligibility)
        val finalSkills = viewModel.characterState.value?.skills ?: emptyList()
        assertEquals(1, finalSkills.size)
        assertEquals("Kotlin", finalSkills[0].name)

        db.close()
    }

    @Test
    fun viewModel_renameAndTagOperations_persistsCorrectly() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖", tags = listOf("OriginalTag"))
        )
        val baseState = createBaseState().copy(skills = skills)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Rename Skill
        val renameResult = viewModel.renameSkill(0, "Modern Android")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(renameResult is SkillOperationResult.Success)
        assertEquals("Modern Android", viewModel.characterState.value?.skills?.get(0)?.name)

        // 2. Add Tag
        viewModel.addTagToSkill(0, "Compose")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("OriginalTag", "Compose"), viewModel.characterState.value?.skills?.get(0)?.tags)

        // 3. Remove Tag
        viewModel.removeTagFromSkill(0, 0) // removes OriginalTag
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("Compose"), viewModel.characterState.value?.skills?.get(0)?.tags)

        db.close()
    }

    @Test
    fun viewModel_prestigeSkill_resetsXpAndLevel_increasesPrestigeCounter() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val maxedSkill = com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 99, xp = 500, emoji = "🤖", prestige = 2)
        val baseState = createBaseState().copy(skills = listOf(maxedSkill))
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.prestigeSkill(0)
        testDispatcher.scheduler.advanceUntilIdle()

        val upgraded = viewModel.characterState.value?.skills?.get(0)
        assertEquals(1, upgraded?.level)
        assertEquals(0, upgraded?.xp)
        assertEquals(3, upgraded?.prestige)

        db.close()
    }

    @Test
    fun viewModel_onAppBackgrounded_withWildernessActive_startsGracePeriod() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime }, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(streak = 5, combo = 10)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        // Start session with Wilderness checked
        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isRunning)

        // App backgrounded
        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isGraceActive)
        assertEquals(3, viewModel.focusSessionState.value.graceSecondsLeft)

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_onAppBackgrounded_withDeathProofTitle_sendsPauseInsteadOfGrace() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(equippedTitle = "DEATH-PROOF")
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0),
                durationMinutes = 25,
                endTimeMillis = 1_500_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        drainStartedServices()
        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()

        // Converted to pause!
        assertEquals(FocusSessionService.ACTION_PAUSE, nextStartedService()?.action)
        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isGraceActive)

        releaseMirrorTicker()
        db.close()
    }

    @Test
    fun viewModel_onAppBackgrounded_withoutWildernessChecked_isNoOp() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime }, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()

        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isGraceActive)
        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isPaused)

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_gracePeriodExpiring_triggersCognitiveDeath() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime }, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(
            charClass = CharClass.Warrior,
            streak = 10,
            combo = 5
        )
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isGraceActive)

        // Advance fakeTime by 4 seconds (grace is 3s)
        fakeTime += 4000L
        testDispatcher.scheduler.advanceTimeBy(3000L)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        org.junit.Assert.assertFalse(state.isGraceActive)
        org.junit.Assert.assertFalse(state.isRunning)

        // Character state streak, combo reset and isPlayerDead set
        val charState = viewModel.characterState.value
        assertTrue(charState?.isPlayerDead == true)
        assertEquals(0, charState?.streak)
        assertEquals(0, charState?.combo)

        // Focus session repository cleared
        val persisted = focusRepository.getSession()
        assertNull(persisted)

        db.close()
    }

    @Test
    fun viewModel_onAppForegrounded_duringGrace_cancelsGraceWithoutClick() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime }, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isGraceActive)

        // Foreground app
        viewModel.onAppForegrounded()
        testDispatcher.scheduler.runCurrent()

        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isGraceActive)
        assertEquals(3, viewModel.focusSessionState.value.graceSecondsLeft)
        assertTrue(viewModel.focusSessionState.value.isRunning)

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_respawnHero_appliesPenalties_restoresHp_clearsDead_andResetsFocusSessionState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(
            combatLevel = 5,
            gold = 120,
            combatXP = 80,
            hp = 0,
            maxHp = 50,
            isPlayerDead = true
        )
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        viewModel.respawnHero()
        testDispatcher.scheduler.runCurrent()

        val charState = viewModel.characterState.value
        assertEquals(4, charState?.combatLevel) // 5 - 1
        assertEquals(70, charState?.gold)        // 120 - 50
        assertEquals(0, charState?.combatXP)
        assertEquals(50, charState?.hp)          // hp restaurado ao maxHp
        org.junit.Assert.assertFalse(charState?.isPlayerDead == true)

        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isRunning)

        db.close()
    }

    @Test
    fun viewModel_respawnHero_whenDeadOutsideFocus_restoresHpAndClearsDead_withoutFocusSession() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        // Morreu por Habit Down (sem sessão de foco iniciada)
        val deadState = createBaseState().copy(
            combatLevel = 3,
            gold = 60,
            combatXP = 40,
            hp = 0,
            maxHp = 50,
            isPlayerDead = true
        )
        viewModel.saveCharacterState(deadState)
        testDispatcher.scheduler.runCurrent()

        viewModel.respawnHero()
        testDispatcher.scheduler.runCurrent()

        val charState = viewModel.characterState.value
        assertEquals(2, charState?.combatLevel) // 3 - 1
        assertEquals(10, charState?.gold)       // 60 - 50
        assertEquals(0, charState?.combatXP)
        assertEquals(50, charState?.hp)         // hp restaurado ao maxHp
        org.junit.Assert.assertFalse(charState?.isPlayerDead == true)

        db.close()
    }

    @Test
    fun breakTimer_startBreakTimer_sendsStartBreakAction() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        drainStartedServices()
        viewModel.startBreakTimer(testContext, 5)
        testDispatcher.scheduler.runCurrent()

        val intent = nextStartedService()
        assertEquals(FocusSessionService.ACTION_START_BREAK, intent?.action)
        assertEquals(5, intent?.getIntExtra(FocusSessionService.EXTRA_BREAK_MINUTES, -1))

        db.close()
    }

    @Test
    fun breakTimer_skipBreak_sendsSkipBreakAction() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        drainStartedServices()
        viewModel.skipBreak(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(FocusSessionService.ACTION_SKIP_BREAK, nextStartedService()?.action)

        db.close()
    }

    @Test
    fun serviceCompleted_withAutoStartBreakTrue_standardSession_requestsShortBreak() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        val baseChar = createBaseState().copy(
            skills = listOf(com.iurispraecepta.herolog.model.Skill(name = "Kotlin", level = 1, xp = 0)),
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = true,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "s1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        FocusSessionService.setStateForTests(ServiceState(phase = ServicePhase.COMPLETED))
        testDispatcher.scheduler.runCurrent()

        assertEquals(5, viewModel.pendingBreakStart.value)
        assertEquals(1, viewModel.pendingCelebration.value?.sessionCount)
        assertFalse(viewModel.breakTimerState.value.isBreakActive)

        db.close()
    }

    @Test
    fun serviceCompleted_withAutoStartBreakTrue_fourthDungeonSession_requestsLongBreakTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        val baseChar = createBaseState().copy(
            skills = listOf(com.iurispraecepta.herolog.model.Skill(name = "Kotlin", level = 1, xp = 0)),
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 20,
                autoStartBreak = true,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "s4",
                sessionCompletedAt = 4_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.COMPLETED,
                lastSessionDungeonMode = true,
                lastSessionDungeonSessions = 3
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.dungeonSessionsProgress.value)
        assertEquals(20, viewModel.pendingBreakStart.value)

        db.close()
    }

    @Test
    fun serviceCompleted_withAutoStartBreakFalse_entersBreakPrepInsteadOfTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        val baseChar = createBaseState().copy(
            skills = listOf(com.iurispraecepta.herolog.model.Skill(name = "Kotlin", level = 1, xp = 0)),
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = false,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "s1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        FocusSessionService.setStateForTests(ServiceState(phase = ServicePhase.COMPLETED))
        testDispatcher.scheduler.runCurrent()

        val breakState = viewModel.breakTimerState.value
        org.junit.Assert.assertFalse(breakState.isBreakActive)
        assertTrue(breakState.isBreakPrep)
        assertNull(viewModel.pendingBreakStart.value)

        db.close()
    }

    @Test
    fun changeFocusDuration_persistsNewFocusDuration_whenIdle() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val baseChar = createBaseState().copy(
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = false,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.changeFocusDuration(50)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.characterState.value
        assertEquals(50, updated?.pomodoroSettings?.focusDuration)
        assertEquals(5, updated?.pomodoroSettings?.shortBreakDuration)
        assertEquals(15, updated?.pomodoroSettings?.longBreakDuration)

        // Verify persistence
        val secondViewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(50, secondViewModel.characterState.value?.pomodoroSettings?.focusDuration)

        db.close()
    }

    @Test
    fun changeFocusDuration_blockedWhileMirroredRunningOrBreakActive() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        val baseChar = createBaseState().copy(
            skills = listOf(com.iurispraecepta.herolog.model.Skill(name = "Kotlin", level = 1, xp = 0)),
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = false,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.runCurrent()

        // Blocked while mirrored session is running
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
                    selectedSkillIdx = 0,
                    isWildernessChecked = false,
                    isDungeonMode = false,
                    dungeonSessions = 0
                ),
                durationMinutes = 25,
                endTimeMillis = 1_500_000L
            )
        )
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isRunning)

        viewModel.changeFocusDuration(90)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        // Blocked while mirrored break is active
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.BREAK_RUNNING,
                breakEndTimeMillis = 300_000L,
                breakDurationMinutes = 5,
                breakTotalSeconds = 300
            )
        )
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.breakTimerState.value.isBreakActive)

        viewModel.changeFocusDuration(90)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        // Allowed once idle again
        releaseMirrorTicker()
        testDispatcher.scheduler.runCurrent()

        viewModel.changeFocusDuration(90)
        testDispatcher.scheduler.runCurrent()
        assertEquals(90, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        db.close()
    }

    @Test
    fun saveCustomTimerSettings_persistsCustomValues_whenIdle() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val baseChar = createBaseState().copy(
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = false,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveCustomTimerSettings(focusMinutes = 45, shortBreakMinutes = 8, longBreakMinutes = 25)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.characterState.value
        assertEquals(45, updated?.pomodoroSettings?.focusDuration)
        assertEquals(8, updated?.pomodoroSettings?.shortBreakDuration)
        assertEquals(25, updated?.pomodoroSettings?.longBreakDuration)

        // Verify persistence
        val secondViewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(45, secondViewModel.characterState.value?.pomodoroSettings?.focusDuration)
        assertEquals(8, secondViewModel.characterState.value?.pomodoroSettings?.shortBreakDuration)
        assertEquals(25, secondViewModel.characterState.value?.pomodoroSettings?.longBreakDuration)

        db.close()
    }

    @Test
    fun saveCustomTimerSettings_isNoOp_whenSessionRunningOrBreakActive() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val baseChar = createBaseState().copy(
            skills = listOf(com.iurispraecepta.herolog.model.Skill(name = "Kotlin", level = 1, xp = 0)),
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = false,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.advanceUntilIdle()

        // Start session
        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        // Attempt saveCustomTimerSettings while running -> no-op
        viewModel.saveCustomTimerSettings(focusMinutes = 60, shortBreakMinutes = 10, longBreakMinutes = 30)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()

        // Start break timer
        viewModel.startBreakTimer(testContext, 5)
        testDispatcher.scheduler.runCurrent()

        // Attempt saveCustomTimerSettings while break active -> no-op
        viewModel.saveCustomTimerSettings(focusMinutes = 60, shortBreakMinutes = 10, longBreakMinutes = 30)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        viewModel.skipBreak(testContext)
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun toggleAutoStartBreak_and_toggleAutoStartFocus_invertsAndPersists() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val baseChar = createBaseState().copy(
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = false,
                autoStartFocus = false
            )
        )
        viewModel.saveCharacterState(baseChar)
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle Break
        viewModel.toggleAutoStartBreak()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, viewModel.characterState.value?.pomodoroSettings?.autoStartBreak)

        // Toggle Focus
        viewModel.toggleAutoStartFocus()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, viewModel.characterState.value?.pomodoroSettings?.autoStartFocus)

        // Verify persistence
        val secondViewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, secondViewModel.characterState.value?.pomodoroSettings?.autoStartBreak)
        assertEquals(true, secondViewModel.characterState.value?.pomodoroSettings?.autoStartFocus)

        // Toggle back
        viewModel.toggleAutoStartBreak()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleAutoStartFocus()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.characterState.value?.pomodoroSettings?.autoStartBreak)
        assertEquals(false, viewModel.characterState.value?.pomodoroSettings?.autoStartFocus)

        db.close()
    }

    @Test
    fun selectBreakDuration_updatesSelectedBreakMins_withoutStartingTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.enterBreakPrep()
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.breakTimerState.value.isBreakPrep)
        assertEquals(5, viewModel.breakTimerState.value.selectedBreakMins)

        viewModel.selectBreakDuration(15)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.breakTimerState.value.isBreakPrep)
        assertEquals(false, viewModel.breakTimerState.value.isBreakActive)
        assertEquals(15, viewModel.breakTimerState.value.selectedBreakMins)

        db.close()
    }

    @Test
    fun serviceCompleted_setsWasLastSessionDungeonMode_inBreakPrepState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        // Turn off autoStartBreak to trigger enterBreakPrep
        val charState = viewModel.characterState.value!!
        viewModel.saveCharacterState(
            charState.copy(
                pomodoroSettings = charState.pomodoroSettings.copy(autoStartBreak = false)
            )
        )
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "s1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.COMPLETED,
                lastSessionDungeonMode = true,
                lastSessionDungeonSessions = 0
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.breakTimerState.value.isBreakPrep)
        assertTrue(viewModel.breakTimerState.value.wasLastSessionDungeonMode)

        db.close()
    }

    @Test
    fun abandonSession_whenDungeonModeActive_resetsDungeonSessionsProgressToZero() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.runCurrent()

        val dungeonConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 2
        )
        viewModel.startSession(testContext, dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        viewModel.abandonSession(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.dungeonSessionsProgress.value)

        db.close()
    }

    @Test
    fun abandonSession_whenNotDungeonMode_leavesDungeonSessionsProgressUnchanged() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        // Complete 1 dungeon session to advance progress to 1
        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "d1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.COMPLETED,
                lastSessionDungeonMode = true,
                lastSessionDungeonSessions = 0
            )
        )
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.dungeonSessionsProgress.value)

        // Start standard session and abandon
        FocusSessionService.setStateForTests(ServiceState())
        val standardConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, standardConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        val logsBefore = viewModel.systemLogs.value.size
        viewModel.abandonSession(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, viewModel.dungeonSessionsProgress.value)
        assertEquals(logsBefore, viewModel.systemLogs.value.size)

        db.close()
    }

    @Test
    fun abandonSession_sendsStopAction() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.runCurrent()

        val standardConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, standardConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()
        assertNotNull(focusRepository.getSession())

        drainStartedServices()
        viewModel.abandonSession(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(FocusSessionService.ACTION_STOP, nextStartedService()?.action)

        db.close()
    }

    @Test
    fun viewModel_mounts_appliesRolloverAndLoginReward_warriorGets120Gold() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val previousDayStr = "Tue Aug 11 2026"
        val todayStr = "Wed Aug 12 2026"
        val existingWarrior = createBaseState().copy(
            charClass = CharClass.Warrior,
            gold = 50,
            totalGoldEarned = 50,
            todayDate = previousDayStr, // Dia anterior
            hasClaimedLogin = true // No dia anterior ja tinha pego, mas rollover reseta hasClaimedLogin = false
        )
        repository.saveCharacterState(existingWarrior)

        val jsDateRef = java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.US).parse(todayStr)!!.time
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { jsDateRef },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.characterState.value
        assertEquals(todayStr, state?.todayDate)
        assertEquals(170, state?.gold) // 50 + 120 (Warrior)
        assertEquals(170, state?.totalGoldEarned)
        assertTrue(state?.hasClaimedLogin == true)

        db.close()
    }

    @Test
    fun viewModel_mounts_whenAlreadyClaimedLoginToday_doesNotAddGoldAgain() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val todayStr = "Wed Aug 12 2026"
        val existingMage = createBaseState().copy(
            charClass = CharClass.Mage,
            gold = 100,
            totalGoldEarned = 100,
            todayDate = todayStr, // Mesmo dia!
            hasClaimedLogin = true // Ja pegou hoje
        )
        repository.saveCharacterState(existingMage)

        val jsDateRef = java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.US).parse(todayStr)!!.time
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { jsDateRef },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.characterState.value
        assertEquals(100, state?.gold) // Sem bonus duplicado
        assertTrue(state?.hasClaimedLogin == true)

        db.close()
    }

    @Test
    fun viewModel_mounts_whenRolloverCausesDeath_setsIsPlayerDeadAfterDelay() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val previousDayStr = "Tue Aug 11 2026"
        val todayStr = "Wed Aug 12 2026"
        val missedDaily = com.iurispraecepta.herolog.model.Daily(
            id = "d1",
            title = "Hard daily",
            notes = "",
            difficulty = com.iurispraecepta.herolog.model.Difficulty.Hard, // 15 de dano
            completed = false,
            streak = 0,
            repeats = com.iurispraecepta.herolog.model.RepeatInterval.Daily,
            every = 1,
            tags = emptyList(),
            checklist = emptyList(),
            value = 0,
            createdAt = "2026-08-01"
        )
        val mortalState = createBaseState().copy(
            hp = 10,
            todayDate = previousDayStr,
            lastStudyDate = previousDayStr,
            dailies = listOf(missedDaily),
            inventory = emptyList() // Sem shield
        )
        repository.saveCharacterState(mortalState)

        val jsDateRef = java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.US).parse(todayStr)!!.time
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { jsDateRef },
            sfxManager = SfxManager.noOp()
        )

        // Antes do delay de 100ms
        testDispatcher.scheduler.runCurrent()
        assertEquals(0, viewModel.characterState.value?.hp)

        // Apos o delay de 100ms
        testDispatcher.scheduler.advanceTimeBy(150)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.characterState.value?.isPlayerDead == true)

        db.close()
    }

    @Test
    fun triggerHabit_up_appliesRewardsAndUpdatesHabit() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Drink Water",
            notes = "",
            up = true,
            down = true,
            difficulty = Difficulty.Medium, // XP: 28, Gold: 14 (Warrior class: gold * 1.2 = 16)
            upCount = 0,
            downCount = 0,
            streak = 0,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        val state = createBaseState().copy(
            charClass = CharClass.Warrior,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0,
            habits = listOf(habit)
        )
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("h1", isUp = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertNotNull(updatedState)
        assertEquals(116, updatedState?.gold)
        assertEquals(116, updatedState?.totalGoldEarned)
        assertEquals(28, updatedState?.totalXP)
        assertEquals(28, updatedState?.combatXP)
        val updatedHabit = updatedState?.habits?.find { it.id == "h1" }
        assertNotNull(updatedHabit)
        assertEquals(1, updatedHabit?.upCount)
        assertEquals(1, updatedHabit?.streak)
        assertNotNull(updatedHabit?.lastTriggeredDate)

        db.close()
    }

    @Test
    fun triggerHabit_down_appliesDamageAndUpdatesHabit() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Junk Food",
            notes = "",
            up = true,
            down = true,
            difficulty = Difficulty.Medium, // Damage: 7
            upCount = 0,
            downCount = 2,
            streak = 5,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        val state = createBaseState().copy(
            charClass = CharClass.Warrior,
            hp = 50,
            maxHp = 50,
            habits = listOf(habit)
        )
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("h1", isUp = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertNotNull(updatedState)
        assertEquals(43, updatedState?.hp)
        val updatedHabit = updatedState?.habits?.find { it.id == "h1" }
        assertNotNull(updatedHabit)
        assertEquals(3, updatedHabit?.downCount)
        assertEquals(4, updatedHabit?.streak)

        db.close()
    }

    @Test
    fun triggerHabit_unknownId_noOp() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Drink Water",
            notes = "",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            upCount = 0,
            downCount = 0,
            streak = 0,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        val state = createBaseState().copy(
            gold = 100,
            hp = 50,
            habits = listOf(habit)
        )
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("unknown_id", isUp = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertEquals(100, updatedState?.gold)
        assertEquals(50, updatedState?.hp)
        assertEquals(0, updatedState?.habits?.find { it.id == "h1" }?.upCount)

        db.close()
    }

    @Test
    fun toggleDaily_completesAndUncompletes_symmetric() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val daily = Daily(
            id = "d1",
            title = "Morning Workout",
            notes = "",
            difficulty = Difficulty.Medium, // XP: 28, Gold: 14 (Warrior class: gold * 1.2 = 16)
            completed = false,
            streak = 2,
            repeats = RepeatInterval.Daily,
            every = 1,
            tags = emptyList(),
            checklist = emptyList(),
            value = 2,
            createdAt = "2026-08-01"
        )
        val state = createBaseState().copy(
            charClass = CharClass.Warrior,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0,
            dailies = listOf(daily)
        )
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle to completed
        viewModel.toggleDaily("d1")
        testDispatcher.scheduler.advanceUntilIdle()

        val completedState = viewModel.characterState.value
        assertNotNull(completedState)
        assertEquals(116, completedState?.gold)
        assertEquals(116, completedState?.totalGoldEarned)
        assertEquals(28, completedState?.totalXP)
        assertEquals(28, completedState?.combatXP)
        val completedDaily = completedState?.dailies?.find { it.id == "d1" }
        assertTrue(completedDaily?.completed == true)
        assertEquals(3, completedDaily?.streak)
        assertEquals(3, completedDaily?.value)

        // Toggle back to uncompleted
        viewModel.toggleDaily("d1")
        testDispatcher.scheduler.advanceUntilIdle()

        val revertedState = viewModel.characterState.value
        assertNotNull(revertedState)
        assertEquals(100, revertedState?.gold)
        assertEquals(100, revertedState?.totalGoldEarned)
        assertEquals(0, revertedState?.totalXP)
        assertEquals(0, revertedState?.combatXP)
        val revertedDaily = revertedState?.dailies?.find { it.id == "d1" }
        assertFalse(revertedDaily?.completed == true)
        assertEquals(2, revertedDaily?.streak)
        assertEquals(2, revertedDaily?.value)

        db.close()
    }

    @Test
    fun toggleTodo_completesAndSetsCompletedAt() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val todo = Todo(
            id = "t1",
            title = "Pay Taxes",
            notes = "",
            difficulty = Difficulty.Hard, // Hard: XP 60, Gold 25 (Warrior class: gold * 1.2 = 30)
            completed = false,
            tags = emptyList(),
            checklist = emptyList(),
            createdAt = "2026-08-01",
            completedAt = null
        )
        val state = createBaseState().copy(
            charClass = CharClass.Warrior,
            gold = 100,
            totalGoldEarned = 100,
            totalXP = 0,
            combatLevel = 1,
            combatXP = 0,
            todos = listOf(todo)
        )
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTodo("t1")
        testDispatcher.scheduler.advanceUntilIdle()

        val completedState = viewModel.characterState.value
        assertNotNull(completedState)
        assertEquals(130, completedState?.gold)
        assertEquals(130, completedState?.totalGoldEarned)
        assertEquals(60, completedState?.totalXP)
        assertEquals(60, completedState?.combatXP)
        val completedTodo = completedState?.todos?.find { it.id == "t1" }
        assertTrue(completedTodo?.completed == true)
        assertNotNull(completedTodo?.completedAt)

        db.close()
    }

    @Test
    fun addHabit_addsNewHabitWithGeneratedIdAndDefaults() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val state = createBaseState().copy(habits = emptyList())
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addHabit(
            title = "Read Books",
            notes = "1 chapter per day",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            tags = listOf("learning")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertNotNull(updatedState)
        assertEquals(1, updatedState?.habits?.size)
        val addedHabit = updatedState?.habits?.first()
        assertNotNull(addedHabit)
        assertEquals("Read Books", addedHabit?.title)
        assertEquals("1 chapter per day", addedHabit?.notes)
        assertTrue(addedHabit?.up == true)
        assertFalse(addedHabit?.down == true)
        assertEquals(Difficulty.Easy, addedHabit?.difficulty)
        assertEquals(0, addedHabit?.upCount)
        assertEquals(0, addedHabit?.downCount)
        assertEquals(0, addedHabit?.streak)
        assertEquals(listOf("learning"), addedHabit?.tags)
        assertNull(addedHabit?.lastTriggeredDate)

        db.close()
    }

    @Test
    fun editHabit_updatesExistingHabit() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Old Title",
            notes = "Old Notes",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            upCount = 5,
            downCount = 1,
            streak = 4,
            tags = listOf("tag1"),
            lastTriggeredDate = "2026-08-01"
        )
        val state = createBaseState().copy(habits = listOf(habit))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val editedHabit = habit.copy(
            title = "New Title",
            notes = "New Notes",
            difficulty = Difficulty.Hard,
            tags = listOf("tag1", "tag2")
        )
        viewModel.editHabit(editedHabit)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        val updatedHabit = updatedState?.habits?.find { it.id == "h1" }
        assertNotNull(updatedHabit)
        assertEquals("New Title", updatedHabit?.title)
        assertEquals("New Notes", updatedHabit?.notes)
        assertEquals(Difficulty.Hard, updatedHabit?.difficulty)
        assertEquals(5, updatedHabit?.upCount)
        assertEquals(listOf("tag1", "tag2"), updatedHabit?.tags)

        db.close()
    }

    @Test
    fun deleteHabit_removesHabit() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit1 = Habit(id = "h1", title = "H1", notes = "", up = true, down = false, difficulty = Difficulty.Easy, upCount = 0, downCount = 0, streak = 0, tags = emptyList())
        val habit2 = Habit(id = "h2", title = "H2", notes = "", up = true, down = false, difficulty = Difficulty.Easy, upCount = 0, downCount = 0, streak = 0, tags = emptyList())
        val state = createBaseState().copy(habits = listOf(habit1, habit2))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteHabit("h1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertEquals(1, updatedState?.habits?.size)
        assertEquals("h2", updatedState?.habits?.first()?.id)

        db.close()
    }

    @Test
    fun addDaily_addsNewDailyWithChecklistAndInitialStreak() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val state = createBaseState().copy(dailies = emptyList())
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addDaily(
            title = "Morning Routine",
            notes = "All tasks",
            difficulty = Difficulty.Medium,
            streak = 3,
            repeats = RepeatInterval.Daily,
            every = 1,
            tags = listOf("morning"),
            checklistTexts = listOf("Drink water", "Stretch")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertNotNull(updatedState)
        assertEquals(1, updatedState?.dailies?.size)
        val addedDaily = updatedState?.dailies?.first()
        assertNotNull(addedDaily)
        assertEquals("Morning Routine", addedDaily?.title)
        assertEquals("All tasks", addedDaily?.notes)
        assertEquals(Difficulty.Medium, addedDaily?.difficulty)
        assertFalse(addedDaily?.completed == true)
        assertEquals(3, addedDaily?.streak)
        assertEquals(RepeatInterval.Daily, addedDaily?.repeats)
        assertEquals(1, addedDaily?.every)
        assertEquals(listOf("morning"), addedDaily?.tags)
        assertEquals(0, addedDaily?.value)
        assertEquals(2, addedDaily?.checklist?.size)
        assertEquals("Drink water", addedDaily?.checklist?.get(0)?.text)
        assertFalse(addedDaily?.checklist?.get(0)?.completed == true)
        assertEquals("Stretch", addedDaily?.checklist?.get(1)?.text)
        assertFalse(addedDaily?.checklist?.get(1)?.completed == true)
        assertNotNull(addedDaily?.createdAt)

        db.close()
    }

    @Test
    fun editDaily_updatesExistingDaily() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val daily = Daily(
            id = "d1",
            title = "Old Daily",
            notes = "Old Notes",
            difficulty = Difficulty.Easy,
            completed = false,
            streak = 5,
            repeats = RepeatInterval.Daily,
            every = 1,
            tags = listOf("tag1"),
            checklist = emptyList(),
            value = 5,
            createdAt = "2026-08-01"
        )
        val state = createBaseState().copy(dailies = listOf(daily))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val editedDaily = daily.copy(
            title = "Updated Daily",
            notes = "Updated Notes",
            difficulty = Difficulty.Hard,
            streak = 10,
            tags = listOf("tag1", "tag2")
        )
        viewModel.editDaily(editedDaily)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        val updatedDaily = updatedState?.dailies?.find { it.id == "d1" }
        assertNotNull(updatedDaily)
        assertEquals("Updated Daily", updatedDaily?.title)
        assertEquals("Updated Notes", updatedDaily?.notes)
        assertEquals(Difficulty.Hard, updatedDaily?.difficulty)
        assertEquals(10, updatedDaily?.streak)
        assertEquals(listOf("tag1", "tag2"), updatedDaily?.tags)

        db.close()
    }

    @Test
    fun deleteDaily_removesDaily() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val daily1 = Daily(id = "d1", title = "D1", notes = "", difficulty = Difficulty.Easy, completed = false, streak = 0, repeats = RepeatInterval.Daily, every = 1, tags = emptyList(), checklist = emptyList())
        val daily2 = Daily(id = "d2", title = "D2", notes = "", difficulty = Difficulty.Easy, completed = false, streak = 0, repeats = RepeatInterval.Daily, every = 1, tags = emptyList(), checklist = emptyList())
        val state = createBaseState().copy(dailies = listOf(daily1, daily2))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteDaily("d1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertEquals(1, updatedState?.dailies?.size)
        assertEquals("d2", updatedState?.dailies?.first()?.id)

        db.close()
    }

    @Test
    fun toggleDailyChecklistItem_flipsCompletedState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val item1 = ChecklistItem(id = "c1", text = "Subtask 1", completed = false)
        val item2 = ChecklistItem(id = "c2", text = "Subtask 2", completed = true)
        val daily = Daily(
            id = "d1",
            title = "Daily With Checklist",
            notes = "",
            difficulty = Difficulty.Easy,
            completed = false,
            streak = 0,
            repeats = RepeatInterval.Daily,
            every = 1,
            tags = emptyList(),
            checklist = listOf(item1, item2)
        )
        val state = createBaseState().copy(dailies = listOf(daily))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        // Flip c1 from false to true
        viewModel.toggleDailyChecklistItem("d1", "c1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        val updatedDaily = updatedState?.dailies?.find { it.id == "d1" }
        assertTrue(updatedDaily?.checklist?.find { it.id == "c1" }?.completed == true)
        assertTrue(updatedDaily?.checklist?.find { it.id == "c2" }?.completed == true)

        // Flip c2 from true to false
        viewModel.toggleDailyChecklistItem("d1", "c2")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState2 = viewModel.characterState.value
        val updatedDaily2 = updatedState2?.dailies?.find { it.id == "d1" }
        assertFalse(updatedDaily2?.checklist?.find { it.id == "c2" }?.completed == true)

        db.close()
    }

    @Test
    fun addTodo_addsNewTodoWithChecklist() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val state = createBaseState().copy(todos = emptyList())
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTodo(
            title = "File Taxes",
            notes = "Before deadline",
            difficulty = Difficulty.Hard,
            tags = listOf("finance"),
            checklistTexts = listOf("Gather receipts", "Fill form")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertNotNull(updatedState)
        assertEquals(1, updatedState?.todos?.size)
        val addedTodo = updatedState?.todos?.first()
        assertNotNull(addedTodo)
        assertEquals("File Taxes", addedTodo?.title)
        assertEquals("Before deadline", addedTodo?.notes)
        assertEquals(Difficulty.Hard, addedTodo?.difficulty)
        assertFalse(addedTodo?.completed == true)
        assertEquals(listOf("finance"), addedTodo?.tags)
        assertEquals(2, addedTodo?.checklist?.size)
        assertEquals("Gather receipts", addedTodo?.checklist?.get(0)?.text)
        assertFalse(addedTodo?.checklist?.get(0)?.completed == true)
        assertEquals("Fill form", addedTodo?.checklist?.get(1)?.text)
        assertFalse(addedTodo?.checklist?.get(1)?.completed == true)
        assertNotNull(addedTodo?.createdAt)
        assertNull(addedTodo?.completedAt)

        db.close()
    }

    @Test
    fun editTodo_updatesExistingTodo() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val todo = Todo(
            id = "t1",
            title = "Old Todo",
            notes = "Old Notes",
            difficulty = Difficulty.Easy,
            completed = false,
            tags = listOf("tag1"),
            checklist = emptyList(),
            createdAt = "2026-08-01",
            completedAt = null
        )
        val state = createBaseState().copy(todos = listOf(todo))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        val editedTodo = todo.copy(
            title = "Updated Todo",
            notes = "Updated Notes",
            difficulty = Difficulty.Medium,
            tags = listOf("tag1", "tag3")
        )
        viewModel.editTodo(editedTodo)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        val updatedTodo = updatedState?.todos?.find { it.id == "t1" }
        assertNotNull(updatedTodo)
        assertEquals("Updated Todo", updatedTodo?.title)
        assertEquals("Updated Notes", updatedTodo?.notes)
        assertEquals(Difficulty.Medium, updatedTodo?.difficulty)
        assertEquals(listOf("tag1", "tag3"), updatedTodo?.tags)

        db.close()
    }

    @Test
    fun deleteTodo_removesTodo() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val todo1 = Todo(id = "t1", title = "T1", notes = "", difficulty = Difficulty.Easy, completed = false, tags = emptyList(), checklist = emptyList())
        val todo2 = Todo(id = "t2", title = "T2", notes = "", difficulty = Difficulty.Easy, completed = false, tags = emptyList(), checklist = emptyList())
        val state = createBaseState().copy(todos = listOf(todo1, todo2))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteTodo("t1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        assertEquals(1, updatedState?.todos?.size)
        assertEquals("t2", updatedState?.todos?.first()?.id)

        db.close()
    }

    @Test
    fun toggleTodoChecklistItem_flipsCompletedState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val item1 = ChecklistItem(id = "c1", text = "Subtask A", completed = false)
        val item2 = ChecklistItem(id = "c2", text = "Subtask B", completed = true)
        val todo = Todo(
            id = "t1",
            title = "Todo With Checklist",
            notes = "",
            difficulty = Difficulty.Easy,
            completed = false,
            tags = emptyList(),
            checklist = listOf(item1, item2)
        )
        val state = createBaseState().copy(todos = listOf(todo))
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        // Flip c1 from false to true
        viewModel.toggleTodoChecklistItem("t1", "c1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.characterState.value
        val updatedTodo = updatedState?.todos?.find { it.id == "t1" }
        assertTrue(updatedTodo?.checklist?.find { it.id == "c1" }?.completed == true)
        assertTrue(updatedTodo?.checklist?.find { it.id == "c2" }?.completed == true)

        // Flip c2 from true to false
        viewModel.toggleTodoChecklistItem("t1", "c2")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState2 = viewModel.characterState.value
        val updatedTodo2 = updatedState2?.todos?.find { it.id == "t1" }
        assertFalse(updatedTodo2?.checklist?.find { it.id == "c2" }?.completed == true)

        db.close()
    }

    @Test
    fun systemLogs_surviveWildernessAchievement_logsCorrectText() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())

        val skills = listOf(com.iurispraecepta.herolog.model.Skill(name = "Programação", level = 1, xp = 0))
        val state = createBaseState().copy(achievements = emptyList(), skills = skills)
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "s1",
                sessionCompletedAt = 1_000L,
                skillName = "Programação",
                durationMinutes = 25,
                xpGained = 50,
                goldGained = 75,
                achievementsUnlocked = "[\"wilderness_5\"]"
            )
        )
        FocusSessionService.setStateForTests(ServiceState(phase = ServicePhase.COMPLETED))
        testDispatcher.scheduler.runCurrent()

        val logs = viewModel.systemLogs.value
        val wildernessLog = logs.find { it.text.contains("Explorador do Ermo") }
        assertNotNull(wildernessLog)
        assertEquals(
            "🏆 Conquista heroica: desbloqueada runa especial [Explorador do Ermo]!",
            wildernessLog?.text
        )
        assertTrue(wildernessLog?.highlighted == true)

        db.close()
    }

    @Test
    fun systemLogs_combatLevelUp_logsSingleUnifiedText() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())

        val state = createBaseState().copy(combatLevel = 1)
        repository.saveCharacterState(state)

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveCharacterState(state.copy(combatLevel = 2))
        testDispatcher.scheduler.advanceUntilIdle()

        val logs = viewModel.systemLogs.value
        val combatLogs = logs.filter { it.text.contains("COMBAT LEVEL UP") }
        assertEquals(1, combatLogs.size)
        assertEquals("🆙 COMBAT LEVEL UP: Nível de combate subiu para 2!", combatLogs[0].text)
        assertTrue(combatLogs[0].highlighted)

        db.close()
    }

    @Test
    fun systemLogs_rollover_shieldConsumedAndDamage_logsCorrectly() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())

        val previousDayStr = "Tue Aug 11 2026"
        val todayStr = "Wed Aug 12 2026"
        val dailyIncomplete = Daily(
            id = "d1",
            title = "Exercício",
            notes = "",
            difficulty = Difficulty.Medium,
            completed = false,
            streak = 5,
            repeats = RepeatInterval.Daily,
            every = 1,
            tags = emptyList(),
            checklist = emptyList(),
            value = 0,
            createdAt = "2026-08-11T10:00:00Z"
        )
        val shieldItem = com.iurispraecepta.herolog.model.InventoryItem(
            id = "shield1",
            name = "Escudo de Streak",
            emoji = "🛡️",
            buff = com.iurispraecepta.herolog.model.BuffType.StreakShield,
            price = 50,
            desc = "Protege contra o esquecimento"
        )
        val stateWithShield = createBaseState().copy(
            todayDate = previousDayStr,
            inventory = listOf(shieldItem),
            dailies = listOf(dailyIncomplete)
        )
        repository.saveCharacterState(stateWithShield)

        val jsDateRef = java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.US).parse(todayStr)!!.time
        val viewModelWithShield = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { jsDateRef },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val logsShield = viewModelWithShield.systemLogs.value
        val shieldLog = logsShield.find { it.text.contains("Escudo de Streak ativado") }
        assertNotNull(shieldLog)
        assertEquals("🛡️ Escudo de Streak ativado! Oração do Santuário absorveu o choque de 1 diárias negligenciadas de ontem.", shieldLog?.text)

        // Cenario 2: Rollover sem escudo com dano solar
        val stateWithoutShield = createBaseState().copy(
            todayDate = previousDayStr,
            inventory = emptyList(),
            hp = 100,
            dailies = listOf(dailyIncomplete)
        )
        repository.saveCharacterState(stateWithoutShield)

        val viewModelDamage = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { jsDateRef },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val logsDamage = viewModelDamage.systemLogs.value
        val damageLog = logsDamage.find { it.text.contains("Dano Solar da Negligência") }
        assertNotNull(damageLog)
        assertTrue(damageLog!!.text.contains("💀 Dano Solar da Negligência: Deixaste 1 Diárias incompletas ontem! Perdeste -"))
        assertFalse(damageLog.highlighted)

        db.close()
    }

    @Test
    fun abandonSession_inDungeonMode_logsDungeonFailureAndResetsProgress() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val initialChar = createBaseState()
        repository.saveCharacterState(initialChar)

        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 2
        )
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.advanceUntilIdle()

        val logsBefore = viewModel.systemLogs.value
        assertEquals(0, logsBefore.count { it.text.contains("FRACASSO NA MASMORRA") })

        viewModel.abandonSession(testContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val logsAfter = viewModel.systemLogs.value
        val dungeonFailureLog = logsAfter.find { it.text.contains("FRACASSO NA MASMORRA") }
        assertNotNull(dungeonFailureLog)
        assertTrue(dungeonFailureLog!!.highlighted)
        assertTrue(dungeonFailureLog.text.contains("💀 FRACASSO NA MASMORRA: Ao abandonar, sua expedição na Masmorra colapsou"))
        assertEquals(0, viewModel.dungeonSessionsProgress.value)

        db.close()
    }

    @Test
    fun abandonSession_inStandardMode_doesNotLogDungeonFailure() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val initialChar = createBaseState()
        repository.saveCharacterState(initialChar)

        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val config = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(testContext, config, durationMinutes = 25)
        testDispatcher.scheduler.advanceUntilIdle()

        val countBefore = viewModel.systemLogs.value.size
        viewModel.abandonSession(testContext)
        testDispatcher.scheduler.advanceUntilIdle()

        val logsAfter = viewModel.systemLogs.value
        assertEquals(countBefore, logsAfter.size)
        assertNull(logsAfter.find { it.text.contains("FRACASSO NA MASMORRA") })

        db.close()
    }

    // ── Spec-001: anúncios de conquistas fora do foco (FR-009/FR-010) ─────────

    @Test
    fun triggerHabit_crossingStreak7_enqueuesAnnouncementAndPersistsId() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Meditar",
            notes = "",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            upCount = 0,
            downCount = 0,
            streak = 6,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        repository.saveCharacterState(createBaseState().copy(habits = listOf(habit)))

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("h1", isUp = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // ID persistido
        assertTrue(viewModel.characterState.value!!.achievements.contains("habit_streak_7"))
        // Anúncio na fila efêmera
        val queue = viewModel.achievementAnnouncementQueue.value
        assertEquals(1, queue.size)
        assertEquals("habit_streak_7", queue.first().id)

        db.close()
    }

    @Test
    fun triggerHabit_belowStreak7_doesNotAnnounce() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Meditar",
            notes = "",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            upCount = 0,
            downCount = 0,
            streak = 2,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        repository.saveCharacterState(createBaseState().copy(habits = listOf(habit)))

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("h1", isUp = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.characterState.value!!.achievements.contains("habit_streak_7"))
        assertTrue(viewModel.achievementAnnouncementQueue.value.isEmpty())

        db.close()
    }

    @Test
    fun triggerHabit_alreadyUnlocked_noDuplicateAnnouncement() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Meditar",
            notes = "",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            upCount = 0,
            downCount = 0,
            streak = 7,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        repository.saveCharacterState(
            createBaseState().copy(
                habits = listOf(habit),
                achievements = listOf("habit_streak_7")
            )
        )

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("h1", isUp = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.characterState.value!!.achievements.count { it == "habit_streak_7" })
        assertTrue(viewModel.achievementAnnouncementQueue.value.isEmpty())

        db.close()
    }

    @Test
    fun buyTitle_fifthTitle_enqueuesArsenalDeTitulos() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        repository.saveCharacterState(
            createBaseState().copy(
                gold = 10000,
                ownedTitles = listOf("t1", "t2", "t3", "t4")
            )
        )

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.buyTitle("t5", price = 100)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.characterState.value!!.achievements.contains("titles_5"))
        assertEquals("titles_5", viewModel.achievementAnnouncementQueue.value.firstOrNull()?.id)

        db.close()
    }

    @Test
    fun claimAchievementTitle_fifthTitle_enqueuesArsenalDeTitulos() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        repository.saveCharacterState(
            createBaseState().copy(
                ownedTitles = listOf("t1", "t2", "t3", "t4")
            )
        )

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.claimAchievementTitle("t5")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.characterState.value!!.achievements.contains("titles_5"))
        assertEquals("titles_5", viewModel.achievementAnnouncementQueue.value.firstOrNull()?.id)

        db.close()
    }

    @Test
    fun dismissNextAchievementAnnouncement_removesOnlyFromQueue_keepsPersistedId() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val habit = Habit(
            id = "h1",
            title = "Meditar",
            notes = "",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            upCount = 0,
            downCount = 0,
            streak = 6,
            tags = emptyList(),
            lastTriggeredDate = null
        )
        repository.saveCharacterState(createBaseState().copy(habits = listOf(habit)))

        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerHabit("h1", isUp = true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.achievementAnnouncementQueue.value.size)

        viewModel.dismissNextAchievementAnnouncement()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.achievementAnnouncementQueue.value.isEmpty())
        // A conquista permanece persistida — fechar o anúncio nunca revoga
        assertTrue(viewModel.characterState.value!!.achievements.contains("habit_streak_7"))

        db.close()
    }

    // ── Spec notificações por modo: abandono pela notificação (FR-10, T5) ────

    private fun dungeonProgressTo(progress: Int, viewModel: HeroLogViewModel) {
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.COMPLETED,
                lastSessionDungeonMode = true,
                lastSessionDungeonSessions = progress - 1
            )
        )
        testDispatcher.scheduler.runCurrent()
    }

    @Test
    fun serviceAbandon_whenDungeonMode_resetsProgressAndLogsFailure() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "d1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        dungeonProgressTo(2, viewModel)
        assertEquals(2, viewModel.dungeonSessionsProgress.value)

        // Sessão de Masmorra em andamento, abandonada pelo botão da notificação.
        FocusSessionService.setStateForTests(ServiceState())
        val dungeonConfig = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 2
        )
        viewModel.startSession(testContext, dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        val logsBefore = viewModel.systemLogs.value.size
        FocusSessionService.emitAbandonForTests(
            com.iurispraecepta.herolog.service.AbandonEvent(isDungeonMode = true)
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.dungeonSessionsProgress.value)
        assertEquals(logsBefore + 1, viewModel.systemLogs.value.size)
        assertTrue(viewModel.systemLogs.value.first().text.contains("FRACASSO NA MASMORRA"))

        db.close()
    }

    @Test
    fun serviceAbandon_whenStandardMode_appliesNoConsequence() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "d1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        dungeonProgressTo(1, viewModel)
        assertEquals(1, viewModel.dungeonSessionsProgress.value)

        viewModel.startSession(
            testContext,
            FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0),
            durationMinutes = 10
        )
        testDispatcher.scheduler.runCurrent()
        val logsBefore = viewModel.systemLogs.value.size

        viewModel.onServiceAbandon(
            com.iurispraecepta.herolog.service.AbandonEvent(isDungeonMode = false)
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, viewModel.dungeonSessionsProgress.value)
        assertEquals(logsBefore, viewModel.systemLogs.value.size)

        db.close()
    }

    @Test
    fun serviceAbandon_staleEvent_isIgnored() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.runCurrent()
        val logsBefore = viewModel.systemLogs.value.size

        // Evento de outra vida do processo (anterior à criação deste ViewModel).
        FocusSessionService.emitAbandonForTests(
            com.iurispraecepta.herolog.service.AbandonEvent(
                isDungeonMode = true,
                emittedAtMillis = -1L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals(logsBefore, viewModel.systemLogs.value.size)

        db.close()
    }

    @Test
    fun cancelSession_whenDungeonMode_appliesNoDungeonConsequence() = runTest {
        // ACTION_STOP segue genérico e sem consequência (FR-10): só o abandono
        // (in-app ou via ACTION_ABANDON) zera o progresso e registra o fracasso.
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepository = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime },
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepository
        )
        testDispatcher.scheduler.runCurrent()

        pendingRepository.queue(
            PendingRewardCelebrationEntity(
                id = "d1",
                sessionCompletedAt = 1_000L,
                skillName = "Kotlin",
                durationMinutes = 10,
                xpGained = 20,
                goldGained = 30
            )
        )
        dungeonProgressTo(1, viewModel)
        assertEquals(1, viewModel.dungeonSessionsProgress.value)

        viewModel.startSession(
            testContext,
            FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = true, dungeonSessions = 1),
            durationMinutes = 10
        )
        testDispatcher.scheduler.runCurrent()
        val logsBefore = viewModel.systemLogs.value.size

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, viewModel.dungeonSessionsProgress.value)
        assertEquals(logsBefore, viewModel.systemLogs.value.size)

        db.close()
    }

    // ── Spec notificações por modo: Morte Cognitiva pós-fato (FR-9, T15) ──────

    private fun grantPostNotifications() {
        Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun notificationManager(): android.app.NotificationManager =
        testContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager

    private fun activeDeathNotifications(): List<android.service.notification.StatusBarNotification> =
        notificationManager().activeNotifications.filter {
            it.id == com.iurispraecepta.herolog.service.FocusNotifications.ID_COGNITIVE_DEATH
        }

    @Test
    fun cognitiveDeath_postsDeathNotificationOnceAndClearsTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        grantPostNotifications()
        com.iurispraecepta.herolog.service.FocusNotifications.createChannels(testContext)
        notificationManager().cancelAll()
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(
            repository, focusRepository, clock = { fakeTime }, sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.saveCharacterState(
            createBaseState().copy(charClass = CharClass.Warrior, streak = 10, combo = 5)
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(
            testContext,
            FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0),
            durationMinutes = 25
        )
        testDispatcher.scheduler.runCurrent()
        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isGraceActive)

        fakeTime += 4000L
        testDispatcher.scheduler.advanceTimeBy(3000L)
        testDispatcher.scheduler.runCurrent()

        val posted = activeDeathNotifications()
        assertEquals(1, posted.size)
        val text = posted.first().notification.extras
            .getCharSequence(android.app.Notification.EXTRA_TEXT).toString()
        assertTrue(text.contains("sequência foi zerada"))
        // A notificação de sessão foi cancelada no mesmo fluxo.
        assertTrue(
            notificationManager().activeNotifications.none {
                it.id == com.iurispraecepta.herolog.service.FocusNotifications.ID_TIMER
            }
        )

        // Tempo extra não reposta: uma única vez.
        fakeTime += 10_000L
        testDispatcher.scheduler.advanceTimeBy(10_000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, activeDeathNotifications().size)

        notificationManager().cancelAll()
        db.close()
    }

    @Test
    fun cognitiveDeath_returningWithinGrace_postsNothing() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        grantPostNotifications()
        com.iurispraecepta.herolog.service.FocusNotifications.createChannels(testContext)
        notificationManager().cancelAll()
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(
            repository, focusRepository, clock = { fakeTime }, sfxManager = SfxManager.noOp()
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(
            testContext,
            FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0),
            durationMinutes = 25
        )
        testDispatcher.scheduler.runCurrent()
        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isGraceActive)

        viewModel.onAppForegrounded()
        testDispatcher.scheduler.runCurrent()
        fakeTime += 10_000L
        testDispatcher.scheduler.advanceTimeBy(10_000L)
        testDispatcher.scheduler.runCurrent()

        assertTrue(activeDeathNotifications().isEmpty())

        viewModel.cancelSession(testContext)
        testDispatcher.scheduler.runCurrent()
        notificationManager().cancelAll()
        db.close()
    }

    @Test
    fun cognitiveDeath_withDeathProof_postsNothing() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        grantPostNotifications()
        com.iurispraecepta.herolog.service.FocusNotifications.createChannels(testContext)
        notificationManager().cancelAll()
        val viewModel = HeroLogViewModel(repository, focusRepository, sfxManager = SfxManager.noOp())
        testDispatcher.scheduler.runCurrent()

        viewModel.saveCharacterState(createBaseState().copy(equippedTitle = "DEATH-PROOF"))
        testDispatcher.scheduler.runCurrent()
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0),
                durationMinutes = 25,
                endTimeMillis = 1_500_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded(testContext)
        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceTimeBy(10_000L)
        testDispatcher.scheduler.runCurrent()

        // DEATH-PROOF converteu a saída em pausa: sem morte, sem notificação.
        assertTrue(activeDeathNotifications().isEmpty())

        releaseMirrorTicker()
        notificationManager().cancelAll()
        db.close()
    }
}
