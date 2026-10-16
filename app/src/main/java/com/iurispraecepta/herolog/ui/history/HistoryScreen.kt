package com.iurispraecepta.herolog.ui.history

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber200
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Red400
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Red950
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone950

/**
 * Tela Missões → Crônicas Diárias — porte de `HistoryTab.tsx` + wrapper
 * `activeTab === 'history'` de `App.tsx`, na abordagem "Timeline / diário"
 * (opção V3, escolhida na exploração visual de out/2026).
 *
 * Condições de exibição idênticas às da versão anterior: vazio geral sem
 * toggle, toggle só com histórico, filtro `notes.isNotBlank()`, vazio de
 * notas, badge "TERRA SELVAGEM" só em `entry.wilderness`, bloco de notas só
 * com nota, REVELAR/OCULTAR + painel só com `aiChronicle` (expansão única por
 * tela). Mesmos textos, ícones Lucide e tokens.
 *
 * Divergências intencionais (ver `PARIDADE.md`):
 * - fallback "Nenhuma crônica antiga disponível" oculto — a geração de crônica
 *   IA não existe no Android (`aiChronicle` nasce sempre null em
 *   `FocusApplyLogic.kt:108`); o slot comentado marca onde o estado volta;
 * - agrupamento por dia + trilha com marcos (só reorganização visual do mesmo
 *   `history`, ordem preservada); marco vermelho na Terra Selvagem e roxo na
 *   Masmorra (sem o canto vermelho girado da versão anterior — marco + badge
 *   carregam o sinal de modo).
 */
@Composable
fun HistoryScreen(
    history: List<HistoryEntry>,
    modifier: Modifier = Modifier
) {
    HistoryContent(history = history, modifier = modifier, initialViewMode = "all")
}

