package com.iurispraecepta.herolog.ui.kingdom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.kingdom.HeatmapCell
import com.iurispraecepta.herolog.logic.kingdom.HeatmapLogic
import com.iurispraecepta.herolog.logic.kingdom.HeatmapScope
import com.iurispraecepta.herolog.logic.kingdom.MinuteBucket
import com.iurispraecepta.herolog.logic.kingdom.MonthLabel
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

// ─── Cores sem equivalente nomeado no tema global (mesmo padrão de GuideScreen.kt/ShopScreen.kt) ───
private val Champagne400 = Color(0xFFE5C158)
private val Amber200 = Color(0xFFFDE68A)
private val Amber100Half = Color(0xFFFEF3C7)
private val Amber300 = Color(0xFFFCD34D)
private val Purple950 = Color(0xFF3B0764)
private val GoldAccent = Color(0xFFE2B054) // #E2B054 — cor de destaque literal da fonte (não é token do tema)
private val GoldMedieval = Color(0xFFC29544) // #C29544 — dourado heráldico usado em bordas/badges
private val Emerald400 = Color(0xFF34D399)
private val Emerald600 = Color(0xFF059669)
private val StatsMuted = Color(0xFF9F9F9F) // #9F9F9F — cinza literal dos rótulos de estatística
private val HintMuted = Color(0xFFA2A2A2) // #A2A2A2 — cinza literal do texto de dica

// ─── Cores literais por faixa de minutos (fillClass da fonte), fora do tema global ───
private val BucketZeroBg = Stone950.copy(alpha = 0.40f)
private val BucketLowBg = Color(0xFF132C1C)
private val BucketLowBorder = Color(0xFF1A452A)
private val BucketMidBg = Color(0xFF1B5E20)
private val BucketMidBorder = Color(0xFF2E7D32)
private val BucketHighBg = Color(0xFF4CAF50)
private val BucketHighBorder = Color(0xFF81C784)
private val BucketEliteBg = GoldMedieval
private val BucketEliteBorder = Amber300
private val FutureBg = Stone900

/**
 * Tela do Heatmap — porte do bloco `activeTab === 'heatmap'` de `App.tsx` (Bloco E), consumindo
 * `HeatmapLogic.calculate` (Bloco B).
 *
 * **Adaptação consciente pra mobile — tooltip de hover vira tap-to-select**: a fonte React usa
 * `onMouseEnter`/`onTouchStart` pra popular um tooltip flutuante posicionado via
 * `getBoundingClientRect`. Isso não existe em touch puro (não há "hover" real em Android) e
 * posicionamento absoluto sobre coordenadas de tela é frágil em Compose. Substituído por: toque
 * numa célula seleciona ela (`selectedCell`, estado local efêmero) e o texto de detalhe aparece
 * numa linha fixa abaixo da grade, no lugar da dica padrão — mesma informação da fonte (data por
 * extenso + minutos), só reposicionada. Documentado aqui em vez de tentar replicar posicionamento
 * flutuante que não faz sentido no ambiente touch.
 *
 * **Simplificações visuais conscientes** (mesma categoria da decisão já tomada em
 * `GuideScreen.kt` de remover o painel externo bordado com sombra): sem os 4 "corner brackets"
 * decorativos do card de consistência, sem glow/shadow customizado nas bordas de célula, sem a
 * animação de pulso no rótulo "ATUAL" da semana corrente. Estrutura, cores, textos e hierarquia
 * de informação replicados fielmente — só ornamentação CSS não-funcional foi simplificada.
 * Revisitar durante a validação visual formal (print lado a lado) se Bruno quiser essas nuances.
 */
