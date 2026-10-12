package com.iurispraecepta.herolog.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.RitualGreen
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceDrawer
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceElevated
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary
import com.iurispraecepta.herolog.ui.theme.RitualTextTertiary
import com.iurispraecepta.herolog.ui.theme.ritualDifficultyStyle

// ============================================================================
// Peças compartilhadas dos cards da aba Rituais (spec-rituais-cards, Bloco B).
//
// Referência: `specs/rituais-ai-studio/MissionCard.tsx` (+ `questTheme.ts`).
// Wiring nos 3 cards acontece nos Blocos C–E; aqui só as peças, sem uso ainda.
// Cor por peça citada como `MissionCard.tsx:<linha>` no KDoc de cada peça.
//
// Decisões conscientes:
// - `RitualDifficultyBadge` é um componente NOVO; o `DifficultyBadge` atual
//   (raio 4dp, 9sp, alphas 15%/30%) permanece até os Blocos C–E migrarem as 3 telas
//   (`difficultyColor`/`difficultyLabel` seguem nos modais, fora de escopo — spec §6).
// - JetBrainsMono só tem Regular e SemiBold carregados (spec §4): `font-bold` do
//   React vira SemiBold e `font-medium` vira Regular — nunca Bold/Medium no mono.
// - Ícones Lucide do AAR têm stroke 2.0 fixo no vetor; os strokes 2.5/3 pedidos na
//   spec são aproximação (só tamanho/posição são literais).
// - A gaveta abre/fecha por alternância imediata (`if (expanded)`), sem
//   `animateContentSize` (spec Bloco B). Nenhuma animação além das de pressão.
// ============================================================================

/** Escalas/durações do feedback de pressão (equivale a `cubic-bezier(0.4,0,0.2,1)`). */
object RitualPress {
    const val PlusScale = 0.9f
    const val PlusDurationMs = 200
    const val MinusScale = 0.95f
    const val MinusDurationMs = 150
}

/**
 * Feedback de pressão: escala ao pressionar ([targetScale] em [durationMs] com
 * `FastOutSlowInEasing`), sem ripple (`indication = null` — "sem ripple adicional").
 */
fun Modifier.ritualPressable(
    targetScale: Float = RitualPress.PlusScale,
    durationMs: Int = RitualPress.PlusDurationMs,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
            }
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed) targetScale else 1f,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        label = "ritualPress"
    )
    this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
}

/**
 * Glow verde do estado concluído: aproximação de
 * `0 0 12px rgba(105,214,183,0.2)` via gradiente radial em `drawBehind`
 * (funciona no minSdk 24; sem `Modifier.shadow` com cor — só API 28+).
 */
fun Modifier.ritualGlow(): Modifier = this.drawBehind {
    val radius = (maxOf(size.width, size.height) / 2f) + 12.dp.toPx()
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to RitualGreen.copy(alpha = 0.2f),
            1.0f to Color.Transparent,
            radius = radius
        ),
        radius = radius
    )
}

/**
 * Barra de dificuldade desenhada (`MissionCard.tsx:52`): 4dp de largura, altura
 * total da linha, cor `pastel`.
 *
 * É `drawBehind` (não um composable com `fillMaxHeight`) de propósito:
 * `fillMaxHeight` exige altura limitada, e o limitador (`height(IntrinsicSize.Min)`
 * na linha) trava a altura intrínseca em 1 linha — o `FlowRow` da linha de status
 * DESCARTAVA as linhas quebradas (streak e contador sumiam em telas estreitas;
 * achado no Bloco F via screenshot). Desenhando após a medição, a barra usa a
 * altura final real (qualquer número de linhas). O clip arredondado do container
 * corta os cantos da barra como no React.
 */
fun Modifier.ritualDifficultyEdge(pastel: androidx.compose.ui.graphics.Color): Modifier =
    this.drawBehind {
        drawRect(
            color = pastel,
            topLeft = androidx.compose.ui.geometry.Offset.Zero,
            size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
        )
    }

/**
 * Badge de dificuldade tonal (`MissionCard.tsx:98-102`): `px 8 / py 2`, raio 7dp,
 * borda 1dp, mono SemiBold 10sp, uppercase, tracking 0,05em (`0.5.sp` a 10sp),
 * `leading-none` (altura de linha = 10sp, sem altura extra), cores do mapa do Bloco A.
 */
@Composable
fun RitualDifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val style = ritualDifficultyStyle(difficulty)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(style.bg)
            .border(1.dp, style.border, RoundedCornerShape(7.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = style.label.uppercase(),
            color = style.text,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = JetBrainsMono,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Chip de progresso (`MissionCard.tsx:286-295`): mono SemiBold 10sp, `px 6 / py 2`,
 * raio 4dp. Texto `c/t` com checklist; senão `1/1` (concluído) ou `0/1`.
 * Concluído: texto verde + fundo verde 10%. Pendente: `#625E69` + `#1A1725`.
 */
@Composable
fun RitualProgressChip(
    completedCount: Int,
    totalCount: Int,
    done: Boolean,
    hasChecklist: Boolean,
    modifier: Modifier = Modifier
) {
    val text = if (hasChecklist) {
        "$completedCount/$totalCount"
    } else if (done) {
        "1/1"
    } else {
        "0/1"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (done) RitualGreen.copy(alpha = 0.1f) else RitualSurfaceElevated)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = if (done) RitualGreen else RitualTextTertiary,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = JetBrainsMono
        )
    }
}

/**
 * Botão chevron da gaveta (`MissionCard.tsx:298-308`): padding 4dp, ícone 14dp
 * `#928C9A`. Só existe quando há checklist (decisão no card, Blocos D–E).
 */
@Composable
fun RitualChevronButton(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .testTag("ritualChevron")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
                role = Role.Button
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(
                if (expanded) LucideR.drawable.lucide_ic_chevron_up
                else LucideR.drawable.lucide_ic_chevron_down
            ),
            contentDescription = if (expanded) "Recolher checklist" else "Expandir checklist",
            tint = RitualTextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * Gaveta de checklist (`MissionCard.tsx:316-349`): borda superior 1dp `#2A2535`,
 * fundo `#100D1C`, `px 16 / py 10`, itens espaçados 6dp. Abre/fecha sem animação.
 */
@Composable
fun RitualChecklistDrawer(
    items: List<ChecklistItem>,
    onToggleItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("ritualDrawer")
            .background(RitualSurfaceDrawer)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawRect(
                    color = RitualBorder,
                    topLeft = androidx.compose.ui.geometry.Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, stroke)
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            RitualChecklistRow(
                item = item,
                onToggle = { onToggleItem(item.id) }
            )
        }
    }
}

/**
 * Item da gaveta (`MissionCard.tsx:319-346`): linha clicável `px 6 / py 4`, raio 4dp,
 * gap 10dp, mono Regular 12sp, uma linha com reticências. Mini-checkbox 16dp, raio
 * 4dp, borda 1dp. Marcado: fundo verde 20%, borda verde, check 12dp; texto `#625E69`
 * riscado. Pendente: texto `#928C9A`.
 */
@Composable
fun RitualChecklistRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
                role = Role.Checkbox
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (item.completed) RitualGreen.copy(alpha = 0.2f) else Color.Transparent
                )
                .border(
                    1.dp,
                    if (item.completed) RitualGreen else RitualBorder,
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.completed) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_check),
                    contentDescription = null,
                    tint = RitualGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Text(
            text = item.text,
            color = if (item.completed) RitualTextTertiary else RitualTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = JetBrainsMono,
            textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
