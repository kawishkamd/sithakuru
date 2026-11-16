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
import org.junit.Assert.assertTrue
import org.junit.Test
import sithakuru.sinhala.keyboard.data.Action
import sithakuru.sinhala.keyboard.datatype.KeyData
import sithakuru.sinhala.keyboard.layouts.BaseLayout
import sithakuru.sinhala.keyboard.layouts.English
import sithakuru.sinhala.keyboard.layouts.Singlish
import sithakuru.sinhala.keyboard.layouts.Symbols
import sithakuru.sinhala.keyboard.layouts.Wijesekara

class KeyboardLayoutTest {

    private fun assertRowWidthEquals10(layoutName: String, row: List<KeyData>) {
        val totalWidth = row.sumOf { it.widthMultiplier.toDouble() }
        assertEquals("Total width of bottom row in $layoutName must be exactly 10.0", 10.0, totalWidth, 0.001)
    }

    @Test
    fun testBottomRowWidthAlwaysEquals10AcrossAllLayoutsAndConfigurations() {
        val flags = listOf(
            Pair(true, false),  // default: emoji on, clipboard off
            Pair(true, true),   // both on
            Pair(false, false), // both off
            Pair(false, true)   // emoji off, clipboard on
        )

        for ((showEmoji, showClipboard) in flags) {
            val configDesc = "emoji=$showEmoji, clipboard=$showClipboard"

            val english = English(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
            assertRowWidthEquals10("English ($configDesc)", english.lower[5]!!)
            assertRowWidthEquals10("English Upper ($configDesc)", english.upper[5]!!)

            val singlish = Singlish(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
            assertRowWidthEquals10("Singlish ($configDesc)", singlish.lower[5]!!)
            assertRowWidthEquals10("Singlish Upper ($configDesc)", singlish.upper[5]!!)

            val wijesekara = Wijesekara(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
            assertRowWidthEquals10("Wijesekara ($configDesc)", wijesekara.lower[5]!!)
            assertRowWidthEquals10("Wijesekara Upper ($configDesc)", wijesekara.upper[5]!!)
            assertRowWidthEquals10("Wijesekara Symbols ($configDesc)", wijesekara.wijesekaraSymbolsRowBottom)

            val symbols = Symbols(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
            assertRowWidthEquals10("Symbols ($configDesc)", symbols.lower[5]!!)
            assertRowWidthEquals10("Symbols Upper ($configDesc)", symbols.upper[5]!!)
        }
    }

    @Test
    fun testEmojiKeyHasClipboardPopupWhenClipboardKeyIsDisabled() {
        val layout = English(showEmojiKey = true, showClipboardKey = false)
        val bottomRow = layout.lower[5]!!

        val emojiKey = bottomRow.find { it.inputData.value == Action.EMOJI }
        assertNotNull(emojiKey)
        assertNotNull(emojiKey?.popupInputData)
        assertEquals(Action.CLIPBOARD, emojiKey?.popupInputData?.value)

        val clipboardKey = bottomRow.find { it.inputData.value == Action.CLIPBOARD }
        assertNull(clipboardKey)
    }

    @Test
    fun testClipboardKeyHasEmojiPopupWhenEmojiKeyIsDisabled() {
        val layout = English(showEmojiKey = false, showClipboardKey = true)
        val bottomRow = layout.lower[5]!!

        val clipboardKey = bottomRow.find { it.inputData.value == Action.CLIPBOARD }
        assertNotNull(clipboardKey)
        assertNotNull(clipboardKey?.popupInputData)
        assertEquals(Action.EMOJI, clipboardKey?.popupInputData?.value)

        val emojiKey = bottomRow.find { it.inputData.value == Action.EMOJI }
        assertNull(emojiKey)
    }

    @Test
    fun testNeitherKeyHasPopupWhenBothKeysAreEnabled() {
        val layout = English(showEmojiKey = true, showClipboardKey = true)
        val bottomRow = layout.lower[5]!!

        val emojiKey = bottomRow.find { it.inputData.value == Action.EMOJI }
        assertNotNull(emojiKey)
        assertNull(emojiKey?.popupInputData)

        val clipboardKey = bottomRow.find { it.inputData.value == Action.CLIPBOARD }
        assertNotNull(clipboardKey)
        assertNull(clipboardKey?.popupInputData)
    }

    @Test
    fun testCommaPopupOnlyShowsEmojiWhenEmojiKeyIsDisabled() {
        // When Emoji key is enabled (default), Comma should have NO popup (no duplicate clipboard popup)
        val layoutEmojiOn = English(showEmojiKey = true, showClipboardKey = false)
        val commaEmojiOn = layoutEmojiOn.lower[5]!!.find { it.inputData.value == CharCode.COMMA }
        assertNotNull(commaEmojiOn)
        assertNull(commaEmojiOn?.popupInputData)

        // When Emoji key is disabled, Comma should have Emoji popup
        val layoutEmojiOff = English(showEmojiKey = false, showClipboardKey = false)
        val commaEmojiOff = layoutEmojiOff.lower[5]!!.find { it.inputData.value == CharCode.COMMA }
        assertNotNull(commaEmojiOff)
        assertEquals(Action.EMOJI, commaEmojiOff?.popupInputData?.value)
    }

    @Test
    fun testWijesekaraLayoutKeyCountAndPopupIntegrity() {
        // Wijesekara with emoji=true, clipboard=false
        val wijeDefault = Wijesekara(showEmojiKey = true, showClipboardKey = false)
        val row5 = wijeDefault.lower[5]!!

        val emojiKey = row5.find { it.inputData.value == Action.EMOJI }
        assertNotNull(emojiKey)
        assertEquals(Action.CLIPBOARD, emojiKey?.popupInputData?.value)

        val spaceKey = row5.find { it.inputData.value == CharCode.SPACE }
        assertNotNull(spaceKey)
        assertEquals(4.0f, spaceKey?.widthMultiplier)

        // Wijesekara with both enabled
        val wijeBoth = Wijesekara(showEmojiKey = true, showClipboardKey = true)
        val row5Both = wijeBoth.lower[5]!!
        val spaceBoth = row5Both.find { it.inputData.value == CharCode.SPACE }
        assertEquals(3.0f, spaceBoth?.widthMultiplier)
    }

    @Test
    fun testSymbolsBottomRowHasNoDuplicateEmojiPopup() {
        // Symbols swaps LANG for SOLIDUS and COMMA for FULL_STOP. With emoji off and
        // clipboard on, only the clipboard key should long-press to emoji.
        val symbols = Symbols(showEmojiKey = false, showClipboardKey = true)
        val row5 = symbols.lower[5]!!

        val emojiPopupKeys = row5.filter { it.popupInputData?.value == Action.EMOJI }
        assertEquals("Only the clipboard key should carry the emoji popup", 1, emojiPopupKeys.size)
        assertEquals(Action.CLIPBOARD, emojiPopupKeys.single().inputData.value)
    }

    @Test
    fun testCustomRowBottomKeepsWidthAndPopupIntegrity() {
        // The IME feeds Wijesekara's symbols row into Symbols; make sure that path
        // (used at runtime) still holds the width invariant.
        for (showClipboard in listOf(false, true)) {
            val wijesekara = Wijesekara(showEmojiKey = true, showClipboardKey = showClipboard)
            val symbols = Symbols(showEmojiKey = true, showClipboardKey = showClipboard)
            symbols.setCustomRowBottom(wijesekara.wijesekaraSymbolsRowBottom)
            assertRowWidthEquals10(
                "Symbols custom row (clipboard=$showClipboard)",
                symbols.lower[5]!!
            )
        }
    }
}
