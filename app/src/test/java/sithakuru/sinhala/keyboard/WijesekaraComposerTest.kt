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
import org.junit.Before
import org.junit.Test

class WijesekaraComposerTest {

    private lateinit var composer: WijesekaraComposer

    /** The text field, as the service would change it. */
    private val field = StringBuilder()

    @Before
    fun setUp() {
        composer = WijesekaraComposer()
        field.setLength(0)
    }

    /** Mirrors the service's erasePrevious: one character, then any joiners before it. */
    private fun erasePrevious() {
        if (field.isEmpty()) return
        val n = if (field.length >= 2 && field[field.length - 1].isLowSurrogate()) 2 else 1
        field.setLength(field.length - n)
        while (field.endsWith(CharText.ZERO_WIDTH_JOINER)) field.setLength(field.length - 1)
    }

    private fun type(vararg inputs: Int, positionChanged: Boolean = false) {
        for (input in inputs) {
            val edit = composer.type(input, field.lastOrNull(), positionChanged)
            if (edit.erasePrevious) erasePrevious()
            field.append(edit.text)
        }
    }

    @Test
    fun ayannaWithAelaPillaBecomesAayanna() {
        type(CharCode.AYANNA, CharCode.AELA_PILLA)
        assertEquals(CharText.AAYANNA, field.toString())
    }

    @Test
    fun kombuvaTypedFirstMovesAfterItsConsonant() {
        type(CharCode.KOMBUVA, CharCode.ALPAPRAANA_KAYANNA)
        assertEquals(CharText.ALPAPRAANA_KAYANNA + CharText.KOMBUVA, field.toString())
    }

    @Test
    fun kombuvaWithEyannaBecomesAiyanna() {
        type(CharCode.KOMBUVA, CharCode.EYANNA)
        assertEquals(CharText.AIYANNA, field.toString())
    }

    @Test
    fun kombuvaWithAlLakunaBecomesDigaKombuva() {
        type(CharCode.KOMBUVA, CharCode.SIGN_AL_LAKUNA)
        assertEquals(CharText.DIGA_KOMBUVA, field.toString())
    }

    @Test
    fun twoKombuvasBecomeKombuDekaAndMoveAfterTheConsonant() {
        type(CharCode.KOMBUVA, CharCode.KOMBUVA)
        assertEquals(CharText.KOMBU_DEKA, field.toString())

        type(CharCode.ALPAPRAANA_KAYANNA)
        assertEquals(CharText.ALPAPRAANA_KAYANNA + CharText.KOMBU_DEKA, field.toString())
    }

    @Test
    fun reepayaGoesBeforeTheLetterItFollows() {
        type(CharCode.ALPAPRAANA_KAYANNA, CharCode.SIGN_REEPAYA)
        assertEquals(CharText.SIGN_REEPAYA + CharText.ALPAPRAANA_KAYANNA, field.toString())
    }

    @Test
    fun afterTheCursorMovesTheCharacterBeforeItIsUsed() {
        field.append(CharText.AYANNA)
        type(CharCode.AELA_PILLA, positionChanged = true)
        assertEquals(CharText.AAYANNA, field.toString())
    }

    @Test
    fun resetForgetsAPendingKombuva() {
        type(CharCode.KOMBUVA)
        composer.reset()
        // The cursor is now after other text, e.g. the user tapped elsewhere.
        field.setLength(0)
        field.append("x")
        type(CharCode.ALPAPRAANA_KAYANNA)
        assertEquals("x" + CharText.ALPAPRAANA_KAYANNA, field.toString())
    }

    @Test
    fun aPlainConsonantIsJustWritten() {
        type(CharCode.ALPAPRAANA_KAYANNA, CharCode.ALPAPRAANA_KAYANNA)
        assertEquals(CharText.ALPAPRAANA_KAYANNA + CharText.ALPAPRAANA_KAYANNA, field.toString())
    }
}
