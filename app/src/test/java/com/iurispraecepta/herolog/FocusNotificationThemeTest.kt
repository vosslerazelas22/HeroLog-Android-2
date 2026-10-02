package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.service.FocusNotificationTheme
import com.iurispraecepta.herolog.ui.focus.RaidMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tabela de cores/ícones por modo (spec notificações por modo, T6/FR-1/FR-2).
 * Pura, sem Robolectric: só resolução de modo e constantes ARGB.
 */
class FocusNotificationThemeTest {

    @Test
    fun forSession_standard_resolvesGoldAndFocusIcon() {
        val theme = FocusNotificationTheme.forSession(
            isDungeonMode = false,
            isWildernessChecked = false
        )

        assertEquals(RaidMode.PADRAO, theme.mode)
        assertEquals(0xFFE5C158.toInt(), theme.accentColor)
        assertEquals(FocusNotificationTheme.ACCENT_STANDARD, theme.accentColor)
    }

    @Test
    fun forSession_dungeon_resolvesPurpleAndDungeonIcon() {
        val theme = FocusNotificationTheme.forSession(
            isDungeonMode = true,
            isWildernessChecked = false
        )

        assertEquals(RaidMode.MASMORRA, theme.mode)
        assertEquals(0xFFC084FC.toInt(), theme.accentColor)
    }

    @Test
    fun forSession_wilderness_resolvesRoseAndWildernessIcon() {
        val theme = FocusNotificationTheme.forSession(
            isDungeonMode = false,
            isWildernessChecked = true
        )

        assertEquals(RaidMode.SELVAGEM, theme.mode)
        assertEquals(0xFFFB7185.toInt(), theme.accentColor)
    }

    @Test
    fun forSession_dungeonTakesPrecedenceOverWilderness() {
        // Mesma precedência do `raidModeFrom` usado pela UI (Masmorra > Selvagem).
        val theme = FocusNotificationTheme.forSession(
            isDungeonMode = true,
            isWildernessChecked = true
        )

        assertEquals(RaidMode.MASMORRA, theme.mode)
        assertEquals(FocusNotificationTheme.ACCENT_DUNGEON, theme.accentColor)
    }

    @Test
    fun breakAccent_isEmerald() {
        assertEquals(0xFF10B981.toInt(), FocusNotificationTheme.ACCENT_BREAK)
    }

    @Test
    fun icons_areDistinctPerMode() {
        val standard = FocusNotificationTheme.forSession(false, false).smallIconRes
        val dungeon = FocusNotificationTheme.forSession(true, false).smallIconRes
        val wilderness = FocusNotificationTheme.forSession(false, true).smallIconRes

        assertEquals(com.iurispraecepta.herolog.R.drawable.ic_focus_small, standard)
        assertEquals(com.iurispraecepta.herolog.R.drawable.ic_focus_dungeon, dungeon)
        assertEquals(com.iurispraecepta.herolog.R.drawable.ic_focus_wilderness, wilderness)
    }
}
