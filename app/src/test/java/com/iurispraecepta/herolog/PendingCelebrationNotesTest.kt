package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.model.HistoryEntry
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Decisão 2 do plano-focus-completion-flow: o Confirm do fluxo completo single atualiza
 * notas/tag no HistoryEntry já gravado (correlação exata via historyId) e consome a fila.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [ShadowFocusSessionService::class])
class PendingCelebrationNotesTest {

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

    private data class Ctx(
        val db: HeroLogDatabase,
        val viewModel: HeroLogViewModel,
        val repository: CharacterRepository,
        val pendingRepo: PendingRewardRepository
    )

    private fun setUpCtx(): Ctx {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val pendingRepo = PendingRewardRepository(db.pendingRewardCelebrationDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            sfxManager = SfxManager.noOp(),
            pendingRewardRepository = pendingRepo
        )
        return Ctx(db, viewModel, repository, pendingRepo)
    }

    private suspend fun seedHistory(ctx: Ctx, entry: HistoryEntry) {
        testDispatcher.scheduler.advanceUntilIdle()
        val base = ctx.viewModel.characterState.value!!
        ctx.repository.saveCharacterState(base.copy(history = listOf(entry)))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun entry(id: String) = HistoryEntry(
        id = id,
        skillName = "Kotlin",
        date = "14/11/2023, 10:00:00",
        duration = 25,
        xp = 60,
        gold = 90,
        notes = "",
        wilderness = false,
        aiChronicle = null,
        subskillTag = null
    )

    private fun queued(id: String, historyId: String?) = PendingRewardCelebrationEntity(
        id = id,
        sessionCompletedAt = 1_000L,
        skillName = "Kotlin",
        durationMinutes = 25,
        xpGained = 60,
        goldGained = 90,
        historyId = historyId
    )

    @Test
    fun confirmWithNotes_updatesHistoryEntryAndConsumesQueue() = runTest {
        val ctx = setUpCtx()
        seedHistory(ctx, entry("h-1"))
        ctx.pendingRepo.queue(queued("s1", "h-1"))
        ctx.viewModel.refreshPendingCelebration()
        testDispatcher.scheduler.advanceUntilIdle()

        ctx.viewModel.confirmPendingCelebrationWithNotes("h-1", "  notas novas  ", "tag-a")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = ctx.repository.getCharacterState()!!
        val updatedEntry = updated.history.first { it.id == "h-1" }
        assertEquals("notas novas", updatedEntry.notes)
        assertEquals("tag-a", updatedEntry.subskillTag)
        assertEquals(0, ctx.pendingRepo.countPending())
        assertNull(ctx.viewModel.pendingCelebration.value)
        ctx.db.close()
    }

    @Test
    fun confirmWithNotes_blankTag_storesNull() = runTest {
        val ctx = setUpCtx()
        seedHistory(ctx, entry("h-1").copy(subskillTag = "antiga"))
        ctx.pendingRepo.queue(queued("s1", "h-1"))
        ctx.viewModel.refreshPendingCelebration()
        testDispatcher.scheduler.advanceUntilIdle()

        ctx.viewModel.confirmPendingCelebrationWithNotes("h-1", "n", "")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedEntry = ctx.repository.getCharacterState()!!.history.first { it.id == "h-1" }
        assertNull(updatedEntry.subskillTag)
        ctx.db.close()
    }

    @Test
    fun confirmWithNotes_unknownHistoryId_consumesWithoutUpdate() = runTest {
        val ctx = setUpCtx()
        seedHistory(ctx, entry("h-1"))
        ctx.pendingRepo.queue(queued("s1", "h-99"))
        ctx.viewModel.refreshPendingCelebration()
        testDispatcher.scheduler.advanceUntilIdle()

        ctx.viewModel.confirmPendingCelebrationWithNotes("h-99", "n", "t")
        testDispatcher.scheduler.advanceUntilIdle()

        val untouched = ctx.repository.getCharacterState()!!.history.first { it.id == "h-1" }
        assertEquals("", untouched.notes)
        assertNull(untouched.subskillTag)
        assertEquals(0, ctx.pendingRepo.countPending())
        ctx.db.close()
    }
}
