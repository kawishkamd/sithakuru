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
import org.junit.Test

class GraphemeDeleterTest {

    private fun backspaceOnce(text: String): String {
        val n = GraphemeDeleter.deleteLength(text)
        return text.dropLast(n)
    }

    @Test
    fun deletesEmptyAndAsciiCorrectly() {
        assertEquals(0, GraphemeDeleter.deleteLength(""))
        assertEquals("hell", backspaceOnce("hello"))
        assertEquals("", backspaceOnce("a"))
    }

    @Test
    fun deletesSingleSurrogateEmoji() {
        assertEquals("hi ", backspaceOnce("hi 😀"))
        assertEquals("😀", backspaceOnce("😀😂"))
    }

    @Test
    fun deletesVariationSelectorEmojisAsOneUnit() {
        assertEquals("", backspaceOnce("☹️"))
        assertEquals("ok ", backspaceOnce("ok ✌️"))
        assertEquals("", backspaceOnce("🖐️"))
    }

    @Test
    fun deletesZwjEmojiSequencesAsOneUnit() {
        assertEquals("", backspaceOnce("🧑‍💻"))
        assertEquals("hi ", backspaceOnce("hi 😮‍💨"))
        assertEquals("", backspaceOnce("🧑‍🤝‍🧑"))
        assertEquals("", backspaceOnce("🐈‍⬛"))
        assertEquals("", backspaceOnce("❤️‍🔥"))
        assertEquals("හෙලෝ", backspaceOnce("හෙලෝ🧑‍💻"))
    }

    @Test
    fun deletesSkinToneModifiedEmojisAsOneUnit() {
        assertEquals("", backspaceOnce("👍🏽"))
        assertEquals("hi ", backspaceOnce("hi 👩🏽‍💻"))
    }

    @Test
    fun deletesRegionalIndicatorFlagsOneFlagAtATime() {
        assertEquals("", backspaceOnce("🇱🇰"))
        assertEquals("🇺🇸", backspaceOnce("🇺🇸🇱🇰"))
        assertEquals("hi ", backspaceOnce("hi 🇱🇰"))
    }

    @Test
    fun deletesKeycapSequencesAsOneUnit() {
        assertEquals("", backspaceOnce("1️⃣"))
        assertEquals("a", backspaceOnce("a#️⃣"))
    }

    @Test
    fun preservesSinhalaVowelSignAndConjunctBackspaceBehavior() {
        // Vowel sign deletes only the pilla, leaving the consonant.
        assertEquals("ක", backspaceOnce("කා"))
        assertEquals("ක", backspaceOnce("කි"))
        // Conjunct deletes the trailing consonant together with the ZWJ.
        assertEquals("ක්", backspaceOnce("ක්\u200Dය"))
        assertEquals("ක්", backspaceOnce("ක්\u200Dර"))
        assertEquals("ර්", backspaceOnce("ර්\u200Dක"))
    }
}
