package com.iurispraecepta.herolog.logic

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Testes unitários de [TavernLogic] — Spec B (tela Taverna).
 *
 * Fonte: `TavernFeat c89af8f`, `src/modules/tavern/TavernScreen.tsx` (`xpNeeded`,
 * `xpPercentage`, `formatHoursMinutes`, `formatRelativeTime` nas linhas 60-80; `gold` via
 * `toLocaleString('pt-BR')`; tile de Sequência na linha 199).
 *
 * Comparação campo a campo (AGENTS.md §6): nunca igualdade de objeto inteiro.
 * O fuso é fixado em UTC para que os casos de `formatRelativeTime` sejam determinísticos
 * (a função formata no fuso local, como o `toLocaleDateString` do React).
 */
class TavernLogicTest {

    private lateinit var defaultTimeZone: TimeZone

    @Before
    fun setUp() {
        defaultTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
    }

    /** Instante fixo 2026-09-20T12:00:00.000Z — "agora" de todos os casos relativos. */
    private val now: Date = isoDate("2026-09-20T12:00:00.000Z")

    private fun isoDate(value: String): Date =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(value)!!

    private fun iso(value: Date): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(value)

    private fun ago(millis: Long): String = iso(Date(now.time - millis))

    // ------------------------------------------------------------------
    // xpNeeded / xpPercent — TAV-3
    // ------------------------------------------------------------------

    @Test
    fun xpNeeded_level3_is300() {
        assertEquals(300, TavernLogic.xpNeeded(3))
    }

    @Test
    fun xpNeeded_level0_floorsAt100() {
        assertEquals(100, TavernLogic.xpNeeded(0))
    }

    @Test
    fun xpNeeded_level1_is100() {
        assertEquals(100, TavernLogic.xpNeeded(1))
    }

    @Test
    fun xpPercent_150xp_level3_is50() {
        assertEquals(50, TavernLogic.xpPercent(combatXP = 150, combatLevel = 3))
    }

    @Test
    fun xpPercent_capsAt100() {
        assertEquals(100, TavernLogic.xpPercent(combatXP = 1000, combatLevel = 1))
    }

    @Test
    fun xpPercent_zeroXP_is0() {
        assertEquals(0, TavernLogic.xpPercent(combatXP = 0, combatLevel = 5))
    }

    // ------------------------------------------------------------------
    // formatHoursMinutes — regra da Taverna (0h omitido) — TAV-4
    // ------------------------------------------------------------------

    @Test
    fun formatHoursMinutes_25_is25m() {
        assertEquals("25m", TavernLogic.formatHoursMinutes(25))
    }

    @Test
    fun formatHoursMinutes_125_is2h05m() {
        assertEquals("2h05m", TavernLogic.formatHoursMinutes(125))
    }

    @Test
    fun formatHoursMinutes_0_is0m() {
        assertEquals("0m", TavernLogic.formatHoursMinutes(0))
    }

    @Test
    fun formatHoursMinutes_5_is5m() {
        assertEquals("5m", TavernLogic.formatHoursMinutes(5))
    }

    @Test
    fun formatHoursMinutes_60_is1h00m() {
        assertEquals("1h00m", TavernLogic.formatHoursMinutes(60))
    }

    @Test
    fun formatHoursMinutes_605_is10h05m() {
        assertEquals("10h05m", TavernLogic.formatHoursMinutes(605))
    }

    // ------------------------------------------------------------------
    // formatGold — NumberFormat pt-BR sem "R$" — TAV-4
    // ------------------------------------------------------------------

    @Test
    fun formatGold_1234_is1234WithPtBrGrouping() {
        assertEquals("1.234", TavernLogic.formatGold(1234))
    }

    @Test
    fun formatGold_zero_is0() {
        assertEquals("0", TavernLogic.formatGold(0))
    }

    @Test
    fun formatGold_oneMillion_is1MilhaoWithPtBrGrouping() {
        assertEquals("1.000.000", TavernLogic.formatGold(1_000_000))
    }

    @Test
    fun formatGold_neverContainsCurrencySymbol() {
        assertTrue(!TavernLogic.formatGold(1234).contains("R$"))
    }

    // ------------------------------------------------------------------
    // streakLabel — pluralização D4 — TAV-4
    // ------------------------------------------------------------------

    @Test
    fun streakLabel_1_isSingular() {
        assertEquals("1 dia", TavernLogic.streakLabel(1))
    }

    @Test
    fun streakLabel_7_isPlural() {
        assertEquals("7 dias", TavernLogic.streakLabel(7))
    }

    @Test
    fun streakLabel_0_isPlural() {
        assertEquals("0 dias", TavernLogic.streakLabel(0))
    }

    // ------------------------------------------------------------------
    // formatRelativeTime — literal do React — TAV-7
    // ------------------------------------------------------------------

    @Test
    fun formatRelativeTime_nullAt_isRecente() {
        assertEquals("recente", TavernLogic.formatRelativeTime(null, now))
    }

    @Test
    fun formatRelativeTime_sameInstant_isAgora() {
        assertEquals("agora", TavernLogic.formatRelativeTime(iso(now), now))
    }

    @Test
    fun formatRelativeTime_futureAt_isAgora() {
        // diffMs < 0 → "agora"
        assertEquals("agora", TavernLogic.formatRelativeTime(iso(Date(now.time + 60_000)), now))
    }

    @Test
    fun formatRelativeTime_30seconds_isAgora() {
        // floor(30s/60s) = 0 minutos → "agora"
        assertEquals("agora", TavernLogic.formatRelativeTime(ago(30_000), now))
    }

    @Test
    fun formatRelativeTime_5minutes_isHa5m() {
        assertEquals("há 5m", TavernLogic.formatRelativeTime(ago(5 * 60_000), now))
    }

    @Test
    fun formatRelativeTime_59minutes_isHa59m() {
        assertEquals("há 59m", TavernLogic.formatRelativeTime(ago(59 * 60_000), now))
    }

    @Test
    fun formatRelativeTime_3hours_isHa3h() {
        assertEquals("há 3h", TavernLogic.formatRelativeTime(ago(3 * 3_600_000), now))
    }

    @Test
    fun formatRelativeTime_23hours_isHa23h() {
        assertEquals("há 23h", TavernLogic.formatRelativeTime(ago(23 * 3_600_000), now))
    }

    @Test
    fun formatRelativeTime_25hours_isOntem() {
        // diffDays = floor(25/24) = 1 → "ontem"
        assertEquals("ontem", TavernLogic.formatRelativeTime(ago(25 * 3_600_000), now))
    }

    @Test
    fun formatRelativeTime_3days_isHa3d() {
        assertEquals("há 3d", TavernLogic.formatRelativeTime(ago(3 * 86_400_000), now))
    }

    @Test
    fun formatRelativeTime_6days_isHa6d() {
        assertEquals("há 6d", TavernLogic.formatRelativeTime(ago(6 * 86_400_000), now))
    }

    @Test
    fun formatRelativeTime_10days_isDayMonthPtBr() {
        // diffDays = 10 (>= 7) → dd/MM pt-BR de 2026-09-10 (fuso UTC fixado no @Before)
        assertEquals("10/09", TavernLogic.formatRelativeTime(ago(10 * 86_400_000), now))
    }

    @Test
    fun formatRelativeTime_exactly7days_isDayMonthPtBr() {
        // diffDays = 7 → sai da faixa "há {d}d" (só < 7)
        assertEquals("13/09", TavernLogic.formatRelativeTime(ago(7 * 86_400_000), now))
    }
}
