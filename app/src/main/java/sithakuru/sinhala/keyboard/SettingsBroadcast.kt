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
import android.content.Intent

/**
 * Snapshot of user-configurable preferences transferred from [MainActivity] to
 * the running [InputMethodService] process.
 *
 * Because the IME runs in its own process and SharedPreferences caches files
 * per process without cross-process invalidation, carrying the updated values
 * in the broadcast lets the IME reload live (including inside MainActivity's
 * test input box) without killing the IME process or writing to the settings
 * file from the IME process.
 */
data class SettingsSnapshot(
    val layoutEnglish: Boolean,
    val layoutWijesekara: Boolean,
    val layoutSinglish: Boolean,
    val englishSuggestions: Boolean,
    val darkTheme: Boolean,
    val keyBorders: Boolean,
    val longPressKeysLabels: Boolean,
    val heightPercentage: Int,
    val textSize: Int,
    val colorBackground: Int,
    val colorPressedKeyBackground: Int,
    val colorLongPressedKeyBackground: Int,
    val colorKeyBorder: Int,
    val colorText: Int,
    val colorTextDim: Int,
    val colorTextLongPress: Int,
    val longPressKeys: Boolean,
    val swipeToErase: Boolean,
    val swipeToMoveCursor: Boolean,
    val hapticFeedback: Boolean,
    val keyPopup: Boolean,
    val autoCapitalize: Boolean,
    val doubleSpacePeriod: Boolean,
    val emojiKey: Boolean,
    val clipboardKey: Boolean,
    val clipboardHistoryEnabled: Boolean,
    val clipboardExpiryMillis: Long,
    val clipboardClearRequests: Int
)

object SettingsBroadcast {
    const val ACTION = "sithakuru.sinhala.keyboard.action.SETTINGS_CHANGED"

    private const val EXTRA_LAYOUT_ENGLISH = "layout_english"
    private const val EXTRA_LAYOUT_WIJESEKARA = "layout_wijesekara"
    private const val EXTRA_LAYOUT_SINGLISH = "layout_singlish"
    private const val EXTRA_ENGLISH_SUGGESTIONS = "english_suggestions"
    private const val EXTRA_DARK_THEME = "dark_theme"
    private const val EXTRA_KEY_BORDERS = "key_borders"
    private const val EXTRA_LONG_PRESS_KEYS_LABELS = "long_press_keys_labels"
    private const val EXTRA_HEIGHT_PERCENTAGE = "height_percentage"
    private const val EXTRA_TEXT_SIZE = "text_size"
    private const val EXTRA_COLOR_BACKGROUND = "color_background"
    private const val EXTRA_COLOR_PRESSED_KEY_BACKGROUND = "color_pressed_key_background"
    private const val EXTRA_COLOR_LONG_PRESSED_KEY_BACKGROUND = "color_long_pressed_key_background"
    private const val EXTRA_COLOR_KEY_BORDER = "color_key_border"
    private const val EXTRA_COLOR_TEXT = "color_text"
    private const val EXTRA_COLOR_TEXT_DIM = "color_text_dim"
    private const val EXTRA_COLOR_TEXT_LONG_PRESS = "color_text_long_press"
    private const val EXTRA_LONG_PRESS_KEYS = "long_press_keys"
    private const val EXTRA_SWIPE_TO_ERASE = "swipe_to_erase"
    private const val EXTRA_SWIPE_TO_MOVE_CURSOR = "swipe_to_move_cursor"
    private const val EXTRA_HAPTIC_FEEDBACK = "haptic_feedback"
    private const val EXTRA_KEY_POPUP = "key_popup"
    private const val EXTRA_AUTO_CAPITALIZE = "auto_capitalize"
    private const val EXTRA_DOUBLE_SPACE_PERIOD = "double_space_period"
    private const val EXTRA_EMOJI_KEY = "emoji_key"
    private const val EXTRA_CLIPBOARD_KEY = "clipboard_key"
    private const val EXTRA_CLIPBOARD_HISTORY_ENABLED = "clipboard_history_enabled"
    private const val EXTRA_CLIPBOARD_EXPIRY_MILLIS = "clipboard_expiry_millis"
    private const val EXTRA_CLIPBOARD_CLEAR_REQUESTS = "clipboard_clear_requests"

