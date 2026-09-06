package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.em
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.OrbConcept
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.animation.core.CubicBezierEasing
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

enum class FocusOrbSize { COMPACT, STANDARD, FULLSCREEN }

enum class FocusMode {
    WORK, DUNGEON, WILDERNESS, BREAK, URGENT, PAUSED
}

// Concept A: Chrono-Relic
private object ConceptAColors {
    val WorkAccent = Color(0xFFE5C158)
    val WorkRingGrad = listOf(Color(0xFFD4AF37), Color(0xFFE5C158), Color(0xFFFEF08A))
    val WorkFrontGrad = listOf(Color(0xFFFEF08A), Color(0xFFE5C158), Color(0xFF855D14))
    val WorkBackGrad = listOf(Color(0xFFA1781B), Color(0xFF3B2505))
    val WorkGlow = Color(0xFFE5C158).copy(alpha = 0.25f)
    val WorkTick = Color(0xFFE5C158)

    val DungeonAccent = Color(0xFFC084FC)
    val DungeonRingGrad = listOf(Color(0xFF9333EA), Color(0xFFC084FC), Color(0xFFE9D5FF))
    val DungeonFrontGrad = listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF581C87))
    val DungeonBackGrad = listOf(Color(0xFF6B21A8), Color(0xFF2E1065))
    val DungeonGlow = Color(0xFFC084FC).copy(alpha = 0.25f)
    val DungeonTick = Color(0xFFC084FC)

    val WildernessAccent = Color(0xFFFB7185)
    val WildernessRingGrad = listOf(Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFFFE4E6))
    val WildernessFrontGrad = listOf(Color(0xFFFFE4E6), Color(0xFFF43F5E), Color(0xFF881337))
    val WildernessBackGrad = listOf(Color(0xFFBE123C), Color(0xFF4C0519))
    val WildernessGlow = Color(0xFFFB7185).copy(alpha = 0.25f)
    val WildernessTick = Color(0xFFFB7185)

    val BreakAccent = Color(0xFF10B981)
    val BreakRingGrad = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFFA7F3D0))
    val BreakFrontGrad = listOf(Color(0xFFA7F3D0), Color(0xFF10B981), Color(0xFF064E3B))
    val BreakBackGrad = listOf(Color(0xFF047857), Color(0xFF022C22))
    val BreakGlow = Color(0xFF10B981).copy(alpha = 0.25f)
    val BreakTick = Color(0xFF10B981)

    val UrgentAccent = Color(0xFFF43F5E)
    val UrgentRingGrad = listOf(Color(0xFFDC2626), Color(0xFFF43F5E), Color(0xFFFECDD3))
    val UrgentFrontGrad = listOf(Color(0xFFFECDD3), Color(0xFFEF4444), Color(0xFF7F1D1D))
    val UrgentBackGrad = listOf(Color(0xFFB91C1C), Color(0xFF450A0A))
    val UrgentGlow = Color(0xFFF43F5E).copy(alpha = 0.4f)
    val UrgentTick = Color(0xFFF43F5E)

    val PausedAccent = Color(0xFFA1A1AA)
    val PausedRingGrad = listOf(Color(0xFF71717A), Color(0xFFA1A1AA), Color(0xFFD4D4D8))
    val PausedFrontGrad = listOf(Color(0xFFD4D4D8), Color(0xFF71717A), Color(0xFF27272A))
    val PausedBackGrad = listOf(Color(0xFF52525B), Color(0xFF18181B))
    val PausedGlow = Color(0xFFA1A1AA).copy(alpha = 0.1f)
    val PausedTick = Color(0xFF71717A)
}

// Concept B: Obsidian Core
private object ConceptBColors {
    val WorkAccent = Color(0xFFFEF08A)
    val WorkLiquidFront = listOf(Color(0xFFFEF08A), Color(0xFFE5C158), Color(0xFF713F12))
    val WorkLiquidBack = listOf(Color(0xFFB48C26), Color(0xFF422006))
    val WorkGlow = Color(0xFFE5C158).copy(alpha = 0.2f)
    val WorkRune = Color(0xFFE5C158)

    val DungeonAccent = Color(0xFFE9D5FF)
    val DungeonLiquidFront = listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF3B0764))
    val DungeonLiquidBack = listOf(Color(0xFF7E22CE), Color(0xFF1E0538))
    val DungeonGlow = Color(0xFFA855F7).copy(alpha = 0.2f)
    val DungeonRune = Color(0xFFA855F7)

    val WildernessAccent = Color(0xFFFFE4E6)
    val WildernessLiquidFront = listOf(Color(0xFFFFE4E6), Color(0xFFF43F5E), Color(0xFF4C0519))
    val WildernessLiquidBack = listOf(Color(0xFFBE123C), Color(0xFF20020A))
    val WildernessGlow = Color(0xFFF43F5E).copy(alpha = 0.2f)
    val WildernessRune = Color(0xFFF43F5E)

    val BreakAccent = Color(0xFFA7F3D0)
    val BreakLiquidFront = listOf(Color(0xFFA7F3D0), Color(0xFF10B981), Color(0xFF022C22))
    val BreakLiquidBack = listOf(Color(0xFF059669), Color(0xFF011711))
    val BreakGlow = Color(0xFF10B981).copy(alpha = 0.2f)
    val BreakRune = Color(0xFF10B981)

    val UrgentAccent = Color(0xFFFECDD3)
    val UrgentLiquidFront = listOf(Color(0xFFFECDD3), Color(0xFFEF4444), Color(0xFF450A0A))
    val UrgentLiquidBack = listOf(Color(0xFFB91C1C), Color(0xFF200404))
    val UrgentGlow = Color(0xFFEF4444).copy(alpha = 0.35f)
    val UrgentRune = Color(0xFFEF4444)

    val PausedAccent = Color(0xFFE4E4E7)
    val PausedLiquidFront = listOf(Color(0xFFD4D4D8), Color(0xFF71717A), Color(0xFF18181B))
    val PausedLiquidBack = listOf(Color(0xFF52525B), Color(0xFF09090B))
    val PausedGlow = Color(0xFF71717A).copy(alpha = 0.1f)
    val PausedRune = Color(0xFF71717A)

    val VignetteStops = listOf(
        Color.White.copy(alpha = 0.08f),
        Color.Black.copy(alpha = 0.4f),
        Color.Black.copy(alpha = 0.85f)
    )
    val GoldRing = Color(0xFFE5C158)
    val DarkRing = Color(0xFF27272A)
}

// Concept C: Arcane Dial
private object ConceptCColors {
    val WorkTrack = Color(0xFFE5C158)
    val WorkTrackBg = Color(0xFF27272A)
    val WorkBgGlow = Color(0xFFE5C158).copy(alpha = 0.15f)
    val WorkText = Color(0xFFFEF08A)

