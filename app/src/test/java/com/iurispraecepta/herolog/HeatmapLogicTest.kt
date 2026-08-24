package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.kingdom.HeatmapLogic
import com.iurispraecepta.herolog.logic.kingdom.HeatmapScope
import com.iurispraecepta.herolog.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Testes de `HeatmapLogic` (Bloco B, módulo Reino). `today` é sempre injetado fixo
 * (2026-08-24, uma segunda-feira) para determinismo — auditado contra `HeatmapTab.tsx` real
 * colado por Bruno, sem divergência do que já estava mapeado em `MOC_reino_pre_port.md` seção 4.
 */
class HeatmapLogicTest {

    private val keyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val today = LocalDate.of(2026, 8, 24) // segunda-feira

    private fun entry(dateStr: String, duration: Int, id: String = "h_${dateStr}_$duration"): HistoryEntry =
        HistoryEntry(
            id = id,
            skillName = "Teste",
            date = dateStr,
            duration = duration,
            xp = 0,
            gold = 0,
            notes = "",
            wilderness = false
        )

    private fun key(date: LocalDate): String = date.format(keyFormat)

    // ---------------------------------------------------------------------
    // formatMinutes
    // ---------------------------------------------------------------------

    @Test
    fun formatMinutes_zeroOrNegative_returnsZeroMin() {
        assertEquals("0 Min", HeatmapLogic.formatMinutes(0))
        assertEquals("0 Min", HeatmapLogic.formatMinutes(-5))
    }

    @Test
    fun formatMinutes_underOneHour_returnsMinutesOnly() {
        assertEquals("45 Min", HeatmapLogic.formatMinutes(45))
        assertEquals("1 Min", HeatmapLogic.formatMinutes(1))
    }

    @Test
    fun formatMinutes_exactHours_omitsMinutesSuffix() {
        assertEquals("1h", HeatmapLogic.formatMinutes(60))
        assertEquals("3h", HeatmapLogic.formatMinutes(180))
    }

    @Test
    fun formatMinutes_hoursAndMinutes_includesBoth() {
        assertEquals("1h 30m", HeatmapLogic.formatMinutes(90))
        assertEquals("2h 5m", HeatmapLogic.formatMinutes(125))
    }

    // ---------------------------------------------------------------------
    // Agregação por dia — soma de múltiplas entradas no mesmo dia + sufixo de hora
    // ---------------------------------------------------------------------

    @Test
    fun calculate_sumsMultipleEntriesOnSameDayAndStripsTimeSuffix() {
        val history = listOf(
            entry("24/08/2026, 09:15:00", 25), // hoje, com sufixo de hora tipo toLocaleString
            entry("24/08/2026, 18:40:00", 20)  // segunda entrada no mesmo dia
        )

        val result = HeatmapLogic.calculate(history, HeatmapScope.THREE_MONTHS, today)

        val todayCell = result.cells.first { it.isToday }
        assertEquals(45, todayCell.minutes)
        assertEquals(45, result.bestDayMinutes)
        assertEquals(1, result.totalStudyDays)
    }

    // ---------------------------------------------------------------------
    // bestDayMinutes / totalStudyDays / thisWeek / thisMonth
    // ---------------------------------------------------------------------

    @Test
    fun calculate_weekMonthAndBestDay_matchExpectedSums() {
        val history = listOf(
            entry("15/07/2026", 50),                 // antes do mês vigente — só entra no melhor dia
            entry("01/08/2026", 20),                  // dentro do mês, fora da semana (thisMonday == hoje)
            entry("10/08/2026", 15),                  // dentro do mês, fora da semana
            entry("24/08/2026, 09:00:00", 25),         // hoje — dentro da semana E do mês
            entry("25/08/2026", 999)                   // amanhã (futuro) — nunca deveria contar
        )

        val result = HeatmapLogic.calculate(history, HeatmapScope.THREE_MONTHS, today)

        // thisMonday == hoje (hoje é segunda-feira) -> "esta semana" é só o próprio dia de hoje.
        assertEquals(25, result.thisWeekMinutes)
        // 20 (01/08) + 15 (10/08) + 25 (hoje) = 60 — exclui 15/07 (mês anterior) e 25/08 (futuro).
        assertEquals(60, result.thisMonthMinutes)
        // Melhor dia é o de 15/07 (50), maior que qualquer outro, incluindo o "futuro" 25/08 que
        // não deveria sequer existir na fonte real, mas a agregação pura não valida isso.
        assertEquals(999, result.bestDayMinutes)
        assertEquals(5, result.totalStudyDays)
    }

    @Test
    fun calculate_malformedDateEntry_countsTowardAggregatesButSilentlyIgnoredInWeekMonthSums() {
        val history = listOf(
            entry("01/08/2026", 20),
            entry("não é uma data", 99) // simula dado corrompido — replica o try/catch silencioso da fonte
        )

        val result = HeatmapLogic.calculate(history, HeatmapScope.THREE_MONTHS, today)

        // Entra na agregação bruta (totalStudyDays/bestDayMinutes lêem o mapa direto)...
        assertEquals(2, result.totalStudyDays)
        assertEquals(99, result.bestDayMinutes)
        // ...mas não quebra nem contamina as somas de semana/mês, que dependem do parsing.
        assertEquals(0, result.thisWeekMinutes)
        assertEquals(20, result.thisMonthMinutes)
    }

    @Test
    fun calculate_emptyHistory_returnsAllZeroesWithoutCrashing() {
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.THREE_MONTHS, today)

