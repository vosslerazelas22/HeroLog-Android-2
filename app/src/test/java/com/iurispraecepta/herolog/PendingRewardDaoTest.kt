package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PendingRewardDaoTest {

    private fun createInMemoryDatabase(): HeroLogDatabase {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return Room.inMemoryDatabaseBuilder(context, HeroLogDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
    }

    private fun entity(id: String, completedAt: Long = 1_000L) =
        PendingRewardCelebrationEntity(
            id = id,
            sessionCompletedAt = completedAt,
            skillName = "Kotlin",
            durationMinutes = 25,
            xpGained = 60,
            goldGained = 90
        )

    @Test
    fun insert_twoRows_getAllUnconsumedReturnsBothOrderedByCompletion() = runTest {
        val db = createInMemoryDatabase()
        val dao = db.pendingRewardCelebrationDao()
        dao.insert(entity("s2", completedAt = 2_000L))
        dao.insert(entity("s1", completedAt = 1_000L))

        val rows = dao.getAllUnconsumed()

        assertEquals(listOf("s1", "s2"), rows.map { it.id })
        db.close()
    }

    @Test
    fun markAllConsumed_hidesRowsFromUnconsumed() = runTest {
        val db = createInMemoryDatabase()
        val dao = db.pendingRewardCelebrationDao()
        dao.insert(entity("s1", completedAt = 1_000L))
        dao.insert(entity("s2", completedAt = 2_000L))

        dao.markAllConsumed(listOf("s1", "s2"))

        assertTrue(dao.getAllUnconsumed().isEmpty())
        assertEquals(0, dao.countUnconsumed())
        db.close()
    }

    @Test
    fun markAllConsumed_partialIds_leavesOthersVisible() = runTest {
        val db = createInMemoryDatabase()
        val dao = db.pendingRewardCelebrationDao()
        dao.insert(entity("s1", completedAt = 1_000L))
        dao.insert(entity("s2", completedAt = 2_000L))

        dao.markAllConsumed(listOf("s1"))

        assertEquals(listOf("s2"), dao.getAllUnconsumed().map { it.id })
        db.close()
    }

    @Test
    fun markAllConsumed_emptyList_noOpWithoutCrash() = runTest {
        val db = createInMemoryDatabase()
        val dao = db.pendingRewardCelebrationDao()
        dao.insert(entity("s1"))

        dao.markAllConsumed(emptyList())

        assertEquals(1, dao.countUnconsumed())
        db.close()
    }
}
