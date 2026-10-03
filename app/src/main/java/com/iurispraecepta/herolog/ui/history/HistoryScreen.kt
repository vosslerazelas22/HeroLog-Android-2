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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Purple950
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Red600
import com.iurispraecepta.herolog.ui.theme.Red950
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Amber200 = Color(0xFFFDE68A)
private val Emerald400 = Color(0xFF34D399)
private val Purple400 = Color(0xFFC084FC)
private val Stone500 = Color(0xFF78716C)
private val Red400 = Color(0xFFF87171)

/**
 * Tela Missões → Crônicas Diárias — porte visual de `HistoryTab.tsx` + wrapper `activeTab === 'history'`
 * de `App.tsx` (painel `bg-quest-panel border border-amber-500/15 rounded-lg p-5`, cabeçalho centralizado
 * com `BookOpen`, e `px-4` interno do `HistoryTab`).
 *
 * Ícones Lucide literais da fonte (`BookOpen`, `Eye`, `EyeOff`, `Sparkles`, `FileText`).
 *
 * Divergência consciente: no React o painel cresce com o conteúdo e a página rola; aqui o painel é o
 * viewport de um `LazyColumn` (o histórico é ilimitado — `[historyObj, ...prev.history]` — e uma lista
 * não-lazy dentro de `verticalScroll` custaria composição de todos os cards). Animação de entrada dos
 * cards (`opacity/y` do motion) não portada.
 *
 * Sombras (aproximações — o `box-shadow` do CSS não tem equivalente exato no Compose; calibração
 * visual em device pendente):
 *  - painel `shadow-[0_12px_40px_rgba(0,0,0,0.7)]` → `Modifier.shadow` com elevação alta, cor preta;
 *  - cards `shadow-md` → `Modifier.shadow` com elevação baixa, cor preta;
 *  - painel da crônica `shadow-inner` (inset 0 2px 4px 5% preto) → gradiente de 6dp no topo.
 */
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

    // Wrapper do painel (App.tsx): `bg-quest-panel border border-amber-500/15 rounded-lg p-5`.
    // Cards têm `space-y-3` (12dp): cada item leva 12dp embaixo e o `contentPadding` inferior desconta
    // esses 12dp do total de 36dp (`p-5` 20dp + `pb-4` 16dp do HistoryTab).
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp, bottom = 12.dp + LocalBottomBarInset.current)
            // shadow-[0_12px_40px_rgba(0,0,0,0.7)] — antes do clip, senão o recorte come a sombra
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(8.dp))
            .background(QuestPanel)
            .border(1.dp, Amber500.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
    ) {
        // ═══ CABEÇALHO DO PAINEL (vem do wrapper em App.tsx) ═══
        // `pb-2.5 border-b border-amber-500/10 mb-4 flex justify-center items-center min-h-[35px]`
        // (min-h inclui o padding e a borda: 34dp + 1dp de linha).
        item(key = "header") {
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

        if (history.isEmpty()) {
            // HistoryTab: `if (history.length === 0) return <empty>` — sem toggle.
            item(key = "empty_general") { EmptyHistoryGeneralState() }
        } else {
            // ── Seletor de Modo (Crônicas completas / Compilado de Notas) ──
            item(key = "toggle") {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Stone950.copy(alpha = 0.4f))
                        .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
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
            }

            if (viewMode == "notes") {
                // ═══ MODO NOTAS ═══
                if (notesEntries.isEmpty()) {
                    item(key = "empty_notes") { EmptyNotesState() }
                } else {
                    items(
                        items = notesEntries,
                        key = { "note_${it.id}" }
                    ) { entry ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                            NotesHistoryCard(entry = entry)
                        }
                    }
                }
            } else {
                // ═══ MODO ALL (CRÔNICAS COMPLETAS) ═══
                items(
                    items = history,
                    key = { "all_${it.id}" }
                ) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
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

/**
 * Botão do seletor: `flex-1 text-center px-3 py-1 text-[10px] uppercase font-serif font-bold rounded
 * tracking-wider` + estado ativo `bg-champagne-500/10 text-champagne-300 border-champagne-500/20`,
 * inativo `text-stone-500 border-transparent`. Sem truncar: no React o texto quebra de linha.
 */
@Composable
private fun ViewModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Champagne500.copy(alpha = 0.1f) else Color.Transparent,
        label = "view_mode_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Champagne500.copy(alpha = 0.2f) else Color.Transparent,
        label = "view_mode_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Champagne300 else Stone500,
        label = "view_mode_text"
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

/**
 * Card do modo "all" (Crônicas completas)
 * React: `bg-purple-950/10 border border-amber-500/10 rounded-lg p-3 shadow-md relative overflow-hidden`.
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
            // shadow-md
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Purple950.copy(alpha = 0.10f))
            .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .drawBehind {
                // Detalhe Wilderness: `absolute top-0 right-0 w-24 h-24 bg-red-600/5 rotate-45
                // translate-x-12 -translate-y-12` → quadrado 96dp com centro no canto superior direito,
                // girado 45° e recortado pelo card (`overflow-hidden`).
                if (entry.wilderness) {
                    val half = 48.dp.toPx()
                    rotate(degrees = 45f, pivot = Offset(size.width, 0f)) {
                        drawRect(
                            color = Red600.copy(alpha = 0.05f),
                            topLeft = Offset(size.width - half, -half),
                            size = Size(half * 2, half * 2)
                        )
                    }
                }
            }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Cabeçalho (`flex justify-between items-start gap-2 mb-2`) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Esquerda: Nome + Badge + Data
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    // h4: `font-semibold text-amber-200 text-sm font-serif flex items-center gap-2`
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                            // `text-[10px] bg-red-950/40 border border-red-500/30 text-red-400 px-2 py-0.5
                            // rounded uppercase font-sans tracking-widest` (peso herdado do h4: semibold)
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

                    // `text-[11px] text-amber-100/40 font-mono mt-0.5`
                    Text(
                        text = entry.date,
                        fontSize = 11.sp,
                        lineHeight = 16.5.sp,
                        fontFamily = JetBrainsMono,
                        color = Amber100.copy(alpha = 0.40f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Direita: `text-xs font-mono font-bold`, XP emerald-400 / GP amber-400
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "⚡ +${entry.xp} XP",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = Emerald400
                    )
                    Text(
                        text = "💎 +${entry.gold} GP",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = Amber400
                    )
                }
            }

            // ── Anotações (`mb-2` após o cabeçalho; bloco: `bg-amber-500/5 rounded p-2 text-xs
            // text-amber-200/70 italic border-l-2 border-amber-500/30 leading-relaxed mb-2 font-serif`) ──
            Spacer(modifier = Modifier.height(8.dp))
            if (entry.notes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Amber500.copy(alpha = 0.05f))
                        .drawBehind {
                            drawRect(
                                color = Amber500.copy(alpha = 0.30f),
                                topLeft = Offset.Zero,
                                size = Size(2.dp.toPx(), size.height)
                            )
                        }
                        // border-l-2 (2dp) + p-2 (8dp) à esquerda
                        .padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Rodapé (`flex justify-between items-center text-xs mt-3 pt-2 border-t border-amber-500/5`) ──
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Amber500.copy(alpha = 0.05f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(top = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Esquerda: `text-amber-100/50 font-mono`
                    Text(
                        text = "⏱ Duração: ${entry.duration}m",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = JetBrainsMono,
                        color = Amber100.copy(alpha = 0.50f)
                    )

                    // Direita: `text-purple-400 font-semibold uppercase text-[10px] tracking-wider gap-1`
                    if (hasChronicle) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onToggleExpand)
                                // Sem padding horizontal (o botão do React não tem); vertical mantido
                                // como área de toque mínima.
                                .padding(vertical = 2.dp)
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
                        // `text-[10px] text-amber-100/30 italic font-serif`
                        Text(
                            text = "Nenhuma crônica antiga disponível",
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            fontFamily = Cinzel,
                            fontStyle = FontStyle.Italic,
                            color = Amber100.copy(alpha = 0.30f)
                        )
                    }
                }
            }

            // ── Painel expansível (AnimatePresence height/opacity 0.3s) ──
            if (hasChronicle) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ) {
                    // `mt-4 p-4 rounded-lg bg-gradient-to-b from-amber-500/[0.04] to-purple-500/[0.04]
                    // border border-amber-500/20 text-amber-100/80 leading-relaxed font-serif text-xs relative`
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
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
                            .drawBehind {
                                // shadow-inner: inset 0 2px 4px 0 rgb(0 0 0 / 5%) — sombra interna só no
                                // topo (offset-y 2px + blur 4px), abaixo do texto e do ícone.
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.05f), Color.Transparent),
                                        startY = 0f,
                                        endY = 6.dp.toPx()
                                    ),
                                    size = Size(size.width, 6.dp.toPx())
                                )
                            }
                    ) {
                        // `absolute top-2 right-2 opacity-15` — Sparkles w-10 h-10 text-amber-400
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

/**
 * Card do modo "notes" (Compilado de Notas)
 * React: `bg-stone-950/40 border border-amber-500/10 rounded-lg p-4 space-y-2.5 relative shadow-md`.
 */
@Composable
private fun NotesHistoryCard(
    entry: HistoryEntry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // shadow-md
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Stone950.copy(alpha = 0.40f))
            .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Linha superior: `flex justify-between items-center text-[10px] text-amber-100/40 font-mono
        // pb-1.5 border-b border-amber-500/5` ──
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // `font-serif text-champagne-400 font-bold uppercase tracking-wider`
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
                    text = entry.date,
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
                    .height(1.dp)
                    .background(Amber500.copy(alpha = 0.05f))
            )
        }

        // ── Nota: `text-xs text-amber-200/90 leading-relaxed font-serif italic pl-3.5 border-l-2
        // border-amber-500/30` ──
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
                // border-l-2 (2dp) + pl-3.5 (14dp)
                .padding(start = 16.dp)
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

        // ── Rodapé: `text-[10px] text-amber-100/30 font-mono pt-1 text-right` ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
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
    }
}

