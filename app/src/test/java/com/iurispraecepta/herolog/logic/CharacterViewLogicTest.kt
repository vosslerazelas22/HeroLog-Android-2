package com.iurispraecepta.herolog.logic

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.ui.theme.Amber300
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Emerald500
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Stone300
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone700
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Spec C — `CharacterViewLogic`. Espelho literal do React `CharacterScreen.tsx`:
 * `CATEGORY_LABELS` (40-65), `rarityMeta` (102-116), `formatHoursMinutes` (136-140),
 * `xpMax`/`xpPercent` (142-143) e o tile de Sequência (260).
 */
class CharacterViewLogicTest {

    // ------------------------------------------------------------------
    // CHAR-2 — selo de raridade
    // ------------------------------------------------------------------

    @Test
    fun rarityBadge_legendary_leLabelEDestaqueAmbar() {
        val badge = CharacterViewLogic.rarityBadge(TitleCategory.Legendary)

        assertEquals("LENDÁRIO", badge.label)
        assertEquals(Amber500.copy(alpha = 0.15f), badge.backgroundColor)
        assertEquals(Amber300, badge.textColor)
        assertEquals(Amber500.copy(alpha = 0.4f), badge.borderColor)
        assertEquals(Amber500.copy(alpha = 0.2f), badge.glowColor)
    }

    @Test
    fun rarityBadge_epic_epicoEVioleta() {
        val badge = CharacterViewLogic.rarityBadge(TitleCategory.Epic)

        assertEquals("ÉPICO", badge.label)
        assertEquals(Purple500.copy(alpha = 0.15f), badge.backgroundColor)
        assertEquals(Purple300, badge.textColor)
        assertEquals(Purple500.copy(alpha = 0.4f), badge.borderColor)
        assertEquals(Purple500.copy(alpha = 0.2f), badge.glowColor)
    }

    @Test
    fun rarityBadge_rare_raroECeleste() {
        val badge = CharacterViewLogic.rarityBadge(TitleCategory.Rare)

        assertEquals("RARO", badge.label)
        assertEquals(Color(0xFF0EA5E9).copy(alpha = 0.15f), badge.backgroundColor)
        assertEquals(Color(0xFF7DD3FC), badge.textColor)
        assertEquals(Color(0xFF0EA5E9).copy(alpha = 0.4f), badge.borderColor)
        assertEquals(Color(0xFF0EA5E9).copy(alpha = 0.2f), badge.glowColor)
    }

    @Test
    fun rarityBadge_achievement_conquistaEEsmeralda() {
        val badge = CharacterViewLogic.rarityBadge(TitleCategory.Achievement)

        assertEquals("CONQUISTA", badge.label)
        assertEquals(Emerald500.copy(alpha = 0.15f), badge.backgroundColor)
        assertEquals(Color(0xFF6EE7B7), badge.textColor)
        assertEquals(Emerald500.copy(alpha = 0.4f), badge.borderColor)
        assertEquals(Emerald500.copy(alpha = 0.2f), badge.glowColor)
    }

    @Test
    fun rarityBadge_drop_celestialECiano() {
        val badge = CharacterViewLogic.rarityBadge(TitleCategory.Drop)

        assertEquals("CELESTIAL", badge.label)
        assertEquals(Color(0xFF06B6D4).copy(alpha = 0.15f), badge.backgroundColor)
        assertEquals(Color(0xFF67E8F9), badge.textColor)
        assertEquals(Color(0xFF06B6D4).copy(alpha = 0.4f), badge.borderColor)
        assertEquals(Color(0xFF06B6D4).copy(alpha = 0.2f), badge.glowColor)
    }

    @Test
    fun rarityBadge_common_comumSemGlow() {
        val badge = CharacterViewLogic.rarityBadge(TitleCategory.Common)

        assertEquals("COMUM", badge.label)
        assertEquals(Stone800.copy(alpha = 0.8f), badge.backgroundColor)
        assertEquals(Stone300, badge.textColor)
        assertEquals(Stone700, badge.borderColor)
        assertNull(badge.glowColor)
    }

    @Test
    fun rarityBadge_semTitulo_aventureiroFundoStone900() {
        val badge = CharacterViewLogic.rarityBadge(null)

        assertEquals("AVENTUREIRO", badge.label)
        assertEquals(Stone900, badge.backgroundColor)
        assertEquals(Stone400, badge.textColor)
        assertEquals(Stone800, badge.borderColor)
        assertNull(badge.glowColor)
    }

    @Test
    fun rarityBadge_categoriasEnumTodasCobertas() {
        val rotulos = TitleCategory.entries.map { CharacterViewLogic.rarityBadge(it).label }

        assertEquals(
            listOf("COMUM", "RARO", "ÉPICO", "LENDÁRIO", "CONQUISTA", "CELESTIAL"),
            rotulos
        )
    }

    // ------------------------------------------------------------------
    // CHAR-4 — progressão
    // ------------------------------------------------------------------

    @Test
    fun xpMax_nivel4_quatrocentos() {
        assertEquals(400, CharacterViewLogic.xpMax(4))
    }

    @Test
    fun xpPercent_nivel4Com100Xp_vinteECincoPorCento() {
        assertEquals(25, CharacterViewLogic.xpPercent(100, 4))
    }

    @Test
    fun xpPercent_nivel1SemXp_zero() {
        assertEquals(0, CharacterViewLogic.xpPercent(0, 1))
    }

    @Test
    fun xpPercent_xpAcimaDoMax_clampEmCem() {
        assertEquals(100, CharacterViewLogic.xpPercent(200, 2))
    }

    @Test
    fun hpProgress_30De100_trintaPorCento() {
        assertEquals(0.3f, CharacterViewLogic.hpProgress(30, 100), 0.0001f)
    }

    @Test
    fun hpProgress_hpAcimaDoMax_clampEmUm() {
        assertEquals(1f, CharacterViewLogic.hpProgress(150, 100), 0.0001f)
    }

    @Test
    fun streakLabel_streak1_singular() {
        assertEquals("1 dia", CharacterViewLogic.streakLabel(1))
    }

    @Test
    fun streakLabel_streak5_plural() {
        assertEquals("5 dias", CharacterViewLogic.streakLabel(5))
    }

    @Test
    fun streakLabel_streak0_plural() {
        assertEquals("0 dias", CharacterViewLogic.streakLabel(0))
    }

    // ------------------------------------------------------------------
    // CHAR-4 — formatador (regra CharacterScreen: sempre com "h")
    // ------------------------------------------------------------------

    @Test
    fun formatHoursMinutes_cincoMinutos_zeroHZeroCincoM() {
        assertEquals("0h05m", CharacterViewLogic.formatHoursMinutes(5))
    }

    @Test
    fun formatHoursMinutes_centoEVinteECincoMinutos_doisHZeroCincoM() {
        assertEquals("2h05m", CharacterViewLogic.formatHoursMinutes(125))
    }

    @Test
    fun formatHoursMinutes_zeroMinutos_zeroHZeroZeroM() {
        assertEquals("0h00m", CharacterViewLogic.formatHoursMinutes(0))
    }

    @Test
    fun formatHoursMinutes_sessentaMinutos_umHZeroZeroM() {
        assertEquals("1h00m", CharacterViewLogic.formatHoursMinutes(60))
    }
}
