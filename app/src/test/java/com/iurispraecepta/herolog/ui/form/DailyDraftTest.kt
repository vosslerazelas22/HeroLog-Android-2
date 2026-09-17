package com.iurispraecepta.herolog.ui.form

import com.iurispraecepta.herolog.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyDraftTest {

    @Test
    fun `equal drafts with same values should be equal`() {
        val draft1 = DailyDraft(
            "Title",
            "Notes",
            Difficulty.Easy,
            "every day",
            "1",
            "3",
            "tag1, tag2",
            emptyList(),
            ""
        )
        val draft2 = DailyDraft(
            "Title",
            "Notes",
            Difficulty.Easy,
            "every day",
            "1",
            "3",
            "tag1, tag2",
            emptyList(),
            ""
        )
        assertTrue(draft1.isEqualTo(draft2))
        assertTrue(draft2.isEqualTo(draft1))
    }

    @Test
    fun `different title should not be equal`() {
        val draft1 = DailyDraft("Title 1", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1, tag2", emptyList(), "")
        val draft2 = DailyDraft("Title 2", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1, tag2", emptyList(), "")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `normalized tags should be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1, tag2", emptyList(), "")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag2, tag1", emptyList(), "")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `tags with spaces should be normalized`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1, tag2, ", emptyList(), "tag1, tag2")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1, tag2", emptyList(), "tag1, tag2")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `lowercase tags should be normalized`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "TAG1, TAG2", emptyList(), "TAG1, TAG2")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1, tag2", emptyList(), "TAG1, TAG2")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `different checklist items should not be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item1")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item2")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `same checklist items should be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", listOf("item1", "item2"), "item1, item2")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", listOf("item1", "item2"), "item1, item2")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `empty tags should be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "", emptyList(), "")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "", emptyList(), "")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `empty checklist should be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "")
        assertTrue(draft1.isEqualTo(draft2))
    }

    @Test
    fun `differences in repeats, every, streak should not be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item1")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every week", "1", "3", "tag1", emptyList(), "item1")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `differences in difficulty should not be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item1")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Hard, "every day", "1", "3", "tag1", emptyList(), "item1")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `checklistInput difference should not be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item1")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item2")
        assertFalse(draft1.isEqualTo(draft2))
    }

    @Test
    fun `same checklistInput should be equal`() {
        val draft1 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item1")
        val draft2 = DailyDraft("Title", "Notes", Difficulty.Easy, "every day", "1", "3", "tag1", emptyList(), "item1")
        assertTrue(draft1.isEqualTo(draft2))
    }
}
