package com.iurispraecepta.herolog.ui.quests

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.ProcessedQuest
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Amber200 = Color(0xFFFDE68A)
private val Amber300 = Color(0xFFFCD34D)
private val Emerald400 = Color(0xFF34D399)
private val Emerald500 = Color(0xFF10B981)
private val Purple400 = Color(0xFFC084FC)
private val Stone500 = Color(0xFF78716C)
private val Stone600 = Color(0xFF57534E)
private val Stone700 = Color(0xFF44403C)

@Composable
fun QuestsScreen(
    dailyQuests: List<ProcessedQuest>,
    guildQuests: List<ProcessedQuest>,
    activeSubTab: String,
    onSubTabChange: (String) -> Unit,
    onClaimQuestReward: (questId: String, goldReward: Int, xpReward: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedQuests = if (activeSubTab == "guild") guildQuests else dailyQuests
    val pendingDailyClaims = dailyQuests.count { it.isCompleted && !it.isClaimed }
    val pendingGuildClaims = guildQuests.count { it.isCompleted && !it.isClaimed }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ── Cabeçalho da Tela ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quadro de Contratos",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Amber400
            )
        }

        Text(
            text = if (activeSubTab == "guild") {
                "Marcos heroicos e feitos lendários da guilda com grandes recompensas."
            } else {
                "Contratos rotativos renovados a cada dia. Cumpra-os para obter ouro e XP."
            },
            color = Stone400,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // ── Alternância de Sub-Abas (Diárias / Jornada da Guilda) ────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Stone900)
                .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SubTabButton(
                title = "Diárias",
                isSelected = activeSubTab == "daily",
                pendingCount = pendingDailyClaims,
                onClick = { onSubTabChange("daily") },
                modifier = Modifier.weight(1f)
            )
            SubTabButton(
                title = "Jornada da Guilda",
                isSelected = activeSubTab == "guild",
                pendingCount = pendingGuildClaims,
                onClick = { onSubTabChange("guild") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Lista de Contratos ───────────────────────────────────────────────
        if (displayedQuests.isEmpty()) {
            EmptyQuestsPlaceholder(
                message = if (activeSubTab == "guild") {
                    "Nenhum contrato da guilda disponível no momento."
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
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Amber500.copy(alpha = 0.2f) else Color.Transparent,
        label = "subtab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Amber400 else Color.Transparent,
        label = "subtab_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Amber300 else Amber100.copy(alpha = 0.5f),
        label = "subtab_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
            if (pendingCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Emerald500)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$pendingCount",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Stone950
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

    val cardBgColor = when {
        isClaimed -> Stone900.copy(alpha = 0.6f)
        isReadyToClaim -> Color(0x22F59E0B)
        else -> Stone900
    }

    val cardBorderColor = when {
        isClaimed -> Color(0x3310B981)
        isReadyToClaim -> Amber400
        else -> Color(0x33F59E0B)
    }

    val cardBorderWidth = if (isReadyToClaim) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBgColor)
            .border(cardBorderWidth, cardBorderColor, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Linha Superior: Nome da Quest + Status ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isClaimed) Stone400 else if (isReadyToClaim) Amber300 else Amber100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                when {
                    isClaimed -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x1A10B981))
                                .border(1.dp, Color(0x3310B981), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Concluído",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    }
                    isReadyToClaim -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x22F59E0B))
                                .border(1.dp, Amber400, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pronto!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber300
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Em andamento",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Stone500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Descrição ───────────────────────────────────────────────────
            Text(
                text = quest.desc,
                fontSize = 12.sp,
                color = if (isClaimed) Stone500 else Amber100.copy(alpha = 0.75f),
                lineHeight = 16.sp
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
                    color = if (isClaimed) Stone400 else if (isReadyToClaim) Emerald400 else Amber200
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Stone800)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isClaimed) Emerald400.copy(alpha = 0.5f)
                            else if (isReadyToClaim) Emerald500
                            else Amber400
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Linha Inferior: Recompensas e Botão de Claim ────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recompensas (Gold + XP)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Gold
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪙",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${quest.rewardGold}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimed) Stone500 else Amber300
                        )
                    }

                    // XP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⭐",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${quest.rewardXp} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimed) Stone500 else Purple400
                        )
                    }
                }

                // Botão de Claim
                if (isReadyToClaim) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Amber400, Amber500)
                                )
                            )
                            .clickable(onClick = onClaim)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Resgatar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone950
                        )
                    }
                } else if (isClaimed) {
                    Text(
                        text = "Resgatado",
                        fontSize = 11.sp,
                        color = Stone500,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
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
