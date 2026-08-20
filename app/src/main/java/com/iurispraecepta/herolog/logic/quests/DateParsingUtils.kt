package com.iurispraecepta.herolog.logic.quests

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateParsingUtils {

    fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            Date.from(Instant.parse(dateStr))
        } catch (_: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.isLenient = false
                sdf.parse(dateStr)
            } catch (_: Exception) {
                try {
                    val sdfJs = SimpleDateFormat("EEE MMM dd yyyy", Locale.US)
                    sdfJs.isLenient = false
                    sdfJs.parse(dateStr)
                } catch (_: Exception) {
                    try {
                        val sdfBr = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
                        sdfBr.isLenient = false
                        sdfBr.parse(dateStr)
                    } catch (_: Exception) {
                        try {
                            dateStr.toLongOrNull()?.let { Date(it) }
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            }
        }
    }

    fun truncateToMidnight(date: Date): Long {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
