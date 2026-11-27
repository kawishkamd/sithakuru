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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingErgonomicsTest {

    private val capSentences = TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_FLAG_CAP_SENTENCES
    private val capWords = TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_FLAG_CAP_WORDS
    private val capChars = TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_FLAG_CAP_CHARACTERS

    @Test
    fun autoCapitalizesStartAndAfterSentenceEnd() {
        assertTrue(TypingErgonomics.shouldAutoCapitalize("", capSentences))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("Hello. ", capSentences))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("Really? ", capSentences))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("Wow! ", capSentences))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("Line 1\n", capSentences))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("Line 1\n ", capSentences))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("She said \"Hi.\" ", capSentences))
    }

    @Test
    fun doesNotAutoCapitalizeMidSentenceOrExcludedFields() {
        assertFalse(TypingErgonomics.shouldAutoCapitalize("Hello ", capSentences))
        assertFalse(TypingErgonomics.shouldAutoCapitalize("Hello", capSentences))
        assertFalse(TypingErgonomics.shouldAutoCapitalize("Hello.", capSentences))

        val password = capSentences or TypingErgonomics.TYPE_TEXT_VARIATION_PASSWORD
        val email = capSentences or TypingErgonomics.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        val uri = capSentences or TypingErgonomics.TYPE_TEXT_VARIATION_URI
        assertFalse(TypingErgonomics.shouldAutoCapitalize("", password))
        assertFalse(TypingErgonomics.shouldAutoCapitalize("", email))
        assertFalse(TypingErgonomics.shouldAutoCapitalize("", uri))
        assertFalse(TypingErgonomics.shouldAutoCapitalize("", TypingErgonomics.TYPE_CLASS_TEXT))
    }

    @Test
    fun respectsCapWordsAndCapCharactersFlags() {
        assertTrue(TypingErgonomics.shouldAutoCapitalize("", capWords))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("John ", capWords))
        assertFalse(TypingErgonomics.shouldAutoCapitalize("John", capWords))
        assertTrue(TypingErgonomics.shouldAutoCapitalize("AB", capChars))
    }

    @Test
    fun doubleSpaceConvertsAfterWordWithinThreshold() {
        assertTrue(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("hello ", 200L))
        assertTrue(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("2026 ", 500L))
    }

    @Test
    fun doubleSpaceIgnoresSlowTapsOrNonWordContext() {
        assertFalse(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("hello ", 501L))
        assertFalse(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("hello ", -1L))
        assertFalse(TypingErgonomics.shouldConvertDoubleSpaceToPeriod(" ", 100L))
        assertFalse(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("  ", 100L))
        assertFalse(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("hello. ", 100L))
        assertFalse(TypingErgonomics.shouldConvertDoubleSpaceToPeriod("hello", 100L))
    }

    @Test
    fun allowsDoubleSpacePeriodOnlyInNormalTextFields() {
        assertTrue(TypingErgonomics.allowsDoubleSpacePeriod(TypingErgonomics.TYPE_CLASS_TEXT))
        assertTrue(TypingErgonomics.allowsDoubleSpacePeriod(capSentences))
        assertFalse(TypingErgonomics.allowsDoubleSpacePeriod(0))
        assertFalse(TypingErgonomics.allowsDoubleSpacePeriod(TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_VARIATION_PASSWORD))
        assertFalse(TypingErgonomics.allowsDoubleSpacePeriod(TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_VARIATION_URI))
        assertFalse(TypingErgonomics.allowsDoubleSpacePeriod(TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertFalse(TypingErgonomics.allowsDoubleSpacePeriod(TypingErgonomics.TYPE_CLASS_TEXT or TypingErgonomics.TYPE_TEXT_VARIATION_FILTER))
    }
}
