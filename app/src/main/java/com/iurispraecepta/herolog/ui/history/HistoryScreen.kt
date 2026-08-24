package com.iurispraecepta.herolog.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Amber200 = Color(0xFFFDE68A)
private val Amber300 = Color(0xFFFCD34D)
private val Orange400 = Color(0xFFFB923C)
private val Purple300 = Color(0xFFD8B4FE)
private val Purple400 = Color(0xFFC084FC)
private val Stone500 = Color(0xFF78716C)
private val Stone600 = Color(0xFF57534E)
private val Red400 = Color(0xFFF87171)

@Composable
fun HistoryScreen(
    history: List<HistoryEntry>,
    modifier: Modifier = Modifier
) {
    var expandedChronicleId by remember { mutableStateOf<String?>(null) }
    var viewMode by remember { mutableStateOf("all") }

    val notesEntries = remember(history) {
        history.filter { it.notes.isNotBlank() }
    }

    val displayedEntries = if (viewMode == "notes") notesEntries else history

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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Crônicas Diárias",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Amber400
            )
        }

        Text(
            text = "O registro imutável de seus esforços, provações e conquistas no reino de Mystara.",
            color = Stone400,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // ── Estado Vazio vs Conteúdo ─────────────────────────────────────────
        if (history.isEmpty()) {
            EmptyHistoryState()
        } else {
            // ── Alternância de Modo (Todas as Sessões / Apenas Anotações) ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Stone900)
                    .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterTabButton(
                    title = "Todas as Sessões (${history.size})",
                    isSelected = viewMode == "all",
                    onClick = { viewMode = "all" },
                    modifier = Modifier.weight(1f)
                )
                FilterTabButton(
                    title = "Anotações (${notesEntries.size})",
                    isSelected = viewMode == "notes",
                    onClick = { viewMode = "notes" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (displayedEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma anotação registrada até o momento.",
                        color = Stone500,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = displayedEntries,
                        key = { it.id }
                    ) { entry ->
                        HistoryCard(
                            entry = entry,
                            isExpanded = expandedChronicleId == entry.id,
                            onToggleExpand = {
                                expandedChronicleId = if (expandedChronicleId == entry.id) null else entry.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Amber500.copy(alpha = 0.2f) else Color.Transparent,
        label = "history_tab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Amber400 else Color.Transparent,
        label = "history_tab_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Amber300 else Amber100.copy(alpha = 0.5f),
        label = "history_tab_text"
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
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasChronicle = !entry.aiChronicle.isNullOrBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Stone900)
            .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(8.dp))
            .then(
                if (hasChronicle) {
                    Modifier.clickable(onClick = onToggleExpand)
                } else {
                    Modifier
                }
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Linha Superior: Nome da Skill, Tags e Data ───────────────────
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
                        text = entry.skillName,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Amber300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!entry.subskillTag.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Amber500.copy(alpha = 0.1f))
                                .border(1.dp, Amber500.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = entry.subskillTag,
                                fontSize = 10.sp,
                                color = Amber200,
                                maxLines = 1
                            )
                        }
                    }

                    if (entry.wilderness) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Red400.copy(alpha = 0.15f))
                                .border(1.dp, Red400.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Red400,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Wilderness",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Red400
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.date,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Stone400
                    )

                    if (hasChronicle) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = if (isExpanded) "Recolher crônica" else "Expandir crônica",
                            tint = Amber400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Métricas da Sessão (Duração, XP, Gold) ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duração
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏱",
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entry.duration} min",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Amber100
                    )
                }

                // XP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐",
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${entry.xp} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple400
                    )
                }

                // Gold
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙",
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${entry.gold} Gold",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber400
                    )
                }
            }

            // ── Anotações do Usuário (se houver) ─────────────────────────────
            if (entry.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Stone950)
                        .border(1.dp, Stone800, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Anotações:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone500,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = entry.notes,
                            fontSize = 12.sp,
                            color = Amber100.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // ── Crônica Gerada por IA (Estrutura de suporte à fonte) ─────────
            if (hasChronicle) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x1A8B5CF6))
                                .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "📜 Crônica do Bardo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Purple300
                                    )
                                }
                                Text(
                                    text = entry.aiChronicle.orEmpty(),
                                    fontSize = 12.sp,
                                    color = Amber100.copy(alpha = 0.9f),
                                    lineHeight = 17.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    val borderColor = Color(0x33F59E0B)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp)
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
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = Amber400.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Seu diário de jornada ainda está em branco.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Amber300,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complete sua primeira sessão de foco para gravar sua lenda nas crônicas do reino.",
                fontSize = 12.sp,
                color = Stone400,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
