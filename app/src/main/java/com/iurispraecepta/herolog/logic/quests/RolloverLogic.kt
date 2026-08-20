package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Daily
import java.util.Date
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

data class RolloverResult(
    val updatedState: CharacterState,
    val updatedDailies: List<Daily>,
    val missedCount: Int,
    val shieldConsumed: Boolean,
    val died: Boolean
)

object RolloverLogic {

    fun applyRollover(
        state: CharacterState,
        dailies: List<Daily>,
        referenceDate: Date = Date()
    ): RolloverResult {
        val todayStr = QuestLogic.toDateStringJs(referenceDate)
        if (state.todayDate == todayStr) {
            return RolloverResult(
                updatedState = state,
                updatedDailies = dailies,
                missedCount = 0,
                shieldConsumed = false,
                died = false
            )
        }

        // diffDays: calculado a partir de lastStudyDate (ausente -> sem mudanca de streak)
        val lastDate = DateParsingUtils.parseDate(state.lastStudyDate)
        val diffDays: Int? = if (lastDate != null) {
            val lastMidnight = DateParsingUtils.truncateToMidnight(lastDate)
            val refMidnight = DateParsingUtils.truncateToMidnight(referenceDate)
            ((refMidnight - lastMidnight).toDouble() / 86400000.0).roundToInt()
        } else {
            null
        }

        // Primeiro calcule missedCount varrendo as Dailies (nao completa + wasDailyScheduledForDate == true)
        val missedCount = dailies.count { !it.completed && DailySchedulingLogic.wasDailyScheduledForDate(it, referenceDate) }

        // Gatilho do shield: (diffDays > 1 OU missedCount > 0) E existe StreakShield no inventario
        val shieldIndex = state.inventory.indexOfFirst { it.buff == BuffType.StreakShield }
        val hasShield = shieldIndex != -1
        val isStreakAtRisk = diffDays != null && diffDays > 1
        val shouldTriggerShield = (isStreakAtRisk || missedCount > 0) && hasShield

        val updatedInventory = if (shouldTriggerShield) {
            state.inventory.toMutableList().apply { removeAt(shieldIndex) }
        } else {
            state.inventory
        }

        // Streak global:
        // diffDays == 1 -> streak global mantem
        // diffDays > 1 -> streak global seria zerada, sujeito a protecao do shield
        // diffDays ausente -> sem mudanca de streak
        val finalStreak = when {
            diffDays == null -> state.streak
            diffDays <= 1 -> state.streak
            shouldTriggerShield -> state.streak
            else -> 0
        }

        // Dailies e dano:
        var dailyNeglectDamage = 0
        val updatedDailies = dailies.map { daily ->
            if (daily.completed) {
                daily.copy(completed = false)
            } else {
                val isScheduled = DailySchedulingLogic.wasDailyScheduledForDate(daily, referenceDate)
                if (isScheduled) {
                    if (shouldTriggerShield) {
                        // Protegida pelo escudo: streak e value mantidos, completed = false
                        daily
                    } else {
                        // Sem escudo: penalizada
                        dailyNeglectDamage += getDifficultyRewards(daily.difficulty).damage
                        daily.copy(
                            streak = 0,
                            value = (daily.value ?: 0) - 1
                        )
                    }
                } else {
                    // Nao agendada: sem mudanca
                    daily
                }
            }
        }

        // Calculo de dano no HP:
        val finalHp = if (shouldTriggerShield || dailyNeglectDamage == 0) {
            state.hp
        } else {
            val finalDmg = if (state.charClass == CharClass.Ranger) {
                max(1, floor(dailyNeglectDamage * 0.7).toInt())
            } else {
                dailyNeglectDamage
            }
            max(0, state.hp - finalDmg)
        }

        val died = finalHp == 0

        // Reset diário
        val updatedAchievements = state.achievements.filter { !it.startsWith("claimed_daily_") }

        val updatedState = state.copy(
            todayDate = todayStr,
            todayMinutes = 0,
            todayXP = 0,
            hasClaimedLogin = false,
            achievements = updatedAchievements,
            dailies = updatedDailies,
            hp = finalHp,
            streak = finalStreak,
            inventory = updatedInventory
        )

        return RolloverResult(
            updatedState = updatedState,
            updatedDailies = updatedDailies,
            missedCount = missedCount,
            shieldConsumed = shouldTriggerShield,
            died = died
        )
    }
}
