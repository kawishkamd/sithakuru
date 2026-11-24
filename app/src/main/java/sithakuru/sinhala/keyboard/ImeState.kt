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

import android.content.SharedPreferences
import sithakuru.sinhala.keyboard.data.KeyboardLayoutId

/**
 * State that only the input method writes: clipboard history, recent emoji
 * and the last used layout.
 *
 * The settings screen and the input method run in separate processes, and
 * SharedPreferences keeps a private in-memory copy of a file per process.
 * Whichever process writes last rewrites the whole file from its own copy, so
 * when both wrote to one file the settings screen could silently roll back
 * clips the keyboard had saved (and the keyboard could roll back settings).
 * Keeping this state in a file the settings screen never opens means each
 * file has exactly one writer.
 */
class ImeState(private val state: SharedPreferences) {

    var clipboardHistory: String
        get() = state.getString(KEY_CLIPBOARD_HISTORY, "") ?: ""
        set(value) = state.edit().putString(KEY_CLIPBOARD_HISTORY, value).apply()

    var recentEmojis: List<String>
        get() {
            val raw = state.getString(KEY_RECENT_EMOJIS, "") ?: ""
            return if (raw.isBlank()) emptyList() else raw.split("\n")
        }
        set(value) = state.edit().putString(KEY_RECENT_EMOJIS, value.joinToString("\n")).apply()

    fun addRecentEmoji(emoji: String) {
        val list = recentEmojis.toMutableList()
        list.remove(emoji)
        list.add(0, emoji)
        recentEmojis = if (list.size > MAX_RECENT_EMOJIS) list.take(MAX_RECENT_EMOJIS) else list
    }

    var currentLayout: Int
        get() = state.getInt(KEY_CURRENT_LAYOUT, KeyboardLayoutId.WIJESEKARA)
        set(value) = state.edit().putInt(KEY_CURRENT_LAYOUT, value).apply()

    companion object {
        const val FILE_NAME = "sithakuru_ime_state"

        // The same key names are used in both files, so migrating is a copy.
        internal const val KEY_CLIPBOARD_HISTORY = "CLIPBOARD_HISTORY"
        internal const val KEY_RECENT_EMOJIS = "RECENT_EMOJIS"
        internal const val KEY_CURRENT_LAYOUT = "CURRENT_LAYOUT"
        internal const val KEY_MIGRATED = "MIGRATED_FROM_SETTINGS"

        internal val LEGACY_KEYS = listOf(KEY_CLIPBOARD_HISTORY, KEY_RECENT_EMOJIS, KEY_CURRENT_LAYOUT)

        const val MAX_RECENT_EMOJIS = 30

        /**
         * Moves state that older versions kept in the settings file into [state].
         *
         * The copy happens once. The legacy keys are removed from [settings]
         * every time they turn up, because a settings screen that loaded the
         * file before the migration still holds them in memory and writes them
         * back with its next change; those copies are stale and are not read.
         */
        fun migrate(settings: SharedPreferences, state: SharedPreferences) {
            if (!state.getBoolean(KEY_MIGRATED, false)) {
                val editor = state.edit()
                settings.getString(KEY_CLIPBOARD_HISTORY, null)?.let { editor.putString(KEY_CLIPBOARD_HISTORY, it) }
                settings.getString(KEY_RECENT_EMOJIS, null)?.let { editor.putString(KEY_RECENT_EMOJIS, it) }
                if (settings.contains(KEY_CURRENT_LAYOUT)) {
                    editor.putInt(KEY_CURRENT_LAYOUT, settings.getInt(KEY_CURRENT_LAYOUT, KeyboardLayoutId.WIJESEKARA))
                }
                editor.putBoolean(KEY_MIGRATED, true)
                // commit, not apply: the legacy copies are deleted next, so the
                // new file must be on disk first.
                editor.commit()
            }
            if (LEGACY_KEYS.any { settings.contains(it) }) {
                val editor = settings.edit()
                LEGACY_KEYS.forEach { editor.remove(it) }
                editor.commit()
            }
        }
    }
}
