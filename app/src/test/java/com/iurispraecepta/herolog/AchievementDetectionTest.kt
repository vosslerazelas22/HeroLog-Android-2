package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.achievements.AchievementDetection
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementDetectionTest {

    // ── FR-004: só cruzamentos novos ─────────────────────────────────────────

    @Test
    fun detectNewAchievements_returnsEmpty_whenNothingCrossed() {
        val previous = createBaseState()
        val candidate = createBaseState().copy(gold = 9999)

        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        assertTrue(result.isEmpty())
    }

    @Test
    fun detectNewAchievements_returnsNewlyCrossedThreshold() {
        val previous = createBaseState().copy(totalSessions = 0, totalMinutes = 0)
        val candidate = createBaseState().copy(totalSessions = 1, totalMinutes = 25)

        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        val ids = result.map { it.id }

        assertTrue(ids.contains("first_quest"))
        assertFalse(ids.contains("sessions_10"))
    }

    @Test
    fun detectNewAchievements_returnsMultiple_inCatalogOrder() {
        val previous = createBaseState().copy(totalSessions = 0, totalMinutes = 0, totalXP = 0)
        val candidate = createBaseState().copy(
            totalSessions = 30,
            totalMinutes = 700,
            totalXP = 1200
        )

        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        val ids = result.map { it.id }

        // Esperados: first_quest(1), xp_1000, sessions_10, sessions_25, minutes_100, minutes_600
        assertTrue(ids.contains("first_quest"))
        assertTrue(ids.contains("xp_1000"))
        assertTrue(ids.contains("sessions_10"))
        assertTrue(ids.contains("sessions_25"))
        assertTrue(ids.contains("minutes_100"))
        assertTrue(ids.contains("minutes_600"))

        // Ordem do catálogo: first_quest vem antes de xp_1000, que vem antes de sessions_10,
        // que vem antes de sessions_25; minutes_100 antes de minutes_600.
        assertTrue(ids.indexOf("first_quest") < ids.indexOf("xp_1000"))
        assertTrue(ids.indexOf("xp_1000") < ids.indexOf("sessions_10"))
        assertTrue(ids.indexOf("sessions_10") < ids.indexOf("sessions_25"))
        assertTrue(ids.indexOf("minutes_100") < ids.indexOf("minutes_600"))
    }

    // ── FR-005: idempotência ─────────────────────────────────────────────────

    @Test
    fun detectNewAchievements_skipsAlreadyUnlockedIds() {
        val previous = createBaseState().copy(
            totalSessions = 1,
            achievements = listOf("first_quest")
        )
        val candidate = createBaseState().copy(
            totalSessions = 2,
            achievements = listOf("first_quest")
        )

        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        assertTrue(result.none { it.id == "first_quest" })
    }

    // ── FR-006: sem anúncios retroativos ─────────────────────────────────────

    @Test
    fun detectNewAchievements_noRetroactiveAnnouncements_whenThresholdAlreadyMetButIdPersisted() {
        // Save antigo: threshold já satisfeito e ID já persistido — nenhum anúncio.
        val previous = createBaseState().copy(
            totalSessions = 10,
            achievements = listOf("first_quest", "sessions_10")
        )
        val candidate = previous.copy(gold = previous.gold + 1)

        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        assertTrue(result.isEmpty())
    }

    @Test
    fun detectNewAchievements_existingSave_thresholdAlreadySatisfied_notAnnounced() {
        // Save existente: threshold já era verdade ANTES da ação e o ID não está
        // persistido. FR-006: não deve anunciar retroativamente — o achievement
        // aparece desbloqueado na tela via AchievementLogic.isUnlocked (check OR).
        val previous = createBaseState().copy(totalSessions = 10, achievements = emptyList())
        val candidate = previous.copy(totalSessions = 11)

        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        assertTrue(result.none { it.id == "sessions_10" })
    }

    // ── Listas nulas/vazias seguras ──────────────────────────────────────────

    @Test
    fun detectNewAchievements_safeWithNullOwnedTitles() {
        val previous = createBaseState().copy(ownedTitles = null)
        val candidate = previous.copy(gold = previous.gold + 1)
        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        assertTrue(result.none { it.id == "titles_5" })
    }

    @Test
    fun detectNewAchievements_safeWithEmptySkillsAndHabits() {
        val previous = createBaseState().copy(skills = emptyList(), habits = emptyList())
        val candidate = previous.copy(totalSessions = previous.totalSessions + 1)
        val result = AchievementDetection.detectNewAchievements(previous, candidate)
        val ids = result.map { it.id }
        assertFalse(ids.contains("skill_level_10"))
        assertFalse(ids.contains("habit_streak_7"))
    }

    // ── withAchievements: persistência idempotente ───────────────────────────

    @Test
    fun withAchievements_addsOnlyNewIds() {
        val state = createBaseState().copy(achievements = listOf("first_quest"))
        val updated = AchievementDetection.withAchievements(state, listOf("first_quest", "streak_3"))
        assertEquals(listOf("first_quest", "streak_3"), updated.achievements)
    }

    @Test
    fun withAchievements_emptyList_returnsSameState() {
        val state = createBaseState().copy(achievements = listOf("first_quest"))
        val updated = AchievementDetection.withAchievements(state, emptyList())
        assertEquals(state.achievements, updated.achievements)
    }

    @Test
    fun withAchievements_doesNotAddDuplicates() {
        val state = createBaseState().copy(achievements = listOf("first_quest"))
        val updated = AchievementDetection.withAchievements(state, listOf("first_quest"))
        assertEquals(1, updated.achievements.size)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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
