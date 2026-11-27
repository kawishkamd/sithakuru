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

/**
 * Pure typing-ergonomics helpers (auto-capitalization and double-space period),
 * free of Android framework dependencies for unit testing.
 */
object TypingErgonomics {
    const val DOUBLE_SPACE_THRESHOLD_MS: Long = 500L

    // Mirror android.text.InputType constants so this stays pure Kotlin.
    const val TYPE_MASK_CLASS = 0x0000000f
    const val TYPE_CLASS_TEXT = 0x00000001
    const val TYPE_MASK_VARIATION = 0x00000ff0
    const val TYPE_TEXT_VARIATION_URI = 0x00000010
    const val TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 0x00000020
    const val TYPE_TEXT_VARIATION_PASSWORD = 0x00000080
    const val TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 0x00000090
    const val TYPE_TEXT_VARIATION_FILTER = 0x000000b0
    const val TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS = 0x000000d0
    const val TYPE_TEXT_VARIATION_WEB_PASSWORD = 0x000000e0
    const val TYPE_TEXT_FLAG_CAP_CHARACTERS = 0x00001000
    const val TYPE_TEXT_FLAG_CAP_WORDS = 0x00002000
    const val TYPE_TEXT_FLAG_CAP_SENTENCES = 0x00004000

    /**
     * Returns true when English shift should engage automatically based on
     * [textBeforeCursor] and the editor's [inputType] flags.
     */
    fun shouldAutoCapitalize(textBeforeCursor: CharSequence, inputType: Int): Boolean {
        if ((inputType and TYPE_MASK_CLASS) != TYPE_CLASS_TEXT) return false
        val variation = inputType and TYPE_MASK_VARIATION
        if (variation == TYPE_TEXT_VARIATION_PASSWORD ||
            variation == TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == TYPE_TEXT_VARIATION_URI ||
            variation == TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
            variation == TYPE_TEXT_VARIATION_FILTER
        ) {
            return false
        }

        if ((inputType and TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0) return true

        if ((inputType and TYPE_TEXT_FLAG_CAP_WORDS) != 0) {
            return textBeforeCursor.isEmpty() || textBeforeCursor.last().isWhitespace()
        }

        if ((inputType and TYPE_TEXT_FLAG_CAP_SENTENCES) != 0) {
            if (textBeforeCursor.isEmpty()) return true
            if (textBeforeCursor.last() == '\n') return true
            if (textBeforeCursor.last() != ' ' && textBeforeCursor.last() != '\t') return false

            var idx = textBeforeCursor.length - 1
            while (idx >= 0 && (textBeforeCursor[idx] == ' ' || textBeforeCursor[idx] == '\t')) {
                idx--
            }
            if (idx < 0) return true
            // Skip optional closing quotes/parentheses before checking sentence punctuation.
            while (idx >= 0 && textBeforeCursor[idx] in "\"')]\u201D\u2019") {
                idx--
            }
            return idx >= 0 && textBeforeCursor[idx] in ".!?"
        }

        return false
    }

    /**
     * Returns true when a second space pressed [elapsedMillis] after the first
     * should replace the trailing space with `". "`.
     */
    fun shouldConvertDoubleSpaceToPeriod(
        textBeforeCursor: CharSequence,
        elapsedMillis: Long,
        thresholdMillis: Long = DOUBLE_SPACE_THRESHOLD_MS
    ): Boolean {
        if (elapsedMillis !in 0..thresholdMillis) return false
        if (textBeforeCursor.length < 2) return false
        if (textBeforeCursor[textBeforeCursor.length - 1] != ' ') return false
        val beforeSpace = textBeforeCursor[textBeforeCursor.length - 2]
        return beforeSpace.isLetterOrDigit()
    }
}
