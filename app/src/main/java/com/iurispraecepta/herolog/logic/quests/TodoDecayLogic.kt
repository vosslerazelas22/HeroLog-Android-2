package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.model.Todo
import java.util.Date
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

object TodoDecayLogic {

    fun getTodoDecayValue(todo: Todo, currentDate: Date = Date()): Int {
        if (todo.completed) return 0
        val createdDate = DateParsingUtils.parseDate(todo.createdAt) ?: return 0

        val createdZero = DateParsingUtils.truncateToMidnight(createdDate)
        val dateZero = DateParsingUtils.truncateToMidnight(currentDate)

        val diffDays = max(0, ((dateZero - createdZero).toDouble() / 86400000.0).roundToInt())
        val decayed = floor(diffDays / 2.0).toInt() * -1
        return max(decayed, -20)
    }
}