/**
 * Estado vazio geral — React: `flex flex-col items-center justify-center py-16 text-center
 * text-amber-100/50`, sem borda/fundo.
 */
@Composable
private fun EmptyHistoryGeneralState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // `w-12 h-12 mb-4 opacity-40 text-amber-500`
        Icon(
            painter = painterResource(LucideR.drawable.lucide_ic_book_open),
            contentDescription = null,
            tint = Amber500,
            modifier = Modifier
                .size(48.dp)
                .alpha(0.40f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // `font-serif italic text-lg`
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
        // `text-xs text-amber-100/30 mt-1`
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

/**
 * Estado vazio do modo "notes" — React: `py-12 text-center text-amber-100/50 bg-stone-950/20 border
 * border-amber-500/5 rounded-lg p-6` (py-12 vence o p-6 na vertical: 48dp / 24dp).
 * O `text-amber-150` do parágrafo não existe no Tailwind (bug da fonte, replicado): a cor herdada
 * `amber-100/50` vale.
 */
@Composable
private fun EmptyNotesState(modifier: Modifier = Modifier) {
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
            // `w-10 h-10 mb-3 opacity-30 text-amber-500`
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_file_text),
                contentDescription = null,
                tint = Amber500,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(0.30f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // `font-serif italic text-sm`
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
            // `text-[11px] text-amber-100/30 mt-1.5 max-w-xs leading-relaxed`
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
