package com.iurispraecepta.herolog.ui.theme

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.model.Difficulty

/**
 * Tokens visuais dos cards da aba Rituais (spec-rituais-cards, Bloco A).
 *
 * Referência: `specs/rituais-ai-studio/questTheme.ts` (repositório de teste do AI
 * Studio — para esta aba, NÃO usar o `HeroLog-React-ref`; o `main` do React tem o
 * visual antigo e não será atualizado).
 *
 * Regras da spec (§3):
 * - Reaproveitar token existente quando o hex for idêntico (nenhum dos hexes abaixo
 *   existia em `Color.kt`; ver spec §6). Sem literais soltos nos composables dos
 *   blocos seguintes.
 * - O `gold` do React (`#D9B85C`) NÃO existe no tema (o Android usa
 *   `Champagne400 #E5C158` / `Champagne500 #D4AF37` nos outros módulos): criado aqui
 *   como token de quest (`RitualGold`); não trocar o `Champagne` dos outros módulos.
 * - D7: o fundo da tela continua o global do Android (`QuestDeep`); `#0D0A17` (bg do
 *   React) NÃO é portado e não aparece aqui.
 * - `blue #75B9E8` do `questTheme.ts` é acento neutro/quest, fora do escopo dos cards
 *   (§2: sem data de vencimento do Todo) — não portado.
 */
val RitualSurface = Color(0xFF15121F)
val RitualSurfaceElevated = Color(0xFF1A1725)
val RitualSurfaceDrawer = Color(0xFF100D1C)
val RitualBorder = Color(0xFF2A2535)
val RitualTextPrimary = Color(0xFFE8E0D0)
val RitualTextTitleDone = Color(0xFFF4EFE6)
val RitualTextSecondary = Color(0xFF928C9A)
val RitualTextTertiary = Color(0xFF625E69)
val RitualGold = Color(0xFFD9B85C)
val RitualGreen = Color(0xFF69D6B7)
val RitualAmber = Color(0xFFDCC36A)
val RitualLavender = Color(0xFFB69AE8)
val RitualRose = Color(0xFFD99AAA)

/**
 * Vermelho do botão `−` **aceso** (SPEC-Rebranding item 4 — Android-first, sem
 * equivalente no React/AI Studio, cujo `−` não tem estado persistente).
 *
 * Deliberadamente **distinto de [RitualRose]** (rosa `#D99AAA`, que é a cor da
 * dificuldade Difícil): o aceso do `−` é preenchimento + borda + glow, não só cor,
 * para não depender de discriminação cromática (daltonismo).
 */
val RitualDanger = Color(0xFFE5484D)

/**
 * Estilo tonal por dificuldade (spec §3 + `DIFFICULTY_CONFIG` em `questTheme.ts`).
 * `bg` = pastel a 12%; `border` = pastel a 35%; `text` = pastel.
 */
data class RitualDifficultyStyle(
    val label: String,
    val pastel: Color,
    val bg: Color,
    val border: Color,
    val text: Color
)

private fun difficultyStyle(pastel: Color, label: String): RitualDifficultyStyle =
    RitualDifficultyStyle(
        label = label,
        pastel = pastel,
        bg = pastel.copy(alpha = 0.12f),
        border = pastel.copy(alpha = 0.35f),
        text = pastel
    )

/** Mapa por chave interna em inglês (label em PT). Fallback: `Easy`. */
fun ritualDifficultyStyle(difficulty: Difficulty): RitualDifficultyStyle = when (difficulty) {
    Difficulty.Trivial -> difficultyStyle(RitualAmber, "Trivial")
    Difficulty.Easy -> difficultyStyle(RitualGreen, "Fácil")
    Difficulty.Medium -> difficultyStyle(RitualLavender, "Médio")
    Difficulty.Hard -> difficultyStyle(RitualRose, "Difícil")
}

/**
 * Lookup por chave String (espelha `DIFFICULTY_CONFIG[key] || DIFFICULTY_CONFIG.Easy`
 * do React). Chave desconhecida/nula → estilo `Easy`.
 */
fun ritualDifficultyStyleForKey(key: String?): RitualDifficultyStyle = when (key) {
    "Trivial" -> difficultyStyle(RitualAmber, "Trivial")
    "Easy" -> difficultyStyle(RitualGreen, "Fácil")
    "Medium" -> difficultyStyle(RitualLavender, "Médio")
    "Hard" -> difficultyStyle(RitualRose, "Difícil")
    else -> difficultyStyle(RitualGreen, "Fácil")
}
