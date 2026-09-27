package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.logic.focus.aggregateCelebrations
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.Rarity
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CelebrationAggregationTest {

    private val json = JsonConfig.default

    private fun entity(
        id: String,
        xp: Int,
        gold: Int,
        leveledUp: Boolean = false,
        previousLevel: Int? = null,
        newLevel: Int? = null,
        achievements: List<String> = emptyList(),
        loot: List<LootItem> = emptyList(),
        completedAt: Long = 1_000L
    ) = PendingRewardCelebrationEntity(
        id = id,
        sessionCompletedAt = completedAt,
        skillName = "Kotlin",
        durationMinutes = 25,
        xpGained = xp,
        goldGained = gold,
        lootedItems = json.encodeToString(loot),
        leveledUp = leveledUp,
        previousLevel = previousLevel,
        newLevel = newLevel,
        achievementsUnlocked = json.encodeToString(achievements)
    )

    private fun loot(name: String) = LootItem(
        name = name,
        emoji = "🔮",
        desc = "loot de teste",
        buff = BuffType.RuneFortune,
        price = 75,
        isEquipment = false,
        charges = 1,
        maxCharges = 1,
        rarity = Rarity.Comum
    )

    @Test
    fun aggregate_singleSession_reportsOwnTotalsAndItselfAsBreakdown() {
        val sessions = listOf(entity("s1", xp = 60, gold = 90))

        val summary = aggregateCelebrations(sessions, json)

        assertEquals(1, summary.sessionCount)
        assertEquals(60, summary.totalXp)
        assertEquals(90, summary.totalGold)
        assertFalse(summary.leveledUp)
        assertNull(summary.initialLevel)
        assertNull(summary.finalLevel)
        assertTrue(summary.allAchievements.isEmpty())
        assertTrue(summary.allLootItems.isEmpty())
        assertEquals(listOf("s1"), summary.sessions.map { it.id })
    }

    @Test
    fun aggregate_threeSessions_sumsXpAndGold() {
        val sessions = listOf(
            entity("s1", xp = 60, gold = 90, completedAt = 1_000L),
            entity("s2", xp = 50, gold = 75, completedAt = 2_000L),
            entity("s3", xp = 70, gold = 105, completedAt = 3_000L)
        )

        val summary = aggregateCelebrations(sessions, json)

        assertEquals(3, summary.sessionCount)
        assertEquals(180, summary.totalXp)
        assertEquals(270, summary.totalGold)
        assertEquals(listOf("s1", "s2", "s3"), summary.sessions.map { it.id })
    }

    @Test
    fun aggregate_levelUpInMiddle_reportsFirstPreviousAndLastNewLevel() {
        val sessions = listOf(
            entity("s1", xp = 60, gold = 90),
            entity("s2", xp = 60, gold = 90, leveledUp = true, previousLevel = 2, newLevel = 3),
            entity("s3", xp = 60, gold = 90, leveledUp = true, previousLevel = 3, newLevel = 4)
        )

        val summary = aggregateCelebrations(sessions, json)

        assertTrue(summary.leveledUp)
        assertEquals(2, summary.initialLevel)
        assertEquals(4, summary.finalLevel)
    }

    @Test
    fun aggregate_noLevelUp_reportsNullLevels() {
        val sessions = listOf(
            entity("s1", xp = 60, gold = 90),
            entity("s2", xp = 50, gold = 75)
        )

        val summary = aggregateCelebrations(sessions, json)

        assertFalse(summary.leveledUp)
        assertNull(summary.initialLevel)
        assertNull(summary.finalLevel)
    }

    @Test
    fun aggregate_duplicateAchievements_deduplicatesById() {
        val sessions = listOf(
            entity("s1", xp = 60, gold = 90, achievements = listOf("first_focus", "streak_7")),
            entity("s2", xp = 50, gold = 75, achievements = listOf("streak_7", "xp_1000"))
        )

        val summary = aggregateCelebrations(sessions, json)

        assertEquals(listOf("first_focus", "streak_7", "xp_1000"), summary.allAchievements)
    }

    @Test
    fun aggregate_lootFromMultipleSessions_concatenatesAllItems() {
        val sessions = listOf(
            entity("s1", xp = 60, gold = 90, loot = listOf(loot("Runa A"))),
            entity("s2", xp = 50, gold = 75),
            entity("s3", xp = 70, gold = 105, loot = listOf(loot("Runa B"), loot("Runa C")))
        )

        val summary = aggregateCelebrations(sessions, json)

        assertEquals(listOf("Runa A", "Runa B", "Runa C"), summary.allLootItems.map { it.name })
    }
}
