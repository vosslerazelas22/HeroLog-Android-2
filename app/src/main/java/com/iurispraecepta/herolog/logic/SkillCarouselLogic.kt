package com.iurispraecepta.herolog.logic

import kotlin.math.abs

/**
 * Lógica pura do carrossel inline de skill do módulo Foco.
 * Port 1:1 de `src/components/SkillInlineCarousel.tsx` (React), sem estado/UI.
 */
object SkillCarouselLogic {

    /** Espelha `activeIdx` do React: clampa dentro dos limites, 0 se lista vazia. */
    fun clampIndex(selectedIndex: Int, size: Int): Int {
        if (size <= 0) return 0
        return selectedIndex.coerceIn(0, size - 1)
    }

    /** Espelha `handleNext`: `(activeIdx + 1) % skills.length`. */
    fun nextIndex(current: Int, size: Int): Int {
        if (size <= 0) return 0
        return (current + 1) % size
    }

    /** Espelha `handlePrev`: `(activeIdx - 1 + skills.length) % skills.length`. */
    fun prevIndex(current: Int, size: Int): Int {
        if (size <= 0) return 0
        return (current - 1 + size) % size
    }

    /** Espelha a guarda `skills.length <= 1` usada pros botões prev/next e pro swipe. */
    fun canNavigate(size: Int): Boolean = size > 1

    /** Espelha a checagem de swipe do `handleTouchEnd`: horizontal domina vertical e passa de 35px. */
    fun isSwipe(deltaX: Float, deltaY: Float): Boolean {
        return abs(deltaX) > abs(deltaY) && abs(deltaX) > 35f
    }
}
