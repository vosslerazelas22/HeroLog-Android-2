package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.ui.history.timelineDotColor
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Red400
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryTimelineDotTest {

    @Test
    fun dotColor_standardSession_isChampagne() {
        // Arrange: sessão padrão (sem modo)
        // Act: resolver a cor do marco
        val color = timelineDotColor(wilderness = false, isDungeon = false)

        // Assert: cor padrão
        assertEquals(Champagne400, color)
    }

    @Test
    fun dotColor_dungeonSession_isPurple() {
        // Arrange: sessão de Masmorra
        // Act: resolver a cor do marco
        val color = timelineDotColor(wilderness = false, isDungeon = true)

        // Assert: roxo da masmorra
        assertEquals(Purple400, color)
    }

    @Test
    fun dotColor_wildernessSession_isRed() {
        // Arrange: sessão de Terra Selvagem
        // Act: resolver a cor do marco
        val color = timelineDotColor(wilderness = true, isDungeon = false)

        // Assert: vermelho da terra selvagem
        assertEquals(Red400, color)
    }

    @Test
    fun dotColor_wildernessWinsOverDungeon() {
        // Arrange: combinação impossível na UI (modos mutuamente exclusivos)
        // Act: resolver a cor do marco
        val color = timelineDotColor(wilderness = true, isDungeon = true)

        // Assert: terra selvagem tem precedência
        assertEquals(Red400, color)
    }
}
