package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.achievements.Achievement
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCatalogTest {

    @Test
    fun achievementCatalogCount_is42() {
        assertEquals(42, AchievementCatalog.ACHIEVEMENTS_LIST.size)
    }

    @Test
    fun allAchievementIds_areUnique() {
        val ids = AchievementCatalog.ACHIEVEMENTS_LIST.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun noAchievementId_reusesClaimedGuildPrefix() {
        AchievementCatalog.ACHIEVEMENTS_LIST.forEach { achievement ->
            assertFalse(
                "ID ${achievement.id} não pode reutilizar prefixo claimed_guild_",
                achievement.id.startsWith("claimed_guild_")
            )
        }
    }

    @Test
    fun catalogContains_allExpectedIds_fromSpec() {
        val expectedIds = setOf(
            // Existentes (8)
            "first_quest", "streak_3", "streak_7", "xp_1000", "xp_10000",
            "gp_1000", "sessions_10", "survive_wilderness",
            // Constância (5)
            "streak_14", "streak_30", "streak_60", "streak_100", "streak_365",
            // Foco acumulado (6)
            "sessions_25", "sessions_50", "sessions_100", "sessions_250",
            "sessions_500", "sessions_1000",
            // Tempo acumulado (3)
            "minutes_100", "minutes_600", "minutes_6000",
            // XP acumulado (2)
            "xp_50000", "xp_100000",
            // GP acumulado (2)
            "gp_10000", "gp_50000",
            // Combate (4)
            "combat_5", "combat_10", "combat_25", "combat_50",
            // Skills (4)
            "skill_level_10", "skill_level_25", "skills_3_level_10", "prestige_any",
            // Wilderness (3)
            "wilderness_5", "wilderness_25", "wilderness_50",
            // Masmorras (3)
            "dungeon_1", "dungeon_10", "dungeon_25",
            // Coleção (1)
            "titles_5",
            // Hábitos (1)
            "habit_streak_7"
        )
        val actualIds = AchievementCatalog.ACHIEVEMENTS_LIST.map { it.id }.toSet()
        assertEquals(expectedIds, actualIds)
    }

    // ── Helpers de limiar ────────────────────────────────────────────────────

    private fun achievement(id: String): Achievement =
        AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == id }

    private fun assertThreshold(
        id: String,
        below: CharacterState,
        atOrAbove: CharacterState
    ) {
        val ach = achievement(id)
        assertFalse("$id deve estar bloqueado abaixo do limiar", ach.check(below))
        assertTrue("$id deve estar desbloqueado no limiar", ach.check(atOrAbove))
    }

    // ── Existentes ───────────────────────────────────────────────────────────

    @Test
    fun threshold_firstQuest() {
        assertThreshold(
            "first_quest",
            below = createBaseState().copy(totalSessions = 0),
            atOrAbove = createBaseState().copy(totalSessions = 1)
        )
    }

    @Test
    fun threshold_streak3() {
        assertThreshold(
            "streak_3",
            below = createBaseState().copy(bestStreak = 2),
            atOrAbove = createBaseState().copy(bestStreak = 3)
        )
    }

    @Test
    fun threshold_streak7() {
        assertThreshold(
            "streak_7",
            below = createBaseState().copy(bestStreak = 6),
            atOrAbove = createBaseState().copy(bestStreak = 7)
        )
    }

    @Test
    fun threshold_xp1000() {
        assertThreshold(
            "xp_1000",
            below = createBaseState().copy(totalXP = 999),
            atOrAbove = createBaseState().copy(totalXP = 1000)
        )
    }

    @Test
    fun threshold_xp10000() {
        assertThreshold(
            "xp_10000",
            below = createBaseState().copy(totalXP = 9999),
            atOrAbove = createBaseState().copy(totalXP = 10000)
        )
    }

    @Test
    fun threshold_gp1000() {
        assertThreshold(
            "gp_1000",
            below = createBaseState().copy(totalGoldEarned = 999),
            atOrAbove = createBaseState().copy(totalGoldEarned = 1000)
        )
    }

    @Test
    fun threshold_sessions10() {
        assertThreshold(
            "sessions_10",
            below = createBaseState().copy(totalSessions = 9),
            atOrAbove = createBaseState().copy(totalSessions = 10)
        )
    }

    @Test
    fun threshold_surviveWilderness() {
        assertThreshold(
            "survive_wilderness",
            below = createBaseState().copy(wildernessWins = 0),
            atOrAbove = createBaseState().copy(wildernessWins = 1)
        )
    }

    // ── Constância ───────────────────────────────────────────────────────────

    @Test
    fun threshold_streak14() {
        assertThreshold(
            "streak_14",
            below = createBaseState().copy(bestStreak = 13),
            atOrAbove = createBaseState().copy(bestStreak = 14)
        )
    }

    @Test
    fun threshold_streak30() {
        assertThreshold(
            "streak_30",
            below = createBaseState().copy(bestStreak = 29),
            atOrAbove = createBaseState().copy(bestStreak = 30)
        )
    }

    @Test
    fun threshold_streak60() {
        assertThreshold(
            "streak_60",
            below = createBaseState().copy(bestStreak = 59),
            atOrAbove = createBaseState().copy(bestStreak = 60)
        )
    }

    @Test
    fun threshold_streak100() {
        assertThreshold(
            "streak_100",
            below = createBaseState().copy(bestStreak = 99),
            atOrAbove = createBaseState().copy(bestStreak = 100)
        )
    }

    @Test
    fun threshold_streak365() {
        assertThreshold(
            "streak_365",
            below = createBaseState().copy(bestStreak = 364),
            atOrAbove = createBaseState().copy(bestStreak = 365)
        )
    }

    // ── Foco acumulado ───────────────────────────────────────────────────────

    @Test
    fun threshold_sessions25() {
        assertThreshold(
            "sessions_25",
            below = createBaseState().copy(totalSessions = 24),
            atOrAbove = createBaseState().copy(totalSessions = 25)
        )
    }

    @Test
    fun threshold_sessions50() {
        assertThreshold(
            "sessions_50",
            below = createBaseState().copy(totalSessions = 49),
            atOrAbove = createBaseState().copy(totalSessions = 50)
        )
    }

    @Test
    fun threshold_sessions100() {
        assertThreshold(
            "sessions_100",
            below = createBaseState().copy(totalSessions = 99),
            atOrAbove = createBaseState().copy(totalSessions = 100)
        )
    }

    @Test
    fun threshold_sessions250() {
        assertThreshold(
            "sessions_250",
            below = createBaseState().copy(totalSessions = 249),
            atOrAbove = createBaseState().copy(totalSessions = 250)
        )
    }

    @Test
    fun threshold_sessions500() {
        assertThreshold(
            "sessions_500",
            below = createBaseState().copy(totalSessions = 499),
            atOrAbove = createBaseState().copy(totalSessions = 500)
        )
    }

    @Test
    fun threshold_sessions1000() {
        assertThreshold(
            "sessions_1000",
            below = createBaseState().copy(totalSessions = 999),
            atOrAbove = createBaseState().copy(totalSessions = 1000)
        )
    }

    // ── Tempo acumulado ──────────────────────────────────────────────────────

    @Test
    fun threshold_minutes100() {
        assertThreshold(
            "minutes_100",
            below = createBaseState().copy(totalMinutes = 99),
            atOrAbove = createBaseState().copy(totalMinutes = 100)
        )
    }

    @Test
    fun threshold_minutes600() {
        assertThreshold(
            "minutes_600",
            below = createBaseState().copy(totalMinutes = 599),
            atOrAbove = createBaseState().copy(totalMinutes = 600)
        )
    }

    @Test
    fun threshold_minutes6000() {
        assertThreshold(
            "minutes_6000",
            below = createBaseState().copy(totalMinutes = 5999),
            atOrAbove = createBaseState().copy(totalMinutes = 6000)
        )
    }

    // ── XP acumulado ─────────────────────────────────────────────────────────

    @Test
    fun threshold_xp50000() {
        assertThreshold(
            "xp_50000",
            below = createBaseState().copy(totalXP = 49999),
            atOrAbove = createBaseState().copy(totalXP = 50000)
        )
    }

    @Test
    fun threshold_xp100000() {
        assertThreshold(
            "xp_100000",
            below = createBaseState().copy(totalXP = 99999),
            atOrAbove = createBaseState().copy(totalXP = 100000)
        )
    }

    // ── GP acumulado ─────────────────────────────────────────────────────────

    @Test
    fun threshold_gp10000() {
        assertThreshold(
            "gp_10000",
            below = createBaseState().copy(totalGoldEarned = 9999),
            atOrAbove = createBaseState().copy(totalGoldEarned = 10000)
        )
    }

    @Test
    fun threshold_gp50000() {
        assertThreshold(
            "gp_50000",
            below = createBaseState().copy(totalGoldEarned = 49999),
            atOrAbove = createBaseState().copy(totalGoldEarned = 50000)
        )
    }

    // ── Combate ──────────────────────────────────────────────────────────────

    @Test
    fun threshold_combat5() {
        assertThreshold(
            "combat_5",
            below = createBaseState().copy(combatLevel = 4),
            atOrAbove = createBaseState().copy(combatLevel = 5)
        )
    }

    @Test
    fun threshold_combat10() {
        assertThreshold(
            "combat_10",
            below = createBaseState().copy(combatLevel = 9),
            atOrAbove = createBaseState().copy(combatLevel = 10)
        )
    }

    @Test
    fun threshold_combat25() {
        assertThreshold(
            "combat_25",
            below = createBaseState().copy(combatLevel = 24),
            atOrAbove = createBaseState().copy(combatLevel = 25)
        )
    }

    @Test
    fun threshold_combat50() {
        assertThreshold(
            "combat_50",
            below = createBaseState().copy(combatLevel = 49),
            atOrAbove = createBaseState().copy(combatLevel = 50)
        )
    }

    // ── Skills ───────────────────────────────────────────────────────────────

    @Test
    fun threshold_skillLevel10() {
        val below = createBaseState().copy(skills = listOf(skill(level = 9)))
        val at = createBaseState().copy(skills = listOf(skill(level = 10)))
        assertThreshold("skill_level_10", below = below, atOrAbove = at)
    }

    @Test
    fun threshold_skillLevel25() {
        val below = createBaseState().copy(skills = listOf(skill(level = 24)))
        val at = createBaseState().copy(skills = listOf(skill(level = 25)))
        assertThreshold("skill_level_25", below = below, atOrAbove = at)
    }

    @Test
    fun threshold_skills3Level10_needsThree() {
        val twoSkills = createBaseState().copy(
            skills = listOf(skill(level = 10), skill(level = 10), skill(level = 2))
        )
        val threeSkills = createBaseState().copy(
            skills = listOf(skill(level = 10), skill(level = 12), skill(level = 10))
        )
        assertThreshold("skills_3_level_10", below = twoSkills, atOrAbove = threeSkills)
    }

    @Test
    fun threshold_prestigeAny() {
        val below = createBaseState().copy(skills = listOf(skill(level = 50, prestige = 0)))
        val at = createBaseState().copy(skills = listOf(skill(level = 50, prestige = 1)))
        assertThreshold("prestige_any", below = below, atOrAbove = at)
    }

    // ── Wilderness ───────────────────────────────────────────────────────────

    @Test
    fun threshold_wilderness5() {
        assertThreshold(
            "wilderness_5",
            below = createBaseState().copy(wildernessWins = 4),
            atOrAbove = createBaseState().copy(wildernessWins = 5)
        )
    }

    @Test
    fun threshold_wilderness25() {
        assertThreshold(
            "wilderness_25",
            below = createBaseState().copy(wildernessWins = 24),
            atOrAbove = createBaseState().copy(wildernessWins = 25)
        )
    }

    @Test
    fun threshold_wilderness50() {
        assertThreshold(
            "wilderness_50",
            below = createBaseState().copy(wildernessWins = 49),
            atOrAbove = createBaseState().copy(wildernessWins = 50)
        )
    }

    // ── Masmorras ────────────────────────────────────────────────────────────

    @Test
    fun threshold_dungeon1() {
        assertThreshold(
            "dungeon_1",
            below = createBaseState().copy(dungeonProgress = 0),
            atOrAbove = createBaseState().copy(dungeonProgress = 1)
        )
    }

    @Test
    fun threshold_dungeon10() {
        assertThreshold(
            "dungeon_10",
            below = createBaseState().copy(dungeonProgress = 9),
            atOrAbove = createBaseState().copy(dungeonProgress = 10)
        )
    }

    @Test
    fun threshold_dungeon25() {
        assertThreshold(
            "dungeon_25",
            below = createBaseState().copy(dungeonProgress = 24),
            atOrAbove = createBaseState().copy(dungeonProgress = 25)
        )
    }

    // ── Coleção ──────────────────────────────────────────────────────────────

    @Test
    fun threshold_titles5() {
        assertThreshold(
            "titles_5",
            below = createBaseState().copy(ownedTitles = listOf("a", "b", "c", "d")),
            atOrAbove = createBaseState().copy(ownedTitles = listOf("a", "b", "c", "d", "e"))
        )
    }

    // ── Hábitos ──────────────────────────────────────────────────────────────

    @Test
    fun threshold_habitStreak7() {
        val below = createBaseState().copy(habits = listOf(habit(streak = 6)))
        val at = createBaseState().copy(habits = listOf(habit(streak = 7)))
        assertThreshold("habit_streak_7", below = below, atOrAbove = at)
    }

    // ── Casos de borda: listas nulas/vazias seguras ──────────────────────────

    @Test
    fun ownedTitlesNull_titles5IsBlocked() {
        val state = createBaseState().copy(ownedTitles = null)
        assertFalse(achievement("titles_5").check(state))
    }

    @Test
    fun skillPrestigeNull_prestigeAnyIsBlocked() {
        val state = createBaseState().copy(skills = listOf(skill(level = 99, prestige = null)))
        assertFalse(achievement("prestige_any").check(state))
    }

    @Test
    fun emptySkills_skillAchievementsBlocked() {
        val state = createBaseState().copy(skills = emptyList())
        assertFalse(achievement("skill_level_10").check(state))
        assertFalse(achievement("skill_level_25").check(state))
        assertFalse(achievement("skills_3_level_10").check(state))
        assertFalse(achievement("prestige_any").check(state))
    }

    @Test
    fun emptyHabits_habitStreak7Blocked() {
        val state = createBaseState().copy(habits = emptyList())
        assertFalse(achievement("habit_streak_7").check(state))
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun skill(level: Int, prestige: Int? = 0) = Skill(
        id = "skill-$level-${prestige ?: "null"}-${System.nanoTime()}",
        name = "Skill",
        level = level,
        xp = 0,
        prestige = prestige
    )

    private fun habit(streak: Int) = Habit(
        id = "h1",
        title = "Hábito",
        notes = "",
        up = true,
        down = false,
        difficulty = Difficulty.Easy,
        upCount = 0,
        downCount = 0,
        streak = streak,
        tags = emptyList()
    )

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
            hasClaimedLogin = false,
            hp = 100,
            maxHp = 100,
            habits = emptyList(),
            dailies = emptyList(),
            todos = emptyList(),
            pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
        )
    }
}
