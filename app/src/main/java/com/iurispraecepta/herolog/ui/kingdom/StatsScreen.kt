package com.iurispraecepta.herolog.ui.kingdom

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val Purple400 = Color(0xFFC084FC)
private val Purple500 = Color(0xFFA855F7)
private val Emerald400 = Color(0xFF34D399)
private val Emerald500 = Color(0xFF10B981)
private val Red400 = Color(0xFFF87171)
private val Red500 = Color(0xFFEF4444)
private val Rose400 = Color(0xFFFB7185)
private val Rose500 = Color(0xFFF43F5E)

private data class StatCardData(
    val label: String,
    val value: String,
    val desc: String,
    val icon: ImageVector,
    val tint: Color,
    val borderColor: Color,
    val backgroundColor: Color
)

/**
 * Porte fiel de StatsTab.tsx (src/modules/kingdom/StatsTab.tsx, 87L) + o cabeçalho de painel
 * do wrapper em App.tsx (activeTab === 'stats').
 *
 * Sem lógica nova — mapeamento direto de campos de `CharacterState`, mais uma única derivação
 * simples (`averageSessionLength = totalMinutes / totalSessions`, com guarda de divisão por
 * zero, igual à fonte).
 *
 * Nota sobre `bg-amber-550/[0.02]` no card "Moedas Acumuladas" da fonte: `amber-550` não existe
 * na paleta padrão do Tailwind (escala vai de amber-500 direto pra amber-600) — classe inválida,
 * não renderiza fundo nenhum no React real (mesma categoria do achado `stone-850` do Bloco D).
 * Replicado aqui como fundo transparente, não como `amber-500/[0.02]` (que seria "consertar" um
 * bug da fonte sem necessidade).
 *
 * Nota sobre ícones: `Swords→SportsMartialArts` e `Coins→MonetizationOn` reaproveitados do
 * Bloco D (mesma escolha já feita lá). `Clock→AccessTime`, `Timer→Timer` (exato),
 * `Flame→LocalFireDepartment` (já usado em FocusCompletionFlow.kt), `ShieldAlert→GppBad`
 * (aproximação nova, ainda não confirmada por build real) e `Shield→Shield` (exato, cabeçalho).
 */
@Composable
fun StatsScreen(state: CharacterState, modifier: Modifier = Modifier) {
    val averageSessionLength = if (state.totalSessions > 0) {
        state.totalMinutes / state.totalSessions
    } else {
        0
    }
    val totalHours = String.format("%.1f", state.totalMinutes / 60.0)

    val stats = listOf(
        StatCardData(
            label = "Sessões Fechadas",
            value = state.totalSessions.toString(),
            desc = "Missões completadas na gilda",
            icon = Icons.Filled.SportsMartialArts,
            tint = Champagne400,
            borderColor = Champagne500.copy(alpha = 0.10f),
            backgroundColor = Champagne500.copy(alpha = 0.02f)
        ),
        StatCardData(
            label = "Tempo Acumulado",
            value = "$totalHours horas",
            desc = "Horas totais de transcendência",
            icon = Icons.Filled.AccessTime,
            tint = Purple400,
            borderColor = Purple500.copy(alpha = 0.10f),
            backgroundColor = Purple500.copy(alpha = 0.02f)
        ),
        StatCardData(
            label = "Média de Incursão",
            value = "$averageSessionLength min",
            desc = "Duração média de cada missão",
            icon = Icons.Filled.Timer,
            tint = Emerald400,
            borderColor = Emerald500.copy(alpha = 0.10f),
            backgroundColor = Emerald500.copy(alpha = 0.02f)
        ),
        StatCardData(
            label = "Maior Série Histórica",
            value = "${state.bestStreak} dias",
            desc = "Série máxima de dias cultivados",
            icon = Icons.Filled.LocalFireDepartment,
            tint = Red400,
            borderColor = Red500.copy(alpha = 0.10f),
            backgroundColor = Red500.copy(alpha = 0.02f)
        ),
        StatCardData(
            label = "Moedas Acumuladas",
            value = "${state.totalGoldEarned} GP",
            desc = "Soma total de espólios extraídos",
            icon = Icons.Filled.MonetizationOn,
            tint = Amber500,
            borderColor = Amber500.copy(alpha = 0.10f),
            backgroundColor = Color.Transparent // bg-amber-550/[0.02] inválido na fonte, ver KDoc
        ),
        StatCardData(
            label = "Vitórias da Wilderness",
            value = state.wildernessWins.toString(),
            desc = "Sobrevivências em terra selvagem",
            icon = Icons.Filled.GppBad,
            tint = Rose400,
            borderColor = Rose500.copy(alpha = 0.10f),
            backgroundColor = Rose500.copy(alpha = 0.02f)
        )
    )

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
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = Champagne500,
                modifier = Modifier.height(16.dp).width(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ESTATÍSTICAS DO HERÓI",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 0.05.em,
                color = Champagne400
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = LocalBottomBarInset.current),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stats) { stat ->
                StatCard(stat)
            }
        }
    }
}

@Composable
private fun StatCard(stat: StatCardData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(stat.backgroundColor)
            .border(1.dp, stat.borderColor, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.label,
                fontFamily = Cinzel,
                fontSize = 10.sp,
                letterSpacing = 0.05.em,
                color = Amber100.copy(alpha = 0.40f)
            )
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = stat.tint.copy(alpha = 0.50f),
                modifier = Modifier.height(16.dp).width(16.dp)
            )
        }
        Column {
            Text(
                text = stat.value,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Amber100
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stat.desc,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = Amber100.copy(alpha = 0.30f)
            )
        }
    }
}
