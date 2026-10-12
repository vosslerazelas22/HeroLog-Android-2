package com.iurispraecepta.herolog.ui.dailies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.logic.quests.getDifficultyRewards
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.ui.components.RitualChecklistDrawer
import com.iurispraecepta.herolog.ui.components.RitualChevronButton
import com.iurispraecepta.herolog.ui.components.RitualDifficultyBadge
import com.iurispraecepta.herolog.ui.components.RitualPress
import com.iurispraecepta.herolog.ui.components.RitualProgressChip
import com.iurispraecepta.herolog.ui.components.ritualDifficultyEdge
import com.iurispraecepta.herolog.ui.components.ritualGlow
import com.iurispraecepta.herolog.ui.components.ritualPressable
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.RitualAmber
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.RitualGold
import com.iurispraecepta.herolog.ui.theme.RitualGreen
import com.iurispraecepta.herolog.ui.theme.RitualSurface
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceElevated
import com.iurispraecepta.herolog.ui.theme.RitualTextPrimary
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary
import com.iurispraecepta.herolog.ui.theme.RitualTextTertiary
import com.iurispraecepta.herolog.ui.theme.ritualDifficultyStyle

// ============================================================================
// Card de Diária (spec-rituais-cards, Bloco D).
//
// Referência: `specs/rituais-ai-studio/MissionCard.tsx` (DailyCard, :172-352).
// Container em coluna `[linha principal][gaveta se expandida]`; barra de 4dp só
// na linha principal. Gaveta abre SÓ pelo chevron (removida a auto-expansão de
// concluída `isExpanded || daily.completed` e o `alpha 0.7` do card — spec §6).
// Sem cor por valor (D3, `ScoreColor` removido); concluído = título cinza riscado
// + checkbox verde (nunca azul). Frequência localizada (D5 — o React exibe
// `Weekly (2x)` em inglês e semanticamente errado; NÃO replicar).
// XP via `getDifficultyRewards` (4/12/28/60 — números do Android, não do React).
// Notas em Cinzel Regular 11sp (spec §4, confirmação visual pendente, Iuris).
// ============================================================================

/**
 * Frequência localizada (D5): `every = 1` → nome do intervalo; `every > 1` →
 * "A cada {n} dias/semanas/meses". Substitui o `getFrequencyText` antigo
 * ("Repete diariamente" / "Repete: Semanal (a cada 2x)").
 */
fun ritualFrequencyText(repeats: RepeatInterval, every: Int): String {
    if (every == 1) {
        return when (repeats) {
            RepeatInterval.Daily -> "Diariamente"
            RepeatInterval.Weekly -> "Semanalmente"
            RepeatInterval.Monthly -> "Mensalmente"
        }
    }
    val noun = when (repeats) {
        RepeatInterval.Daily -> "dias"
        RepeatInterval.Weekly -> "semanas"
        RepeatInterval.Monthly -> "meses"
    }
    return "A cada $every $noun"
}

@Composable
fun RitualDailyCard(
    daily: Daily,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleDaily: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onOpenEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasChecklist = daily.checklist.isNotEmpty()
    val completedCount = daily.checklist.count { it.completed }
    val xp = getDifficultyRewards(daily.difficulty).xp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RitualSurface)
            .border(1.dp, RitualBorder, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onOpenEdit
                )
        ) {
            // Barra desenhada (sem IntrinsicSize — ele trava o FlowRow em 1 linha).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .ritualDifficultyEdge(ritualDifficultyStyle(daily.difficulty).pastel),
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
                    DailyCheckbox(
                        title = daily.title,
                        completed = daily.completed,
                        onClick = onToggleDaily
                    )

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
                                text = daily.title,
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 0.3.sp,
                                color = if (daily.completed) RitualTextSecondary else RitualTextPrimary,
                                textDecoration = if (daily.completed) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            RitualDifficultyBadge(difficulty = daily.difficulty)
                        }

                        if (daily.notes.isNotBlank()) {
                            Text(
                                text = daily.notes,
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = RitualTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(LucideR.drawable.lucide_ic_coins),
                                        contentDescription = null,
                                        tint = RitualGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    MetaText(text = "$xp XP", color = RitualGold)
                                }
                                MetaDot()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(LucideR.drawable.lucide_ic_repeat),
                                        contentDescription = null,
                                        tint = RitualTextTertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    MetaText(
                                        text = ritualFrequencyText(daily.repeats, daily.every),
                                        color = RitualTextSecondary
                                    )
                                }
                                if (daily.streak > 0) {
                                    MetaDot()
                                    MetaText(
                                        text = "${daily.streak}d",
                                        color = RitualAmber
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RitualProgressChip(
                                    completedCount = completedCount,
                                    totalCount = daily.checklist.size,
                                    done = daily.completed,
                                    hasChecklist = hasChecklist
                                )
                                if (hasChecklist) {
                                    RitualChevronButton(
                                        expanded = isExpanded,
                                        onToggle = onToggleExpand
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hasChecklist && isExpanded) {
            RitualChecklistDrawer(
                items = daily.checklist,
                onToggleItem = onToggleChecklistItem
            )
        }
    }
}

/**
 * Checkbox quadrado (`MissionCard.tsx:213-228`): 36dp, raio 8dp, borda 1dp.
 * Pendente: fundo `#1A1725`, ícone de quadrado vazado 16dp `#625E69`. Concluído:
 * fundo verde 15%, borda verde, check 20dp + glow. Toque não abre a edição.
 */
@Composable
private fun DailyCheckbox(
    title: String,
    completed: Boolean,
    onClick: () -> Unit
) {
    var glowModifier: Modifier = Modifier
    if (completed) glowModifier = glowModifier.ritualGlow()
    Box(
        modifier = glowModifier
            .size(36.dp)
            .ritualPressable(
                targetScale = RitualPress.PlusScale,
                durationMs = RitualPress.PlusDurationMs,
                onClick = onClick
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (completed) RitualGreen.copy(alpha = 0.15f) else RitualSurfaceElevated
            )
            .border(
                1.dp,
                if (completed) RitualGreen else RitualBorder,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(
                if (completed) LucideR.drawable.lucide_ic_check
                else LucideR.drawable.lucide_ic_square
            ),
            contentDescription = "Alternar missão diária $title",
            tint = if (completed) RitualGreen else RitualTextTertiary,
            modifier = Modifier.size(if (completed) 20.dp else 16.dp)
        )
    }
}

@Composable
private fun MetaText(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        lineHeight = 11.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = JetBrainsMono
    )
}

@Composable
private fun MetaDot() {
    MetaText(text = "·", color = RitualTextTertiary)
}