    val DungeonTrack = Color(0xFFC084FC)
    val DungeonTrackBg = Color(0xFF2E1065)
    val DungeonBgGlow = Color(0xFFC084FC).copy(alpha = 0.15f)
    val DungeonText = Color(0xFFE9D5FF)

    val WildernessTrack = Color(0xFFFB7185)
    val WildernessTrackBg = Color(0xFF4C0519)
    val WildernessBgGlow = Color(0xFFFB7185).copy(alpha = 0.15f)
    val WildernessText = Color(0xFFFFE4E6)

    val BreakTrack = Color(0xFF10B981)
    val BreakTrackBg = Color(0xFF022C22)
    val BreakBgGlow = Color(0xFF10B981).copy(alpha = 0.15f)
    val BreakText = Color(0xFFA7F3D0)

    val UrgentTrack = Color(0xFFF43F5E)
    val UrgentTrackBg = Color(0xFF450A0A)
    val UrgentBgGlow = Color(0xFFF43F5E).copy(alpha = 0.3f)
    val UrgentText = Color(0xFFFECDD3)

    val PausedTrack = Color(0xFF71717A)
    val PausedTrackBg = Color(0xFF18181B)
    val PausedBgGlow = Color(0xFF71717A).copy(alpha = 0.05f)
    val PausedText = Color(0xFFD4D4D8)
}

// Concept D: Alchemist Flask
private object ConceptDColors {
    val WorkText = Color(0xFFFEF08A)
    val WorkLiquid = listOf(Color(0xFFFEF08A), Color(0xFFE5C158), Color(0xFF854D0E))
    val WorkBack = listOf(Color(0xFFA16207), Color(0xFF451A03))
    val WorkGlow = Color(0xFFE5C158).copy(alpha = 0.25f)

    val DungeonText = Color(0xFFE9D5FF)
    val DungeonLiquid = listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF581C87))
    val DungeonBack = listOf(Color(0xFF7E22CE), Color(0xFF3B0764))
    val DungeonGlow = Color(0xFFA855F7).copy(alpha = 0.25f)

    val WildernessText = Color(0xFFFFE4E6)
    val WildernessLiquid = listOf(Color(0xFFFFE4E6), Color(0xFFF43F5E), Color(0xFF881337))
    val WildernessBack = listOf(Color(0xFFBE123C), Color(0xFF4C0519))
    val WildernessGlow = Color(0xFFF43F5E).copy(alpha = 0.25f)

    val BreakText = Color(0xFFA7F3D0)
    val BreakLiquid = listOf(Color(0xFFA7F3D0), Color(0xFF10B981), Color(0xFF064E3B))
    val BreakBack = listOf(Color(0xFF059669), Color(0xFF022C22))
    val BreakGlow = Color(0xFF10B981).copy(alpha = 0.25f)

    val UrgentText = Color(0xFFFECDD3)
    val UrgentLiquid = listOf(Color(0xFFFECDD3), Color(0xFFEF4444), Color(0xFF7F1D1D))
    val UrgentBack = listOf(Color(0xFFB91C1C), Color(0xFF450A0A))
    val UrgentGlow = Color(0xFFEF4444).copy(alpha = 0.35f)

    val PausedText = Color(0xFFD4D4D8)
    val PausedLiquid = listOf(Color(0xFFD4D4D8), Color(0xFF71717A), Color(0xFF27272A))
    val PausedBack = listOf(Color(0xFF52525B), Color(0xFF18181B))
    val PausedGlow = Color(0xFF71717A).copy(alpha = 0.1f)

    val OctagonStroke = Color(0xFFE5C158)
    val HighlightWhite = Color.White
    val RingWhite = Color.White
}

// ============================================================================
// Helpers
// ============================================================================
private fun getMode(
    isBreakActive: Boolean,
    isUrgent: Boolean,
    isPaused: Boolean,
    isDungeonMode: Boolean,
    isWildernessMode: Boolean
): FocusMode {
    return when {
        isBreakActive -> FocusMode.BREAK
        isUrgent -> FocusMode.URGENT
        isPaused -> FocusMode.PAUSED
        isDungeonMode -> FocusMode.DUNGEON
        isWildernessMode -> FocusMode.WILDERNESS
        else -> FocusMode.WORK
    }
}

private fun formatOrbTime(seconds: Int): String {
    val m = (seconds / 60).toString().padStart(2, '0')
    val s = (seconds % 60).toString().padStart(2, '0')
    return "$m:$s"
}

private fun buildCirclePath(center: Offset, radius: Float): Path {
    return Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(center = center, radius = radius))
    }
}

private fun buildWavePath(baseY: Float, phase: Float, amplitude: Float, scale: Float, w: Float, h: Float): Path {
    val n = 12
    val width = 120f
    val startX = -10f
    val path = Path()
    path.moveTo(-10f * scale, 110f * scale)
    for (i in 0..n) {
        val x = startX + (i * width) / n
        val angle = (x / width) * PI.toFloat() * 2f * 1.2f + phase
        val y = baseY + sin(angle) * amplitude
        path.lineTo(x * scale, y * scale)
    }
    path.lineTo(110f * scale, 110f * scale)
    path.close()
    return path
}

/**
 * Converte um comando de arco elíptico no formato SVG `A rx,ry x-rot large-arc,sweep x,y`
 * (endpoint parameterization, ver spec SVG 1.1 F.6.5) para os parâmetros de centro
 * exigidos pelo `Path.arcTo` do Compose. Assume x-axis-rotation = 0, que é suficiente
 * para todos os paths estáticos de highlight/decoração do FocusOrb (Conceitos A/B/D).
 *
 * Sem essa conversão, arcos "A" do React acabam sendo aproximados a olho (ex.: circulo
 * centrado no meio do orb), o que desloca visualmente o highlight de vidro.
 */
