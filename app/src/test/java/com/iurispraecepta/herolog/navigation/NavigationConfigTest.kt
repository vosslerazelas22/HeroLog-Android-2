package com.iurispraecepta.herolog.navigation

import com.iurispraecepta.herolog.ui.navigation.NAV_ITEMS
import com.iurispraecepta.herolog.ui.navigation.SUB_TABS
import com.iurispraecepta.herolog.ui.navigation.getActiveModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T3 da Spec A (`A-spec-tavern-navigation.md`): tabelas de navegação do `HeroLogBottomNav.kt`
 * contra o `BottomNav.tsx` do TavernFeat (`c89af8f`) — 5 destinos, Taverna central,
 * `missions` → `rituals` e Skills como sub-aba do Herói (NAV-2..5).
 */
class NavigationConfigTest {

    @Test
    fun getActiveModule_focus_returnsFocus() {
        assertEquals("focus", getActiveModule("focus"))
    }

    @Test
    fun getActiveModule_tavern_returnsTavern() {
        assertEquals("tavern", getActiveModule("tavern"))
    }

    @Test
    fun getActiveModule_character_returnsCharacter() {
        assertEquals("character", getActiveModule("character"))
    }

    @Test
    fun getActiveModule_inventory_returnsCharacter() {
        assertEquals("character", getActiveModule("inventory"))
    }

    @Test
    fun getActiveModule_skills_returnsCharacter() {
        assertEquals("character", getActiveModule("skills"))
    }

    @Test
    fun getActiveModule_habits_returnsRituals() {
        assertEquals("rituals", getActiveModule("habits"))
    }

    @Test
    fun getActiveModule_dailies_returnsRituals() {
        assertEquals("rituals", getActiveModule("dailies"))
    }

    @Test
    fun getActiveModule_todos_returnsRituals() {
        assertEquals("rituals", getActiveModule("todos"))
    }

    @Test
    fun getActiveModule_quests_returnsRituals() {
        assertEquals("rituals", getActiveModule("quests"))
    }

    @Test
    fun getActiveModule_history_returnsRituals() {
        assertEquals("rituals", getActiveModule("history"))
    }

    @Test
    fun getActiveModule_shop_returnsKingdom() {
        assertEquals("kingdom", getActiveModule("shop"))
    }

    @Test
    fun navItems_sizeIsFive() {
        assertEquals(5, NAV_ITEMS.size)
    }

    @Test
    fun navItems_order_matchesReact() {
        assertEquals(
            listOf("focus", "rituals", "tavern", "character", "kingdom"),
            NAV_ITEMS.map { it.id }
        )
    }

    @Test
    fun subTabs_rituals_hasFiveSubTabs() {
        assertEquals(5, SUB_TABS["rituals"]?.size)
    }

    @Test
    fun subTabs_character_hasThreeSubTabs() {
        assertEquals(3, SUB_TABS["character"]?.size)
    }

    @Test
    fun subTabs_kingdom_hasSevenSubTabs() {
        assertEquals(7, SUB_TABS["kingdom"]?.size)
    }

    @Test
    fun subTabs_character_containsSkillsTab() {
        val characterTabs = SUB_TABS["character"]?.map { it.value }.orEmpty()
        assertTrue("skills deve ser sub-aba do módulo character (NAV-5)", "skills" in characterTabs)
    }

    @Test
    fun subTabs_missionsKeyWasRemoved() {
        assertEquals(null, SUB_TABS["missions"])
    }
}
