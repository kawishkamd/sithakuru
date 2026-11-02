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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SuggestionEngineTest {

    private lateinit var engine: SuggestionEngine

    @Before
    fun setUp() {
        val wordList = listOf(
            "the", "of", "and", "to", "a", "in", "for", "is", "on", "that",
            "this", "with", "i", "you", "it", "not", "or", "be", "are",
            "help", "held", "hello", "helpful", "here", "apple", "where",
            "whereas", "tomorrow"
        )
        engine = SuggestionEngine(wordList, wordList.toHashSet())
    }

    @Test
    fun testExactMatch() {
        val result = engine.getSuggestions("help")
        assertNotNull(result)
        assertEquals("help", result!!.raw)
        assertEquals("help", result.primary)
        assertEquals("helpful", result.secondary)
    }

    @Test
    fun testPrefixCompletion() {
        val result = engine.getSuggestions("hel")
        assertNotNull(result)
        assertEquals("hel", result!!.raw)
        assertEquals("help", result.primary)
        assertEquals("held", result.secondary)
    }

    @Test
    fun testContractionReplacement() {
        val result = engine.getSuggestions("dont")
        assertNotNull(result)
        assertEquals("dont", result!!.raw)
        assertEquals("don't", result.primary)

        val resultIm = engine.getSuggestions("im")
        assertNotNull(resultIm)
        assertEquals("I'm", resultIm!!.primary)
    }

    @Test
    fun testTypoReplacement() {
        val result = engine.getSuggestions("thsi")
        assertNotNull(result)
        assertEquals("thsi", result!!.raw)
        assertEquals("this", result.primary)

        val resultTeh = engine.getSuggestions("teh")
        assertNotNull(resultTeh)
        assertEquals("the", resultTeh!!.primary)
    }

    @Test
    fun testEditDistanceOne() {
        // Missing leading letter
        val result = engine.getSuggestions("pple")
        assertNotNull(result)
        assertEquals("pple", result!!.raw)
        assertEquals("apple", result.primary)
    }

    @Test
    fun testCasingPreservation() {
        // Title case
        val capPrefix = engine.getSuggestions("Hel")
        assertNotNull(capPrefix)
        assertEquals("Help", capPrefix!!.primary)

        // Upper case
        val upperPrefix = engine.getSuggestions("HEL")
        assertNotNull(upperPrefix)
        assertEquals("HELP", upperPrefix!!.primary)

        // Contraction casing
        val capContraction = engine.getSuggestions("Dont")
        assertNotNull(capContraction)
        assertEquals("Don't", capContraction!!.primary)

        val upperContraction = engine.getSuggestions("DONT")
        assertNotNull(upperContraction)
        assertEquals("DON'T", upperContraction!!.primary)
    }

    @Test
    fun testBlankAndEmpty() {
        assertNull(engine.getSuggestions(""))
        assertNull(engine.getSuggestions("   "))
    }
}