private fun Path.svgArcTo(
    startX: Float, startY: Float,
    endX: Float, endY: Float,
    rx: Float, ry: Float,
    largeArcFlag: Boolean,
    sweepFlag: Boolean
) {
    val x1p = (startX - endX) / 2f
    val y1p = (startY - endY) / 2f

    var rxAdj = rx
    var ryAdj = ry
    val lambda = (x1p * x1p) / (rxAdj * rxAdj) + (y1p * y1p) / (ryAdj * ryAdj)
    if (lambda > 1f) {
        val s = sqrt(lambda)
        rxAdj *= s
        ryAdj *= s
    }

    val sign = if (largeArcFlag == sweepFlag) -1f else 1f
    val num = rxAdj * rxAdj * ryAdj * ryAdj - rxAdj * rxAdj * y1p * y1p - ryAdj * ryAdj * x1p * x1p
    val den = rxAdj * rxAdj * y1p * y1p + ryAdj * ryAdj * x1p * x1p
    val coef = sign * sqrt(max(0f, num / den))

    val cxp = coef * (rxAdj * y1p / ryAdj)
    val cyp = coef * (-ryAdj * x1p / rxAdj)

    val cx = cxp + (startX + endX) / 2f
    val cy = cyp + (startY + endY) / 2f

    val startAngle = Math.toDegrees(
        atan2(((startY - cy) / ryAdj).toDouble(), ((startX - cx) / rxAdj).toDouble())
    ).toFloat()
    val endAngle = Math.toDegrees(
        atan2(((endY - cy) / ryAdj).toDouble(), ((endX - cx) / rxAdj).toDouble())
    ).toFloat()

    var sweep = endAngle - startAngle
    if (!sweepFlag && sweep > 0f) sweep -= 360f
    if (sweepFlag && sweep < 0f) sweep += 360f

    arcTo(
        rect = androidx.compose.ui.geometry.Rect(cx - rxAdj, cy - ryAdj, cx + rxAdj, cy + ryAdj),
        startAngleDegrees = startAngle,
        sweepAngleDegrees = sweep,
        forceMoveTo = false
    )
}

private data class ColorSet(
    val accent: Color = Color.Transparent,
    val frontGrad: List<Color> = emptyList(),
    val backGrad: List<Color> = emptyList(),
    val glow: Color = Color.Transparent,
    val ringGrad: List<Color>? = null,
    val tickColor: Color? = null,
    val liquidFront: List<Color>? = null,
    val liquidBack: List<Color>? = null,
    val runeColor: Color? = null,
    val track: Color? = null,
    val trackBg: Color? = null,
    val bgGlow: Color? = null,
    val textColor: Color? = null,
    val liquid: List<Color>? = null,
    val back: List<Color>? = null,
    val octagonStroke: Color? = null,
    val highlightWhite: Color? = null,
    val ringWhite: Color? = null,
    val vignetteStops: List<Color>? = null,
    val goldRing: Color? = null,
    val darkRing: Color? = null
)

private fun getColorsA(mode: FocusMode): ColorSet = when (mode) {
    FocusMode.WORK -> ColorSet(
        accent = ConceptAColors.WorkAccent,
        frontGrad = ConceptAColors.WorkFrontGrad,
        backGrad = ConceptAColors.WorkBackGrad,
        glow = ConceptAColors.WorkGlow,
        ringGrad = ConceptAColors.WorkRingGrad,
        tickColor = ConceptAColors.WorkTick
    )
    FocusMode.DUNGEON -> ColorSet(
        accent = ConceptAColors.DungeonAccent,
        frontGrad = ConceptAColors.DungeonFrontGrad,
        backGrad = ConceptAColors.DungeonBackGrad,
        glow = ConceptAColors.DungeonGlow,
        ringGrad = ConceptAColors.DungeonRingGrad,
        tickColor = ConceptAColors.DungeonTick
    )
    FocusMode.WILDERNESS -> ColorSet(
        accent = ConceptAColors.WildernessAccent,
        frontGrad = ConceptAColors.WildernessFrontGrad,
        backGrad = ConceptAColors.WildernessBackGrad,
        glow = ConceptAColors.WildernessGlow,
        ringGrad = ConceptAColors.WildernessRingGrad,
        tickColor = ConceptAColors.WildernessTick
    )
    FocusMode.BREAK -> ColorSet(
        accent = ConceptAColors.BreakAccent,
        frontGrad = ConceptAColors.BreakFrontGrad,
        backGrad = ConceptAColors.BreakBackGrad,
        glow = ConceptAColors.BreakGlow,
        ringGrad = ConceptAColors.BreakRingGrad,
        tickColor = ConceptAColors.BreakTick
    )
    FocusMode.URGENT -> ColorSet(
        accent = ConceptAColors.UrgentAccent,
        frontGrad = ConceptAColors.UrgentFrontGrad,
        backGrad = ConceptAColors.UrgentBackGrad,
        glow = ConceptAColors.UrgentGlow,
        ringGrad = ConceptAColors.UrgentRingGrad,
        tickColor = ConceptAColors.UrgentTick
    )
    FocusMode.PAUSED -> ColorSet(
        accent = ConceptAColors.PausedAccent,
        frontGrad = ConceptAColors.PausedFrontGrad,
        backGrad = ConceptAColors.PausedBackGrad,
        glow = ConceptAColors.PausedGlow,
        ringGrad = ConceptAColors.PausedRingGrad,
        tickColor = ConceptAColors.PausedTick
    )
}

private fun getColorsB(mode: FocusMode): ColorSet = when (mode) {
    FocusMode.WORK -> ColorSet(
        accent = ConceptBColors.WorkAccent,
        liquidFront = ConceptBColors.WorkLiquidFront,
        liquidBack = ConceptBColors.WorkLiquidBack,
        glow = ConceptBColors.WorkGlow,
        runeColor = ConceptBColors.WorkRune
    )
    FocusMode.DUNGEON -> ColorSet(
        accent = ConceptBColors.DungeonAccent,
        liquidFront = ConceptBColors.DungeonLiquidFront,
        liquidBack = ConceptBColors.DungeonLiquidBack,
        glow = ConceptBColors.DungeonGlow,
        runeColor = ConceptBColors.DungeonRune
    )
    FocusMode.WILDERNESS -> ColorSet(
        accent = ConceptBColors.WildernessAccent,
        liquidFront = ConceptBColors.WildernessLiquidFront,
        liquidBack = ConceptBColors.WildernessLiquidBack,
        glow = ConceptBColors.WildernessGlow,
        runeColor = ConceptBColors.WildernessRune
    )
    FocusMode.BREAK -> ColorSet(
        accent = ConceptBColors.BreakAccent,
        liquidFront = ConceptBColors.BreakLiquidFront,
        liquidBack = ConceptBColors.BreakLiquidBack,
        glow = ConceptBColors.BreakGlow,
        runeColor = ConceptBColors.BreakRune
    )
    FocusMode.URGENT -> ColorSet(
        accent = ConceptBColors.UrgentAccent,
        liquidFront = ConceptBColors.UrgentLiquidFront,
        liquidBack = ConceptBColors.UrgentLiquidBack,
        glow = ConceptBColors.UrgentGlow,
        runeColor = ConceptBColors.UrgentRune
    )
    FocusMode.PAUSED -> ColorSet(
        accent = ConceptBColors.PausedAccent,
        liquidFront = ConceptBColors.PausedLiquidFront,
        liquidBack = ConceptBColors.PausedLiquidBack,
        glow = ConceptBColors.PausedGlow,
        runeColor = ConceptBColors.PausedRune
    )
}

