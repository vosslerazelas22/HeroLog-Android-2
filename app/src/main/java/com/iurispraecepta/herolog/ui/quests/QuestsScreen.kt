package com.iurispraecepta.herolog.ui.quests

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.ProcessedQuest
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Amber200 = Color(0xFFFDE68A)
private val Amber300 = Color(0xFFFCD34D)
private val Amber600 = Color(0xFFD97706)
private val Emerald400 = Color(0xFF34D399)
private val Emerald500 = Color(0xFF10B981)
private val Stone500 = Color(0xFF78716C)

@Composable
fun QuestsScreen(
    dailyQuests: List<ProcessedQuest>,
    guildQuests: List<ProcessedQuest>,
    activeSubTab: String,
    onSubTabChange: (String) -> Unit,
    onClaimQuestReward: (questId: String, goldReward: Int, xpReward: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isJourney = activeSubTab == "guild" || activeSubTab == "journey"
    val displayedQuests = if (isJourney) guildQuests else dailyQuests

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ═══ CABEÇALHO DO PAINEL (vem do wrapper em App.tsx) ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONTRATOS",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.12.em,
                color = Amber400
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Seletor de Sub-Abas ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Stone950.copy(alpha = 0.4f))
                .border(1.dp, Color(0x1AF59E0B), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SubTabButton(
                title = "Contratos Diários",
                isSelected = !isJourney,
                onClick = { onSubTabChange("daily") },
                modifier = Modifier.weight(1f)
            )
            SubTabButton(
                title = "Marcos da Jornada",
                isSelected = isJourney,
                onClick = { onSubTabChange("guild") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ═══ LEGENDA (muda conforme a sub-aba ativa) ═══
        Text(
            text = if (!isJourney) {
                "Sorteio rotativo — 3 missões por dia. Renova à meia-noite."
            } else {
                "Marcos de progresso do herói e conquistas de longo prazo."
            },
            color = Amber200.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ═══ LISTA DE CONTRATOS ═══
        if (displayedQuests.isEmpty()) {
            EmptyQuestsPlaceholder(
                message = if (isJourney) {
                    "Nenhum contrato da jornada disponível no momento."
                } else {
                    "Nenhum contrato diário ativo para hoje."
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = displayedQuests,
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
private fun SubTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Amber500.copy(alpha = 0.1f) else Color.Transparent,
        label = "subtab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Amber500.copy(alpha = 0.2f) else Color.Transparent,
        label = "subtab_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Amber300 else Stone500,
        label = "subtab_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.em,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

    // Estilos de borda e fundo conforme os 3 estados
    val cardBorderColor = when {
        isClaimed -> Emerald500.copy(alpha = 0.1f)
        isReadyToClaim -> Amber400
        else -> Amber500.copy(alpha = 0.1f)
    }

    val cardBgColor = when {
        isReadyToClaim -> Amber500.copy(alpha = 0.03f)
        else -> Stone900
    }

    val cardModifier = modifier
        .fillMaxWidth()
        .then(if (isClaimed) Modifier.alpha(0.6f) else Modifier)
        .clip(RoundedCornerShape(8.dp))
        .background(cardBgColor)
        .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
        .padding(14.dp)

    Box(modifier = cardModifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isClaimed) Amber100.copy(alpha = 0.6f) else if (isReadyToClaim) Amber300 else Amber100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Bolinha pulsante (animate-ping) se completed && !claimed
                    if (isReadyToClaim) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PulsingDot()
                    }
                }

                // Badge no canto direito se isClaimed
                if (isClaimed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x1A10B981))
                            .border(1.dp, Emerald500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✔️ Concluído",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // ── Descrição da Quest ──────────────────────────────────────────
            Text(
                text = quest.desc,
                fontSize = 11.sp,
                color = Amber100.copy(alpha = 0.5f),
                lineHeight = 15.sp
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
                    fontSize = 11.sp,
                    color = Stone400
                )
                Text(
                    text = "$clampedProgress / ${quest.target}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (quest.isCompleted) Amber400 else Stone400
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Stone900)
            ) {
                val fillBrush = if (quest.isCompleted) {
                    Brush.horizontalGradient(listOf(Amber500, Amber300))
                } else {
                    Brush.horizontalGradient(listOf(Amber600.copy(alpha = 0.4f), Amber600.copy(alpha = 0.4f)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(fillBrush)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Rodapé (Recompensas + Ação) com Borda Superior Separadora ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Color(0x1AF59E0B),
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
                    // Recompensas: 💎 {rewardGold} GP em Amber400 | ⚡ {rewardXp} XP em Emerald400
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "💎 ${quest.rewardGold} GP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber400
                        )

                        Text(
                            text = "⚡ ${quest.rewardXp} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                    }

                    // Ação à direita
                    when {
                        isReadyToClaim -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Amber500, Amber400)
                                        )
                                    )
                                    .clickable(onClick = onClaim)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "REIVINDICAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.em,
                                    color = Stone950
                                )
                            }
                        }
                        isClaimed -> {
                            Text(
                                text = "Baú de espólios recolhido",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                color = Amber100.copy(alpha = 0.3f)
                            )
                        }
                        else -> {
                            Text(
                                text = "Desbloqueia ao atingir progresso",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Serif,
                                color = Amber100.copy(alpha = 0.3f)
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
                .background(Amber400.copy(alpha = alpha))
        )
        // Círculo sólido central
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Amber400)
        )
    }
}

@Composable
private fun EmptyQuestsPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    val borderColor = Color(0x33F59E0B)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    ),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📜",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = Stone400,
                textAlign = TextAlign.Center
            )
        }
    }
}