@Composable
private fun HistoryContent(
    history: List<HistoryEntry>,
    modifier: Modifier = Modifier,
    initialViewMode: String = "all"
) {
    var expandedChronicleId by remember { mutableStateOf<String?>(null) }
    var viewMode by remember { mutableStateOf(initialViewMode) }

    val notesEntries = remember(history) {
        history.filter { it.notes.isNotBlank() }
    }
    // Agrupamento por dia — só reorganização visual do mesmo `history`
    // (ordem original preservada dentro de cada dia).
    val groupedAll = remember(history) {
        history.groupBy { it.date.substringBefore(",") }
    }
    val groupedNotes = remember(notesEntries) {
        notesEntries.groupBy { it.date.substringBefore(",") }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        HistoryHeader()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, bottom = 12.dp + LocalBottomBarInset.current),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp)
        ) {
            if (history.isEmpty()) {
                item(key = "empty_general") { HistoryEmptyGeneral() }
            } else {
                item(key = "toggle") {
                    HistoryViewModeToggle(
                        viewMode = viewMode,
                        onSelect = { viewMode = it },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val grouped = if (viewMode == "notes") groupedNotes else groupedAll
                if (viewMode == "notes" && notesEntries.isEmpty()) {
                    item(key = "empty_notes") { HistoryEmptyNotes() }
                } else {
                    grouped.forEach { (day, entries) ->
                        item(key = "day_$day") {
                            HistoryDayHeader(day = day)
                        }
                        items(
                            items = entries,
                            key = { if (viewMode == "notes") "note_${it.id}" else "all_${it.id}" }
                        ) { entry ->
                            if (viewMode == "notes") {
                                HistoryTimelineBlock(
                                    entry = entry,
                                    isExpanded = false,
                                    onToggleExpand = {},
                                    notesOnly = true
                                )
                            } else {
                                HistoryTimelineBlock(
                                    entry = entry,
                                    isExpanded = expandedChronicleId == entry.id,
                                    onToggleExpand = {
                                        expandedChronicleId =
                                            if (expandedChronicleId == entry.id) null else entry.id
                                    },
                                    notesOnly = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Cabeçalho (BookOpen + título + divisor). */
@Composable
private fun HistoryHeader() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 34.dp)
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_book_open),
                    contentDescription = null,
                    tint = Champagne400,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CRÔNICAS DIÁRIAS",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.05.em,
                    color = Champagne400
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.10f))
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** Seletor de modo (Crônicas completas / Compilado de Notas). */
@Composable
private fun HistoryViewModeToggle(
    viewMode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Stone950.copy(alpha = 0.4f))
            .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HistoryViewModeButton(
            title = "Crônicas completas",
            isSelected = viewMode == "all",
            onClick = { onSelect("all") },
            modifier = Modifier.weight(1f)
        )
        HistoryViewModeButton(
            title = "Compilado de Notas",
            isSelected = viewMode == "notes",
            onClick = { onSelect("notes") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HistoryViewModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Champagne500.copy(alpha = 0.1f) else Color.Transparent,
        label = "history_view_mode_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Champagne500.copy(alpha = 0.2f) else Color.Transparent,
        label = "history_view_mode_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Champagne300 else Stone500,
        label = "history_view_mode_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.uppercase(),
            fontFamily = Cinzel,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.05.em,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/** Cabeçalho do dia na timeline (dia extraído da própria data da entrada). */
@Composable
private fun HistoryDayHeader(day: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Text(
            text = day,
            fontFamily = Cinzel,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.05.em,
            color = Champagne400
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.10f))
        )
    }
}

/**
 * Cor do marco da timeline por modo da sessão. Precedência: Terra Selvagem >
 * Masmorra > padrão (modos mutuamente exclusivos na UI). Pura para teste unitário.
 */
internal fun timelineDotColor(wilderness: Boolean, isDungeon: Boolean): Color =
    if (wilderness) Red400 else if (isDungeon) Purple400 else Champagne400

/**
 * Bloco da timeline: trilha com marco à esquerda, conteúdo sem card à direita.
 * No modo "notes" ([notesOnly]) mostra só skill/data/nota/duração.
 */
@Composable
private fun HistoryTimelineBlock(
    entry: HistoryEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    notesOnly: Boolean
) {
    val hasChronicle = !entry.aiChronicle.isNullOrBlank()
    val dotColor = timelineDotColor(wilderness = entry.wilderness, isDungeon = entry.isDungeon)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp)
    ) {
        // ── Trilha ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .weight(1f)
                    .background(Amber500.copy(alpha = 0.15f))
            )
        }

        // ── Conteúdo ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp, top = 2.dp)
        ) {
            if (notesOnly) {
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
                        lineHeight = 15.sp,
                        letterSpacing = 0.05.em,
                        color = Champagne400,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.date.substringAfter(", "),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        fontFamily = JetBrainsMono,
                        color = Amber100.copy(alpha = 0.40f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRect(
                                color = Amber500.copy(alpha = 0.30f),
                                topLeft = Offset.Zero,
                                size = Size(2.dp.toPx(), size.height)
                            )
                        }
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = "\"${entry.notes}\"",
                        fontFamily = Cinzel,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        lineHeight = 19.5.sp,
                        color = Amber200.copy(alpha = 0.90f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Duração do Estudo: ${entry.duration} min",
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        fontFamily = JetBrainsMono,
                        color = Amber100.copy(alpha = 0.30f)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.skillName,
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Amber200,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (entry.wilderness) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Red950.copy(alpha = 0.40f))
                                .border(1.dp, Red500.copy(alpha = 0.30f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TERRA SELVAGEM",
                                fontFamily = Inter,
                                fontSize = 10.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.1.em,
                                color = Red400
                            )
                        }
                    }
                }
                Text(
                    text = entry.date,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    fontFamily = JetBrainsMono,
                    color = Amber100.copy(alpha = 0.40f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                // XP/GP/duração inline numa única linha (leitura de diário).
                Text(
                    text = "⚡ +${entry.xp} XP · 💎 +${entry.gold} GP · ⏱ Duração: ${entry.duration}m",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    color = Emerald400,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (entry.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRect(
                                    color = Amber500.copy(alpha = 0.30f),
                                    topLeft = Offset.Zero,
                                    size = Size(2.dp.toPx(), size.height)
                                )
                            }
                            .padding(start = 10.dp)
                    ) {
                        Text(
                            text = "\"${entry.notes}\"",
                            fontFamily = Cinzel,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            lineHeight = 19.5.sp,
                            color = Amber200.copy(alpha = 0.70f)
                        )
                    }
                }

                if (hasChronicle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onToggleExpand)
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isExpanded) LucideR.drawable.lucide_ic_eye_off
                                else LucideR.drawable.lucide_ic_eye
                            ),
                            contentDescription = null,
                            tint = Purple400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "OCULTAR CRÔNICA MÍSTICA" else "REVELAR CRÔNICA MÍSTICA",
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.05.em,
                            color = Purple400
                        )
                    }
                } else {
                    // Slot reservado: crônica IA (feature não implementada — `aiChronicle`
                    // nasce sempre null em `FocusApplyLogic.kt:108`). Quando a geração
                    // existir, renderizar aqui o "Nenhuma crônica antiga disponível".
                    // Placeholder oculto por decisão do usuário.
                }

                if (hasChronicle) {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Amber500.copy(alpha = 0.04f),
                                            Purple500.copy(alpha = 0.04f)
                                        )
                                    )
                                )
                                .border(1.dp, Amber500.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_sparkles),
                                contentDescription = null,
                                tint = Amber400,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(40.dp)
                                    .alpha(0.15f)
                            )
                            Text(
                                text = entry.aiChronicle.orEmpty(),
                                fontFamily = Cinzel,
                                fontSize = 12.sp,
                                lineHeight = 19.5.sp,
                                color = Amber100.copy(alpha = 0.80f),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEmptyGeneral(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(LucideR.drawable.lucide_ic_book_open),
            contentDescription = null,
            tint = Amber500,
            modifier = Modifier
                .size(48.dp)
                .alpha(0.40f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Seu diário de jornada ainda está em branco.",
            fontFamily = Cinzel,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            color = Amber100.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Conclua sua primeira Missão de Foco para registrar novos feitos!",
            fontFamily = Inter,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Amber100.copy(alpha = 0.30f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryEmptyNotes(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Stone950.copy(alpha = 0.20f))
                .border(1.dp, Amber500.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_file_text),
                contentDescription = null,
                tint = Amber500,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(0.30f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nenhuma Nota de Estudo registrada ainda.",
                fontFamily = Cinzel,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Amber100.copy(alpha = 0.50f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Digite notas de estudo no formulário de foco (\"Notas Teológicas\") antes de iniciar suas missões para compilar teus registros aqui!",
                fontFamily = Inter,
                fontSize = 11.sp,
                lineHeight = 17.875.sp,
                color = Amber100.copy(alpha = 0.30f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

private fun historySampleEntries() = listOf(
    HistoryEntry(
        id = "h1",
        skillName = "Teologia Sistemática",
        date = "14/10/2026, 09:41:22",
        duration = 45,
        xp = 120,
        gold = 35,
        notes = "Revisar a doutrina da Trindade antes da próxima sessão.",
        wilderness = false,
        aiChronicle = "Sob a luz bruxuleante das velas, o escriba mergulhou nos tomos antigos e emergiu com a mente afiada como lâmina élfica.",
        subskillTag = "Dogmática"
    ),
    HistoryEntry(
        id = "h2",
        skillName = "Grego Koiné",
        date = "13/10/2026, 21:05:03",
        duration = 90,
        xp = 240,
        gold = 60,
        notes = "",
        wilderness = true,
        aiChronicle = null,
        subskillTag = "Alfabeto"
    ),
    HistoryEntry(
        id = "h3",
        skillName = "História da Igreja",
        date = "13/10/2026, 08:12:47",
        duration = 25,
        xp = 60,
        gold = 15,
        notes = "Anotar as datas dos concílios ecumênicos.",
        wilderness = false,
        isDungeon = true,
        aiChronicle = null,
        subskillTag = null
    )
)

@Preview(name = "Crônicas — com dados", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 800)
@Composable
private fun HistoryPreviewDados() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            HistoryContent(history = historySampleEntries())
        }
    }
}

@Preview(name = "Crônicas — vazio", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 600)
@Composable
private fun HistoryPreviewVazio() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            HistoryContent(history = emptyList())
        }
    }
}

@Preview(name = "Crônicas — sem crônica IA", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 800)
@Composable
private fun HistoryPreviewSemCronica() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            HistoryContent(
                history = listOf(
                    historySampleEntries()[1].copy(id = "h2b"),
                    historySampleEntries()[2].copy(id = "h3b")
                )
            )
        }
    }
}
