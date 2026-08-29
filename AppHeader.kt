package com.herolog.android.ui.components // TODO Bruno: ajustar pro pacote real do projeto

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.herolog.android.R // TODO Bruno: descomentar quando os .ttf abaixo existirem em res/font

/*
 * ============================================================================
 * AppHeader — porte 1:1 do bloco `{/* HEADER BAR */}` de App.tsx (fonte React).
 *
 * Fiel a: logo animado (⚔️ girando 8s), título "HEROLOG" (Cinzel, champagne-400,
 * uppercase), pill de streak (Flame laranja + "{streak}d"), pill de gold
 * (Coins champagne + gold), botão mute/unmute (VolumeX vermelho / Volume2
 * champagne), botão de ajustes (Settings zinc, hover ice-400 — hover N/A em
 * touch).
 *
 * DECISÕES CONSCIENTES (fora de escopo neste componente, mesmo precedente do
 * BottomNav mobile-only — ver PARIDADE.md):
 *   1. Subtítulo "RPG Pomodoro Gamificado" — `hidden md:block` na fonte,
 *      nunca aparece em viewport mobile real. Omitido.
 *   2. Pill de fase do relógio (`getClockPhase()`) — `hidden sm:flex` na
 *      fonte (breakpoint 640px), também nunca aparece em celular real.
 *      Omitido. Se o Bruno quiser essa pill mesmo assim (uso tablet/foldable
 *      grande), é bloco à parte.
 *   3. `sticky top-0` / `z-40` / `backdrop-blur-md` / box-shadow custom —
 *      são responsabilidade de quem posiciona o header (Scaffold topBar ou
 *      Box com zIndex), não do Composable em si.
 *
 * ACHADO EM ABERTO (não bloqueia, registrar em PARIDADE.md): a pill de gold
 * na fonte tem classes conflitantes `font-mono font-bold font-serif` no
 * mesmo elemento — ambiguidade real da fonte (Tailwind, não bug de porte).
 * Assumido font-serif (Cinzel) por ser a última na ordem alfabética das
 * utilities geradas. Se Bruno confirmar visualmente que é mono, trocar
 * `textFontFamily` da pill de gold abaixo.
 *
 * Ícones: NÃO são Material Icons. São os SVGs reais do Lucide (extraídos de
 * raw.githubusercontent.com/lucide-icons/lucide/main/icons/{flame,coins,
 * volume-2,volume-x,settings}.svg), portados via PathParser do Compose —
 * mesma geometria, mesmo stroke-width 2, mesmo round cap/join da fonte.
 * `<circle>` convertido pra `<path>` com dois arcos (equivalente matemático
 * exato, Compose ImageVector não tem elemento de círculo nativo).
 *
 * Fontes: Cinzel (font-serif) e JetBrains Mono (font-mono), idênticas ao
 * `index.html`. PRECISA dos .ttf em res/font antes de compilar — ver bloco
 * de tipografia abaixo.
 * ============================================================================
 */

// ---------------------------------------------------------------------------
// Paleta 1:1 com src/index.css (@theme) do React. NUNCA usar MaterialTheme
// ou cor Material aproximada aqui — valores confirmados por Bruno direto na
// fonte (26/08).
// ---------------------------------------------------------------------------
private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val QuestPanel = Color(0xFF0B0915)
private val Ice400 = Color(0xFFA9D6E5) // hover do botão de ajustes — N/A em touch, mantido por referência
private val Orange500 = Color(0xFFF97316) // orange-500 padrão Tailwind (ícone do streak)
private val Red500 = Color(0xFFEF4444)    // red-500 padrão Tailwind (VolumeX)
private val Zinc400 = Color(0xFF9CA3AF)   // zinc-400 padrão Tailwind (Settings)
private val Stone900 = Color(0xFF1C1917)  // stone-900 padrão Tailwind (fundo das pills/botões)

// ---------------------------------------------------------------------------
// Tipografia — Cinzel e JetBrains Mono, iguais ao index.html:
//   Cinzel:wght@400;600;700;800  |  JetBrains+Mono:wght@400;600
// TODO Bruno: baixar os .ttf (Google Fonts) e colocar em res/font/ antes de
// buildar — sem eles, cai no fallback do sistema e quebra a paridade visual
// do "HEROLOG" no header.
// ---------------------------------------------------------------------------
// private val CinzelFamily = FontFamily(Font(R.font.cinzel_black, FontWeight.Black))
// private val JetBrainsMonoFamily = FontFamily(Font(R.font.jetbrains_mono_bold, FontWeight.Bold))
private val CinzelFamily = FontFamily.Serif // placeholder até os .ttf existirem
private val JetBrainsMonoFamily = FontFamily.Monospace // placeholder até os .ttf existirem

// ---------------------------------------------------------------------------
// Ícones Lucide — path data extraído byte a byte do repo oficial. Ver nota
// completa no cabeçalho do arquivo.
// ---------------------------------------------------------------------------
private fun buildLucideIcon(name: String, vararg pathsD: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        pathsD.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black), // sobrescrito pelo `tint` do Icon() no uso real
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f
            )
        }
    }.build()

private val LucideFlame: ImageVector by lazy {
    buildLucideIcon(
        "Flame",
        "M12 3q1 4 4 6.5t3 5.5a1 1 0 0 1-14 0 5 5 0 0 1 1-3 1 1 0 0 0 5 0c0-2-1.5-3-1.5-5q0-2 2.5-4"
    )
}

private val LucideCoins: ImageVector by lazy {
    buildLucideIcon(
        "Coins",
        "M13.744 17.736a6 6 0 1 1-7.48-7.48",
        "M15 6h1v4",
        "m6.134 14.768.866-.5 2 3.464",
        "M10,8 A6,6 0 1,0 22,8 A6,6 0 1,0 10,8 Z" // <circle cx="16" cy="8" r="6"> convertido pra arco
    )
}

private val LucideVolume2: ImageVector by lazy {
    buildLucideIcon(
        "Volume2",
        "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z",
        "M16 9a5 5 0 0 1 0 6",
        "M19.364 18.364a9 9 0 0 0 0-12.728"
    )
}

private val LucideVolumeX: ImageVector by lazy {
    buildLucideIcon(
        "VolumeX",
        "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z",
        "M22,9 L16,15",
        "M16,9 L22,15"
    )
}

private val LucideSettings: ImageVector by lazy {
    buildLucideIcon(
        "Settings",
        "M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915",
        "M9,12 A3,3 0 1,0 15,12 A3,3 0 1,0 9,12 Z" // <circle cx="12" cy="12" r="3"> convertido pra arco
    )
}

/**
 * Header global do app. Chamar dentro de um container que já resolve
 * sticky/elevation (ex: `Scaffold(topBar = { AppHeader(...) })`).
 *
 * @param streak `gameState.streak` — dias de combo de consistência.
 * @param gold `gameState.gold`.
 * @param isSfxMuted `muteSfx` — controla ícone Volume2/VolumeX.
 * @param onToggleSfx equivalente a `setMuteSfx(!muteSfx)`.
 * @param onOpenSettings equivalente a `setIsSettingsOpen(true)`.
 */
@Composable
fun AppHeader(
    streak: Int,
    gold: Int,
    isSfxMuted: Boolean,
    onToggleSfx: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Rotação infinita do ⚔️ — 8s linear, porte de
    // `animate-spin` + `style={{ animationDuration: '8s' }}`.
    val infiniteTransition = rememberInfiniteTransition(label = "sword_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sword_spin_angle"
    )

    val bottomBorderWidth = 2.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(QuestPanel.copy(alpha = 0.95f))
            .drawBehind {
                val strokePx = bottomBorderWidth.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.10f), // border-white/10
                    start = Offset(0f, size.height - strokePx / 2f),
                    end = Offset(size.width, size.height - strokePx / 2f),
                    strokeWidth = strokePx
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp), // px-3 py-2.5 (base mobile da fonte)
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ---------- Bloco esquerdo: logo + título ----------
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp), // gap-1.5
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚔️",
                fontSize = 20.sp, // text-xl (mobile) — text-2xl é só md:, fora de escopo
                modifier = Modifier.rotate(rotation)
            )

            Column {
                Text(
                    text = "HeroLog".uppercase(), // classe `uppercase` da fonte -> "HEROLOG"
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Black,
                    color = Champagne400,
                    fontSize = 16.sp, // text-base (mobile) — text-lg é só md:
                    letterSpacing = 2.sp, // tracking-wider (aproximação — Compose não mapeia 1:1 pra em)
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Subtítulo "RPG Pomodoro Gamificado" omitido — ver nota de
                // decisão consciente no cabeçalho do arquivo (item 1).
            }
        }

        // ---------- Bloco direito: pills + botões ----------
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp), // gap-1.5
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(
                icon = LucideFlame,
                iconTint = Orange500,
                iconSize = 12.dp, // w-3 h-3
                text = "${streak}d",
                textFontFamily = JetBrainsMonoFamily
            )

            StatusPill(
                icon = LucideCoins,
                iconTint = Champagne500,
                iconSize = 14.dp, // w-3.5 h-3.5
                text = gold.toString(),
                textFontFamily = CinzelFamily // ver "ACHADO EM ABERTO" no cabeçalho do arquivo
            )

            // Pill de fase do relógio omitida — ver decisão consciente
            // no cabeçalho do arquivo (item 2).

            IconButton(
                onClick = onToggleSfx,
                modifier = Modifier
                    .size(32.dp) // w-8 h-8
                    .background(Stone900.copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                    .border(1.dp, Champagne500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = if (isSfxMuted) LucideVolumeX else LucideVolume2,
                    contentDescription = if (isSfxMuted) "Som mutado" else "Som ativo",
                    tint = if (isSfxMuted) Red500 else Champagne400,
                    modifier = Modifier.size(16.dp) // w-4 h-4
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(32.dp) // w-8 h-8
                    .background(Stone900.copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = LucideSettings,
                    contentDescription = "Ajustes de Campanha",
                    tint = Zinc400, // hover vira ice-400 na fonte — N/A em touch
                    modifier = Modifier.size(16.dp) // w-4 h-4
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    icon: ImageVector,
    iconTint: Color,
    iconSize: Dp,
    text: String,
    textFontFamily: FontFamily
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp), // gap-1
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Stone900.copy(alpha = 0.60f), RoundedCornerShape(4.dp)) // bg-stone-900/60 rounded
            .border(1.dp, Champagne500.copy(alpha = 0.15f), RoundedCornerShape(4.dp)) // border-champagne-500/15
            .padding(horizontal = 10.dp, vertical = 4.dp) // px-2.5 py-1
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
        Text(
            text = text,
            fontFamily = textFontFamily,
            fontWeight = FontWeight.Bold,
            color = Champagne400,
            fontSize = 12.sp // text-xs
        )
    }
}
