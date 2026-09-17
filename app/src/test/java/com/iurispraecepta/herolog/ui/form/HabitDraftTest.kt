package com.iurispraecepta.herolog.ui.form

import com.iurispraecepta.herolog.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitDraftTest {

    @Test
    fun `equal drafts with same values should be equal`() {
        val draft1 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        val draft2 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        assertTrue(draft1.isEqualTo(draft2))
        assertTrue(draft2.isEqualTo(draft1))
    }

    @Test
    fun `different title should not be equal`() {
        val draft1 = HabitDraft("Title 1", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        val draft2 = HabitDraft("Title 2", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `normalized tags should be equal`() {
        val draft1 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        val draft2 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag2, tag1")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `tags with spaces should be normalized`() {
        val draft1 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1, tag2, ")
        val draft2 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `lowercase tags should be normalized`() {
        val draft1 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "TAG1, TAG2")
        val draft2 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1, tag2")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `empty tags should be equal`() {
        val draft1 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "")
        val draft2 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `one empty vs one with tags should not be equal`() {
        val draft1 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "")
        val draft2 = HabitDraft("Title", "Notes", true, false, Difficulty.Easy, "tag1")
        assertFalse(draft1.isEqualTo(draft2))
    }
}