    fun send(context: Context, snapshot: SettingsSnapshot) {
        context.sendBroadcast(
            Intent(ACTION)
                .setPackage(context.packageName)
                .putExtra(EXTRA_LAYOUT_ENGLISH, snapshot.layoutEnglish)
                .putExtra(EXTRA_LAYOUT_WIJESEKARA, snapshot.layoutWijesekara)
                .putExtra(EXTRA_LAYOUT_SINGLISH, snapshot.layoutSinglish)
                .putExtra(EXTRA_ENGLISH_SUGGESTIONS, snapshot.englishSuggestions)
                .putExtra(EXTRA_DARK_THEME, snapshot.darkTheme)
                .putExtra(EXTRA_KEY_BORDERS, snapshot.keyBorders)
                .putExtra(EXTRA_LONG_PRESS_KEYS_LABELS, snapshot.longPressKeysLabels)
                .putExtra(EXTRA_HEIGHT_PERCENTAGE, snapshot.heightPercentage)
                .putExtra(EXTRA_TEXT_SIZE, snapshot.textSize)
                .putExtra(EXTRA_COLOR_BACKGROUND, snapshot.colorBackground)
                .putExtra(EXTRA_COLOR_PRESSED_KEY_BACKGROUND, snapshot.colorPressedKeyBackground)
                .putExtra(EXTRA_COLOR_LONG_PRESSED_KEY_BACKGROUND, snapshot.colorLongPressedKeyBackground)
                .putExtra(EXTRA_COLOR_KEY_BORDER, snapshot.colorKeyBorder)
                .putExtra(EXTRA_COLOR_TEXT, snapshot.colorText)
                .putExtra(EXTRA_COLOR_TEXT_DIM, snapshot.colorTextDim)
                .putExtra(EXTRA_COLOR_TEXT_LONG_PRESS, snapshot.colorTextLongPress)
                .putExtra(EXTRA_LONG_PRESS_KEYS, snapshot.longPressKeys)
                .putExtra(EXTRA_SWIPE_TO_ERASE, snapshot.swipeToErase)
                .putExtra(EXTRA_SWIPE_TO_MOVE_CURSOR, snapshot.swipeToMoveCursor)
                .putExtra(EXTRA_HAPTIC_FEEDBACK, snapshot.hapticFeedback)
                .putExtra(EXTRA_KEY_POPUP, snapshot.keyPopup)
                .putExtra(EXTRA_AUTO_CAPITALIZE, snapshot.autoCapitalize)
                .putExtra(EXTRA_DOUBLE_SPACE_PERIOD, snapshot.doubleSpacePeriod)
                .putExtra(EXTRA_EMOJI_KEY, snapshot.emojiKey)
                .putExtra(EXTRA_CLIPBOARD_KEY, snapshot.clipboardKey)
                .putExtra(EXTRA_CLIPBOARD_HISTORY_ENABLED, snapshot.clipboardHistoryEnabled)
                .putExtra(EXTRA_CLIPBOARD_EXPIRY_MILLIS, snapshot.clipboardExpiryMillis)
                .putExtra(EXTRA_CLIPBOARD_CLEAR_REQUESTS, snapshot.clipboardClearRequests)
        )
    }

