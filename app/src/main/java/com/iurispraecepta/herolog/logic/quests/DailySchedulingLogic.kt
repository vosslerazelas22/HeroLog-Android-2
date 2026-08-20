package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.RepeatInterval
import java.util.Date
import kotlin.math.roundToInt

object DailySchedulingLogic {

    fun wasDailyScheduledForDate(daily: Daily, date: Date = Date()): Boolean {
        if (daily.repeats != RepeatInterval.Daily) {
            return true
        }

        val createdDate = DateParsingUtils.parseDate(daily.createdAt) ?: return true

        val createdZero = DateParsingUtils.truncateToMidnight(createdDate)
        val dateZero = DateParsingUtils.truncateToMidnight(date)

        val diffDays = ((dateZero - createdZero).toDouble() / 86400000.0).roundToInt()
        val every = if (daily.every > 0) daily.every else 1

        return diffDays >= 0 && diffDays % every == 0
    }
}