private fun getColorsC(mode: FocusMode): ColorSet = when (mode) {
    FocusMode.WORK -> ColorSet(
        track = ConceptCColors.WorkTrack,
        trackBg = ConceptCColors.WorkTrackBg,
        bgGlow = ConceptCColors.WorkBgGlow,
        textColor = ConceptCColors.WorkText
    )
    FocusMode.DUNGEON -> ColorSet(
        track = ConceptCColors.DungeonTrack,
        trackBg = ConceptCColors.DungeonTrackBg,
        bgGlow = ConceptCColors.DungeonBgGlow,
        textColor = ConceptCColors.DungeonText
    )
    FocusMode.WILDERNESS -> ColorSet(
        track = ConceptCColors.WildernessTrack,
        trackBg = ConceptCColors.WildernessTrackBg,
        bgGlow = ConceptCColors.WildernessBgGlow,
        textColor = ConceptCColors.WildernessText
    )
    FocusMode.BREAK -> ColorSet(
        track = ConceptCColors.BreakTrack,
        trackBg = ConceptCColors.BreakTrackBg,
        bgGlow = ConceptCColors.BreakBgGlow,
        textColor = ConceptCColors.BreakText
    )
    FocusMode.URGENT -> ColorSet(
        track = ConceptCColors.UrgentTrack,
        trackBg = ConceptCColors.UrgentTrackBg,
        bgGlow = ConceptCColors.UrgentBgGlow,
        textColor = ConceptCColors.UrgentText
    )
    FocusMode.PAUSED -> ColorSet(
        track = ConceptCColors.PausedTrack,
        trackBg = ConceptCColors.PausedTrackBg,
        bgGlow = ConceptCColors.PausedBgGlow,
        textColor = ConceptCColors.PausedText
    )
}

private fun getColorsD(mode: FocusMode): ColorSet = when (mode) {
    FocusMode.WORK -> ColorSet(
        textColor = ConceptDColors.WorkText,
        liquid = ConceptDColors.WorkLiquid,
        back = ConceptDColors.WorkBack,
        glow = ConceptDColors.WorkGlow,
        octagonStroke = ConceptDColors.OctagonStroke,
        ringWhite = ConceptDColors.RingWhite,
        highlightWhite = ConceptDColors.HighlightWhite
    )
    FocusMode.DUNGEON -> ColorSet(
        textColor = ConceptDColors.DungeonText,
        liquid = ConceptDColors.DungeonLiquid,
        back = ConceptDColors.DungeonBack,
        glow = ConceptDColors.DungeonGlow,
        octagonStroke = ConceptDColors.OctagonStroke,
        ringWhite = ConceptDColors.RingWhite,
        highlightWhite = ConceptDColors.HighlightWhite
    )
    FocusMode.WILDERNESS -> ColorSet(
        textColor = ConceptDColors.WildernessText,
        liquid = ConceptDColors.WildernessLiquid,
        back = ConceptDColors.WildernessBack,
        glow = ConceptDColors.WildernessGlow,
        octagonStroke = ConceptDColors.OctagonStroke,
        ringWhite = ConceptDColors.RingWhite,
        highlightWhite = ConceptDColors.HighlightWhite
    )
    FocusMode.BREAK -> ColorSet(
        textColor = ConceptDColors.BreakText,
        liquid = ConceptDColors.BreakLiquid,
        back = ConceptDColors.BreakBack,
        glow = ConceptDColors.BreakGlow,
        octagonStroke = ConceptDColors.OctagonStroke,
        ringWhite = ConceptDColors.RingWhite,
        highlightWhite = ConceptDColors.HighlightWhite
    )
    FocusMode.URGENT -> ColorSet(
        textColor = ConceptDColors.UrgentText,
        liquid = ConceptDColors.UrgentLiquid,
        back = ConceptDColors.UrgentBack,
        glow = ConceptDColors.UrgentGlow,
        octagonStroke = ConceptDColors.OctagonStroke,
        ringWhite = ConceptDColors.RingWhite,
        highlightWhite = ConceptDColors.HighlightWhite
    )
    FocusMode.PAUSED -> ColorSet(
        textColor = ConceptDColors.PausedText,
        liquid = ConceptDColors.PausedLiquid,
        back = ConceptDColors.PausedBack,
        glow = ConceptDColors.PausedGlow,
        octagonStroke = ConceptDColors.OctagonStroke,
        ringWhite = ConceptDColors.RingWhite,
        highlightWhite = ConceptDColors.HighlightWhite
    )
}

