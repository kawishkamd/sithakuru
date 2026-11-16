/*
 * This file is part of Sithakuru.
 *
 * Sithakuru is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Sithakuru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sithakuru.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package sithakuru.sinhala.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sithakuru.sinhala.keyboard.clipboard.ClipboardHistoryManager

class ClipboardHistoryTest {

    private var savedData: String = ""
    private lateinit var manager: ClipboardHistoryManager

    @Before
    fun setUp() {
        savedData = ""
        manager = ClipboardHistoryManager(
            maxItems = 5,
            saveCallback = { savedData = it }
        )
    }

    @Test
    fun testAddItemAndGetItems() {
        val item1 = manager.addItem("Hello")
        val item2 = manager.addItem("World")

        assertNotNull(item1)
        assertNotNull(item2)

        val items = manager.getItems()
        assertEquals(2, items.size)
        // Latest item is first
        assertEquals("World", items[0].text)
        assertEquals("Hello", items[1].text)
        assertFalse(items[0].isPinned)
        assertFalse(items[1].isPinned)
    }

    @Test
    fun testAddEmptyAndBlankIgnored() {
        assertNull(manager.addItem(""))
        assertNull(manager.addItem("   "))
        assertNull(manager.addItem("\n\t"))
        assertEquals(0, manager.getItems().size)
    }

    @Test
    fun testDuplicateMovesToTop() {
        manager.addItem("First")
        manager.addItem("Second")
        manager.addItem("Third")

        // Re-adding "First"
        val updated = manager.addItem("First")
        assertNotNull(updated)

        val items = manager.getItems()
        assertEquals(3, items.size)
        assertEquals("First", items[0].text)
        assertEquals("Third", items[1].text)
        assertEquals("Second", items[2].text)
    }

    @Test
    fun testPinAndUnpin() {
        val item1 = manager.addItem("Item 1")!!
        val item2 = manager.addItem("Item 2")!!

        // Initially unpinned, Item 2 is first
        assertEquals("Item 2", manager.getItems()[0].text)

        // Pin Item 1
        val pinned = manager.togglePin(item1.id)
        assertTrue(pinned)

        // Pinned item must now be first
        val itemsAfterPin = manager.getItems()
        assertEquals("Item 1", itemsAfterPin[0].text)
        assertTrue(itemsAfterPin[0].isPinned)
        assertEquals("Item 2", itemsAfterPin[1].text)
        assertFalse(itemsAfterPin[1].isPinned)

        // Unpin Item 1
        val unpinned = manager.togglePin(item1.id)
        assertFalse(unpinned)

        // Item 2 is again first because Item 2 has newer timestamp
        val itemsAfterUnpin = manager.getItems()
        assertEquals("Item 2", itemsAfterUnpin[0].text)
        assertEquals("Item 1", itemsAfterUnpin[1].text)
    }

    @Test
    fun testClearUnpinned() {
        val item1 = manager.addItem("Keep me (pinned)")!!
        manager.addItem("Delete me 1")
        manager.addItem("Delete me 2")

        manager.togglePin(item1.id)
        assertEquals(3, manager.getItems().size)

        val clearedCount = manager.clearUnpinned()
        assertEquals(2, clearedCount)

        val remaining = manager.getItems()
        assertEquals(1, remaining.size)
        assertEquals("Keep me (pinned)", remaining[0].text)
        assertTrue(remaining[0].isPinned)
    }

    @Test
    fun testMaxCapacityPruning() {
        // maxItems is set to 5 in setUp()
        manager.addItem("Item 1")
        manager.addItem("Item 2")
        manager.addItem("Item 3")
        manager.addItem("Item 4")
        manager.addItem("Item 5")
        assertEquals(5, manager.getItems().size)

        // Adding 6th item should drop the oldest ("Item 1")
        manager.addItem("Item 6")
        val items = manager.getItems()
        assertEquals(5, items.size)
        assertEquals("Item 6", items[0].text)
        assertFalse(items.any { it.text == "Item 1" })
    }

    @Test
    fun testMaxCapacityPreservesPinned() {
        val item1 = manager.addItem("Important pinned")!!
        manager.togglePin(item1.id)

        manager.addItem("A")
        manager.addItem("B")
        manager.addItem("C")
        manager.addItem("D")
        manager.addItem("E")
        manager.addItem("F")

        val items = manager.getItems()
        assertEquals(5, items.size)
        // Pinned item MUST be kept at index 0
        assertEquals("Important pinned", items[0].text)
        assertTrue(items[0].isPinned)
    }

    @Test
    fun testDeleteItem() {
        val item1 = manager.addItem("Item A")!!
        manager.addItem("Item B")

        val deleted = manager.deleteItem(item1.id)
        assertTrue(deleted)

        val items = manager.getItems()
        assertEquals(1, items.size)
        assertEquals("Item B", items[0].text)

        assertFalse(manager.deleteItem("non_existent_id"))
    }

    @Test
    fun testSerializationAndDeserialization() {
        val sinhalaText = "සිංහල අකුරු සහ සංකේත\nපේළි දෙකක්!\nTab\tseparated? \"Quotes\" and emojis: 😊🇱🇰"
        val item1 = manager.addItem(sinhalaText)!!
        manager.togglePin(item1.id)
        manager.addItem("Second simple string")

        val serialized = manager.serializeToString()
        assertTrue(savedData.isNotEmpty())

        val newManager = ClipboardHistoryManager()
        newManager.loadFromString(serialized)

        val loaded = newManager.getItems()
        assertEquals(2, loaded.size)
        assertEquals(sinhalaText, loaded[0].text)
        assertTrue(loaded[0].isPinned)
        assertEquals("Second simple string", loaded[1].text)
        assertFalse(loaded[1].isPinned)
    }

    @Test
    fun testAddingItemWhenAllSlotsArePinnedNeverDropsNewItem() {
        // Fill all 5 capacity slots with pinned items
        val i1 = manager.addItem("P1")!!; manager.togglePin(i1.id)
        val i2 = manager.addItem("P2")!!; manager.togglePin(i2.id)
        val i3 = manager.addItem("P3")!!; manager.togglePin(i3.id)
        val i4 = manager.addItem("P4")!!; manager.togglePin(i4.id)
        val i5 = manager.addItem("P5")!!; manager.togglePin(i5.id)

        assertEquals(5, manager.getItems().size)
        assertTrue(manager.getItems().all { it.isPinned })

        // Adding a 6th (unpinned) item must NOT be dropped
        val newUnpinned = manager.addItem("Newly copied snippet")
        assertNotNull(newUnpinned)

        val items = manager.getItems()
        assertEquals(6, items.size)
        val unpinned = items.find { !it.isPinned }
        assertNotNull(unpinned)
        assertEquals("Newly copied snippet", unpinned?.text)
    }

    @Test
    fun testMaxClipLengthTruncation() {
        val hugeString = "a".repeat(15_000)
        val item = manager.addItem(hugeString)
        assertNotNull(item)
        assertEquals(ClipboardHistoryManager.MAX_CLIP_LENGTH, item!!.text.length)
        assertEquals(10_000, item.text.length)
    }

    @Test
    fun testLoadFromStringDeduplicatesAndHandlesMalformedLines() {
        val malformedData = """
            id1	1000	1	Sample1
            malformed_line_with_no_tabs
            id2	2000	0	Sample2
            id3	3000	0	Sample2
            id4	invalid_timestamp	0	Sample3
        """.trimIndent()

        val newManager = ClipboardHistoryManager()
        newManager.loadFromString(malformedData)

        val items = newManager.getItems()
        // id3 should be deduplicated because Sample2 already exists from id2
        assertEquals(3, items.size)
        assertTrue(items.any { it.text == "Sample1" && it.isPinned })
        assertTrue(items.any { it.text == "Sample2" && !it.isPinned })
        assertTrue(items.any { it.text == "Sample3" && !it.isPinned })
    }

    @Test
    fun testWhitespaceIsTrimmedBeforeDedupe() {
        manager.addItem("foo")
        manager.addItem("  foo  ")

        val items = manager.getItems()
        assertEquals(1, items.size)
        assertEquals("foo", items[0].text)
    }

    @Test
    fun testTruncationDoesNotSplitSurrogatePair() {
        // Emoji is a surrogate pair; place it so the cut would land on its high half.
        val prefix = "a".repeat(ClipboardHistoryManager.MAX_CLIP_LENGTH - 1)
        val item = manager.addItem(prefix + "\uD83D\uDE00")

        assertNotNull(item)
        val text = item!!.text
        assertTrue("Truncated text must not end on a lone high surrogate", !text.last().isHighSurrogate())
        assertEquals(ClipboardHistoryManager.MAX_CLIP_LENGTH - 1, text.length)
    }

    @Test
    fun testPinningIsBoundedByCapacity() {
        // Fill every slot, then pin them all.
        repeat(5) { manager.addItem("item$it") }
        manager.getItems().forEach { manager.togglePin(it.id) }
        assertEquals(5, manager.getItems().count { it.isPinned })

        // A sixth pin request must be refused rather than growing without bound.
        manager.addItem("overflow")
        val extra = manager.getItems().first { it.text == "overflow" }
        assertFalse(manager.togglePin(extra.id))
        assertEquals(5, manager.getItems().count { it.isPinned })
    }

    @Test
    fun testLoadFromStringReplacesExistingItems() {
        manager.addItem("stale")
        manager.addItem("fresh")
        val serialized = manager.serializeToString()

        // Loading is a replacement: it must mirror the persisted list exactly.
        manager.loadFromString(serialized)
        val items = manager.getItems()
        assertEquals(2, items.size)
        assertEquals("fresh", items[0].text)
        assertEquals("stale", items[1].text)

        // And a blank blob clears rather than retaining stale entries.
        manager.loadFromString("")
        assertEquals(0, manager.getItems().size)
    }
}
