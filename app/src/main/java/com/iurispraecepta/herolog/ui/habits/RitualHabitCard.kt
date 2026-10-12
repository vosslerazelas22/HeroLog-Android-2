package com.iurispraecepta.herolog.ui.habits

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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.logic.quests.getDifficultyRewards
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.ui.components.RitualDifficultyBadge
import com.iurispraecepta.herolog.ui.components.RitualPress
import com.iurispraecepta.herolog.ui.components.ritualDifficultyEdge
import com.iurispraecepta.herolog.ui.components.ritualGlow
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.RitualDanger
import com.iurispraecepta.herolog.ui.theme.RitualGreen
import com.iurispraecepta.herolog.ui.theme.RitualSurface
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceElevated
import com.iurispraecepta.herolog.ui.theme.RitualTextPrimary
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary
import com.iurispraecepta.herolog.ui.theme.RitualTextTertiary
import com.iurispraecepta.herolog.ui.theme.ritualDifficultyStyle
import kotlinx.coroutines.delay

// ============================================================================
// Card de Hábito (spec-rituais-cards, Bloco C).
//
// Referência: `specs/rituais-ai-studio/MissionCard.tsx` (HabitCard, :27-160).
// Decisões da spec aplicadas: D1 (`+` nunca vira check, fica `+` nos dois estados;
// o React mostra Check quando registrado — NÃO replicar), D3 (sem cor por valor;
// `ScoreColor` removido deste card), D4 (`−` 32dp visual, toque 48dp), D7 (fundo
// global mantido; sem `#0D0A17`), D8 ("hoje" booleano, sem contador), D10
// (contador `(+X | -Y)` mantido — só existe no Android).
// Notas em Cinzel Regular 11sp (herdam `font-serif` do container — spec §4;
// confirmação visual pendente, Iuris).
// `justTriggered`: após o toque mantém escala 0,9 por 250ms (`:36-37`); re-toques
// rápidos compartilham a janela (aproximação — React agenda um timeout por clique).
// Lado ausente mantém espaço: sem `up` → ponto 8dp; sem `down` → vazio 32dp.
// ============================================================================

@Composable
fun RitualHabitCard(
    habit: Habit,
    todayDateStr: String,
    onTriggerHabit: (String, Boolean) -> Unit,
    onOpenEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUpToday = habit.isUpToday(todayDateStr)
    val isDownToday = habit.isDownToday(todayDateStr)
    val xp = getDifficultyRewards(habit.difficulty).xp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RitualSurface)
            .border(1.dp, RitualBorder, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onOpenEdit
            )
    ) {
        // Sem height(IntrinsicSize.Min): ele trava a altura intrínseca em 1 linha e
        // o FlowRow do status descarta as quebradas. A barra é desenhada
        // (ritualDifficultyEdge) na altura final medida.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .ritualDifficultyEdge(ritualDifficultyStyle(habit.difficulty).pastel),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (habit.up) {
                    HabitPlusButton(
                        title = habit.title,
                        isUpToday = isUpToday,
                        onClick = { onTriggerHabit(habit.id, true) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .testTag("habitDotSlot"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .testTag("habitDot")
                                .clip(CircleShape)
                                .background(RitualTextTertiary)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = habit.title,
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            letterSpacing = 0.3.sp,
                            color = RitualTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        RitualDifficultyBadge(difficulty = habit.difficulty)
                    }

                    if (habit.notes.isNotBlank()) {
                        Text(
                            text = habit.notes,
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = RitualTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HabitStatusLine(
                        xp = xp,
                        upCount = habit.upCount,
                        downCount = habit.downCount
                    )
                }

                if (habit.down) {
                    HabitMinusButton(
                        title = habit.title,
                        isDownToday = isDownToday,
                        onClick = { onTriggerHabit(habit.id, false) }
                    )
                } else {
                    Box(modifier = Modifier.width(32.dp))
                }
            }
        }
    }
}

/**
 * Botão `+` (`MissionCard.tsx:59-77` com D1): círculo 36dp, borda 1dp, ícone `+`
 * 16dp nos dois estados. Sem registro: fundo/borda/ícone padrão; **aceso** quando
 * `habit.isUpToday(hoje)`: fundo verde 15%, borda e ícone verdes + glow. Escala 0,9
 * ao pressionar e 250ms após.
 *
 * Acessibilidade (SPEC-Rebranding item 4): o estado aceso também é anunciado por
 * `stateDescription` — não depende só de cor (daltonismo).
 */