// ============================================================================
// Concept A: Chrono-Relic Composables
// ============================================================================
@Composable
private fun OrbConceptA(
    progress: Float,
    timeLeftSeconds: Int,
    mode: FocusMode,
    isRunning: Boolean,
    size: FocusOrbSize,
    boxSizeDp: androidx.compose.ui.unit.Dp
) {
    val colors = getColorsA(mode)

    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(isRunning && mode != FocusMode.PAUSED) {
        if (isRunning && mode != FocusMode.PAUSED) {
            while (true) {
                phase += 0.06f
                if (phase > (2 * PI).toFloat()) phase -= (2 * PI).toFloat()
                delay(33)
            }
        }
    }

    val baseY = 100f - progress * 100f
    val amplitude = if (isRunning && mode != FocusMode.PAUSED && progress > 0.02f && progress < 0.98f) {
        3.5f * sin(progress * PI.toFloat())
    } else 0f

    val radius = 46f

    // React usa `transition-all duration-300` no progress ring — animação suave de 300ms
    val animatedSweepAngle by animateFloatAsState(
        targetValue = progress * 360f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "sweepAngleA"
    )

    val textSizeSp = if (size == FocusOrbSize.FULLSCREEN) 44.sp else if (size == FocusOrbSize.STANDARD) 26.sp else 18.sp
    val percentSizeSp = if (size == FocusOrbSize.FULLSCREEN) 14.sp else if (size == FocusOrbSize.STANDARD) 9.sp else 7.sp

    Box(
        modifier = androidx.compose.ui.Modifier.size(boxSizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val scale = min(w, h) / 100f
            val center = Offset(w / 2f, h / 2f)

            // O path da onda precisa ser construído AQUI, com o `scale` real em px —
            // construí-lo fora do Canvas deixava as coordenadas em unidades 0-100 "cruas",
            // nunca multiplicadas pelo scale de pixels, então o líquido era desenhado como
            // um patch minúsculo perto do canto (0,0) do canvas, fora da área visível do círculo.
            val frontPath = buildWavePath(baseY, phase, amplitude, scale, 100f, 100f)
            val backPath = buildWavePath(baseY, -phase * 0.8f + PI.toFloat(), amplitude * 0.7f, scale, 100f, 100f)

            drawCircle(color = colors.glow, radius = 50f * scale, center = center)

            // Background circles
            drawCircle(color = Color(0xFF0C0C10), radius = 48.5f * scale, center = center) // preenchimento
            drawCircle(color = Color(0xFF27272A), radius = 48.5f * scale, center = center, style = Stroke(width = 0.8f * scale)) // contorno
            drawCircle(color = Color(0xFF18181B), radius = 46f * scale, center = center, style = Stroke(width = 3f * scale))

            // Ticks — React: x1="50" y1="2.5" rotacionado ao redor de (50,50).
            // Coordenadas relativas ao topo do viewBox (y=0), não ao centro.
            for (i in 0..11) {
                val angle = (i * 360f) / 12
                val isMajor = i % 3 == 0
                rotate(angle, pivot = center) {
                    drawLine(
                        start = Offset(center.x, center.y - (50f - 2.5f) * scale),
                        end = Offset(center.x, center.y - (50f - (if (isMajor) 6.5f else 5f)) * scale),
                        color = colors.tickColor!!,
                        strokeWidth = (if (isMajor) 1.2f else 0.6f) * scale,
                        alpha = if (isMajor) 0.9f else 0.4f
                    )
                }
            }

            // Progress ring with sweep gradient — animação suave de 300ms
            val ringGrad = Brush.sweepGradient(
                colors = colors.ringGrad!!,
                center = center
            )
            if (animatedSweepAngle > 0f) {
                drawArc(
                    brush = ringGrad,
                    startAngle = -90f,
                    sweepAngle = animatedSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * scale, center.y - radius * scale),
                    size = Size(radius * 2f * scale, radius * 2f * scale),
                    style = Stroke(
                        width = 2.2f * scale,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Inner circle
            drawCircle(color = Color(0xFF09090B), radius = 40f * scale, center = center)
            drawCircle(color = Color(0xFF3F3F46), radius = 40f * scale, center = center, style = Stroke(width = 0.8f * scale))

            // Clipped wave area
            clipPath(buildCirclePath(center, 39.5f * scale)) {
                drawPath(
                    path = backPath,
                    brush = Brush.verticalGradient(colors.backGrad.map { it.copy(alpha = if (it == colors.backGrad[0]) 0.6f else 0.75f) })
                )
                drawPath(
                    path = frontPath,
                    brush = Brush.verticalGradient(colors.frontGrad.map { it.copy(alpha = if (it == colors.frontGrad[0]) 0.9f else if (it == colors.frontGrad[1]) 0.92f else 0.98f) })
                )
                drawLine(
                    start = Offset(0f, baseY * scale),
                    end = Offset(100f * scale, baseY * scale),
                    color = Color.White.copy(alpha = 0.3f),
                    strokeWidth = 0.6f * scale
                )
                // Decorative bubbles
                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 0.9f * scale, center = Offset(35f * scale, 58f * scale))
                drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 1.3f * scale, center = Offset(68f * scale, 68f * scale))
                drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 1.1f * scale, center = Offset(50f * scale, 78f * scale))
            }

            // Highlight (React: `M 20,30 A 32,32 0 0,1 80,30 A 35,35 0 0,0 20,30 Z`)
            // Dois arcos elípticos "A" cujo centro geométrico não é (50,50) —
            // o resultado correto é um crescente fino perto do topo do orb (brilho de vidro).
            fun pt(x: Float, y: Float) = Offset(center.x + (x - 50f) * scale, center.y + (y - 50f) * scale)
            val s0 = pt(20f, 30f)
            val s1 = pt(80f, 30f)
            val highlightPath = Path().apply {
                moveTo(s0.x, s0.y)
                svgArcTo(
                    startX = s0.x, startY = s0.y, endX = s1.x, endY = s1.y,
                    rx = 32f * scale, ry = 32f * scale, largeArcFlag = false, sweepFlag = true
                )
                svgArcTo(
                    startX = s1.x, startY = s1.y, endX = s0.x, endY = s0.y,
                    rx = 35f * scale, ry = 35f * scale, largeArcFlag = false, sweepFlag = false
                )
                close()
            }
            drawPath(path = highlightPath, color = Color.White.copy(alpha = 0.12f))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatOrbTime(timeLeftSeconds),
                color = colors.accent,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Black,
                fontSize = textSizeSp,
                letterSpacing = (-0.5f).sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.9f),
                        offset = Offset(0f, 2f),
                        blurRadius = 12f
                    )
                ),
                modifier = Modifier.padding(vertical = 0.5.dp)
            )
            Text(
                text = "${Math.round(progress * 100)}% Restante",
                color = Color(0xFF9CA3AF),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = percentSizeSp,
                letterSpacing = 0.2f.em,
                modifier = Modifier.padding(top = 0.5.dp)
            )
        }
    }
}

