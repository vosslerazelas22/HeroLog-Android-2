package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.service.FocusNotificationTheme
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.ui.focus.RaidMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * T101 — Paleta por modo e estado (spec NOTIFICACOES_FOCO, FR-C2, seção 6).
 * Pura, sem Robolectric: `backgroundFor(mode, phase, confirming)`.
 */
class NotificationBackgroundTest {

    @Test
    fun backgroundFor_standardRunning_isGold() {
        assertEquals(
            0xFFE5C158.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.PADRAO, ServicePhase.RUNNING, false)
        )
    }

    @Test
    fun backgroundFor_dungeonRunning_isLightPurple() {
        assertEquals(
            0xFFD0A3FD.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.MASMORRA, ServicePhase.RUNNING, false)
        )
    }

    @Test
    fun backgroundFor_wildernessRunning_isLightRose() {
        assertEquals(
            0xFFFC99A7.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.SELVAGEM, ServicePhase.RUNNING, false)
        )
    }

    @Test
    fun backgroundFor_break_isMint() {
        assertEquals(
            0xFF45C89D.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.PADRAO, ServicePhase.BREAK_RUNNING, false)
        )
    }

    @Test
    fun backgroundFor_standardPaused_isDarkGold() {
        assertEquals(
            0xFF6B4F00.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.PADRAO, ServicePhase.PAUSED, false)
        )
    }

    @Test
    fun backgroundFor_dungeonPaused_isDarkPurple() {
        assertEquals(
            0xFF5B21B6.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.MASMORRA, ServicePhase.PAUSED, false)
        )
    }

    @Test
    fun backgroundFor_wildernessPaused_isDarkRed() {
        assertEquals(
            0xFF9F1239.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.SELVAGEM, ServicePhase.PAUSED, false)
        )
    }

    @Test
    fun backgroundFor_confirming_winsOverModeAndPhase() {
        assertEquals(
            0xFF991B1B.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.MASMORRA, ServicePhase.RUNNING, true)
        )
        assertEquals(
            0xFF991B1B.toInt(),
            FocusNotificationTheme.backgroundFor(RaidMode.MASMORRA, ServicePhase.PAUSED, true)
        )
    }
}
