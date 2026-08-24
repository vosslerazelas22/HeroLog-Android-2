package com.iurispraecepta.herolog.logic.kingdom

import com.iurispraecepta.herolog.model.HistoryEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Faixa de intensidade de minutos de uma célula do heatmap — porte de uma das 5 faixas de cor
 * calculadas em `HeatmapTab.tsx` (`fillClass`). Esta enum representa só a faixa numérica; a
 * decisão de qual estilo aplicar quando a célula é `isFuture`/`isToday` (que na fonte tem
 * precedência sobre a faixa) fica a cargo da tela (`HeatmapScreen.kt`, Bloco E), exatamente como
 * a ordem dos `if/else if` na fonte: `isFuture` é checado primeiro (ignorando a faixa por
 * completo), depois `dayMinutes === 0` (com variante se `isToday`), só então as faixas 1-29/
 * 30-59/60-89/90+.
 */
enum class MinuteBucket {
    ZERO,   // 0 min
    LOW,    // 1-29 min
    MID,    // 30-59 min
    HIGH,   // 60-89 min
    ELITE;  // 90+ min (dourado heráldico)

    companion object {
        fun forMinutes(minutes: Int): MinuteBucket = when {
            minutes <= 0 -> ZERO
            minutes in 1..29 -> LOW
            minutes in 30..59 -> MID
            minutes in 60..89 -> HIGH
            else -> ELITE
        }
    }
}

/** Escopo temporal do heatmap — porte do toggle `monthsLimit` (`3 | 6`) de `HeatmapTab.tsx`. */
enum class HeatmapScope(val months: Int, val weeksCount: Int) {
    THREE_MONTHS(months = 3, weeksCount = 12),
    SIX_MONTHS(months = 6, weeksCount = 24)
}

/** Uma célula do grid — porte do objeto `cells[]` construído no passo 3 da fonte. */
data class HeatmapCell(
    val date: String,
    val dateObj: LocalDate,
    val minutes: Int,
    val minuteBucket: MinuteBucket,
    val isFuture: Boolean,
    val isToday: Boolean
)

/** Rótulo de mês numa coluna do grid — porte de `monthLabels[]` (passo 4 da fonte). */
data class MonthLabel(
    val col: Int,
    val name: String
)

/** Resultado completo do cálculo do heatmap — tudo que `HeatmapTab.tsx` deriva de `history`. */
data class HeatmapResult(
    val cells: List<HeatmapCell>,
    val monthLabels: List<MonthLabel>,
    val consistencyPercentage: Int,
    val studyDaysInLast30: Int,
    val totalStudyDays: Int,
    val bestDayMinutes: Int,
    val thisWeekMinutes: Int,
    val thisMonthMinutes: Int
)

/**
 * Porte literal da lógica pura de `src/modules/kingdom/HeatmapTab.tsx` (Bloco B do módulo Reino).
 *
 * Auditado contra a fonte real colada por Bruno (não contra o MOC de memória) — sem divergência
 * do que já estava mapeado em `MOC_reino_pre_port.md` seção 4. `streak` da fonte é só exibido
 * (`Streak Atual: {streak}`), nunca usado em nenhum cálculo — por isso não entra na assinatura
 * desta função, fica só na tela (Bloco E).
 *
 * **Duas fontes de chave de data distintas, replicadas fielmente (mesma fragilidade da fonte)**:
 * 1. `minutesByDay` é chaveado pela substring bruta de `entry.date` antes da primeira vírgula
 *    (`entry.date.split(',')[0].trim()`, mesma família de `todayLocalStr` já documentada no
 *    `PARIDADE.md` seção 8 — risco pendente se `HistoryEntry.date` não nascer em `dd/MM/yyyy`).
 * 2. As chaves usadas para *consultar* esse mapa (dia de hoje, os 30 dias da consistência, e
 *    cada célula do grid) são geradas por esta própria função com `toLocaleDateString('pt-BR')`
 *    na fonte — sempre zero-padded (`dd/MM/yyyy`). Replicado aqui via [KEY_FORMATTER].
 * Se o formato real de `HistoryEntry.date` divergir de `dd/MM/yyyy` zero-padded, os lookups
 * silenciosamente não batem — comportamento idêntico ao da fonte React, não um bug introduzido
 * no port.
 *
 * @param today injetável (default [LocalDate.now]) para testes determinísticos.
 */
object HeatmapLogic {

    private val KEY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val MONTH_ABBREVIATIONS = listOf(
        "JAN.", "FEV.", "MAR.", "ABR.", "MAI.", "JUN.",
        "JUL.", "AGO.", "SET.", "OUT.", "NOV.", "DEZ."
    )

    /**
     * Porte literal de `formatMinutes` (`HeatmapTab.tsx`): "0 Min" / "N Min" (< 60) / "Xh" ou
     * "Xh Ym" (>= 60, omitindo "0m" quando o resto é zero).
     */
    fun formatMinutes(minutes: Int): String {
        if (minutes <= 0) return "0 Min"
        if (minutes < 60) return "$minutes Min"
        val hrs = minutes / 60
        val mins = minutes % 60
        return if (mins > 0) "${hrs}h ${mins}m" else "${hrs}h"
    }

