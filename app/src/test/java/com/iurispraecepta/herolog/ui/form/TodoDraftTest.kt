package com.iurispraecepta.herolog.ui.form

import com.iurispraecepta.herolog.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoDraftTest {

    @Test
    fun `equal drafts with same values should be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        assertTrue(draft1.isEqualTo(draft2))
        assertTrue(draft2.isEqualTo(draft1))
    }

    @Test
    fun `different title should not be equal`() {
        val draft1 = TodoDraft("Title 1", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        val draft2 = TodoDraft("Title 2", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `normalized tags should be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag2, tag1", listOf("item1"), "item1")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `tags with spaces should be normalized`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1, tag2, ", listOf("item1"), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `lowercase tags should be normalized`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "TAG1, TAG2", listOf("item1"), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1, tag2", listOf("item1"), "item1")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `different checklist items should not be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", listOf("item1"), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", listOf("item2"), "item2")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `same checklist items should be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", listOf("item1", "item2"), "item1, item2")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", listOf("item1", "item2"), "item1, item2")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `empty tags should be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "", emptyList(), "")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "", emptyList(), "")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `empty checklist should be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `different difficulty should not be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Hard, "tag1", emptyList(), "")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `differences in notes should not be equal`() {
        val draft1 = TodoDraft("Title", "Notes 1", Difficulty.Easy, "tag1", emptyList(), "")
        val draft2 = TodoDraft("Title", "Notes 2", Difficulty.Easy, "tag1", emptyList(), "")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `checklistInput difference should not be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "item2")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `same checklistInput should be equal`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "item1")
        val draft2 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "item1")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `checklistItems from draft should match checklistInput`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", listOf("item1", "item2"), "item1, item2")
        assertEquals(listOf("item1", "item2"), draft1.checklistItems)
    }

    @Test
    fun `checklistItems should be empty when checklistInput is empty`() {
        val draft1 = TodoDraft("Title", "Notes", Difficulty.Easy, "tag1", emptyList(), "")
        assertEquals(emptyList<String>(), draft1.checklistItems)
    }
}
