package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.logic.quests.QuestApplyLogic
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class QuestApplyLogicTest {

    @Test
    fun claimQuestReward_dailyQuest_addsClaimIdWithDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!
        val state = createBaseState()

        val updatedState = QuestApplyLogic.claimQuestReward(
            state = state,
            questId = "daily_first_torch",
            goldReward = 50,
            xpReward = 100,
            referenceDate = aug4Date
        )

        val expectedClaimId = QuestLogic.getQuestClaimId("daily_first_torch", aug4Date)
        assertEquals("claimed_daily_first_torch_Tue Aug 04 2026", expectedClaimId)
        assertTrue(updatedState.achievements.contains(expectedClaimId))
        assertEquals(state.gold + 50, updatedState.gold)
        assertEquals(state.totalGoldEarned + 50, updatedState.totalGoldEarned)
        assertEquals(state.totalXP + 100, updatedState.totalXP)
        // 100 * 0.4 = 40 combatXP
        assertEquals(40, updatedState.combatXP)
        assertEquals(1, updatedState.combatLevel)
    }

    @Test
    fun claimQuestReward_guildQuest_addsClaimIdWithoutDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!
        val state = createBaseState()

        val updatedState = QuestApplyLogic.claimQuestReward(
            state = state,
            questId = "guild_master_triad",
            goldReward = 150,
            xpReward = 300,
            referenceDate = aug4Date
        )

        val expectedClaimId = "claimed_guild_master_triad"
        assertTrue(updatedState.achievements.contains(expectedClaimId))
        assertEquals(state.gold + 150, updatedState.gold)
        assertEquals(state.totalGoldEarned + 150, updatedState.totalGoldEarned)
        assertEquals(state.totalXP + 300, updatedState.totalXP)
        // Level 1 requer 1 * 100 = 100 XP.
        // 300 * 0.4 = 120 combatXP -> 120 >= 100 -> combatLevel = 2, combatXP = 20
        assertEquals(20, updatedState.combatXP)
        assertEquals(2, updatedState.combatLevel)
    }

    @Test
    fun claimQuestReward_duplicateClaim_isIdempotentAndNoOp() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!
        val state = createBaseState()

        val firstClaim = QuestApplyLogic.claimQuestReward(
            state = state,
            questId = "daily_first_torch",
            goldReward = 50,
            xpReward = 100,
            referenceDate = aug4Date
        )

        val secondClaim = QuestApplyLogic.claimQuestReward(
            state = firstClaim,
            questId = "daily_first_torch",
            goldReward = 50,
            xpReward = 100,
            referenceDate = aug4Date
        )

        assertSame(firstClaim, secondClaim)
        assertEquals(firstClaim.gold, secondClaim.gold)
        assertEquals(firstClaim.totalGoldEarned, secondClaim.totalGoldEarned)
        assertEquals(firstClaim.totalXP, secondClaim.totalXP)
        assertEquals(firstClaim.combatXP, secondClaim.combatXP)
        assertEquals(firstClaim.achievements.size, secondClaim.achievements.size)
    }

    @Test
    fun claimQuestReward_triggersCombatLevelUp_whenCrossingThreshold() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!

        // Nível 1 exige CombatLogic.requiredXpForCombatLevel(1) = 100 XP
        val reqLvl1 = CombatLogic.requiredXpForCombatLevel(1)
        assertEquals(100, reqLvl1)

        val state = createBaseState().copy(
            combatLevel = 1,
            combatXP = 80 // faltam 20 XP para o nível 2 (80 + 40 = 120 >= 100 -> combatLevel = 2, combatXP = 20)
        )

        // xpReward = 100 -> floor(100 * 0.4) = 40 XP
        // nextXP inicial = 80 + 40 = 120 >= 100 -> nextCombatLevel = 2, nextXP = 20
        val updatedState = QuestApplyLogic.claimQuestReward(
            state = state,
            questId = "daily_boss_slayer",
            goldReward = 200,
            xpReward = 100,
            referenceDate = aug4Date
        )

        assertEquals(2, updatedState.combatLevel)
        assertEquals(20, updatedState.combatXP)
        assertEquals(state.gold + 200, updatedState.gold)
        assertEquals(state.totalGoldEarned + 200, updatedState.totalGoldEarned)
        assertEquals(state.totalXP + 100, updatedState.totalXP)
    }

    @Test
    fun claimQuestReward_incrementsTotalXpCorrectly() {
        val state = createBaseState().copy(
            totalXP = 1500
        )

        val updatedState = QuestApplyLogic.claimQuestReward(
            state = state,
            questId = "daily_test_quest",
            goldReward = 80,
            xpReward = 250
        )

        // totalXP deve ser 1500 + 250 = 1750 (soma normal de xpReward, sem truncamento/bug)
        assertEquals(1750, updatedState.totalXP)
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
