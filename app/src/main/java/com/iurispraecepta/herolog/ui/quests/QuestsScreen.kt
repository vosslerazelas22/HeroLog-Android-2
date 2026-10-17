package com.iurispraecepta.herolog.ui.quests

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.ui.ProcessedQuest
import com.iurispraecepta.herolog.ui.components.RitualEmptyState
import com.iurispraecepta.herolog.ui.components.RitualPress
import com.iurispraecepta.herolog.ui.components.RitualTabHeader
import com.iurispraecepta.herolog.ui.components.ritualDifficultyEdge
import com.iurispraecepta.herolog.ui.components.ritualGlow
import com.iurispraecepta.herolog.ui.components.ritualPressable
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.RitualGold
import com.iurispraecepta.herolog.ui.theme.RitualGreen
import com.iurispraecepta.herolog.ui.theme.RitualSurface
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceElevated
import com.iurispraecepta.herolog.ui.theme.RitualTextPrimary
import com.iurispraecepta.herolog.ui.theme.RitualTextTertiary


@Composable
fun QuestsScreen(
    dailyQuests: List<ProcessedQuest>,
    onClaimQuestReward: (questId: String, goldReward: Int, xpReward: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        RitualTabHeader(
            iconRes = LucideR.drawable.lucide_ic_scroll_text,
            title = "Contratos",
            counterText = if (dailyQuests.size == 1) "1 contrato" else "${dailyQuests.size} contratos",
            showNewButton = false,
            newContentDescription = "",
            onNewClick = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Legenda ──────────────────────────────────────────────────
        Text(
            text = "Sorteio rotativo — 3 missões por dia. Renova à meia-noite.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = RitualTextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ═══ LISTA DE CONTRATOS ═══
        if (dailyQuests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                RitualEmptyState(
                    message = "Nenhum contrato diário ativo para hoje."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = LocalBottomBarInset.current
                )
            ) {
                items(
                    items = dailyQuests,
                    key = { it.id }
                ) { quest ->
                    QuestCard(
                        quest = quest,
                        onClaim = {
                            onClaimQuestReward(quest.id, quest.rewardGold, quest.rewardXp)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestCard(
    quest: ProcessedQuest,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReadyToClaim = quest.isCompleted && !quest.isClaimed
    val isClaimed = quest.isClaimed

    // Estado → cor da barra lateral e da borda (tokens Ritual*, padrão das abas de Rituais).
    val stateColor = when {
        isClaimed -> RitualGreen
        isReadyToClaim -> RitualGold
        else -> RitualTextTertiary
    }
    val cardBorderColor = if (isReadyToClaim) RitualGold else RitualBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RitualSurface)
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .ritualDifficultyEdge(stateColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // ── Linha Superior: Nome da Quest + Bolinha Pulsante + Badge Concluído ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = quest.name,
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.3.sp,
                        color = when {
                            isClaimed -> RitualTextSecondary
                            isReadyToClaim -> RitualGold
                            else -> RitualTextPrimary
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Bolinha pulsante (animate-ping) se completed && !claimed
                    if (isReadyToClaim) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PulsingDot()
                    }
                }

                // Badge no canto direito se isClaimed (tonal verde, como o badge de dificuldade)
                if (isClaimed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(RitualGreen.copy(alpha = 0.12f))
                            .border(1.dp, RitualGreen.copy(alpha = 0.35f), RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_check),
                            contentDescription = null,
                            tint = RitualGreen,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "CONCLUÍDO",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = RitualGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // ── Descrição da Quest ──────────────────────────────────────────
            Text(
                text = quest.desc,
                fontFamily = Cinzel,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = if (isClaimed) RitualTextTertiary else RitualTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Barra de Progresso ──────────────────────────────────────────
            val clampedProgress = quest.progress.coerceIn(0, quest.target)
            val progressFraction = if (quest.target > 0) {
                (quest.progress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f)
            } else 1f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progresso",
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    color = RitualTextSecondary
                )
                Text(
                    text = "$clampedProgress / ${quest.target}",
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isClaimed -> RitualGreen
                        quest.isCompleted -> RitualGold
                        else -> RitualTextSecondary
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RitualSurfaceElevated)
            ) {
                val fillColor = when {
                    isClaimed -> RitualGreen
                    quest.isCompleted -> RitualGold
                    else -> RitualGold.copy(alpha = 0.4f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(fillColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Rodapé (Recompensas + Ação) com Borda Superior Separadora ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = RitualBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recompensas: 💎 {rewardGold} GP em RitualGold | ⚡ {rewardXp} XP em RitualGreen
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "💎 ${quest.rewardGold} GP",
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.SemiBold,
                            color = RitualGold
                        )

                        Text(
                            text = "⚡ ${quest.rewardXp} XP",
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.SemiBold,
                            color = RitualGreen
                        )
                    }

                    // Ação à direita
                    when {
                        isReadyToClaim -> {
                            // Área de toque de 48dp; visual compacto com glow dourado.
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .ritualPressable(
                                        targetScale = RitualPress.MinusScale,
                                        durationMs = RitualPress.MinusDurationMs,
                                        onClick = onClaim
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .ritualGlow(RitualGold)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RitualGold.copy(alpha = 0.15f))
                                        .border(1.dp, RitualGold, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "REIVINDICAR",
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        color = RitualGold
                                    )
                                }
                            }
                        }
                        isClaimed -> {
                            Text(
                                text = "Baú de espólios recolhido",
                                fontSize = 10.sp,
                                fontFamily = Cinzel,
                                fontStyle = FontStyle.Italic,
                                color = RitualTextTertiary
                            )
                        }
                        else -> {
                            Text(
                                text = "Desbloqueia ao atingir progresso",
                                fontSize = 10.sp,
                                fontFamily = Cinzel,
                                color = RitualTextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping_transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_alpha"
    )

    Box(
        modifier = modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Círculo que expande e desaparece (simulando animate-ping)
        Box(
            modifier = Modifier
                .size(6.dp * scale)
                .clip(CircleShape)
                .background(RitualGold.copy(alpha = alpha))
        )
        // Círculo sólido central
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(RitualGold)
        )
    }
}
