package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HeroLogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
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
        val viewModel = HeroLogViewModel(repository, focusRepository)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.characterState.value
        // 200 inicial + 100 login reward para Mage
        assertEquals(300, state?.gold)
        assertEquals("Aventureiro do Foco", state?.charName)
        assertTrue(state?.hasClaimedLogin == true)

        // Confirma que persistiu de verdade - novo ViewModel no mesmo banco nao recria, so recarrega
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(state, secondViewModel.characterState.value)

        db.close()
    }

    @Test
    fun viewModel_unequipItem_movesItemFromEquipmentToInventory() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = createBaseState().copy(charName = "Aethelgard", gold = 500)
        viewModel.saveCharacterState(state)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, viewModel.characterState.value)

        // Novo ViewModel, mesmo banco - prova persistência real, não só estado em memória do primeiro objeto
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, secondViewModel.characterState.value)
        db.close()
    }

    @Test
    fun viewModel_addCustomSkill_success_persistsSkills() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isRunning)

        // Try to delete skill while running
        val eligibility = viewModel.deleteSkill(0)
        testDispatcher.scheduler.runCurrent()

        assertEquals(DeleteSkillEligibility.Blocked, eligibility)
        assertEquals(2, viewModel.characterState.value?.skills?.size) // Still 2

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_deleteSkill_eligibleAndDeletesSuccessfully() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(streak = 5, combo = 10)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        // Start session with Wilderness checked
        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isRunning)

        // App backgrounded
        viewModel.onAppBackgrounded()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isGraceActive)
        assertEquals(3, viewModel.focusSessionState.value.graceSecondsLeft)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_onAppBackgrounded_withDeathProofTitle_convertsToPauseInsteadOfGrace() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(equippedTitle = "DEATH-PROOF")
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded()
        testDispatcher.scheduler.runCurrent()

        // Converted to pause!
        assertTrue(viewModel.focusSessionState.value.isPaused)
        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isGraceActive)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_onAppBackgrounded_withoutWildernessChecked_isNoOp() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded()
        testDispatcher.scheduler.runCurrent()

        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isGraceActive)
        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isPaused)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_gracePeriodExpiring_triggersCognitiveDeath() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1000000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        val baseState = createBaseState().copy(
            charClass = CharClass.Warrior,
            streak = 10,
            combo = 5
        )
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded()
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
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()

        viewModel.onAppBackgrounded()
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isGraceActive)

        // Foreground app
        viewModel.onAppForegrounded()
        testDispatcher.scheduler.runCurrent()

        org.junit.Assert.assertFalse(viewModel.focusSessionState.value.isGraceActive)
        assertEquals(3, viewModel.focusSessionState.value.graceSecondsLeft)
        assertTrue(viewModel.focusSessionState.value.isRunning)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_respawnHero_appliesPenalties_restoresHp_clearsDead_andResetsFocusSessionState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
    fun breakTimer_startBreakTimer_setsBreakActive_andCountsDownCorrectly() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1_000_000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        viewModel.startBreakTimer(5)
        testDispatcher.scheduler.runCurrent()

        val breakStateInitial = viewModel.breakTimerState.value
        assertTrue(breakStateInitial.isBreakActive)
        org.junit.Assert.assertFalse(breakStateInitial.isBreakPrep)
        assertEquals(5, breakStateInitial.selectedBreakMins)
        assertEquals(300, breakStateInitial.secondsLeft)
        assertEquals(300, breakStateInitial.totalSeconds)

        // Advance 2 seconds
        fakeTime += 2000L
        testDispatcher.scheduler.advanceTimeBy(2000L)
        testDispatcher.scheduler.runCurrent()

        val breakStateAfter2s = viewModel.breakTimerState.value
        assertTrue(breakStateAfter2s.isBreakActive)
        assertEquals(298, breakStateAfter2s.secondsLeft)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun breakTimer_skipBreak_resetsToDefaultState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.runCurrent()

        viewModel.startBreakTimer(10)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.breakTimerState.value.isBreakActive)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.breakTimerState.value
        org.junit.Assert.assertFalse(state.isBreakActive)
        org.junit.Assert.assertFalse(state.isBreakPrep)
        assertEquals(0, state.secondsLeft)
        assertEquals(0, state.totalSeconds)

        db.close()
    }

    @Test
    fun breakTimer_reachingZero_completesBreakAndDeactivates() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1_000_000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
        testDispatcher.scheduler.runCurrent()

        viewModel.startBreakTimer(1) // 60s
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.breakTimerState.value.isBreakActive)

        fakeTime += 60_000L
        testDispatcher.scheduler.advanceTimeBy(60_000L)
        testDispatcher.scheduler.runCurrent()

        val finalState = viewModel.breakTimerState.value
        org.junit.Assert.assertFalse(finalState.isBreakActive)
        assertEquals(0, finalState.secondsLeft)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_withAutoStartBreakTrue_standardSession_startsShortBreakTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1_000_000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
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

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(config, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        fakeTime += 600_000L
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isFocusCompleted)

        viewModel.confirmFocusSession(editedNotes = "Done", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        val breakState = viewModel.breakTimerState.value
        assertTrue(breakState.isBreakActive)
        assertEquals(5, breakState.selectedBreakMins)
        assertEquals(300, breakState.secondsLeft)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_withAutoStartBreakTrue_fourthDungeonSession_startsLongBreakTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1_000_000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
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

        val dungeonConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 3 // 4th session completing
        )
        viewModel.startSession(dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        fakeTime += 600_000L
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isFocusCompleted)

        viewModel.confirmFocusSession(editedNotes = "Dungeon Clear", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        val breakState = viewModel.breakTimerState.value
        assertTrue(breakState.isBreakActive)
        assertEquals(20, breakState.selectedBreakMins)
        assertEquals(1200, breakState.secondsLeft)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_withAutoStartBreakFalse_entersBreakPrepInsteadOfTimer() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        var fakeTime = 1_000_000L
        val viewModel = HeroLogViewModel(repository, focusRepository, clock = { fakeTime })
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

        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(config, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        fakeTime += 600_000L
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.focusSessionState.value.isFocusCompleted)

        viewModel.confirmFocusSession(editedNotes = "Manual break", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        val breakState = viewModel.breakTimerState.value
        org.junit.Assert.assertFalse(breakState.isBreakActive)
        assertTrue(breakState.isBreakPrep)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun changeFocusDuration_persistsNewFocusDuration_whenIdle() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(50, secondViewModel.characterState.value?.pomodoroSettings?.focusDuration)

        db.close()
    }

    @Test
    fun changeFocusDuration_isNoOp_whenSessionRunningOrBreakActive() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        viewModel.startSession(config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isRunning)

        // Attempt to change duration while running -> should be no-op
        viewModel.changeFocusDuration(90)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        // Cancel session
        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()

        // Start break timer
        viewModel.startBreakTimer(5)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.breakTimerState.value.isBreakActive)

        // Attempt to change duration while break is active -> should be no-op
        viewModel.changeFocusDuration(90)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun saveCustomTimerSettings_persistsCustomValues_whenIdle() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
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
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        viewModel.startSession(config, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        // Attempt saveCustomTimerSettings while running -> no-op
        viewModel.saveCustomTimerSettings(focusMinutes = 60, shortBreakMinutes = 10, longBreakMinutes = 30)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()

        // Start break timer
        viewModel.startBreakTimer(5)
        testDispatcher.scheduler.runCurrent()

        // Attempt saveCustomTimerSettings while break active -> no-op
        viewModel.saveCustomTimerSettings(focusMinutes = 60, shortBreakMinutes = 10, longBreakMinutes = 30)
        testDispatcher.scheduler.runCurrent()
        assertEquals(25, viewModel.characterState.value?.pomodoroSettings?.focusDuration)

        viewModel.skipBreak()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun toggleAutoStartBreak_and_toggleAutoStartFocus_invertsAndPersists() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
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
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
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
            clock = { testDispatcher.scheduler.currentTime }
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
    fun confirmFocusSession_setsWasLastSessionDungeonMode_inBreakPrepState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
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

        // Dungeon mode session
        val dungeonConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 0
        )
        viewModel.startSession(dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.confirmFocusSession(editedNotes = "", selectedTag = "")
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
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val dungeonConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 2
        )
        viewModel.startSession(dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        viewModel.abandonSession()
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.dungeonSessionsProgress.value)
        assertEquals(FocusSessionState(), viewModel.focusSessionState.value)
        assertNull(focusRepository.getSession())

        db.close()
    }

    @Test
    fun abandonSession_whenNotDungeonMode_leavesDungeonSessionsProgressUnchanged() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        // Complete 1 dungeon session to advance progress to 1
        val dungeonConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 0
        )
        viewModel.startSession(dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()
        viewModel.confirmFocusSession(editedNotes = "", selectedTag = "")
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.dungeonSessionsProgress.value)

        // Start standard session and abandon
        val standardConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(standardConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        viewModel.abandonSession()
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, viewModel.dungeonSessionsProgress.value)

        db.close()
    }

    @Test
    fun abandonSession_alwaysClearsFocusSessionStateAndPersistedSession() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val standardConfig = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = false,
            dungeonSessions = 0
        )
        viewModel.startSession(standardConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()
        assertNotNull(focusRepository.getSession())

        viewModel.abandonSession()
        testDispatcher.scheduler.runCurrent()

        assertEquals(FocusSessionState(), viewModel.focusSessionState.value)
        assertNull(focusRepository.getSession())

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
            clock = { jsDateRef }
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
            clock = { jsDateRef }
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
            clock = { jsDateRef }
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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

        val viewModel = HeroLogViewModel(repository, focusRepository)
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
}
