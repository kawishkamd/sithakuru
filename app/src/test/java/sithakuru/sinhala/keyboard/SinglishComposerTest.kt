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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SinglishComposerTest {

    /** A render whose output length differs from the keys, to catch miscounted deletes. */
    private val bracketRender: (String) -> String = { keys -> keys.map { "<$it>" }.joinToString("") }

    /** The text field, as the service would change it. */
    private val field = StringBuilder()

    private fun applyEdit(edit: SinglishComposer.Edit) {
        field.setLength(field.length - edit.deleteBefore)
        field.append(edit.insert)
    }

    private fun SinglishComposer.typeAll(keys: String) = keys.forEach { applyEdit(type(it)) }

    @Test
    fun eachKeyReplacesTheWholeWord() {
        val composer = SinglishComposer(bracketRender)
        composer.typeAll("abc")

        assertEquals("<a><b><c>", field.toString())
        assertEquals("abc", composer.keys)
    }

    @Test
    fun separatorEndsTheWordAndLeavesItAlone() {
        val composer = SinglishComposer(bracketRender)
        composer.typeAll("ab cd")

        assertEquals("<a><b> <c><d>", field.toString())
        assertEquals("cd", composer.keys)
    }

    @Test
    fun backspaceInsideAWordDropsTheLastKeyAndRerenders() {
        val composer = SinglishComposer(bracketRender)
        composer.typeAll("abc")

        applyEdit(composer.backspace()!!)
        assertEquals("<a><b>", field.toString())

        applyEdit(composer.backspace()!!)
        applyEdit(composer.backspace()!!)
        assertEquals("", field.toString())
        assertFalse(composer.isComposing)
        // Past the start of the word, backspace is the service's normal delete.
        assertNull(composer.backspace())
    }

    @Test
    fun resetStartsANewWordWithoutTouchingEarlierText() {
        val composer = SinglishComposer(bracketRender)
        composer.typeAll("ab")
        // E.g. the cursor moved: the buffered keys no longer describe the text.
        composer.reset()
        composer.typeAll("c")

        assertEquals("<a><b><c>", field.toString())
        assertEquals("c", composer.keys)
    }

    @Test
    fun backspaceOutsideAWordIsNotHandled() {
        val composer = SinglishComposer(bracketRender)
        composer.typeAll("a ")
        assertNull(composer.backspace())
    }

    @Test
    fun realKeymapFieldAlwaysMatchesARenderOfTheKeys() {
        val composer = SinglishComposer()
        for (word in listOf("amma", "kshNa", "sRu", "Chandaya", "NGa")) {
            field.setLength(0)
            composer.reset()
            for (i in word.indices) {
                applyEdit(composer.type(word[i]))
                assertEquals(word, SinglishKeymap.render(word.substring(0, i + 1)), field.toString())
            }
            applyEdit(composer.backspace()!!)
            assertEquals(word, SinglishKeymap.render(word.dropLast(1)), field.toString())
        }
    }

    @Test
    fun helpEntriesMatchHelakuruKeymapWithoutDuplicatesOrGaps() {
        val seenOutputs = mutableSetOf<String>()
        for ((expected, shortcuts) in SinglishKeymap.helpEntries) {
            if (expected.isEmpty() && shortcuts.isEmpty()) continue
            assertTrue("Expected output must not be blank for '$shortcuts'", expected.isNotEmpty())
            assertTrue("Shortcuts must not be blank for '$expected'", shortcuts.isNotBlank())
            assertTrue("Duplicate help entry for '$expected'", seenOutputs.add(expected))
            for (seq in shortcuts.split(" / ")) {
                assertEquals("Shortcut '$seq' in help", expected, SinglishKeymap.render(seq))
            }
        }
        val missing = SinglishKeymap.mappedOutputs() - seenOutputs
        assertEquals("Every mapped Singlish character should appear in help", emptySet<String>(), missing)
    }
}
