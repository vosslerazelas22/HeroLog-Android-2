package com.iurispraecepta.herolog.logic

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.ui.theme.Amber300
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Emerald500
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Stone300
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone700
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import java.util.Locale
import kotlin.math.min

// Cores Tailwind sem constante no tema (hex v3, convenção do projeto) — Spec C.
// Não tocar no Color.kt global: centralização de tokens é dívida separada (B2).
private val Sky300 = Color(0xFF7DD3FC)
private val Sky500 = Color(0xFF0EA5E9)
private val Cyan300 = Color(0xFF67E8F9)
private val Cyan500 = Color(0xFF06B6D4)
private val Emerald300 = Color(0xFF6EE7B7)

/**
 * Funções puras de apresentação da "Ficha do Herói" — Spec C (`CharacterScreen`).
 *
 * Fonte: `TavernFeat c89af8f`, `src/modules/character/CharacterScreen.tsx` —
 * `CATEGORY_LABELS` (linhas 40-65), `rarityMeta` (102-116), `formatHoursMinutes` (136-140),
 * `xpMax`/`xpPercent` (142-143) e o tile de Sequência (linha 260).
 *
 * Regras que NÃO são as da Taverna (TAVERN_DELTA §3.6):
 * - [formatHoursMinutes] é a **regra da CharacterScreen**: sempre `XhYYm`
 *   (`5 → "0h05m"`). A versão da Taverna (omite `0h` → `"25m"`) fica em [TavernLogic].
 * - [xpMax] é `nível × 100` puro (React linha 142). A Taverna usa `Math.max(100, …)`.
 */
object CharacterViewLogic {

    /** Selo de raridade do título equipado (React `CATEGORY_LABELS` + `rarityMeta`). */
    data class RarityBadge(
        val label: String,
        val backgroundColor: Color,
        val textColor: Color,
        val borderColor: Color,
        /** `shadow-[0_0_10px_rgba(…)]` do React; `null` quando a categoria não tem glow. */
        val glowColor: Color?
    )

    // ------------------------------------------------------------------
    // Identidade (CHAR-2)
    // ------------------------------------------------------------------

    /**
     * `TitleCategory` → selo exibido ao lado do nome. `null` (sem título equipado) →
     * `AVENTUREIRO` com fundo `stone-900` (React `rarityMeta`, linhas 102-108).
     *
     * React linha 111: `CATEGORY_LABELS[cat] || { label: cat.toUpperCase(), … }` — todos os
     * 6 valores do enum Android estão no mapa, então o fallback nunca dispara.
     */
    fun rarityBadge(category: TitleCategory?): RarityBadge = when (category) {
        TitleCategory.Legendary -> RarityBadge(
            label = "LENDÁRIO",
            backgroundColor = Amber500.copy(alpha = 0.15f),
            textColor = Amber300,
            borderColor = Amber500.copy(alpha = 0.4f),
            glowColor = Amber500.copy(alpha = 0.2f)
        )
        TitleCategory.Epic -> RarityBadge(
            label = "ÉPICO",
            backgroundColor = Purple500.copy(alpha = 0.15f),
            textColor = Purple300,
            borderColor = Purple500.copy(alpha = 0.4f),
            glowColor = Purple500.copy(alpha = 0.2f)
        )
        TitleCategory.Rare -> RarityBadge(
            label = "RARO",
            backgroundColor = Sky500.copy(alpha = 0.15f),
            textColor = Sky300,
            borderColor = Sky500.copy(alpha = 0.4f),
            glowColor = Sky500.copy(alpha = 0.2f)
        )
        TitleCategory.Achievement -> RarityBadge(
            label = "CONQUISTA",
            backgroundColor = Emerald500.copy(alpha = 0.15f),
            textColor = Emerald300,
            borderColor = Emerald500.copy(alpha = 0.4f),
            glowColor = Emerald500.copy(alpha = 0.2f)
        )
        TitleCategory.Drop -> RarityBadge(
            label = "CELESTIAL",
            backgroundColor = Cyan500.copy(alpha = 0.15f),
            textColor = Cyan300,
            borderColor = Cyan500.copy(alpha = 0.4f),
            glowColor = Cyan500.copy(alpha = 0.2f)
        )
        TitleCategory.Common -> RarityBadge(
            label = "COMUM",
            backgroundColor = Stone800.copy(alpha = 0.8f),
            textColor = Stone300,
            borderColor = Stone700,
            glowColor = null
        )
        null -> RarityBadge(
            label = "AVENTUREIRO",
            backgroundColor = Stone900,
            textColor = Stone400,
            borderColor = Stone800,
            glowColor = null
        )
    }

    // ------------------------------------------------------------------
    // Progressão (CHAR-4)
    // ------------------------------------------------------------------

    /** `xpMax = combatLevel * 100` — React linha 142 (a Taverna usa `max(100, …)`). */
    fun xpMax(combatLevel: Int): Int = combatLevel * 100

    /** `Math.min(100, Math.round(combatXP / xpMax * 100))` — React linha 143. */
    fun xpPercent(combatXP: Int, combatLevel: Int): Int {
        val max = xpMax(combatLevel).coerceAtLeast(1)
        val percent = (combatXP.toDouble() / max * 100).let { Math.round(it) }
        return min(100, percent.toInt())
    }

    /** `Math.min(100, (hp / maxHp) * 100)%` — largura do fill da barra de HP (React linha 304). */
    fun hpProgress(hp: Int, maxHp: Int): Float =
        if (maxHp <= 0) 0f else (hp.toFloat() / maxHp).coerceIn(0f, 1f)

    /** `{streak} {streak === 1 ? 'dia' : 'dias'}` — React linha 260. */
    fun streakLabel(streak: Int): String = if (streak == 1) "1 dia" else "$streak dias"

    /**
     * Regra da **CharacterScreen** (React linhas 136-140): o `h` **nunca** é omitido —
     * `5 → "0h05m"`, `125 → "2h05m"`, `0 → "0h00m"`. Diferente de `TavernLogic.formatHoursMinutes`.
     */
    fun formatHoursMinutes(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return String.format(Locale.US, "%dh%02dm", h, m)
    }
}
