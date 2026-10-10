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

    /**
     * Fundos coloridos por modo e estado (spec NOTIFICACOES_FOCO, FR-C2, seção 6).
     * Os rodando de Masmorra/Selvagem/Descanso são versões mais claras dos
     * acentos do app — divergência consciente (paleta da notificação ≠
     * `ConceptAColors`), porque os acentos originais davam ~3,9:1 no texto
     * secundário. O fundo colorizado independe do tema claro/escuro do sistema.
     */
    const val BG_STANDARD_RUNNING: Int = 0xFFE5C158.toInt()
    const val BG_DUNGEON_RUNNING: Int = 0xFFD0A3FD.toInt()
    const val BG_WILDERNESS_RUNNING: Int = 0xFFFC99A7.toInt()
    const val BG_BREAK: Int = 0xFF45C89D.toInt()
    const val BG_STANDARD_PAUSED: Int = 0xFF6B4F00.toInt()
    const val BG_DUNGEON_PAUSED: Int = 0xFF5B21B6.toInt()
    const val BG_WILDERNESS_PAUSED: Int = 0xFF9F1239.toInt()
    const val BG_ABANDON_CONFIRM: Int = 0xFF991B1B.toInt()

    data class Theme(
        val mode: RaidMode,
        @ColorInt val accentColor: Int,
        @DrawableRes val smallIconRes: Int,
        /** Fundo do `setColor` (= fundo colorizado). */
        @ColorInt val backgroundColor: Int
    )

    /** Tema da sessão em andamento/pausada a partir dos flags legados da config. */
    fun forSession(isDungeonMode: Boolean, isWildernessChecked: Boolean): Theme {
        return when (val mode = raidModeFrom(isDungeonMode, isWildernessChecked)) {
            RaidMode.PADRAO -> Theme(mode, ACCENT_STANDARD, R.drawable.ic_focus_small, BG_STANDARD_RUNNING)
            RaidMode.MASMORRA -> Theme(mode, ACCENT_DUNGEON, R.drawable.ic_focus_dungeon, BG_DUNGEON_RUNNING)
            RaidMode.SELVAGEM -> Theme(mode, ACCENT_WILDERNESS, R.drawable.ic_focus_wilderness, BG_WILDERNESS_RUNNING)
        }
    }

    /**
     * Fundo por modo e estado (pura, sem Context). A confirmação vence tudo,
     * depois o pausado (versão escura), depois o rodando. Descanso usa a
     * constante própria. `ACCENT_*` permanecem para conclusão e Morte Cognitiva.
     */
    fun backgroundFor(mode: RaidMode, phase: ServicePhase, confirming: Boolean): Int {
        if (confirming) return BG_ABANDON_CONFIRM
        if (phase == ServicePhase.BREAK_RUNNING) return BG_BREAK
        if (phase == ServicePhase.PAUSED) {
            return when (mode) {
                RaidMode.PADRAO -> BG_STANDARD_PAUSED
                RaidMode.MASMORRA -> BG_DUNGEON_PAUSED
                RaidMode.SELVAGEM -> BG_WILDERNESS_PAUSED
            }
        }
        return when (mode) {
            RaidMode.PADRAO -> BG_STANDARD_RUNNING
            RaidMode.MASMORRA -> BG_DUNGEON_RUNNING
            RaidMode.SELVAGEM -> BG_WILDERNESS_RUNNING
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