@Composable
fun HeatmapScreen(
    history: List<HistoryEntry>,
    streak: Int,
    modifier: Modifier = Modifier
) {
    var scope by remember { mutableStateOf(HeatmapScope.THREE_MONTHS) }
    var selectedCell by remember { mutableStateOf<HeatmapCell?>(null) }

    val result = remember(history, scope) { HeatmapLogic.calculate(history, scope) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ═══ CABEÇALHO (vem do wrapper em App.tsx) ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = Champagne400,
                modifier = Modifier.height(18.dp).width(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HEATMAP",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.12.em,
                color = Champagne400
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ConsistencyCard(
            consistencyPercentage = result.consistencyPercentage,
            studyDaysInLast30 = result.studyDaysInLast30
        )

        Spacer(modifier = Modifier.height(20.dp))

        HeatmapGridPanel(
            scope = scope,
            onScopeChange = { scope = it },
            streak = streak,
            cells = result.cells,
            monthLabels = result.monthLabels,
            selectedCell = selectedCell,
            onCellTap = { selectedCell = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        StatsCardsSection(
            totalStudyDays = result.totalStudyDays,
            bestDayMinutes = result.bestDayMinutes,
            thisWeekMinutes = result.thisWeekMinutes,
            thisMonthMinutes = result.thisMonthMinutes
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ConsistencyCard(
    consistencyPercentage: Int,
    studyDaysInLast30: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Stone950, Purple950.copy(alpha = 0.20f), Stone950)
                )
            )
            .border(1.dp, Amber500.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "\uD83D\uDEE1\uFE0F", fontSize = 12.sp) // 🛡️
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CONSISTÊNCIA NOS ÚLTIMOS 30 DIAS",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.08.em,
                            color = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Seu comprometimento diário molda o seu heroísmo. Cada dia de foco é um golpe contra a estagnação.",
                        fontFamily = FontFamily.Serif,
                        fontSize = 10.sp,
                        color = Amber100Half.copy(alpha = 0.50f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$consistencyPercentage",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            color = Amber200
                        )
                        Text(
                            text = "%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Amber500.copy(alpha = 0.70f)
                        )
                    }
                    Text(
                        text = "$studyDaysInLast30 / 30 DIAS CUMPRIDOS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.06.em,
                        color = Emerald400
                    )
                }

                // Barra de progresso segmentada (10 blocos)
                Row(
                    modifier = Modifier
                        .background(Stone950, RoundedCornerShape(4.dp))
                        .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeSegments = Math.round(consistencyPercentage / 10f)
                    repeat(10) { idx ->
                        val isActive = idx < activeSegments
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isActive) Emerald600 else Stone900)
                                .then(
                                    if (!isActive) Modifier.border(1.dp, Stone900, RoundedCornerShape(1.dp))
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapGridPanel(
    scope: HeatmapScope,
    onScopeChange: (HeatmapScope) -> Unit,
    streak: Int,
    cells: List<HeatmapCell>,
    monthLabels: List<MonthLabel>,
    selectedCell: HeatmapCell?,
    onCellTap: (HeatmapCell) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Stone950.copy(alpha = 0.25f))
            .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        // Título + toggle de escopo + streak
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "\uD83D\uDCDC VISÃO GERAL DO PERÍODO — ${if (scope == HeatmapScope.THREE_MONTHS) "ÚLTIMOS 3 MESES" else "ÚLTIMOS 6 MESES"}",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.08.em,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sua consagração é medida em horas e minutos focados",
                fontFamily = FontFamily.Serif,
                fontSize = 9.sp,
                color = Amber100Half.copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(Stone950.copy(alpha = 0.80f), RoundedCornerShape(4.dp))
                        .border(1.dp, Amber500.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                        .padding(2.dp)
                ) {
                    ScopeToggleButton(
                        label = "3 MESES",
                        selected = scope == HeatmapScope.THREE_MONTHS,
                        onClick = { onScopeChange(HeatmapScope.THREE_MONTHS) }
                    )
                    // app/src/main/java/com/iurispraecepta/herolog/ui/kingdom/HeatmapScreen.kt — PARTE 2/2 (continuação, colar logo após a parte 1)
                    ScopeToggleButton(
                        label = "6 MESES",
                        selected = scope == HeatmapScope.SIX_MONTHS,
                        onClick = { onScopeChange(HeatmapScope.SIX_MONTHS) }
                    )
                }

                Row(
                    modifier = Modifier
                        .background(GoldMedieval.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        .border(1.dp, GoldMedieval.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Streak Atual: $streak ${if (streak == 1) "Dia" else "Dias"}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = GoldAccent
                    )
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.width(12.dp).height(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rótulos de mês (uma coluna por semana, alinhado à grade abaixo)
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(20.dp))
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (col in 0 until scope.weeksCount) {
                    val label = monthLabels.find { it.col == col }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (label != null) {
                            Text(
                                text = label.name,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = Amber500.copy(alpha = 0.80f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Matriz principal: rótulos de dia da semana + grade de células
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.width(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("SEG" to true, "TER" to false, "QUA" to true, "QUI" to false, "SEX" to true, "SÁB" to false, "DOM" to true)
                    .forEach { (label, visible) ->
                        Text(
                            text = label,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (visible) Amber100Half.copy(alpha = 0.40f) else Color.Transparent,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (col in 0 until scope.weeksCount) {
                    val isCurrentWeek = col == scope.weeksCount - 1
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isCurrentWeek) {
                                    Modifier
                                        .background(Amber500.copy(alpha = 0.04f), RoundedCornerShape(2.dp))
                                        .border(1.dp, Amber500.copy(alpha = 0.20f), RoundedCornerShape(2.dp))
                                } else Modifier
                            )
                            .padding(1.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (row in 0 until 7) {
                            val cellIndex = col * 7 + row
                            val cell = cells.getOrNull(cellIndex) ?: continue
                            HeatmapCellBox(
                                cell = cell,
                                isSelected = selectedCell?.date == cell.date,
                                onTap = { onCellTap(cell) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Detalhe da célula selecionada / dica padrão
        Text(
            text = selectedCell?.let { describeCell(it) }
                ?: "Toque nos blocos para ver os detalhes diários de estudo.",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = if (selectedCell != null) Emerald400 else HintMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Legenda de faixas de minutos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "MENOS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Amber100Half.copy(alpha = 0.40f))
            LegendSwatch(BucketZeroBg, Amber500.copy(alpha = 0.05f))
            LegendSwatch(BucketLowBg, BucketLowBorder)
            LegendSwatch(BucketMidBg, BucketMidBorder)
            LegendSwatch(BucketHighBg, BucketHighBorder)
            LegendSwatch(BucketEliteBg, BucketEliteBorder)
            Text(text = "MAIS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Amber100Half.copy(alpha = 0.40f))
        }
    }
}

@Composable
private fun ScopeToggleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (selected) Amber500.copy(alpha = 0.15f) else Color.Transparent)
            .then(
                if (selected) Modifier.border(1.dp, Amber500.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = if (selected) GoldAccent else Amber100Half.copy(alpha = 0.40f)
        )
    }
}

@Composable
private fun HeatmapCellBox(cell: HeatmapCell, isSelected: Boolean, onTap: () -> Unit) {
    val (bg, border) = when {
        cell.isFuture -> FutureBg.copy(alpha = 0.20f) to Amber500.copy(alpha = 0.01f)
        cell.minuteBucket == MinuteBucket.ZERO && cell.isToday -> BucketZeroBg to GoldMedieval
        cell.minuteBucket == MinuteBucket.ZERO -> BucketZeroBg to Amber500.copy(alpha = 0.05f)
        cell.minuteBucket == MinuteBucket.LOW -> BucketLowBg to BucketLowBorder
        cell.minuteBucket == MinuteBucket.MID -> BucketMidBg to BucketMidBorder
        cell.minuteBucket == MinuteBucket.HIGH -> BucketHighBg to BucketHighBorder
        else -> BucketEliteBg to BucketEliteBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(1.dp))
            .background(bg)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Amber200 else border, RoundedCornerShape(1.dp))
            .then(if (!cell.isFuture) Modifier.clickable(onClick = onTap) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (cell.minutes >= 90 && !cell.isFuture) {
            Text(text = "★", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Stone950)
        }
    }
}

@Composable
private fun LegendSwatch(bg: Color, border: Color) {
    Box(
        modifier = Modifier
            .width(10.dp)
            .height(10.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(1.dp))
    )
}

/** Porte de `getMinsShort` + montagem da data por extenso, usados no tooltip da fonte. */
private fun describeCell(cell: HeatmapCell): String {
    val dayNames = listOf("DOMINGO", "SEGUNDA-FEIRA", "TERÇA-FEIRA", "QUARTA-FEIRA", "QUINTA-FEIRA", "SEXTA-FEIRA", "SÁBADO")
    val months = listOf(
        "DE JANEIRO DE", "DE FEVEREIRO DE", "DE MARÇO DE", "DE ABRIL DE", "DE MAIO DE", "DE JUNHO DE",
        "DE JULHO DE", "DE AGOSTO DE", "DE SETEMBRO DE", "DE OUTUBRO DE", "DE NOVEMBRO DE", "DE DEZEMBRO DE"
    )
    val d = cell.dateObj
    // LocalDate.dayOfWeek: SEGUNDA=1..DOMINGO=7 → índice do array acima (DOMINGO=0..SÁBADO=6) via % 7
    val dayName = dayNames[d.dayOfWeek.value % 7]
    val monthAndYear = "${months[d.monthValue - 1]} ${d.year}"
    val fullDate = "$dayName, ${d.dayOfMonth} $monthAndYear"

    val minsShort = when {
        cell.minutes <= 0 -> "0M"
        cell.minutes < 60 -> "${cell.minutes}M"
        else -> {
            val hrs = cell.minutes / 60
            val rem = cell.minutes % 60
            if (rem > 0) "${hrs}H ${rem}M" else "${hrs}H"
        }
    }

    return "$fullDate — $minsShort ESTUDADO"
}

@Composable
private fun StatsCardsSection(
    totalStudyDays: Int,
    bestDayMinutes: Int,
    thisWeekMinutes: Int,
    thisMonthMinutes: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                label = "DIAS DE ESTUDO",
                value = "$totalStudyDays ${if (totalStudyDays == 1) "Dia" else "Dias"}",
                valueColor = Amber200,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "MELHOR DIA",
                value = HeatmapLogic.formatMinutes(bestDayMinutes),
                valueColor = GoldAccent,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "ESTA SEMANA",
                value = HeatmapLogic.formatMinutes(thisWeekMinutes),
                valueColor = Amber200,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Stone950.copy(alpha = 0.50f))
                .border(1.dp, GoldMedieval.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ESTE MÊS",
                    fontSize = 9.sp,
                    letterSpacing = 0.08.em,
                    color = HintMuted
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = HeatmapLogic.formatMinutes(thisMonthMinutes),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.04.em,
                    color = GoldAccent
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Stone950.copy(alpha = 0.40f))
            .border(1.dp, GoldMedieval.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 8.5.sp,
                letterSpacing = 0.06.em,
                color = StatsMuted,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}