    fun read(intent: Intent): SettingsSnapshot? {
        if (intent.action != ACTION) return null
        if (!intent.hasExtra(EXTRA_LAYOUT_ENGLISH) || !intent.hasExtra(EXTRA_COLOR_BACKGROUND)) return null
        return SettingsSnapshot(
            layoutEnglish = intent.getBooleanExtra(EXTRA_LAYOUT_ENGLISH, true),
            layoutWijesekara = intent.getBooleanExtra(EXTRA_LAYOUT_WIJESEKARA, true),
            layoutSinglish = intent.getBooleanExtra(EXTRA_LAYOUT_SINGLISH, false),
            englishSuggestions = intent.getBooleanExtra(EXTRA_ENGLISH_SUGGESTIONS, Prefs.DEFAULT_ENGLISH_SUGGESTIONS),
            darkTheme = intent.getBooleanExtra(EXTRA_DARK_THEME, Prefs.DEFAULT_DARK_THEME),
            keyBorders = intent.getBooleanExtra(EXTRA_KEY_BORDERS, Prefs.DEFAULT_KEY_BORDERS),
            longPressKeysLabels = intent.getBooleanExtra(EXTRA_LONG_PRESS_KEYS_LABELS, Prefs.DEFAULT_LONG_PRESS_KEYS_LABELS),
            heightPercentage = intent.getIntExtra(EXTRA_HEIGHT_PERCENTAGE, Prefs.DEFAULT_HEIGHT_PERCENTAGE),
            textSize = intent.getIntExtra(EXTRA_TEXT_SIZE, Prefs.DEFAULT_TEXT_SIZE),
            colorBackground = intent.getIntExtra(EXTRA_COLOR_BACKGROUND, 0),
            colorPressedKeyBackground = intent.getIntExtra(EXTRA_COLOR_PRESSED_KEY_BACKGROUND, 0),
            colorLongPressedKeyBackground = intent.getIntExtra(EXTRA_COLOR_LONG_PRESSED_KEY_BACKGROUND, 0),
            colorKeyBorder = intent.getIntExtra(EXTRA_COLOR_KEY_BORDER, 0),
            colorText = intent.getIntExtra(EXTRA_COLOR_TEXT, 0),
            colorTextDim = intent.getIntExtra(EXTRA_COLOR_TEXT_DIM, 0),
            colorTextLongPress = intent.getIntExtra(EXTRA_COLOR_TEXT_LONG_PRESS, 0),
            longPressKeys = intent.getBooleanExtra(EXTRA_LONG_PRESS_KEYS, Prefs.DEFAULT_LONG_PRESS_KEYS),
            swipeToErase = intent.getBooleanExtra(EXTRA_SWIPE_TO_ERASE, Prefs.DEFAULT_SWIPE_TO_ERASE),
            swipeToMoveCursor = intent.getBooleanExtra(EXTRA_SWIPE_TO_MOVE_CURSOR, Prefs.DEFAULT_SWIPE_TO_MOVE_CURSOR),
            hapticFeedback = intent.getBooleanExtra(EXTRA_HAPTIC_FEEDBACK, Prefs.DEFAULT_HAPTIC_FEEDBACK),
            keyPopup = intent.getBooleanExtra(EXTRA_KEY_POPUP, Prefs.DEFAULT_KEY_POPUP),
            autoCapitalize = intent.getBooleanExtra(EXTRA_AUTO_CAPITALIZE, Prefs.DEFAULT_AUTO_CAPITALIZE),
            doubleSpacePeriod = intent.getBooleanExtra(EXTRA_DOUBLE_SPACE_PERIOD, Prefs.DEFAULT_DOUBLE_SPACE_PERIOD),
            emojiKey = intent.getBooleanExtra(EXTRA_EMOJI_KEY, Prefs.DEFAULT_EMOJI_KEY),
            clipboardKey = intent.getBooleanExtra(EXTRA_CLIPBOARD_KEY, Prefs.DEFAULT_CLIPBOARD_KEY),
            clipboardHistoryEnabled = intent.getBooleanExtra(EXTRA_CLIPBOARD_HISTORY_ENABLED, Prefs.DEFAULT_CLIPBOARD_HISTORY_ENABLED),
            clipboardExpiryMillis = intent.getLongExtra(EXTRA_CLIPBOARD_EXPIRY_MILLIS, Prefs.DEFAULT_CLIPBOARD_EXPIRY),
            clipboardClearRequests = intent.getIntExtra(EXTRA_CLIPBOARD_CLEAR_REQUESTS, 0)
        )
    }
}
