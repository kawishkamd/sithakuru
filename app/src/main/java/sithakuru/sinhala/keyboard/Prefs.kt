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

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import sithakuru.sinhala.keyboard.clipboard.ClipboardSettings

class Prefs(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("sithakuru_preferences", Context.MODE_PRIVATE)

    var layoutEnglish: Boolean
        get() = preferences.getBoolean(KEY_ENGLISH, true)
        set(value) = preferences.edit().putBoolean(KEY_ENGLISH, value).apply()
    var layoutWijesekara: Boolean
        get() = preferences.getBoolean(KEY_WIJESEKARA, true)
        set(value) = preferences.edit().putBoolean(KEY_WIJESEKARA, value).apply()
    var layoutSinglish: Boolean
        get() = preferences.getBoolean(KEY_SINGLISH, false)
        set(value) = preferences.edit().putBoolean(KEY_SINGLISH, value).apply()
    var englishSuggestions: Boolean
        get() = preferences.getBoolean(KEY_ENGLISH_SUGGESTIONS, DEFAULT_ENGLISH_SUGGESTIONS)
        set(value) = preferences.edit().putBoolean(KEY_ENGLISH_SUGGESTIONS, value).apply()
    var darkTheme: Boolean
        get() = preferences.getBoolean(KEY_DARK_THEME, DEFAULT_DARK_THEME)
        set(value) = preferences.edit().putBoolean(KEY_DARK_THEME, value).apply()
    var keyBorders: Boolean
        get() = preferences.getBoolean(KEY_KEY_BORDERS, DEFAULT_KEY_BORDERS)
        set(value) = preferences.edit().putBoolean(KEY_KEY_BORDERS, value).apply()
    var longPressKeysLabels: Boolean
        get() = preferences.getBoolean(KEY_LONG_PRESS_KEYS_LABELS, DEFAULT_LONG_PRESS_KEYS_LABELS)
        set(value) = preferences.edit().putBoolean(KEY_LONG_PRESS_KEYS_LABELS, value).apply()
    var heightPercentage: Int
        get() = preferences.getInt(KEY_HEIGHT_PERCENTAGE, DEFAULT_HEIGHT_PERCENTAGE)
        set(value) = preferences.edit().putInt(KEY_HEIGHT_PERCENTAGE, value).apply()
    var textSize: Int
        get() = preferences.getInt(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
        set(value) = preferences.edit().putInt(KEY_TEXT_SIZE, value).apply()
    var colorBackground: Int
        get() = preferences.getInt(KEY_COLOR_BACKGROUND, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_BACKGROUND, value).apply()
    var colorPressedKeyBackground: Int
        get() = preferences.getInt(KEY_COLOR_PRESSED_KEY_BACKGROUND, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_PRESSED_KEY_BACKGROUND, value).apply()
    var colorLongPressedKeyBackground: Int
        get() = preferences.getInt(KEY_COLOR_LONG_PRESSED_KEY_BACKGROUND, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_LONG_PRESSED_KEY_BACKGROUND, value).apply()
    var colorKeyBorder: Int
        get() = preferences.getInt(KEY_COLOR_KEY_BORDER, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_KEY_BORDER, value).apply()
    var colorText: Int
        get() = preferences.getInt(KEY_COLOR_TEXT, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_TEXT, value).apply()
    var colorTextDim: Int
        get() = preferences.getInt(KEY_COLOR_TEXT_DIM, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_TEXT_DIM, value).apply()
    var colorTextLongPress: Int
        get() = preferences.getInt(KEY_COLOR_TEXT_LONG_PRESS, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_TEXT_LONG_PRESS, value).apply()
    var colorCustomBackground: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_BACKGROUND, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_BACKGROUND, value).apply()
    var colorCustomPressedKeyBackground: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_PRESSED_KEY_BACKGROUND, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_PRESSED_KEY_BACKGROUND, value).apply()
    var colorCustomLongPressedKeyBackground: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_LONG_PRESSED_KEY_BACKGROUND, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_LONG_PRESSED_KEY_BACKGROUND, value).apply()
    var colorCustomKeyBorder: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_KEY_BORDER, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_KEY_BORDER, value).apply()
    var colorCustomText: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_TEXT, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_TEXT, value).apply()
    var colorCustomTextDim: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_TEXT_DIM, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_TEXT_DIM, value).apply()
    var colorCustomTextLongPress: Int
        get() = preferences.getInt(KEY_COLOR_CUSTOM_TEXT_LONG_PRESS, 0)
        set(value) = preferences.edit().putInt(KEY_COLOR_CUSTOM_TEXT_LONG_PRESS, value).apply()
    var longPressKeys: Boolean
        get() = preferences.getBoolean(KEY_LONG_PRESS_KEYS, DEFAULT_LONG_PRESS_KEYS)
        set(value) = preferences.edit().putBoolean(KEY_LONG_PRESS_KEYS, value).apply()
    var swipeToErase: Boolean
        get() = preferences.getBoolean(KEY_SWIPE_TO_ERASE, DEFAULT_SWIPE_TO_ERASE)
        set(value) = preferences.edit().putBoolean(KEY_SWIPE_TO_ERASE, value).apply()
    var swipeToMoveCursor: Boolean
        get() = preferences.getBoolean(KEY_SWIPE_TO_MOVE_CURSOR, DEFAULT_SWIPE_TO_MOVE_CURSOR)
        set(value) = preferences.edit().putBoolean(KEY_SWIPE_TO_MOVE_CURSOR, value).apply()

    var emojiKey: Boolean
        get() = preferences.getBoolean(KEY_EMOJI_KEY, DEFAULT_EMOJI_KEY)
        set(value) = preferences.edit().putBoolean(KEY_EMOJI_KEY, value).apply()

    var clipboardKey: Boolean
        get() = preferences.getBoolean(KEY_CLIPBOARD_KEY, DEFAULT_CLIPBOARD_KEY)
        set(value) = preferences.edit().putBoolean(KEY_CLIPBOARD_KEY, value).apply()

    var clipboardHistoryEnabled: Boolean
        get() = preferences.getBoolean(KEY_CLIPBOARD_HISTORY_ENABLED, DEFAULT_CLIPBOARD_HISTORY_ENABLED)
        set(value) = preferences.edit().putBoolean(KEY_CLIPBOARD_HISTORY_ENABLED, value).apply()

    /** How long unpinned clips are kept; [CLIPBOARD_EXPIRY_NEVER] keeps them until pushed out. */
    var clipboardExpiryMillis: Long
        get() = preferences.getLong(KEY_CLIPBOARD_EXPIRY, DEFAULT_CLIPBOARD_EXPIRY)
        set(value) = preferences.edit().putLong(KEY_CLIPBOARD_EXPIRY, value).apply()

    /**
     * How many times "clear all" was confirmed in settings. The settings
     * screen cannot write the keyboard's state file, so it counts requests and
     * the keyboard clears everything whenever the count differs from the one
     * it handled last (see ClipboardPolicy).
     */
    var clipboardClearRequests: Int
        get() = preferences.getInt(KEY_CLIPBOARD_CLEAR_REQUESTS, 0)
        set(value) = preferences.edit().putInt(KEY_CLIPBOARD_CLEAR_REQUESTS, value).apply()

    val clipboardSettings: ClipboardSettings
        get() = ClipboardSettings(
            enabled = clipboardHistoryEnabled,
            expiryMillis = clipboardExpiryMillis,
            clearRequests = clipboardClearRequests
        )

    /** The [clipboardClearRequests] value the keyboard last handled. */
    var appliedClipboardClearRequests: Int
        get() = imeState.appliedClipboardClearRequests
        set(value) {
            imeState.appliedClipboardClearRequests = value
        }

    /*
     * Keyboard-only state lives in its own file (see ImeState). It is opened
     * lazily, so the settings screen, which never touches these, never loads it.
     */
    private val imeState: ImeState by lazy {
        val state = context.getSharedPreferences(ImeState.FILE_NAME, Context.MODE_PRIVATE)
        ImeState.migrate(preferences, state)
        ImeState(state)
    }

    var clipboardHistory: String
        get() = imeState.clipboardHistory
        set(value) {
            imeState.clipboardHistory = value
        }

    val recentEmojis: List<String>
        get() = imeState.recentEmojis

    fun addRecentEmoji(emoji: String) = imeState.addRecentEmoji(emoji)

    var currentLayout: Int
        get() = imeState.currentLayout
        set(value) {
            imeState.currentLayout = value
        }

    private var colorsMigrated: Boolean
        get() = preferences.getBoolean(KEY_COLORS_MIGRATED, false)
        set(value) = preferences.edit().putBoolean(KEY_COLORS_MIGRATED, value).apply()

    init {
        fun Context.getColorInt(@ColorRes id: Int) = ResourcesCompat.getColor(resources, id, null)
        if (!colorsMigrated) {
            if (darkTheme) {
                colorBackground = context.getColorInt(R.color.night_1)
                colorPressedKeyBackground = context.getColorInt(R.color.night_2)
                colorLongPressedKeyBackground = context.getColorInt(R.color.yellow)
                colorKeyBorder = context.getColorInt(R.color.night_2)
                colorText = context.getColorInt(R.color.light_1)
                colorTextDim = context.getColorInt(R.color.night_2)
                colorTextLongPress = context.getColorInt(R.color.light_1)
            } else {
                colorBackground = context.getColorInt(R.color.light_1)
                colorPressedKeyBackground = context.getColorInt(R.color.light_2)
                colorLongPressedKeyBackground = context.getColorInt(R.color.blue)
                colorKeyBorder = context.getColorInt(R.color.light_2)
                colorText = context.getColorInt(R.color.night_1)
                colorTextDim = context.getColorInt(R.color.light_2)
                colorTextLongPress = context.getColorInt(R.color.night_1)
            }

            colorCustomBackground = colorBackground
            colorCustomPressedKeyBackground = colorPressedKeyBackground
            colorCustomLongPressedKeyBackground = colorLongPressedKeyBackground
            colorCustomKeyBorder = colorKeyBorder
            colorCustomText = colorText
            colorCustomTextDim = colorTextDim
            colorCustomTextLongPress = colorTextLongPress

            colorsMigrated = true
        }
    }

    companion object {
        private const val KEY_ENGLISH = "LAYOUT_ENGLISH"
        private const val KEY_WIJESEKARA = "LAYOUT_WIJESEKARA"
        private const val KEY_SINGLISH = "LAYOUT_SINGLISH"
        private const val KEY_ENGLISH_SUGGESTIONS = "ENGLISH_SUGGESTIONS"
        private const val KEY_DARK_THEME = "DARK_THEME"
        private const val KEY_KEY_BORDERS = "KEY_BORDERS"
        private const val KEY_LONG_PRESS_KEYS_LABELS = "LONG_PRESS_KEYS_LABELS"
        private const val KEY_HEIGHT_PERCENTAGE = "HEIGHT_PERCENTAGE"
        private const val KEY_TEXT_SIZE = "TEXT_SIZE"

        private const val KEY_COLORS_MIGRATED = "COLORS_MIGRATED"
        private const val KEY_COLOR_BACKGROUND = "COLOR_BACKGROUND"
        private const val KEY_COLOR_PRESSED_KEY_BACKGROUND = "COLOR_PRESSED_KEY_BACKGROUND"
        private const val KEY_COLOR_LONG_PRESSED_KEY_BACKGROUND = "COLOR_LONG_PRESSED_KEY_BACKGROUND"
        private const val KEY_COLOR_KEY_BORDER = "COLOR_KEY_BORDER"
        private const val KEY_COLOR_TEXT = "COLOR_TEXT"
        private const val KEY_COLOR_TEXT_DIM = "COLOR_TEXT_DIM"
        private const val KEY_COLOR_TEXT_LONG_PRESS = "COLOR_TEXT_LONG_PRESS"

        private const val KEY_COLOR_CUSTOM_BACKGROUND = "COLOR_CUSTOM_BACKGROUND"
        private const val KEY_COLOR_CUSTOM_PRESSED_KEY_BACKGROUND = "COLOR_CUSTOM_PRESSED_KEY_BACKGROUND"
        private const val KEY_COLOR_CUSTOM_LONG_PRESSED_KEY_BACKGROUND = "COLOR_CUSTOM_LONG_PRESSED_KEY_BACKGROUND"
        private const val KEY_COLOR_CUSTOM_KEY_BORDER = "COLOR_CUSTOM_KEY_BORDER"
        private const val KEY_COLOR_CUSTOM_TEXT = "COLOR_CUSTOM_TEXT"
        private const val KEY_COLOR_CUSTOM_TEXT_DIM = "COLOR_CUSTOM_TEXT_DIM"
        private const val KEY_COLOR_CUSTOM_TEXT_LONG_PRESS = "COLOR_CUSTOM_TEXT_LONG_PRESS"

        private const val KEY_LONG_PRESS_KEYS = "LONG_PRESS_KEYS"
        private const val KEY_SWIPE_TO_ERASE = "SWIPE_TO_ERASE"
        private const val KEY_SWIPE_TO_MOVE_CURSOR = "SWIPE_TO_MOVE_CURSOR"
        private const val KEY_EMOJI_KEY = "EMOJI_KEY"
        private const val KEY_CLIPBOARD_KEY = "CLIPBOARD_KEY"
        private const val KEY_CLIPBOARD_HISTORY_ENABLED = "CLIPBOARD_HISTORY_ENABLED"
        private const val KEY_CLIPBOARD_EXPIRY = "CLIPBOARD_EXPIRY"
        private const val KEY_CLIPBOARD_CLEAR_REQUESTS = "CLIPBOARD_CLEAR_REQUESTS"

        const val DEFAULT_DARK_THEME = true
        const val DEFAULT_KEY_BORDERS = true
        const val DEFAULT_LONG_PRESS_KEYS_LABELS = true
        const val DEFAULT_HEIGHT_PERCENTAGE = 100
        const val DEFAULT_TEXT_SIZE = 20
        const val DEFAULT_LONG_PRESS_KEYS = false
        const val DEFAULT_SWIPE_TO_ERASE = true
        const val DEFAULT_SWIPE_TO_MOVE_CURSOR = true
        const val DEFAULT_EMOJI_KEY = true
        const val DEFAULT_CLIPBOARD_KEY = false
        const val DEFAULT_ENGLISH_SUGGESTIONS = true
        const val DEFAULT_CLIPBOARD_HISTORY_ENABLED = true

        const val CLIPBOARD_EXPIRY_NEVER = 0L
        const val CLIPBOARD_EXPIRY_HOUR = 60L * 60 * 1000
        const val CLIPBOARD_EXPIRY_DAY = 24 * CLIPBOARD_EXPIRY_HOUR
        const val DEFAULT_CLIPBOARD_EXPIRY = CLIPBOARD_EXPIRY_DAY
    }
}
