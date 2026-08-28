package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.character.LevelUpEvent
import com.iurispraecepta.herolog.logic.character.LevelUpLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class LevelUpLogicTest {

    private val baseState = createInitialCharacterState(Date())

    @Test
    fun `detecta level up de combate quando combatLevel aumenta`() {
        val previous = baseState.copy(combatLevel = 3, charName = "Thalric", charClass = CharClass.Warrior)
        val next = previous.copy(combatLevel = 4)

        val events = LevelUpLogic.detectLevelUps(previous, next)

        assertEquals(1, events.size)
        val event = events[0] as LevelUpEvent.Combat
        assertEquals(3, event.oldLevel)
        assertEquals(4, event.newLevel)
        assertEquals("Thalric", event.charName)
        assertEquals("Warrior", event.charClass)
    }

    @Test
    fun `nao detecta level up de combate quando nivel anterior era zero`() {
        val previous = baseState.copy(combatLevel = 0)
        val next = previous.copy(combatLevel = 1)

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `nao detecta level up de combate quando nivel nao muda`() {
        val previous = baseState.copy(combatLevel = 5)
        val next = previous.copy(combatLevel = 5)

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `nao detecta level up de combate quando nivel diminui`() {
        val previous = baseState.copy(combatLevel = 5)
        val next = previous.copy(combatLevel = 4)

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `detecta level up de skill quando level da skill aumenta`() {
        val skillPrev = Skill(id = "sk-1", name = "Kotlin", level = 2, xp = 100, emoji = "\uD83D\uDCBB")
        val skillNext = skillPrev.copy(level = 3, xp = 10)
        val previous = baseState.copy(skills = listOf(skillPrev))
        val next = previous.copy(skills = listOf(skillNext))

        val events = LevelUpLogic.detectLevelUps(previous, next)

        assertEquals(1, events.size)
        val event = events[0] as LevelUpEvent.Skill
        assertEquals("Kotlin", event.skillName)
        assertEquals("\uD83D\uDCBB", event.emoji)
        assertEquals(2, event.oldLevel)
        assertEquals(3, event.newLevel)
    }

    @Test
    fun `usa emoji padrao quando skill nao tem emoji`() {
        val skillPrev = Skill(id = "sk-1", name = "Kotlin", level = 1, xp = 0, emoji = null)
        val skillNext = skillPrev.copy(level = 2)
        val previous = baseState.copy(skills = listOf(skillPrev))
        val next = previous.copy(skills = listOf(skillNext))

        val event = LevelUpLogic.detectLevelUps(previous, next).single() as LevelUpEvent.Skill

        assertEquals("\uD83C\uDFAF", event.emoji)
    }

    @Test
    fun `nao detecta level up de skill nova sem nivel anterior conhecido`() {
        val skillNext = Skill(id = "sk-1", name = "Kotlin", level = 3, xp = 0)
        val previous = baseState.copy(skills = emptyList())
        val next = previous.copy(skills = listOf(skillNext))

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `nao detecta level up de skill quando nivel anterior era zero`() {
        val skillPrev = Skill(id = "sk-1", name = "Kotlin", level = 0, xp = 0)
        val skillNext = skillPrev.copy(level = 1)
        val previous = baseState.copy(skills = listOf(skillPrev))
        val next = previous.copy(skills = listOf(skillNext))

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `nao detecta level up de skill quando nivel diminui`() {
        val skillPrev = Skill(id = "sk-1", name = "Kotlin", level = 5, xp = 0)
        val skillNext = skillPrev.copy(level = 4)
        val previous = baseState.copy(skills = listOf(skillPrev))
        val next = previous.copy(skills = listOf(skillNext))

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `detecta multiplos level ups simultaneos de combate e skills`() {
        val skillPrev = Skill(id = "sk-1", name = "Kotlin", level = 1, xp = 0, emoji = "\uD83D\uDCBB")
        val skillNext = skillPrev.copy(level = 2)
        val previous = baseState.copy(combatLevel = 2, skills = listOf(skillPrev))
        val next = previous.copy(combatLevel = 3, skills = listOf(skillNext))

        val events = LevelUpLogic.detectLevelUps(previous, next)

        assertEquals(2, events.size)
        assertTrue(events[0] is LevelUpEvent.Combat)
        assertTrue(events[1] is LevelUpEvent.Skill)
    }

    @Test
    fun `mantem ordem de multiplas skills evoluindo pela ordem da lista`() {
        val skillsPrev = listOf(
            Skill(id = "sk-1", name = "Kotlin", level = 1, xp = 0),
            Skill(id = "sk-2", name = "Ingles", level = 4, xp = 0)
        )
        val skillsNext = listOf(
            skillsPrev[0].copy(level = 2),
            skillsPrev[1].copy(level = 5)
        )
        val previous = baseState.copy(skills = skillsPrev)
        val next = previous.copy(skills = skillsNext)

        val events = LevelUpLogic.detectLevelUps(previous, next).map { it as LevelUpEvent.Skill }

        assertEquals(listOf("Kotlin", "Ingles"), events.map { it.skillName })
    }

    @Test
    fun `usa nome de fallback Aventureiro quando charName esta em branco`() {
        val previous = baseState.copy(combatLevel = 1, charName = "")
        val next = previous.copy(combatLevel = 2)

        val event = LevelUpLogic.detectLevelUps(previous, next).single() as LevelUpEvent.Combat

        assertEquals("Aventureiro", event.charName)
    }

    @Test
    fun `ignora skill removida entre os dois estados`() {
        val skillPrev = Skill(id = "sk-1", name = "Kotlin", level = 3, xp = 0)
        val previous = baseState.copy(skills = listOf(skillPrev))
        val next = previous.copy(skills = emptyList())

        assertTrue(LevelUpLogic.detectLevelUps(previous, next).isEmpty())
    }

    @Test
    fun `sem mudancas retorna lista vazia`() {
        val events = LevelUpLogic.detectLevelUps(baseState, baseState)

        assertTrue(events.isEmpty())
    }
}