@Composable
private fun HabitPlusButton(
    title: String,
    isUpToday: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    var justTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
            }
        }
    }
    LaunchedEffect(justTriggered) {
        if (justTriggered) {
            delay(250)
            justTriggered = false
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed || justTriggered) RitualPress.PlusScale else 1f,
        animationSpec = tween(RitualPress.PlusDurationMs, easing = FastOutSlowInEasing),
        label = "habitPlusPress"
    )
    var glowModifier: Modifier = Modifier
    if (isUpToday) glowModifier = glowModifier.ritualGlow(RitualGreen)
    Box(
        modifier = glowModifier
            .size(36.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(
                if (isUpToday) RitualGreen.copy(alpha = 0.15f) else RitualSurfaceElevated
            )
            .border(
                1.dp,
                if (isUpToday) RitualGreen else RitualBorder,
                CircleShape
            )
            .semantics { stateDescription = habitMarkerState(isUpToday) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    justTriggered = true
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(LucideR.drawable.lucide_ic_plus),
            contentDescription = "Registrar hábito $title",
            tint = if (isUpToday) RitualGreen else RitualTextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Botão `−` (SPEC-Rebranding item 4): 32dp visual, raio 8dp, área de toque 48dp,
 * escala 0,95/150ms. **Aceso** quando `habit.isDownToday(hoje)`, com o mesmo
 * tratamento do `+` (fundo tonal 15% + borda sólida + glow) em `RitualDanger` —
 * preenchimento e borda, não só a cor do ícone, para o estado não depender de
 * discriminação cromática.
 */
@Composable
private fun HabitMinusButton(
    title: String,
    isDownToday: Boolean,
    onClick: () -> Unit
) {
    var glowModifier: Modifier = Modifier
    if (isDownToday) glowModifier = glowModifier.ritualGlow(RitualDanger)
    Box(
        modifier = Modifier
            .semantics { stateDescription = habitMarkerState(isDownToday) }
            .size(48.dp)
            .ritualPressScaleButton(
                targetScale = RitualPress.MinusScale,
                durationMs = RitualPress.MinusDurationMs,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = glowModifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDownToday) RitualDanger.copy(alpha = 0.15f) else RitualSurfaceElevated
                )
                .border(
                    1.dp,
                    if (isDownToday) RitualDanger else RitualBorder,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_minus),
                contentDescription = "Registrar desvio negativo de $title",
                tint = if (isDownToday) RitualDanger else RitualTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/** Estado anunciado pelos marcadores `+`/`−` (a11y — não só cor). */
private fun habitMarkerState(markedToday: Boolean): String =
    if (markedToday) "registrado hoje" else "não registrado hoje"

/**
 * Linha de status (mono 11sp, gap 8, quebra permitida — `MissionCard.tsx:117-142`
 * + D10): `+XP`, `·`, `(+up | -down)`.
 *
 * SPEC-Rebranding item 3: o segmento `{n}d streak` foi removido — o streak de hábito
 * continua calculado (`HabitLogic`) e continua alimentando a conquista
 * `habit_streak_7`, mas **não é exibido** (decisão de produto).
 *
 * Item 4: os textos "Concluído hoje"/"Pendente hoje" saíram também — o estado de hoje
 * vive nos botões `+`/`−` acesos. A linha continua existindo porque `+XP` e o contador
 * permanecem; se um dia ficasse vazia, o composable seria removido.
 */
@Composable
private fun HabitStatusLine(
    xp: Int,
    upCount: Int,
    downCount: Int
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "+$xp XP",
            color = RitualTextSecondary,
            fontSize = 11.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = JetBrainsMono
        )
        StatusDot()
        Text(
            text = "(+$upCount | -$downCount)",
            color = RitualTextSecondary,
            fontSize = 11.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = JetBrainsMono
        )
    }
}

@Composable
private fun StatusDot() {
    Text(
        text = "·",
        color = RitualTextTertiary,
        fontSize = 11.sp,
        lineHeight = 11.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = JetBrainsMono
    )
}

/**
 * Botão genérico com escala de pressão e área de toque maior que o visual
 * (caso do `−`: visual interno + toque 48dp).
 */
@Composable
private fun Modifier.ritualPressScaleButton(
    targetScale: Float,
    durationMs: Int,
    onClick: () -> Unit
): Modifier {
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
        label = "ritualPressScale"
    )
    return this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
}
