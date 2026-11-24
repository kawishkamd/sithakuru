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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sithakuru.sinhala.keyboard.data.KeyboardLayoutId

class ImeStateTest {

    private lateinit var settings: FakeSharedPreferences
    private lateinit var state: FakeSharedPreferences

    @Before
    fun setUp() {
        settings = FakeSharedPreferences()
        state = FakeSharedPreferences()
    }

    @Test
    fun migrationMovesLegacyStateOutOfSettings() {
        settings.values["CLIPBOARD_HISTORY"] = "id\t1\t0\tclip"
        settings.values["RECENT_EMOJIS"] = "😀\n🎉"
        settings.values["CURRENT_LAYOUT"] = KeyboardLayoutId.SINGLISH
        settings.values["DARK_THEME"] = false

        ImeState.migrate(settings, state)
        val ime = ImeState(state)

        assertEquals("id\t1\t0\tclip", ime.clipboardHistory)
        assertEquals(listOf("😀", "🎉"), ime.recentEmojis)
        assertEquals(KeyboardLayoutId.SINGLISH, ime.currentLayout)
        ImeState.LEGACY_KEYS.forEach { assertFalse("$it left in settings", settings.contains(it)) }
        // Real settings are untouched.
        assertEquals(false, settings.values["DARK_THEME"])
    }

    @Test
    fun migrationWithNothingToMoveKeepsDefaults() {
        ImeState.migrate(settings, state)
        val ime = ImeState(state)

        assertEquals("", ime.clipboardHistory)
        assertTrue(ime.recentEmojis.isEmpty())
        assertEquals(KeyboardLayoutId.WIJESEKARA, ime.currentLayout)
    }

    @Test
    fun staleLegacyCopyWrittenBackLaterIsDiscardedNotRestored() {
        settings.values["CLIPBOARD_HISTORY"] = "old"
        ImeState.migrate(settings, state)
        val ime = ImeState(state)
        ime.clipboardHistory = "new"

        // A settings screen that loaded the file before the migration writes
        // its stale in-memory copy back with its next change.
        settings.values["CLIPBOARD_HISTORY"] = "old"
        settings.values["DARK_THEME"] = true

        ImeState.migrate(settings, state)

        assertEquals("new", ImeState(state).clipboardHistory)
        assertFalse(settings.contains("CLIPBOARD_HISTORY"))
        assertEquals(true, settings.values["DARK_THEME"])
    }

    @Test
    fun keyboardStateWritesNeverTouchTheSettingsFile() {
        ImeState.migrate(settings, state)
        settings.values["DARK_THEME"] = true
        val before = settings.all

        val ime = ImeState(state)
        ime.clipboardHistory = "clip"
        ime.addRecentEmoji("😀")
        ime.currentLayout = KeyboardLayoutId.ENGLISH

        assertEquals(before, settings.all)
    }

    @Test
    fun recentEmojisMoveToFrontAndAreCapped() {
        val ime = ImeState(state)
        for (i in 0 until ImeState.MAX_RECENT_EMOJIS + 5) ime.addRecentEmoji("e$i")
        ime.addRecentEmoji("e10")

        val recents = ime.recentEmojis
        assertEquals(ImeState.MAX_RECENT_EMOJIS, recents.size)
        assertEquals("e10", recents.first())
        assertEquals(1, recents.count { it == "e10" })
    }
}
