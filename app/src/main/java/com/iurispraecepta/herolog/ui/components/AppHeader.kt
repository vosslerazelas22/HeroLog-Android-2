package com.iurispraecepta.herolog.ui.components

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R

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
 *   4. [28/08, correção pós-integração] `statusBarsPadding()` adicionado — o
 *      React não tem conceito de status bar (é web), então isso não vem da
 *      fonte; é ajuste puramente nativo pra evitar o conteúdo (logo/pills/
 *      botões) desenhar por baixo do relógio/notch/ícones do Android quando
 *      a Activity usa edge-to-edge. O fundo (QuestPanel) continua full-bleed
 *      atrás da status bar — só o conteúdo é empurrado pra baixo.
 *
 * ACHADO EM ABERTO (não bloqueia, registrar em PARIDADE.md): a pill de gold
 * na fonte tem classes conflitantes `font-mono font-bold font-serif` no
 * mesmo elemento — ambiguidade real da fonte (Tailwind, não bug de porte).
 * Assumido font-serif (Cinzel) por ser a última na ordem alfabética das
 * utilities geradas. Se Bruno confirmar visualmente que é mono, trocar
 * `textFontFamily` da pill de gold abaixo.
 *
 * Ícones: com.composables:icons-lucide-android:2.2.1 (Lucide oficial via
 * Vector Drawable + R.drawable), não mais portados manualmente.
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
            .background(QuestPanel.copy(alpha = 0.95f)) // pinta atrás da status bar também (full-bleed)
            .statusBarsPadding() // empurra o CONTEÚDO pra baixo da status bar/notch — não existe equivalente no React (web não tem status bar), ajuste nativo necessário
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
                iconRes = R.drawable.lucide_ic_flame,
                iconTint = Orange500,
                iconSize = 12.dp, // w-3 h-3
                text = "${streak}d",
                textFontFamily = JetBrainsMonoFamily
            )

            StatusPill(
                iconRes = R.drawable.lucide_ic_coins,
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
                    painter = painterResource(if (isSfxMuted) R.drawable.lucide_ic_volume_x else R.drawable.lucide_ic_volume_2),
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
                    painter = painterResource(R.drawable.lucide_ic_settings),
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
    iconRes: Int,
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
            painter = painterResource(iconRes),
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