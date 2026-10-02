package com.iurispraecepta.herolog.service

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.iurispraecepta.herolog.R
import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.raidModeFrom

/**
 * Tema das notificações de foco por modo (spec notificações por modo, FR-1/FR-2).
 *
 * Resolve o modo com o `raidModeFrom` existente de `ui/focus/RaidModeSection.kt`
 * (única regra de precedência Masmorra > Wilderness > Padrão, por decisão do spec).
 * As cores são constantes `Int` ARGB espelhando `ConceptAColors` (`ui/focus/FocusOrb.kt`,
 * privado e do tipo `Color` do Compose — o pacote `service` não depende de UI).
 */
object FocusNotificationTheme {

    /** `ConceptAColors.WorkAccent` (0xFFE5C158). */
    const val ACCENT_STANDARD: Int = 0xFFE5C158.toInt()

    /** `ConceptAColors.DungeonAccent` (0xFFC084FC). */
    const val ACCENT_DUNGEON: Int = 0xFFC084FC.toInt()

    /** `ConceptAColors.WildernessAccent` (0xFFFB7185). */
    const val ACCENT_WILDERNESS: Int = 0xFFFB7185.toInt()

    /** `ConceptAColors.BreakAccent` (0xFF10B981). */
    const val ACCENT_BREAK: Int = 0xFF10B981.toInt()

    data class Theme(
        val mode: RaidMode,
        @ColorInt val accentColor: Int,
        @DrawableRes val smallIconRes: Int
    )

    /** Tema da sessão em andamento/pausada a partir dos flags legados da config. */
    fun forSession(isDungeonMode: Boolean, isWildernessChecked: Boolean): Theme {
        return when (val mode = raidModeFrom(isDungeonMode, isWildernessChecked)) {
            RaidMode.PADRAO -> Theme(mode, ACCENT_STANDARD, R.drawable.ic_focus_small)
            RaidMode.MASMORRA -> Theme(mode, ACCENT_DUNGEON, R.drawable.ic_focus_dungeon)
            RaidMode.SELVAGEM -> Theme(mode, ACCENT_WILDERNESS, R.drawable.ic_focus_wilderness)
        }
    }
}

/** Aplica o cronômetro nativo de contagem regressiva (FR-4): o sistema desenha o
 * tempo restante a partir de [endTimeMillis], sem republicação por segundo e
 * correto mesmo se o processo morrer. Suportado no `minSdk = 24`. */
internal fun NotificationCompat.Builder.withCountDownChronometer(endTimeMillis: Long):
    NotificationCompat.Builder = apply {
        setUsesChronometer(true)
        setChronometerCountDown(true)
        setWhen(endTimeMillis)
    }
