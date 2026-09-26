package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.dao.PendingRewardCelebrationDao
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePendingRewardCelebrationDao : PendingRewardCelebrationDao {
    private val rows = mutableListOf<PendingRewardCelebrationEntity>()
    val markAllConsumedCalls = mutableListOf<List<String>>()

    override suspend fun insert(entity: PendingRewardCelebrationEntity) {
        rows.add(entity)
    }

    override suspend fun getAllUnconsumed(): List<PendingRewardCelebrationEntity> {
        return rows.filter { !it.consumed }.sortedBy { it.sessionCompletedAt }
    }

    override suspend fun countUnconsumed(): Int {
        return rows.count { !it.consumed }
    }

    override suspend fun markConsumed(ids: List<String>) {
        val idSet = ids.toSet()
        rows.replaceAll { if (it.id in idSet) it.copy(consumed = true) else it }
    }

    override suspend fun markAllConsumed(ids: List<String>) {
        markAllConsumedCalls.add(ids)
        if (ids.isNotEmpty()) {
            markConsumed(ids)
        }
    }
}

class PendingRewardRepositoryTest {

    private fun entity(id: String, xp: Int = 60, gold: Int = 90, completedAt: Long = 1_000L) =
        PendingRewardCelebrationEntity(
            id = id,
            sessionCompletedAt = completedAt,
            skillName = "Kotlin",
            durationMinutes = 25,
            xpGained = xp,
            goldGained = gold
        )

    @Test
    fun queue_singleSession_getPendingReturnsOneUnconsumed() = runTest {
        val repository = PendingRewardRepository(FakePendingRewardCelebrationDao())

        repository.queue(entity("s1"))

        val pending = repository.getPending()
        assertEquals(1, pending.size)
        assertEquals("s1", pending.first().id)
        assertEquals(60, pending.first().xpGained)
        assertEquals(90, pending.first().goldGained)
    }

    @Test
    fun queue_threeSessions_accumulatesWithoutOverwrite() = runTest {
        val repository = PendingRewardRepository(FakePendingRewardCelebrationDao())

        repository.queue(entity("s1", completedAt = 1_000L))
        repository.queue(entity("s2", completedAt = 2_000L))
        repository.queue(entity("s3", completedAt = 3_000L))

        assertEquals(3, repository.countPending())
        assertEquals(listOf("s1", "s2", "s3"), repository.getPending().map { it.id })
    }

    @Test
    fun consume_empty_returnsNullAndMarksNothing() = runTest {
        val dao = FakePendingRewardCelebrationDao()
        val repository = PendingRewardRepository(dao)

        val summary = repository.consumePendingCelebrations()

        assertNull(summary)
        assertTrue(dao.markAllConsumedCalls.isEmpty())
    }

    @Test
    fun consume_twoPending_returnsAggregatedSummaryAndHidesBoth() = runTest {
        val repository = PendingRewardRepository(FakePendingRewardCelebrationDao())
        repository.queue(entity("s1", xp = 60, gold = 90, completedAt = 1_000L))
        repository.queue(entity("s2", xp = 50, gold = 75, completedAt = 2_000L))

        val summary = repository.consumePendingCelebrations()

        assertNotNull(summary)
        assertEquals(2, summary!!.sessionCount)
        assertEquals(110, summary.totalXp)
        assertEquals(165, summary.totalGold)
        assertEquals(0, repository.countPending())
        assertTrue(repository.getPending().isEmpty())
    }

    @Test
    fun consume_twoPending_marksAllIdsInSingleAtomicCall() = runTest {
        val dao = FakePendingRewardCelebrationDao()
        val repository = PendingRewardRepository(dao)
        repository.queue(entity("s1", completedAt = 1_000L))
        repository.queue(entity("s2", completedAt = 2_000L))

        repository.consumePendingCelebrations()

        assertEquals(1, dao.markAllConsumedCalls.size)
        assertEquals(listOf("s1", "s2"), dao.markAllConsumedCalls.first().sorted())
    }
}
