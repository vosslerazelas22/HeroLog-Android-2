package com.iurispraecepta.herolog

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.theme
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Rose400
import com.iurispraecepta.herolog.ui.theme.Rose500
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ModeThemeTest {

    @Test
    fun theme_padrao_usesTodayHeaderTokens() {
        // Arrange/Act
        val theme = RaidMode.PADRAO.theme()
        // Assert: idêntico ao header de hoje (FR-005, SC-001)
        assertEquals(Champagne400, theme.accent)
        assertEquals(Champagne500, theme.accentDim)
        assertEquals(Champagne400, theme.statusMain)
    }

    @Test
    fun theme_masmorra_usesDungeonTokens() {
        // Arrange/Act
        val theme = RaidMode.MASMORRA.theme()
        // Assert: FR-002
        assertEquals(Purple400, theme.accent)
        assertEquals(Purple500, theme.accentDim)
        assertEquals(Purple300, theme.statusMain)
    }

    @Test
    fun theme_selvagem_usesRoseAccent_D10() {
        // Arrange/Act
        val theme = RaidMode.SELVAGEM.theme()
        // Assert: D10 — accent FB7185 (Rose400), não F87171
        assertEquals(Rose400, theme.accent)
        assertEquals(Rose500, theme.accentDim)
        assertEquals(Rose400, theme.statusMain)
    }

    // SC-003: contraste de accent e statusMain sobre Stone950 >= 4.5:1 (WCAG).
    private val stone950 = Color(0xFF0C0A09)

    private fun linearize(channel: Float): Double {
        val c = channel.toDouble()
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(color: Color): Double =
        0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = luminance(foreground)
        val l2 = luminance(background)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun theme_contrast_accentAndStatusMain_meetAA_onStone950() {
        // Arrange: os três modos (inclui C084FC, D8B4FE e FB7185)
        val candidates = RaidMode.values().flatMap { mode ->
            val theme = mode.theme()
            listOf(theme.accent, theme.statusMain)
        }
        // Act/Assert: campo a campo, nunca igualdade de objeto inteiro
        candidates.forEach { color ->
            val ratio = contrastRatio(color, stone950)
            assertTrue(
                "Contraste ${"%.2f".format(ratio)}:1 insuficiente para $color sobre Stone950",
                ratio >= 4.5
            )
        }
    }
}
