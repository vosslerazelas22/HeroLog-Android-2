package com.iurispraecepta.herolog.ui.form

import org.junit.Assert.assertEquals
import org.junit.Test

class MergePendingChecklistInputTest {

    @Test
    fun `blank input returns items unchanged`() {
        assertEquals(
            listOf("a", "b"),
            mergePendingChecklistInput(listOf("a", "b"), "")
        )
    }

    @Test
    fun `whitespace-only input returns items unchanged`() {
        assertEquals(
            listOf("a"),
            mergePendingChecklistInput(listOf("a"), "   ")
        )
    }

    @Test
    fun `non-blank input is trimmed and appended`() {
        assertEquals(
            listOf("a", "b"),
            mergePendingChecklistInput(listOf("a"), "  b  ")
        )
    }

    @Test
    fun `non-blank input appended to empty list`() {
        assertEquals(
            listOf("novo item"),
            mergePendingChecklistInput(emptyList(), "novo item")
        )
    }

    @Test
    fun `blank input with empty list returns empty list`() {
        assertEquals(
            emptyList<String>(),
            mergePendingChecklistInput(emptyList(), "")
        )
    }
}
