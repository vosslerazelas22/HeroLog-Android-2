package com.iurispraecepta.herolog.ui.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.iurispraecepta.herolog.model.Todo
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
// Card de Tarefa (spec-rituais-cards, Bloco E).
//
// Igual ao DailyCard (Bloco D), com: sem frequência e sem streak (metadados só
// com `{xp} XP` em gold); sem linha de data de vencimento (spec §2 — `todo.date`
// não existe, a UI nunca a grava; `Todo` tem só `createdAt`/`completedAt`);
// sem rótulo "🛡️ Tarefa Única" (spec §6); sem cor por valor (D3 — `ScoreColor` e
// `TodoDecayLogic.getTodoDecayValue` removidos do card; a lógica de decaimento
// continua existindo, só deixa de pintar — ver ANDROID_FIRST).
// Referência: `specs/rituais-ai-studio/MissionCard.tsx` (TodoCard, :364-525).
// ============================================================================

@Composable
fun RitualTodoCard(
    todo: Todo,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTodo: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onOpenEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasChecklist = todo.checklist.isNotEmpty()
    val completedCount = todo.checklist.count { it.completed }
    val xp = getDifficultyRewards(todo.difficulty).xp

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
                    .ritualDifficultyEdge(ritualDifficultyStyle(todo.difficulty).pastel),
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
                    TodoCheckbox(
                        title = todo.title,
                        completed = todo.completed,
                        onClick = onToggleTodo
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
                                text = todo.title,
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 0.3.sp,
                                color = if (todo.completed) RitualTextSecondary else RitualTextPrimary,
                                textDecoration = if (todo.completed) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            RitualDifficultyBadge(difficulty = todo.difficulty)
                        }

                        if (todo.notes.isNotBlank()) {
                            Text(
                                text = todo.notes,
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
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_coins),
                                    contentDescription = null,
                                    tint = RitualGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "$xp XP",
                                    color = RitualGold,
                                    fontSize = 11.sp,
                                    lineHeight = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = JetBrainsMono
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RitualProgressChip(
                                    completedCount = completedCount,
                                    totalCount = todo.checklist.size,
                                    done = todo.completed,
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
                items = todo.checklist,
                onToggleItem = onToggleChecklistItem
            )
        }
    }
}

/** Checkbox igual ao da Diária; rótulo "Alternar afazer {título}" (spec Bloco E). */
@Composable
private fun TodoCheckbox(
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
            contentDescription = "Alternar afazer $title",
            tint = if (completed) RitualGreen else RitualTextTertiary,
            modifier = Modifier.size(if (completed) 20.dp else 16.dp)
        )
    }
}