    /**
     * Porte literal de `parseDatePart` (`HeatmapTab.tsx`): parsing tolerante de "D/M/YYYY" ou
     * "DD/MM/YYYY" (sem exigir zero-padding, ao contrário de [KEY_FORMATTER] usado pra gerar
     * chaves). Retorna `null` em vez de lançar — replica o `try/catch` silencioso da fonte que
     * envolve o uso desta função nas somas de semana/mês.
     */
    private fun parseDatePartLenient(dateStr: String): LocalDate? {
        return try {
            val parts = dateStr.split("/")
            if (parts.size != 3) return null
            val d = parts[0].trim().toInt()
            val m = parts[1].trim().toInt()
            val y = parts[2].trim().toInt()
            LocalDate.of(y, m, d)
        } catch (e: Exception) {
            null
        }
    }

    fun calculate(
        history: List<HistoryEntry>,
        scope: HeatmapScope,
        today: LocalDate = LocalDate.now()
    ): HeatmapResult {

        // 1. Agregação por dia — chave bruta (substring antes da vírgula), fiel à fonte.
        val minutesByDay = mutableMapOf<String, Int>()
        history.forEach { entry ->
            val datePart = entry.date.substringBefore(',').trim()
            if (datePart.isNotEmpty()) {
                minutesByDay[datePart] = (minutesByDay[datePart] ?: 0) + entry.duration
            }
        }

        // 2. Segunda-feira desta semana + início do mês vigente.
        // DayOfWeek.value: SEGUNDA=1..DOMINGO=7. `% 7` reproduz getDay() do JS (DOMINGO=0..SÁBADO=6).
        val jsDayOfWeek = today.dayOfWeek.value % 7
        val daysToMonday = if (jsDayOfWeek == 0) 6 else jsDayOfWeek - 1
        val thisMonday = today.minusDays(daysToMonday.toLong())
        val thisMonthStart = today.withDayOfMonth(1)

        // Estatística: Dias de Estudo (dias únicos com minutos > 0).
        val totalStudyDays = minutesByDay.values.count { it > 0 }

        // Estatística: Melhor Dia — guarda contra Math.max de coleção vazia (JS retornaria -Infinity).
        val bestDayMinutes = minutesByDay.values.maxOrNull() ?: 0

        // Estatística: Esta Semana / Este Mês — soma via parsing tolerante das chaves brutas.
        var thisWeekMinutes = 0
        var thisMonthMinutes = 0
        minutesByDay.forEach { (dateKey, minutes) ->
            val cellDate = parseDatePartLenient(dateKey) ?: return@forEach
            if (!cellDate.isBefore(thisMonday) && !cellDate.isAfter(today)) {
                thisWeekMinutes += minutes
            }
            if (!cellDate.isBefore(thisMonthStart) && !cellDate.isAfter(today)) {
                thisMonthMinutes += minutes
            }
        }

        // Consistência dos últimos 30 dias — fix de v1.1.7: desloca a janela em +1 dia se hoje
        // ainda não tem minutos registrados, pra não penalizar um dia "em andamento".
        val todayKey = today.format(KEY_FORMATTER)
        val todayMins = minutesByDay[todayKey] ?: 0
        val startI = if (todayMins > 0) 0 else 1

        var studyDaysInLast30 = 0
        for (i in startI until startI + 30) {
            val checkDate = today.minusDays(i.toLong())
            val key = checkDate.format(KEY_FORMATTER)
            val mins = minutesByDay[key] ?: 0
            if (mins > 0) studyDaysInLast30++
        }
        val consistencyPercentage = Math.round(studyDaysInLast30 / 30.0 * 100).toInt()

        // 3. Grid de células.
        val weeksCount = scope.weeksCount
        val startMondayOfGrid = thisMonday.minusDays((weeksCount - 1) * 7L)
        val totalDaysInGrid = weeksCount * 7

        val cells = (0 until totalDaysInGrid).map { i ->
            val d = startMondayOfGrid.plusDays(i.toLong())
            val dateKey = d.format(KEY_FORMATTER)
            val dayMinutes = minutesByDay[dateKey] ?: 0
            val isFuture = d.isAfter(today)
            val isToday = d == today

            HeatmapCell(
                date = dateKey,
                dateObj = d,
                minutes = dayMinutes,
                minuteBucket = MinuteBucket.forMinutes(dayMinutes),
                isFuture = isFuture,
                isToday = isToday
            )
        }

        // 4. Rótulos de mês — adicionado na coluna 0 e toda vez que o mês da segunda-feira
        // daquela semana mudar em relação à semana anterior (comparação sequencial, não cálculo
        // direto de posição).
        val monthLabels = mutableListOf<MonthLabel>()
        for (col in 0 until weeksCount) {
            val wMonday = startMondayOfGrid.plusDays((col * 7).toLong())
            val shouldAdd = if (col == 0) {
                true
            } else {
                val prevWMonday = startMondayOfGrid.plusDays(((col - 1) * 7).toLong())
                wMonday.month != prevWMonday.month
            }
            if (shouldAdd) {
                monthLabels.add(MonthLabel(col = col, name = MONTH_ABBREVIATIONS[wMonday.monthValue - 1]))
            }
        }

        return HeatmapResult(
            cells = cells,
            monthLabels = monthLabels,
            consistencyPercentage = consistencyPercentage,
            studyDaysInLast30 = studyDaysInLast30,
            totalStudyDays = totalStudyDays,
            bestDayMinutes = bestDayMinutes,
            thisWeekMinutes = thisWeekMinutes,
            thisMonthMinutes = thisMonthMinutes
        )
    }
}