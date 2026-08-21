package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.ui.theme.Amber400

private val Emerald400 = Color(0xFF34D399)
private val Purple400 = Color(0xFFC084FC)
private val Rose400 = Color(0xFFFB7185)

fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.Trivial -> "Trivial"
    Difficulty.Easy -> "Fácil"
    Difficulty.Medium -> "Médio"
    Difficulty.Hard -> "Difícil"
}

fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.Trivial -> Amber400
    Difficulty.Easy -> Emerald400
    Difficulty.Medium -> Purple400
    Difficulty.Hard -> Rose400
}

@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val (label, bg, border, textCol) = when (difficulty) {
        Difficulty.Trivial -> Quad(
            "Trivial",
            Color(0x26FBBF24),
            Color(0x4DFBBF24),
            Amber400
        )
        Difficulty.Easy -> Quad(
            "Fácil",
            Color(0x2610B981),
            Color(0x4D10B981),
            Emerald400
        )
        Difficulty.Medium -> Quad(
            "Médio",
            Color(0x26A855F7),
            Color(0x4DA855F7),
            Purple400
        )
        Difficulty.Hard -> Quad(
            "Difícil",
            Color(0x26F43F5E),
            Color(0x4DF43F5E),
            Rose400
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