// ============================================================================
// Concept B: Obsidian Core Composables
// ============================================================================
@Composable
private fun OrbConceptB(
    progress: Float,
    timeLeftSeconds: Int,
    mode: FocusMode,
    isRunning: Boolean,
    size: FocusOrbSize,
    boxSizeDp: androidx.compose.ui.unit.Dp
) {
    val colors = getColorsB(mode)

    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(isRunning && mode != FocusMode.PAUSED) {
        if (isRunning && mode != FocusMode.PAUSED) {
            while (true) {
                phase += 0.05f
                if (phase > (2 * PI).toFloat()) phase -= (2 * PI).toFloat()
                delay(33)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubblePulse")
    val bubbleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleAlpha"
    )

    val baseY = 100f - progress * 100f
    val amplitude = if (isRunning && mode != FocusMode.PAUSED && progress > 0.02f && progress < 0.98f) {
        4.2f * sin(progress * PI.toFloat())
    } else 0f

    val textSizeSp = if (size == FocusOrbSize.FULLSCREEN) 44.sp else if (size == FocusOrbSize.STANDARD) 26.sp else 18.sp

    Box(
        modifier = androidx.compose.ui.Modifier.size(boxSizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val scale = min(w, h) / 100f
            val center = Offset(w / 2f, h / 2f)

            // O path da onda precisa ser construído AQUI, com o `scale` real em px.
            val frontPath = buildWavePath(baseY, phase, amplitude, scale, 100f, 100f)
            val backPath = buildWavePath(baseY, -phase * 0.75f + PI.toFloat(), amplitude * 0.75f, scale, 100f, 100f)

            drawCircle(color = colors.glow, radius = 48f * scale, center = center)

            // Base circle
            drawCircle(color = Color(0xFF09090B), radius = 45f * scale, center = center)
            drawCircle(color = Color(0xFF1F1F23), radius = 45f * scale, center = center, style = Stroke(width = 1f * scale))

            // Runes
            val runeColor = colors.runeColor!!
            drawPath(
                path = Path().apply {
                    moveTo(center.x, center.y - 35f * scale)
                    lineTo(center.x + 28f * scale, center.y + 15f * scale)
                    lineTo(center.x - 28f * scale, center.y + 15f * scale)
                    close()
                },
                color = runeColor.copy(alpha = 0.08f),
                style = Stroke(width = 0.8f * scale)
            )
            drawPath(
                path = Path().apply {
                    moveTo(center.x, center.y + 35f * scale)
                    lineTo(center.x + 28f * scale, center.y - 15f * scale)
                    lineTo(center.x - 28f * scale, center.y - 15f * scale)
                    close()
                },
                color = runeColor.copy(alpha = 0.08f),
                style = Stroke(width = 0.8f * scale)
            )
            drawCircle(
                color = runeColor.copy(alpha = 0.08f),
                radius = 18f * scale,
                center = center,
                style = Stroke(width = 0.8f * scale)
            )

            // Clipped wave area
            clipPath(buildCirclePath(center, 43f * scale)) {
                drawPath(
                    path = backPath,
                    brush = Brush.verticalGradient(colors.liquidBack!!.map { it.copy(alpha = if (it == colors.liquidBack!![0]) 0.6f else 0.75f) })
                )
                drawPath(
                    path = frontPath,
                    brush = Brush.verticalGradient(colors.liquidFront!!.map { it.copy(alpha = if (it == colors.liquidFront!![0]) 0.9f else if (it == colors.liquidFront!![1]) 0.95f else 0.98f) })
                )
                drawLine(
                    start = Offset(0f, baseY * scale),
                    end = Offset(100f * scale, baseY * scale),
                    color = Color.White.copy(alpha = 0.35f),
                    strokeWidth = 1f * scale
                )
                // Bubbles with pulse animation
                drawCircle(color = Color.White.copy(alpha = bubbleAlpha), radius = 1.2f * scale, center = Offset(38f * scale, 62f * scale))
                drawCircle(color = Color.White.copy(alpha = bubbleAlpha * 0.65f), radius = 1.5f * scale, center = Offset(64f * scale, 74f * scale))
                drawCircle(color = Color.White.copy(alpha = bubbleAlpha * 0.8f), radius = 0.9f * scale, center = Offset(48f * scale, 82f * scale))
            }

            // Vignette
            drawCircle(
                radius = 43f * scale,
                center = center,
                brush = Brush.radialGradient(
                    center = center,
                    radius = 43f * scale,
                    colors = ConceptBColors.VignetteStops
                )
            )

            // Gold ring
            drawCircle(
                color = ConceptBColors.GoldRing.copy(alpha = 0.3f),
                radius = 44.5f * scale,
                center = center,
                style = Stroke(width = 0.75f * scale)
            )
            drawCircle(
                color = ConceptBColors.DarkRing.copy(alpha = 1f),
                radius = 45.5f * scale,
                center = center,
                style = Stroke(width = 0.5f * scale)
            )

            // Highlight (React: `M 16,36 A 38,38 0 0,1 84,36 A 42,42 0 0,0 16,36 Z`)
            // Dois arcos elípticos "A" cujo centro geométrico não é (50,50) —
            // o resultado correto é um crescente fino perto do topo do orb (brilho de vidro).
            fun pt(x: Float, y: Float) = Offset(center.x + (x - 50f) * scale, center.y + (y - 50f) * scale)
            val s0 = pt(16f, 36f)
            val s1 = pt(84f, 36f)
            val highlightPath = Path().apply {
                moveTo(s0.x, s0.y)
                svgArcTo(
                    startX = s0.x, startY = s0.y, endX = s1.x, endY = s1.y,
                    rx = 38f * scale, ry = 38f * scale, largeArcFlag = false, sweepFlag = true
                )
                svgArcTo(
                    startX = s1.x, startY = s1.y, endX = s0.x, endY = s0.y,
                    rx = 42f * scale, ry = 42f * scale, largeArcFlag = false, sweepFlag = false
                )
                close()
            }
            drawPath(path = highlightPath, color = Color.White.copy(alpha = 0.22f))
            drawCircle(color = Color.White.copy(alpha = 0.08f), radius = 2.5f * scale, center = Offset(75f * scale, 75f * scale))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            // React: colon com `animate-pulse` (opacidade 1↔0.5, CSS keyframes).
            // Separar horas/colon/minutos para animar só o colon.
            val timeStr = formatOrbTime(timeLeftSeconds)
            val hours = timeStr.substringBefore(":")
            val rest = timeStr.substringAfter(":")
            val minutes = rest
            val colonAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "colonAlpha"
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = hours,
                    color = colors.accent,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Black,
                    fontSize = textSizeSp,
                    letterSpacing = (textSizeSp.value * 0.025f).sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.95f),
                            offset = Offset(0f, 4f),
                            blurRadius = 16f
                        )
                    )
                )
                Text(
                    text = ":",
                    color = colors.accent,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Black,
                    fontSize = textSizeSp,
                    letterSpacing = (textSizeSp.value * 0.025f).sp,
                    alpha = colonAlpha,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.95f),
                            offset = Offset(0f, 4f),
                            blurRadius = 16f
                        )
                    )
                )
                Text(
                    text = minutes,
                    color = colors.accent,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Black,
                    fontSize = textSizeSp,
                    letterSpacing = (textSizeSp.value * 0.025f).sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.95f),
                            offset = Offset(0f, 4f),
                            blurRadius = 16f
                        )
                    )
                )
            }
            val modeLabel = when (mode) {
                FocusMode.WORK -> "⚔\uFE0F Foco Puro"
                FocusMode.DUNGEON -> "🗝\uFE0F Masmorra"
                FocusMode.WILDERNESS -> "💀 Selvagem"
                else -> "☕ Pausa"
            }
            val labelSizeSp = if (size == FocusOrbSize.FULLSCREEN) 12.sp else if (size == FocusOrbSize.STANDARD) 9.sp else 7.sp
            Text(
                text = modeLabel,
                color = Color(0xFFA1A1AA),
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = labelSizeSp,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ============================================================================
// Concept C: Arcane Dial Composables
// ============================================================================
@Composable
private fun OrbConceptC(
    progress: Float,
    timeLeftSeconds: Int,
    mode: FocusMode,
    isRunning: Boolean,
    size: FocusOrbSize,
    boxSizeDp: androidx.compose.ui.unit.Dp
) {
    val colors = getColorsC(mode)

    val radius = 43f

    // React usa `transition-all duration-300` no progress ring — animação suave de 300ms
    val animatedSweepAngle by animateFloatAsState(
        targetValue = progress * 360f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "sweepAngleC"
    )

    val textSizeSp = if (size == FocusOrbSize.FULLSCREEN) 44.sp else if (size == FocusOrbSize.STANDARD) 26.sp else 18.sp
    val labelSizeSp = if (size == FocusOrbSize.FULLSCREEN) 14.sp else if (size == FocusOrbSize.STANDARD) 10.sp else 8.sp
    val percentSizeSp = if (size == FocusOrbSize.FULLSCREEN) 14.sp else if (size == FocusOrbSize.STANDARD) 9.5.sp else 7.sp

    Box(
        modifier = androidx.compose.ui.Modifier.size(boxSizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val scale = min(w, h) / 100f
            val center = Offset(w / 2f, h / 2f)

            drawCircle(color = colors.bgGlow!!, radius = 44f * scale, center = center)

            // Base circles
            drawCircle(color = Color(0xFF111116), radius = 48f * scale, center = center)
            drawCircle(color = Color(0xFF27272A), radius = 48f * scale, center = center, style = Stroke(width = 0.8f * scale))
            drawCircle(color = Color(0xFF09090B), radius = 46.5f * scale, center = center)

            // Subtle grid
            val gridColor = Color.White.copy(alpha = 0.06f)
            drawCircle(color = gridColor, radius = 32f * scale, center = center, style = Stroke(width = 0.3f * scale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f * scale, 3f * scale))))
            drawCircle(color = gridColor, radius = 20f * scale, center = center, style = Stroke(width = 0.3f * scale))
            drawLine(start = Offset(center.x, center.y - 18f * scale), end = Offset(center.x, center.y + 18f * scale), color = gridColor, strokeWidth = 0.3f * scale)
            drawLine(start = Offset(center.x - 18f * scale, center.y), end = Offset(center.x + 18f * scale, center.y), color = gridColor, strokeWidth = 0.3f * scale)
            drawLine(start = Offset(center.x - 15f * scale, center.y - 15f * scale), end = Offset(center.x + 15f * scale, center.y + 15f * scale), color = gridColor, strokeWidth = 0.3f * scale)
            drawLine(start = Offset(center.x - 15f * scale, center.y + 15f * scale), end = Offset(center.x + 15f * scale, center.y - 15f * scale), color = gridColor, strokeWidth = 0.3f * scale)

            // Track background
            drawCircle(color = colors.trackBg!!, radius = radius * scale, center = center, style = Stroke(width = 3f * scale))

            // Progress ring with gradient — animação suave de 300ms
            val trackGrad = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f),
                    colors.track!!,
                    colors.track!!.copy(alpha = 0.6f)
                ),
                start = Offset(0f, 0f),
                end = Offset(radius * 2f * scale, radius * 2f * scale)
            )
            if (animatedSweepAngle > 0f) {
                drawArc(
                    brush = trackGrad,
                    startAngle = -90f,
                    sweepAngle = animatedSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * scale, center.y - radius * scale),
                    size = Size(radius * 2f * scale, radius * 2f * scale),
                    style = Stroke(
                        width = 3.2f * scale,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Inner circle
            drawCircle(color = Color(0xFF0C0C10), radius = 36f * scale, center = center)
            drawCircle(color = Color(0xFF1F1F23), radius = 36f * scale, center = center, style = Stroke(width = 0.75f * scale))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (mode == FocusMode.BREAK) "Recuperação" else "Cronômetro",
                color = Color(0xFF71717A),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = labelSizeSp,
                letterSpacing = 0.25f.em
            )
            Text(
                text = formatOrbTime(timeLeftSeconds),
                color = colors.textColor!!,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Black,
                fontSize = textSizeSp,
                letterSpacing = (-0.5f).sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(0f, 2f),
                        blurRadius = 12f
                    )
                ),
                modifier = Modifier.padding(vertical = 0.5.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 0.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(colors.track!!, shape = CircleShape)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "${Math.round(progress * 100)}%",
                    color = Color(0xFF9CA3AF),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = percentSizeSp
                )
            }
        }
    }
}

