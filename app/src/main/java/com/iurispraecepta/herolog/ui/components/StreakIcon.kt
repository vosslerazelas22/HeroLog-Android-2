package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iurispraecepta.herolog.R

/*
 * ============================================================================
 * StreakIcon — ícone centralizado da sequência (streak) do Herói.
 *
 * Decisão de produto (divergência consciente do React): o React usa `Flame`
 * do lucide-react para streak em todos os lugares (App.tsx header/cabeçalho,
 * FocusCompletionFlow.tsx celebration/summary, CharacterScreen.tsx tiles).
 * No Android a sequência é representada por uma **vela customizada** (SVG
 * Vector Drawable) — ícone minimalista estilo Lucide com corpo cilíndrico,
 * pavio e chama. Dois estados:
 *   - isLit=true: vela acesa com chama (streak_candle_lit.xml)
 *   - isLit=false: vela apagada, sem chama (streak_candle_unlit.xml)
 * Não existe ícone `candle` no Lucide; asset próprio garante identidade visual
 * RPG (capela/taverna) e legibilidade em 12–16 dp.
 *
 * Usos (todos passam por este composable, nunca pelo drawable direto):
 *   - AppHeader (pill de streak)
 *   - FocusCompletionFlow.StreakCelebrationScreen (sempre acesa: só aparece
 *     quando a sequência incrementou) e SessionSummaryScreen (acesa se
 *     streak > 0)
 *   - CharacterScreen (tile "Sequência Ativa")
 *   - TavernScreen (tile "SEQUÊNCIA")
 *
 * Outros ícones de chama fora desse escopo (HeatmapScreen, StatsScreen,
 * GuideScreen, DailyReportModal, catálogo de conquistas) permanecem
 * intocados — ver boundaries da spec feature/streak-candle-icon.
 * ============================================================================
 */

/** Laranja padrão da streak (orange-500 Tailwind) — estado aceso. */
val StreakLitTint = Color(0xFFF97316)

/** Cinza apagado (stone-500 Tailwind) — estado apagado (sequência zerada). */
val StreakUnlitTint = Color(0xFF78716C)

/**
 * Ícone da sequência (vela customizada).
 *
 * @param isLit `true` = vela acesa com chama ([litTint]), `false` = vela
 *   apagada sem chama ([unlitTint]). Convenção: acesa quando `streak > 0`.
 * @param size tamanho do ícone — passar por aqui, não via [modifier], para
 *   não encadear dois `Modifier.size` (o último venceria).
 * @param modifier só para decorações (offset, shadow, etc.), sem `size`.
 * @param litTint cor do estado aceso (cada tela passa a sua: o header usa
 *   Orange500, o tile da ficha usa Orange400, etc.).
 */
@Composable
fun StreakIcon(
    isLit: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    litTint: Color = StreakLitTint,
    unlitTint: Color = StreakUnlitTint,
    contentDescription: String? = null
) {
    val drawableRes = if (isLit) R.drawable.streak_candle_lit else R.drawable.streak_candle_unlit
    Icon(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        tint = if (isLit) litTint else unlitTint,
        modifier = modifier.size(size)
    )
}
