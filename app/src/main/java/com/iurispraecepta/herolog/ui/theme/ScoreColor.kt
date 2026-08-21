package com.iurispraecepta.herolog.ui.theme

import androidx.compose.ui.graphics.Color

data class ScoreColorTokens(val start: Color, val end: Color, val border: Color)

// Porte fiel de src/utils/scoreColor.ts (7 faixas). Alpha convertido de % Tailwind pra hex:
// 85%=0xD9 60%=0x99 55%=0x8C 50%=0x80 40%=0x66 30%=0x4D 25%=0x40
object ScoreColor {
    fun forScore(score: Int): ScoreColorTokens = when {
        score >= 10 -> ScoreColorTokens(Color(0x990C4A6E), Color(0x66083344), Color(0x9938BDF8))
        score >= 5  -> ScoreColorTokens(Color(0x80082F49), Color(0x401E1B4B), Color(0x660EA5E9))
        score >= 1  -> ScoreColorTokens(Color(0x8C022C22), Color(0x40042F2E), Color(0x8010B981))
        score >= -1 -> ScoreColorTokens(Color(0x80451A03), Color(0x4D1C1917), Color(0x80FBBF24))
        score >= -10 -> ScoreColorTokens(Color(0x807C2D12), Color(0x409A3412), Color(0x66FB923C))
        score >= -20 -> ScoreColorTokens(Color(0x99450A0A), Color(0x4D7F1D1D), Color(0x66EF4444))
        else -> ScoreColorTokens(Color(0xD9450A0A), Color(0x99000000), Color(0x99B91C1C))
    }
}
