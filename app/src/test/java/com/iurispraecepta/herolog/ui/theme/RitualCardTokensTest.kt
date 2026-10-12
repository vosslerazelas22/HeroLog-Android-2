package com.iurispraecepta.herolog.ui.theme

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class RitualCardTokensTest {

    @Test
    fun tokens_matchSpecHexes() {
        // Arrange/Act/Assert: campo a campo contra spec-rituais-cards §3
        assertEquals(Color(0xFF15121F), RitualSurface)
        assertEquals(Color(0xFF1A1725), RitualSurfaceElevated)
        assertEquals(Color(0xFF100D1C), RitualSurfaceDrawer)
        assertEquals(Color(0xFF2A2535), RitualBorder)
        assertEquals(Color(0xFFE8E0D0), RitualTextPrimary)
        assertEquals(Color(0xFFF4EFE6), RitualTextTitleDone)
        assertEquals(Color(0xFF928C9A), RitualTextSecondary)
        assertEquals(Color(0xFF625E69), RitualTextTertiary)
        assertEquals(Color(0xFFD9B85C), RitualGold)
        assertEquals(Color(0xFF69D6B7), RitualGreen)
        assertEquals(Color(0xFFDCC36A), RitualAmber)
        assertEquals(Color(0xFFB69AE8), RitualLavender)
        assertEquals(Color(0xFFD99AAA), RitualRose)
    }

    @Test
    fun difficultyMap_hasFourKeys_withPtLabelsAndPastels() {
        // Arrange/Act: mapa por enum (chave interna em inglês, label PT)
        // Assert: campo a campo
        val trivial = ritualDifficultyStyle(Difficulty.Trivial)
        assertEquals("Trivial", trivial.label)
        assertEquals(RitualAmber, trivial.pastel)
        assertEquals(RitualAmber, trivial.text)

        val easy = ritualDifficultyStyle(Difficulty.Easy)
        assertEquals("Fácil", easy.label)
        assertEquals(RitualGreen, easy.pastel)
        assertEquals(RitualGreen, easy.text)

        val medium = ritualDifficultyStyle(Difficulty.Medium)
        assertEquals("Médio", medium.label)
        assertEquals(RitualLavender, medium.pastel)
        assertEquals(RitualLavender, medium.text)

        val hard = ritualDifficultyStyle(Difficulty.Hard)
        assertEquals("Difícil", hard.label)
        assertEquals(RitualRose, hard.pastel)
        assertEquals(RitualRose, hard.text)
    }

    @Test
    fun difficultyMap_usesTwelveAndThirtyFivePercentAlphas() {
        // Arrange/Act: bg = pastel a 12%, border = pastel a 35%
        // Nota: Color(0xFF...) vive no espaço sRGB de 8 bits; copy(alpha) quantiza
        // 0.12*255=30.6→31 (31/255≈12.16%) e 0.35*255=89.25→89 (89/255≈34.9%).
        // Assert: canal alpha exato por chave
        for (difficulty in Difficulty.entries) {
            val style = ritualDifficultyStyle(difficulty)
            assertEquals(31f / 255f, style.bg.alpha, 0.0001f)
            assertEquals(89f / 255f, style.border.alpha, 0.0001f)
            assertEquals(style.pastel.red, style.bg.red, 0.001f)
            assertEquals(style.pastel.green, style.bg.green, 0.001f)
            assertEquals(style.pastel.blue, style.bg.blue, 0.001f)
            assertEquals(style.pastel.red, style.border.red, 0.001f)
            assertEquals(style.pastel.green, style.border.green, 0.001f)
            assertEquals(style.pastel.blue, style.border.blue, 0.001f)
        }
    }

    @Test
    fun difficultyMap_unknownKey_fallsBackToEasy() {
        // Arrange/Act: chave desconhecida/nula (espelha `|| DIFFICULTY_CONFIG.Easy`)
        // Assert: estilo Easy
        val expected = ritualDifficultyStyle(Difficulty.Easy)
        assertEquals(expected, ritualDifficultyStyleForKey("Unknown"))
        assertEquals(expected, ritualDifficultyStyleForKey(null))
        assertEquals(expected, ritualDifficultyStyleForKey(""))
        assertEquals(expected, ritualDifficultyStyleForKey("easy"))
    }

    @Test
    fun difficultyMap_stringKeys_matchEnumMap() {
        // Arrange/Act: lookup por String deve equivaler ao lookup por enum
        // Assert: campo a campo
        assertEquals(ritualDifficultyStyle(Difficulty.Trivial), ritualDifficultyStyleForKey("Trivial"))
        assertEquals(ritualDifficultyStyle(Difficulty.Easy), ritualDifficultyStyleForKey("Easy"))
        assertEquals(ritualDifficultyStyle(Difficulty.Medium), ritualDifficultyStyleForKey("Medium"))
        assertEquals(ritualDifficultyStyle(Difficulty.Hard), ritualDifficultyStyleForKey("Hard"))
    }
}
