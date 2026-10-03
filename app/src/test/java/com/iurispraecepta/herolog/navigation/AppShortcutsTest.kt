package com.iurispraecepta.herolog.navigation

import com.iurispraecepta.herolog.ui.navigation.AppShortcuts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Roteamento dos App Shortcuts estáticos (`res/xml/shortcuts.xml`) — mapeamento puro
 * action → destino, sem depender do framework Android (AGENTS.md §6, padrão AAA campo a campo).
 */
class AppShortcutsTest {

    @Test
    fun destinationFor_startPomodoro_returnsFocus() {
        // Arrange: action declarada no shortcuts.xml + Manifest
        // Act
        val result = AppShortcuts.destinationFor(AppShortcuts.ACTION_START_POMODORO)
        // Assert: destino exato, sem igualdade de objeto inteiro fora do sealed
        assertTrue(result is AppShortcuts.Destination.Focus)
    }

    @Test
    fun destinationFor_newTodo_returnsNewTodo() {
        val result = AppShortcuts.destinationFor(AppShortcuts.ACTION_NEW_TODO)

        assertTrue(result is AppShortcuts.Destination.NewTodo)
    }

    @Test
    fun destinationFor_openDailies_returnsDailies() {
        val result = AppShortcuts.destinationFor(AppShortcuts.ACTION_OPEN_DAILIES)

        assertTrue(result is AppShortcuts.Destination.Dailies)
    }

    @Test
    fun destinationFor_nullOrUnknown_returnsNull() {
        assertNull(AppShortcuts.destinationFor(null))
        assertNull(AppShortcuts.destinationFor("android.intent.action.MAIN"))
        assertNull(AppShortcuts.destinationFor(""))
    }

    @Test
    fun activeTabFor_mapsToExistingTabs() {
        // Arrange/Act: cada action deve resolver para um id de activeTab já existente
        // (focus/todos/dailies vindos de HeroLogBottomNav — sem aba nova)
        // Assert: campo a campo
        assertEquals("focus", AppShortcuts.activeTabFor(AppShortcuts.ACTION_START_POMODORO))
        assertEquals("todos", AppShortcuts.activeTabFor(AppShortcuts.ACTION_NEW_TODO))
        assertEquals("dailies", AppShortcuts.activeTabFor(AppShortcuts.ACTION_OPEN_DAILIES))
        assertNull(AppShortcuts.activeTabFor(null))
        assertNull(AppShortcuts.activeTabFor("unknown"))
    }

    @Test
    fun opensTodoCreator_onlyTrueForNewTodo() {
        assertTrue(AppShortcuts.opensTodoCreator(AppShortcuts.ACTION_NEW_TODO))
        assertFalse(AppShortcuts.opensTodoCreator(AppShortcuts.ACTION_START_POMODORO))
        assertFalse(AppShortcuts.opensTodoCreator(AppShortcuts.ACTION_OPEN_DAILIES))
        assertFalse(AppShortcuts.opensTodoCreator(null))
    }
}
