package com.iurispraecepta.herolog.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SkillCarouselLogicTest {

    // clampIndex — espelha: skills.length > 0 ? Math.max(0, Math.min(selectedIndex, skills.length - 1)) : 0
    @Test
    fun clampIndex_returnsZero_whenListEmpty() {
        assertEquals(0, SkillCarouselLogic.clampIndex(selectedIndex = 3, size = 0))
    }

    @Test
    fun clampIndex_returnsSameIndex_whenWithinBounds() {
        assertEquals(1, SkillCarouselLogic.clampIndex(selectedIndex = 1, size = 3))
    }

    @Test
    fun clampIndex_clampsToLastIndex_whenSelectedIndexOutOfBounds() {
        assertEquals(2, SkillCarouselLogic.clampIndex(selectedIndex = 99, size = 3))
    }

    @Test
    fun clampIndex_clampsToZero_whenSelectedIndexNegative() {
        assertEquals(0, SkillCarouselLogic.clampIndex(selectedIndex = -1, size = 3))
    }

    // nextIndex — espelha: (activeIdx + 1) % skills.length
    @Test
    fun nextIndex_incrementsByOne_whenNotAtEnd() {
        assertEquals(1, SkillCarouselLogic.nextIndex(current = 0, size = 3))
    }

    @Test
    fun nextIndex_wrapsToZero_atLastIndex() {
        assertEquals(0, SkillCarouselLogic.nextIndex(current = 2, size = 3))
    }

    // prevIndex — espelha: (activeIdx - 1 + skills.length) % skills.length
    @Test
    fun prevIndex_decrementsByOne_whenNotAtStart() {
        assertEquals(1, SkillCarouselLogic.prevIndex(current = 2, size = 3))
    }

    @Test
    fun prevIndex_wrapsToLast_atFirstIndex() {
        assertEquals(2, SkillCarouselLogic.prevIndex(current = 0, size = 3))
    }

    // canNavigate — espelha guarda usada nos dois botões e no swipe: skills.length <= 1 desabilita
    @Test
    fun canNavigate_false_whenSizeOne() {
        assertEquals(false, SkillCarouselLogic.canNavigate(size = 1))
    }

    @Test
    fun canNavigate_false_whenSizeZero() {
        assertEquals(false, SkillCarouselLogic.canNavigate(size = 0))
    }

    @Test
    fun canNavigate_true_whenSizeGreaterThanOne() {
        assertEquals(true, SkillCarouselLogic.canNavigate(size = 3))
    }

    // isSwipe — espelha: Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 35
    @Test
    fun isSwipe_true_whenHorizontalDeltaExceedsThresholdAndDominatesVertical() {
        assertEquals(true, SkillCarouselLogic.isSwipe(deltaX = 40f, deltaY = 5f))
    }

    @Test
    fun isSwipe_false_whenDeltaXBelowThreshold() {
        assertEquals(false, SkillCarouselLogic.isSwipe(deltaX = 30f, deltaY = 5f))
    }

    @Test
    fun isSwipe_false_whenVerticalDeltaDominates() {
        assertEquals(false, SkillCarouselLogic.isSwipe(deltaX = 40f, deltaY = 50f))
    }
}
