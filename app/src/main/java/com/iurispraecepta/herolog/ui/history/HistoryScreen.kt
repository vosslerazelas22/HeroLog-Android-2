package com.iurispraecepta.herolog.ui.history

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Amber200 = Color(0xFFFDE68A)
private val Amber300 = Color(0xFFFCD34D)
private val Emerald400 = Color(0xFF34D399)
private val Purple300 = Color(0xFFD8B4FE)
private val Purple400 = Color(0xFFC084FC)
private val Stone500 = Color(0xFF78716C)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ═══ CABEÇALHO DO PAINEL (vem do wrapper em App.tsx) ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Crônicas Diárias",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.12.em,
                color = Amber400
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ═══ ESTADO VAZIO GERAL vs CONTEÚDO COM TOGGLE ═══
        if (history.isEmpty()) {
            EmptyHistoryGeneralState()
        } else {
            // ── Seletor de Modo (Crônicas completas / Compilado de Notas) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Stone950.copy(alpha = 0.4f))
                    .border(1.dp, Color(0x1AF59E0B), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ViewModeButton(
                    title = "Crônicas completas",
                    isSelected = viewMode == "all",
                    onClick = { viewMode = "all" },
                    modifier = Modifier.weight(1f)
                )
                ViewModeButton(
                    title = "Compilado de Notas",
                    isSelected = viewMode == "notes",
                    onClick = { viewMode = "notes" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (viewMode == "notes") {
                // ═══ MODO NOTAS ═══
                if (notesEntries.isEmpty()) {
                    EmptyNotesState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = LocalBottomBarInset.current)
                    ) {
                        items(
                            items = notesEntries,
                            key = { "note_${it.id}" }
                        ) { entry ->
                            NotesHistoryCard(entry = entry)
                        }
                    }
                }
            } else {
                // ═══ MODO ALL (CRÔNICAS COMPLETAS) ═══
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = LocalBottomBarInset.current)
                ) {
                    items(
                        items = history,
                        key = { "all_${it.id}" }
                    ) { entry ->
                        FullHistoryCard(
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
private fun ViewModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Amber500.copy(alpha = 0.1f) else Color.Transparent,
        label = "view_mode_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Amber500.copy(alpha = 0.2f) else Color.Transparent,
        label = "view_mode_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Amber300 else Stone500,
        label = "view_mode_text"
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

/**
 * Card do modo "all" (Crônicas completas)
 */
@Composable
private fun FullHistoryCard(
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
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Cabeçalho: Skill + Badge Wilderness + Data à esquerda; XP e GP à direita ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Esquerda: Nome + Badge + Data
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = entry.skillName,
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Amber200,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (entry.wilderness) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Red400.copy(alpha = 0.15f))
                                    .border(1.dp, Red400.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "TERRA SELVAGEM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.em,
                                    color = Red400
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = entry.date,
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        color = Stone400
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Direita: XP e GP empilhados
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "⚡ +${entry.xp} XP",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = Emerald400
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "💎 +${entry.gold} GP",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono,
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
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x0DF59E0B))
                        .drawBehind {
                            drawLine(
                                color = Amber500.copy(alpha = 0.4f),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        .padding(start = 10.dp, top = 6.dp, bottom = 6.dp, end = 8.dp)
                ) {
                    Text(
                        text = "\u201C${entry.notes}\u201D",
                        fontFamily = Cinzel,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        color = Amber100.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Rodapé: Duração à esquerda + Revelar Crônica / Mensagem à direita ──
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
                    // Esquerda: Duração
                    Text(
                        text = "⏱ Duração: ${entry.duration}m",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono,
                        color = Stone400
                    )

                    // Direita: Botão ou texto
                    if (hasChronicle) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onToggleExpand)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Purple400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) "OCULTAR CRÔNICA MÍSTICA" else "REVELAR CRÔNICA MÍSTICA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.08.em,
                                color = Purple400
                            )
                        }
                    } else {
                        Text(
                            text = "Nenhuma crônica antiga disponível",
                            fontSize = 10.sp,
                            fontFamily = Cinzel,
                            fontStyle = FontStyle.Italic,
                            color = Amber100.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ── Painel Expansível da Crônica Mística ─────────────────────────
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
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Amber500.copy(alpha = 0.08f),
                                            Purple400.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .border(1.dp, Amber400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "📜 Crônica Mística",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Purple300
                                    )
                                }
                                Text(
                                    text = entry.aiChronicle.orEmpty(),
                                    fontSize = 12.sp,
                                    color = Amber100.copy(alpha = 0.9f),
                                    lineHeight = 18.sp,
                                    fontFamily = Cinzel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card do modo "notes" (Compilado de Notas) - compacto e focado na citação
 */
@Composable
private fun NotesHistoryCard(
    entry: HistoryEntry,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Stone950.copy(alpha = 0.6f))
            .border(1.dp, Amber500.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Linha Superior: Skill à esquerda, Data à direita ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.skillName.uppercase(),
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.08.em,
                    color = Amber400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = entry.date,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMono,
                    color = Stone500
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Linha divisória fina ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x1AF59E0B))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Corpo: Nota entre aspas com borda esquerda tipo blockquote ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Amber400,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
            ) {
                Text(
                    text = "\u201C${entry.notes}\u201D",
                    fontFamily = Cinzel,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = Amber100,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Rodapé: Duração alinhada à direita ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Duração do Estudo: ${entry.duration} min",
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMono,
                    color = Stone500
                )
            }
        }
    }
}

/**
 * Estado vazio quando não há nenhuma entrada de histórico geral
 */
@Composable
private fun EmptyHistoryGeneralState(modifier: Modifier = Modifier) {
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
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = Amber400.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Seu diário de jornada ainda está em branco.",
                fontFamily = Cinzel,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Amber300,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Conclua sua primeira Missão de Foco para registrar novos feitos!",
                fontSize = 11.sp,
                color = Stone400,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Estado vazio quando o modo "notes" é selecionado mas não há notas
 */
@Composable
private fun EmptyNotesState(modifier: Modifier = Modifier) {
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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Amber400.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Nenhuma Nota de Estudo registrada ainda.",
                fontFamily = Cinzel,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = Amber300,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Digite notas de estudo no formulário de foco (\"Notas Teológicas\") antes de iniciar suas missões para compilar teus registros aqui!",
                fontSize = 11.sp,
                color = Stone500,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}
