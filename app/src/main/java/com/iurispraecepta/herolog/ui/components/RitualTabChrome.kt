package com.iurispraecepta.herolog.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.RitualGold
import com.iurispraecepta.herolog.ui.theme.RitualSurface
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceElevated
import com.iurispraecepta.herolog.ui.theme.RitualTextPrimary
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary

// ============================================================================
// Chrome das abas de Rituais (spec-rituais-cards, Bloco F).
//
// Referência: `specs/rituais-ai-studio/{Habits,Dailies,Todos}Tab.tsx` (cabeçalho
// `:99-119`/`:129-...`/`:123-141`, vazio `:279-281`, filtros de Todos `:144-187`).
// D2: botão "Novo" em uppercase (`NOVO`, só o texto — o `+` vem do ícone
// `lucide_ic_plus`, que permanece; spec-rebranding item 1).
// D7: sem `#0D0A17` (fundo segue o global do Android).
// Lista: `px 16` (via `contentPadding` nas telas) e 10dp entre cards; o `pr-1` do
// React divide o elemento com `px-4` (efeito real incerto) — mantido `px 16`
// simétrico (decisão registrada no DEV_LOG).
// Tracejado do vazio: `drawBehind` com `PathEffect.dashPathEffect` (valores do
// código anterior: 12f/8f — o `dashed` do CSS não tem medida literal).
// ============================================================================

/** Borda inferior 1dp `#2A2535` full-bleed (cabeçalho e filtros). */
fun Modifier.ritualBottomBorder(): Modifier = this.drawBehind {
    val stroke = 1.dp.toPx()
    drawRect(
        color = RitualBorder,
        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - stroke),
        size = androidx.compose.ui.geometry.Size(size.width, stroke)
    )
}

/**
 * Cabeçalho das 3 abas: linha `px 16 / pb 12` com borda inferior; esquerda com
 * ícone 16dp gold + título Cinzel Bold 14sp `#E8E0D0` tracking 0,025em uppercase
 * + contador mono 11sp `#928C9A`; direita com `RitualNewButton`.
 */
@Composable
fun RitualTabHeader(
    @DrawableRes iconRes: Int,
    title: String,
    counterText: String,
    showNewButton: Boolean,
    newContentDescription: String,
    onNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .ritualBottomBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = RitualGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title.uppercase(),
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.35.sp,
                    color = RitualTextPrimary
                )
                Text(
                    text = counterText,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    color = RitualTextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            if (showNewButton) {
                RitualNewButton(
                    contentDescription = newContentDescription,
                    onClick = onNewClick
                )
            }
        }
    }
}

/**
 * Botão `NOVO`: mono SemiBold 12sp gold, fundo `#1A1725`, borda 1dp `#2A2535`,
 * raio 8dp, `px 12 / py 6`, ícone `+` 14dp, gap 6, escala 0,95 ao pressionar.
 * O `+` é o ícone; o texto não repete o sinal (spec-rebranding item 1).
 */
@Composable
fun RitualNewButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .ritualPressable(
                targetScale = RitualPress.MinusScale,
                durationMs = RitualPress.MinusDurationMs,
                onClick = onClick
            )
            .clip(RoundedCornerShape(8.dp))
            .background(RitualSurfaceElevated)
            .border(1.dp, RitualBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_plus),
                contentDescription = contentDescription,
                tint = RitualGold,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "NOVO",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = RitualGold
            )
        }
    }
}

/**
 * Estado vazio: card tracejado (fundo `surface`, borda tracejada 1dp `#2A2535`,
 * raio 12dp, `py 48 / px 24`), texto Inter itálico 12sp `#928C9A` centralizado.
 */
@Composable
fun RitualEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = RitualBorder,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .background(RitualSurface, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontFamily = Inter,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 22.sp,
            color = RitualTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