// ============================================================================
// Concept D: Alchemist Flask Composables
// ============================================================================
@Composable
private fun OrbConceptD(
    progress: Float,
    timeLeftSeconds: Int,
    mode: FocusMode,
    isRunning: Boolean,
    size: FocusOrbSize,
    boxSizeDp: androidx.compose.ui.unit.Dp
) {
    val colors = getColorsD(mode)

    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(isRunning && mode != FocusMode.PAUSED) {
        if (isRunning && mode != FocusMode.PAUSED) {
            while (true) {
                phase += 0.05f
                if (phase > (2 * PI).toFloat()) phase -= (2 * PI).toFloat()
                delay(33)
            }
        }
    }

    // Pulse: no React é `animate-pulse` do Tailwind (opacidade 1 -> 0.5 -> 1, 4s,
    // cubic-bezier(0.4,0,0.6,1)) aplicado ao container inteiro — não é um scale.
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseOpacity by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseOpacity"
    )

    val baseY = 100f - progress * 100f
    val amplitude = if (isRunning && mode != FocusMode.PAUSED && progress > 0.02f && progress < 0.98f) {
        3.8f * sin(progress * PI.toFloat())
    } else 0f

    val textSizeSp = if (size == FocusOrbSize.FULLSCREEN) 44.sp else if (size == FocusOrbSize.STANDARD) 26.sp else 18.sp
    // React: label "Ritmo de Foco" usa `text-[8.5px]` fixo — NÃO escala com o tamanho
    // do orb (diferente do valor do tempo, que usa TEXT_SIZES). O código anterior
    // variava esse valor por FocusOrbSize (7/8.5/12sp), o que diverge da fonte.
    val labelSizeSp = 8.5.sp

    Box(
        modifier = androidx.compose.ui.Modifier
            .size(boxSizeDp)
            .graphicsLayer {
                alpha = if (isRunning && mode != FocusMode.PAUSED) pulseOpacity else 1f
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val scale = min(w, h) / 100f
            val center = Offset(w / 2f, h / 2f)

            // O path da onda precisa ser construído AQUI, com o `scale` real em px —
            // construí-lo fora do Canvas (como antes) deixava as coordenadas em unidades
            // 0-100 "cruas", nunca multiplicadas pelo scale de pixels, então o líquido
            // era desenhado como um patch minúsculo perto do canto (0,0) do canvas,
            // fora da área visível do círculo (por isso o orb aparecia sem preenchimento).
            val frontPath = buildWavePath(baseY, phase, amplitude, scale, 100f, 100f)
            val backPath = buildWavePath(baseY, -phase * 0.8f + PI.toFloat(), amplitude * 0.7f, scale, 100f, 100f)

            // React usa `blur-2xl` (CSS gaussian blur) no glow — Canvas não tem blur nativo
            // barato aqui, então aproxima com um radial gradient (halo suave) em vez de
            // um disco sólido de borda dura, que é o que havia antes.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.glow, colors.glow.copy(alpha = 0f)),
                    center = center,
                    radius = 48f * scale
                ),
                radius = 48f * scale,
                center = center
            )

            // Base circle
            drawCircle(color = Color(0xFF09090B), radius = 45f * scale, center = center)
            drawCircle(color = Color(0xFF3F3F46), radius = 45f * scale, center = center, style = Stroke(width = 0.8f * scale))

            // Octagon
            val octagonPath = Path().apply {
                val points = listOf(
                    Offset(center.x, center.y - 44f * scale),
                    Offset(center.x + 31f * scale, center.y - 31f * scale),
                    Offset(center.x + 44f * scale, center.y),
                    Offset(center.x + 31f * scale, center.y + 31f * scale),
                    Offset(center.x, center.y + 44f * scale),
                    Offset(center.x - 31f * scale, center.y + 31f * scale),
                    Offset(center.x - 44f * scale, center.y),
                    Offset(center.x - 31f * scale, center.y - 31f * scale)
                )
                points.forEachIndexed { idx, p ->
                    if (idx == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(path = octagonPath, color = colors.octagonStroke!!.copy(alpha = 0.15f), style = Stroke(width = 0.5f * scale))

            // Clipped wave area
            clipPath(buildCirclePath(center, 43.5f * scale)) {
                drawPath(
                    path = backPath,
                    brush = Brush.verticalGradient(colors.back!!.map { it.copy(alpha = if (it == colors.back!![0]) 0.65f else 0.75f) })
                )
                drawPath(
                    path = frontPath,
                    brush = Brush.verticalGradient(colors.liquid!!.map { it.copy(alpha = if (it == colors.liquid!![0]) 0.92f else if (it == colors.liquid!![1]) 0.94f else 0.98f) })
                )
                drawLine(
                    start = Offset(0f, baseY * scale),
                    end = Offset(100f * scale, baseY * scale),
                    color = Color.White.copy(alpha = 0.4f),
                    strokeWidth = 1.2f * scale
                )
                // Bubbles
                drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 1.3f * scale, center = Offset(35f * scale, 55f * scale))
                drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 1.8f * scale, center = Offset(65f * scale, 65f * scale))
                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 1.1f * scale, center = Offset(45f * scale, 78f * scale))
                drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 1.4f * scale, center = Offset(58f * scale, 88f * scale))
            }

            // White ring
            drawCircle(
                color = colors.ringWhite!!.copy(alpha = 0.2f),
                radius = 44.5f * scale,
                center = center,
                style = Stroke(width = 1.2f * scale)
            )

            // Highlight (React: `M 18,34 A 36,36 0 0,1 82,34 A 40,40 0 0,0 18,34 Z`)
            // A versão anterior centrava os dois arcos no centro do orb (50,50) com
            // sweep de 180° cada, o que criava uma lente atravessando o meio do círculo.
            // A fonte real usa dois arcos elípticos "A" cujo centro geométrico não é
            // (50,50) — o resultado correto é um crescente fino perto do topo do orb
            // (efeito de brilho de vidro), não uma lente central. Usamos svgArcTo para
            // reproduzir a geometria exata do SVG.
            fun pt(x: Float, y: Float) = Offset(center.x + (x - 50f) * scale, center.y + (y - 50f) * scale)
            val s0 = pt(18f, 34f)
            val s1 = pt(82f, 34f)
            val highlightPath = Path().apply {
                moveTo(s0.x, s0.y)
                svgArcTo(
                    startX = s0.x, startY = s0.y, endX = s1.x, endY = s1.y,
                    rx = 36f * scale, ry = 36f * scale, largeArcFlag = false, sweepFlag = true
                )
                svgArcTo(
                    startX = s1.x, startY = s1.y, endX = s0.x, endY = s0.y,
                    rx = 40f * scale, ry = 40f * scale, largeArcFlag = false, sweepFlag = false
                )
                close()
            }
            drawPath(path = highlightPath, color = colors.highlightWhite!!.copy(alpha = 0.2f))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatOrbTime(timeLeftSeconds),
                color = colors.textColor!!,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Black,
                fontSize = textSizeSp,
                // React usa `tracking-wider` (0.025em) — proporcional ao tamanho da
                // fonte, não um valor fixo em sp (2.sp ficava desproporcional no
                // COMPACT/FULLSCREEN).
                letterSpacing = (textSizeSp.value * 0.025f).sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.9f),
                        offset = Offset(0f, 2f),
                        blurRadius = 12f
                    )
                )
            )
            Text(
                text = "Ritmo de Foco",
                color = Color(0xFFA1A1AA),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = labelSizeSp,
                letterSpacing = 0.2.em,
                modifier = Modifier.padding(top = 0.5.dp)
            )
        }
    }
}