        assertEquals(0, result.totalStudyDays)
        assertEquals(0, result.bestDayMinutes)
        assertEquals(0, result.thisWeekMinutes)
        assertEquals(0, result.thisMonthMinutes)
        assertEquals(0, result.consistencyPercentage)
        assertEquals(0, result.studyDaysInLast30)
    }

    // ---------------------------------------------------------------------
    // Consistência dos últimos 30 dias — fix de v1.1.7 (deslocamento de startI)
    // ---------------------------------------------------------------------

    @Test
    fun calculate_consistency_todayHasNoMinutes_windowShiftsToExcludeTodayAndIncludeDayThirty() {
        // Dados nos dias -1 a -30 (30 dias distintos), hoje SEM entrada nenhuma.
        // Se o fix (startI = 1) não existisse, o loop checaria os dias 0..29 (hoje + só 29 dias
        // anteriores), perdendo o dia -30 e contando hoje (vazio) à toa — resultado seria 29/30.
        // Com o fix, checa os dias -1..-30: os 30 dias batem exatamente.
        val history = (1..30).map { i -> entry(key(today.minusDays(i.toLong())), 10) }

        val result = HeatmapLogic.calculate(history, HeatmapScope.THREE_MONTHS, today)

        assertEquals(30, result.studyDaysInLast30)
        assertEquals(100, result.consistencyPercentage)
    }

    @Test
    fun calculate_consistency_todayHasMinutes_windowStartsAtDayZero() {
        // Dados nos dias 0 (hoje) a -29 (30 dias distintos, incluindo hoje).
        val history = (0..29).map { i -> entry(key(today.minusDays(i.toLong())), 5) }

        val result = HeatmapLogic.calculate(history, HeatmapScope.THREE_MONTHS, today)

        assertEquals(30, result.studyDaysInLast30)
        assertEquals(100, result.consistencyPercentage)
    }

    @Test
    fun calculate_consistency_partialCoverage_roundsPercentageCorrectly() {
        // 7 de 30 dias com foco (hoje sem entrada -> startI = 1, checa dias -1..-30).
        val focusedDaysAgo = listOf(2, 5, 9, 14, 18, 23, 29)
        val history = focusedDaysAgo.map { i -> entry(key(today.minusDays(i.toLong())), 10) }

        val result = HeatmapLogic.calculate(history, HeatmapScope.THREE_MONTHS, today)

        assertEquals(7, result.studyDaysInLast30)
        // 7/30*100 = 23.333... -> arredonda para 23, igual ao Math.round do JS.
        assertEquals(23, result.consistencyPercentage)
    }

    // ---------------------------------------------------------------------
    // Grid — dimensões, isToday, isFuture
    // ---------------------------------------------------------------------

    @Test
    fun calculate_grid_threeMonths_has84Cells() {
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.THREE_MONTHS, today)
        assertEquals(84, result.cells.size)
    }

    @Test
    fun calculate_grid_sixMonths_has168Cells() {
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.SIX_MONTHS, today)
        assertEquals(168, result.cells.size)
    }

    @Test
    fun calculate_grid_exactlyOneCellIsToday_andMatchesInjectedToday() {
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.THREE_MONTHS, today)

        val todayCells = result.cells.filter { it.isToday }
        assertEquals(1, todayCells.size)
        assertEquals(today, todayCells.first().dateObj)
    }

    @Test
    fun calculate_grid_futureCells_areOnlyTheRemainingDaysOfCurrentWeek() {
        // hoje é segunda-feira (thisMonday == hoje); a última coluna do grid é a semana atual,
        // então terça a domingo dessa semana (6 dias) são "futuro" — nenhuma outra semana deveria
        // ter dia futuro, já que o grid nunca ultrapassa o domingo da semana atual.
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.THREE_MONTHS, today)

        val futureCells = result.cells.filter { it.isFuture }
        assertEquals(6, futureCells.size)
        assertTrue(futureCells.all { it.dateObj.isAfter(today) })
        assertTrue(futureCells.all { !it.dateObj.isAfter(today.plusDays(6)) })
    }

    // ---------------------------------------------------------------------
    // Rótulos de mês — coluna 0 sempre presente + mudança sequencial de mês
    // ---------------------------------------------------------------------

    @Test
    fun calculate_monthLabels_threeMonths_matchExpectedColumnsAndNames() {
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.THREE_MONTHS, today)

        // Calculado independentemente (script Python fora do projeto): startMondayOfGrid =
        // 2026-06-08, mudanças de mês nas colunas 0 (JUN.), 4 (JUL.), 8 (AGO.).
        val labelsByCol = result.monthLabels.associate { it.col to it.name }
        assertEquals(3, result.monthLabels.size)
        assertEquals("JUN.", labelsByCol[0])
        assertEquals("JUL.", labelsByCol[4])
        assertEquals("AGO.", labelsByCol[8])
    }

    @Test
    fun calculate_monthLabels_sixMonths_matchExpectedColumnsAndNames() {
        val result = HeatmapLogic.calculate(emptyList(), HeatmapScope.SIX_MONTHS, today)

        // Calculado independentemente (script Python fora do projeto): startMondayOfGrid =
        // 2026-03-16, mudanças de mês nas colunas 0 (MAR.), 3 (ABR.), 7 (MAI.), 11 (JUN.),
        // 16 (JUL.), 20 (AGO.).
        val labelsByCol = result.monthLabels.associate { it.col to it.name }
        assertEquals(6, result.monthLabels.size)
        assertEquals("MAR.", labelsByCol[0])
        assertEquals("ABR.", labelsByCol[3])
        assertEquals("MAI.", labelsByCol[7])
        assertEquals("JUN.", labelsByCol[11])
        assertEquals("JUL.", labelsByCol[16])
        assertEquals("AGO.", labelsByCol[20])
    }
}