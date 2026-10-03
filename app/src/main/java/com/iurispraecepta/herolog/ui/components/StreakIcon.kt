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
 * Vector Drawable) — ícone minimalista estilo Lucide (stroke 2, viewBox 24)
 * com chama, pavio, corpo e pratinho. Dois estados, e variantes por tamanho
 * para manter a leitura em 12–16 dp sem ruído:
 *   - acesa (isLit=true):  streak_candle_lit (>= 16dp, com gota de cera)
 *                          streak_candle_lit_small (< 16dp, sem gota)
 *   - apagada (isLit=false): streak_candle_unlit_large (>= 24dp, fumaça + gota)
 *                          streak_candle_unlit (16–23dp, pavio curto + gota)
 *                          streak_candle_unlit_small (< 16dp, sem gota)
 * Limiares em [StreakIconDripMinSize] e [StreakIconSmokeMinSize]. Não existe
 * ícone `candle` no Lucide; asset próprio garante identidade visual RPG
 * (capela/taverna) e legibilidade em 12–16 dp.
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

/** Abaixo deste tamanho a gota de cera some (vira ruído em 12–14 dp). */
val StreakIconDripMinSize = 16.dp

/** A partir deste tamanho a vela apagada ganha o fio de fumaça. */
val StreakIconSmokeMinSize = 24.dp

/** Laranja padrão da streak (orange-500 Tailwind) — estado aceso. */
val StreakLitTint = Color(0xFFF97316)

/** Cinza apagado (stone-500 Tailwind) — estado apagado (sequência zerada). */
val StreakUnlitTint = Color(0xFF78716C)

/**
 * Ícone da sequência (vela customizada).
 *
 * @param isLit `true` = vela acesa com chama ([litTint]), `false` = vela
 *   apagada sem chama ([unlitTint]). Convenção: acesa quando `streak > 0`.
 *   A variante do drawable é escolhida por [size] (ver cabeçalho do arquivo).
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
    val drawableRes = when {
        isLit && size >= StreakIconDripMinSize -> R.drawable.streak_candle_lit
        isLit -> R.drawable.streak_candle_lit_small
        size >= StreakIconSmokeMinSize -> R.drawable.streak_candle_unlit_large
        size >= StreakIconDripMinSize -> R.drawable.streak_candle_unlit
        else -> R.drawable.streak_candle_unlit_small
    }
    Icon(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        tint = if (isLit) litTint else unlitTint,
        modifier = modifier.size(size)
    )
}