// ============================================================================
// Main FocusOrb Wrapper
// ============================================================================
@Composable
fun FocusOrb(
    timeLeft: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    isBreakActive: Boolean = false,
    isDungeonMode: Boolean = false,
    isWildernessMode: Boolean = false,
    orbConcept: OrbConcept = OrbConcept.D,
    size: FocusOrbSize = FocusOrbSize.STANDARD,
    modifier: Modifier = Modifier
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val boxSizeDp = when (size) {
        FocusOrbSize.COMPACT -> 172.dp
        FocusOrbSize.STANDARD -> 256.dp
        FocusOrbSize.FULLSCREEN -> when {
            screenWidthDp >= 840 -> 437.dp
            screenWidthDp >= 600 -> 345.dp
            else -> 253.dp
        }
    }

    val progress = if (totalSeconds > 0) timeLeft.toFloat() / totalSeconds.toFloat() else 0f
    val isActiveAnimating = isRunning && !isPaused
    val isUrgent = timeLeft <= 60 && isActiveAnimating

    val mode = getMode(isBreakActive, isUrgent, isPaused, isDungeonMode, isWildernessMode)

    when (orbConcept) {
        OrbConcept.A -> OrbConceptA(progress, timeLeft, mode, isRunning, size, boxSizeDp)
        OrbConcept.B -> OrbConceptB(progress, timeLeft, mode, isRunning, size, boxSizeDp)
        OrbConcept.C -> OrbConceptC(progress, timeLeft, mode, isRunning, size, boxSizeDp)
        OrbConcept.D -> OrbConceptD(progress, timeLeft, mode, isRunning, size, boxSizeDp)
    }
}