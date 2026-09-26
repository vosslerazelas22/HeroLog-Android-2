package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.model.Rarity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow


/**
 * Celebração agregada para N sessões concluídas em background (spec-008, T11).
 *
 * Exibe: header com contagem, total fixo (XP/Gold), level-up final único,
 * achievements deduplicados (emoji + nome), breakdown colapsável por sessão,
 * botão "Continuar" que consome a fila atomicamente.
 */
@Composable
fun MultipleSessionCelebration(
    summary: AggregatedCelebrationSummary,
    onDismiss: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuestPanel)
            .padding(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            )
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${summary.sessionCount} sessões concluídas enquanto você estava fora",
                color = Color(0xFFE7E5E4),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Cinzel,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            // Total fixo (always visible)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF292524), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TOTAL XP",
                            color = Color(0xFF9F9F9F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Cinzel,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "+${summary.totalXp} XP",
                            color = Color(0xFF34D399),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = JetBrainsMono,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFF34D399).copy(alpha = 0.25f),
                                    offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF292524), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TOTAL OURO",
                            color = Color(0xFF9F9F9F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Cinzel,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "+${summary.totalGold} Ouro",
                            color = Champagne400,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = JetBrainsMono,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFFE2B054).copy(alpha = 0.25f),
                                    offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }
            }
        }

        // Level up indicator (if any)
        if (summary.leveledUp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF2E1065).copy(alpha = 0.4f),
                                Color(0xFF0C0A09),
                                Color(0xFF0C0A09)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎉 Level Up! Nv. ${summary.initialLevel} → ${summary.finalLevel}",
                        color = Color(0xFFC084FC),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = Cinzel,
                        letterSpacing = 0.5.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFFA855F7).copy(alpha = 0.3f),
                                offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                                blurRadius = 8f
                            )
                        )
                    )
                }
            }
        }

        // Achievements (emoji + nome)
        if (summary.allAchievements.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CONQUISTAS DESBLOQUEADAS",
                    color = Color(0xFFE5C158),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Cinzel,
                    letterSpacing = 1.sp
                )

                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    summary.allAchievements.forEach { id ->
                        val achievement = com.iurispraecepta.herolog.logic.achievements.AchievementCatalog.ACHIEVEMENTS_LIST
                            .firstOrNull { it.id == id }
                        if (achievement != null) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(110.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF2E1065).copy(alpha = 0.3f),
                                                Color(0xFF0C0A09),
                                                Color(0xFF0C0A09)
                                            )
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = achievement.icon,
                                        fontSize = 32.sp,
                                        modifier = Modifier
                                            .fillMaxSize()
                                    )
                                    Text(
                                        text = achievement.name,
                                        color = Color(0xFFC4B5FD),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Cinzel,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collapsible breakdown
        var expanded by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Toggle button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF292524), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "▾ Ocultar detalhes" else "▾ Ver detalhes por sessão",
                    color = Color(0xFFE7E5E4),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Cinzel
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF292524), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF44403C), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icons.Default.ArrowDropDown.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = if (expanded) "Ocultar detalhes" else "Ver detalhes",
                            tint = Color(0xFFA8A29E),
                            modifier = Modifier
                                .graphicsLayer { rotationZ = if (expanded) 180f else 0f }
                                .size(24.dp)
                        )
                    }
                }
            }

            // Breakdown expandable
            androidx.compose.animation.AnimatedVisibility(
                visible = expanded,
                enter = androidx.compose.animation.expandHorizontally(
                    animationSpec = androidx.compose.animation.core.tween(200, easing = androidx.compose.animation.core.EaseOut)
                ) +
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
                exit = androidx.compose.animation.shrinkHorizontally(
                    animationSpec = androidx.compose.animation.core.tween(150)
                ) +
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(100))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF292524))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown items
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        summary.sessions.forEach { session ->
                            SessionBreakdownItem(
                                entity = session,
                                onToggle = { },
                                expanded = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer { shadowElevation = 12.dp.toPx() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC29544),
                contentColor = Stone950
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9C37A))
        ) {
            Text(
                text = "Continuar",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontFamily = Cinzel,
                color = Stone950
            )
        }
    }
}