package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.sfx.SfxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestsViewModelIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: HeroLogDatabase
    private lateinit var characterRepository: CharacterRepository
    private lateinit var focusSessionRepository: FocusSessionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, HeroLogDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        characterRepository = CharacterRepository(database.characterStateDao())
        focusSessionRepository = FocusSessionRepository(database.activeFocusSessionDao())
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun dailyQuests_generatesProcessedQuestsCorrectly() = runTest(testDispatcher) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = dateFormat.parse("2026-08-04")!!

        val state = createBaseState()
        val viewModel = HeroLogViewModel(characterRepository, focusSessionRepository, sfxManager = SfxManager.noOp())
        advanceUntilIdle()

        val dailyList = viewModel.dailyQuests(state, testDate)
        assertEquals(3, dailyList.size)
        dailyList.forEach { quest ->
            assertNotNull(quest.id)
            assertNotNull(quest.name)
            assertNotNull(quest.desc)
            assertTrue(quest.target > 0)
            assertTrue(quest.rewardGold > 0)
            assertTrue(quest.rewardXp > 0)
            assertEquals(quest.progress >= quest.target, quest.isCompleted)
            assertFalse(quest.isClaimed)
        }
    }

    @Test
    fun guildQuestsProcessed_generatesGuildQuestsCorrectly() = runTest(testDispatcher) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = dateFormat.parse("2026-08-04")!!

        val state = createBaseState().copy(
            combatLevel = 5,
            totalSessions = 12,
            bestStreak = 3
        )
        val viewModel = HeroLogViewModel(characterRepository, focusSessionRepository, sfxManager = SfxManager.noOp())
        advanceUntilIdle()

        val guildList = viewModel.guildQuestsProcessed(state, testDate)
        assertEquals(3, guildList.size)

        // Com esses atributos, todas as 3 guild quests devem estar completas e não resgatadas
        guildList.forEach { quest ->
            assertTrue(quest.isCompleted)
            assertFalse(quest.isClaimed)
        }
    }

    @Test
    fun claimQuestReward_updatesStateAndPersists() = runTest(testDispatcher) {
        val initialHero = createBaseState().copy(
            gold = 100,
            totalXP = 500,
            combatLevel = 1,
            combatXP = 0,
            hasClaimedLogin = true
        )
        characterRepository.saveCharacterState(initialHero)

        val viewModel = HeroLogViewModel(characterRepository, focusSessionRepository, sfxManager = SfxManager.noOp())
        advanceUntilIdle()

        val beforeClaim = viewModel.characterState.value!!
        val beforeGold = beforeClaim.gold
        val beforeTotalXp = beforeClaim.totalXP

        viewModel.claimQuestReward(
            questId = "guild_1",
            goldReward = 400,
            xpReward = 200
        )
        advanceUntilIdle()

        val updated = viewModel.characterState.value
        assertNotNull(updated)
        assertEquals(beforeGold + 400, updated!!.gold)
        assertEquals(beforeTotalXp + 200, updated.totalXP)
        assertTrue(updated.achievements.contains("claimed_guild_1"))

        // Verifica se persistiu no repositório
        val persisted = characterRepository.getCharacterState()
        assertNotNull(persisted)
        assertEquals(beforeGold + 400, persisted!!.gold)
        assertEquals(beforeTotalXp + 200, persisted.totalXP)
    }

    private fun createBaseState(): CharacterState {
        return CharacterState(
            gold = 100,
            totalXP = 500,
            totalGoldEarned = 200,
            totalSessions = 5,
            totalMinutes = 120,
            combatLevel = 1,
            combatXP = 0,
            skills = emptyList(),
            history = emptyList(),
            inventory = emptyList(),
            streak = 1,
            bestStreak = 2,
            lastStudyDate = "05/08/2026",
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
            todayDate = "Wed Aug 05 2026",
            hasClaimedLogin = true,
            hp = 100,
            maxHp = 100,
            habits = emptyList(),
            dailies = emptyList(),
            todos = emptyList(),
            pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
        )
    }
}
