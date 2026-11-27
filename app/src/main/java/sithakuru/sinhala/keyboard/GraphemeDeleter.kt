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
 * Computes how many UTF-16 code units a single backspace should remove from the
 * end of [textBeforeCursor].
 *
 * - Multi-codepoint emojis (surrogate pairs, variation selectors, skin-tone
 *   modifiers, ZWJ sequences, keycaps, and regional-indicator flags) are
 *   deleted as a whole cluster, consistently across Android API 21+.
 * - Sinhala vowel signs are deleted one code unit at a time (so backspacing
 *   `කා` leaves `ක`), while a preceding ZWJ in a conjunct (`ක්‍ය`, `ක්‍ර`) is
 *   removed alongside its trailing consonant.
 */
object GraphemeDeleter {
    /** Maximum UTF-16 units to inspect before the cursor (covers longest ZWJ/flag emojis). */
    const val LOOKBACK_CHARS: Int = 32

    private const val ZWJ = '\u200D'
    private const val VS15 = '\uFE0E'
    private const val VS16 = '\uFE0F'
    private const val COMBINING_KEYCAP = '\u20E3'
    private const val CANCEL_TAG = 0xE007F
    private const val TAG_START = 0xE0020
    private const val TAG_END = 0xE007E
    private const val REGIONAL_START = 0x1F1E6
    private const val REGIONAL_END = 0x1F1FF
    private const val SKIN_TONE_START = 0x1F3FB
    private const val SKIN_TONE_END = 0x1F3FF
    private const val SINHALA_START = 0x0D80
    private const val SINHALA_END = 0x0DFF

    fun deleteLength(textBeforeCursor: CharSequence): Int {
        val len = textBeforeCursor.length
        if (len == 0) return 0

        val lastChar = textBeforeCursor[len - 1]

        // Keycap sequence: [0-9#*] + optional FE0F + 20E3
        if (lastChar == COMBINING_KEYCAP) {
            var pos = len - 1
            if (pos > 0 && textBeforeCursor[pos - 1] == VS16) pos--
            if (pos > 0 && isKeycapBase(textBeforeCursor[pos - 1])) pos--
            return len - pos
        }

        val lastCp = Character.codePointBefore(textBeforeCursor, len)

        // Subdivision flag tag sequence: black flag + tag chars + cancel tag
        if (lastCp == CANCEL_TAG) {
            var pos = len - Character.charCount(lastCp)
            while (pos > 0) {
                val cp = Character.codePointBefore(textBeforeCursor, pos)
                if (cp in TAG_START..TAG_END) {
                    pos -= Character.charCount(cp)
                } else {
                    break
                }
            }
            if (pos > 0) {
                val baseCp = Character.codePointBefore(textBeforeCursor, pos)
                pos -= Character.charCount(baseCp)
            }
            return len - pos
        }

        // Regional indicator flags (UAX #29 GB12/GB13): pair indicators from the left of the run.
        if (lastCp in REGIONAL_START..REGIONAL_END) {
            var runStart = len
            var count = 0
            while (runStart > 0) {
                val cp = Character.codePointBefore(textBeforeCursor, runStart)
                if (cp in REGIONAL_START..REGIONAL_END) {
                    runStart -= Character.charCount(cp)
                    count++
                } else {
                    break
                }
            }
            // Even run -> last 2 indicators form a flag (4 UTF-16 units); odd run -> 1 trailing indicator (2 units).
            return if (count >= 2 && count % 2 == 0) 4 else 2
        }

        // Surrogate pair or variation selector -> emoji / ZWJ sequence cluster.
        if (Character.isLowSurrogate(lastChar) || lastChar == VS16 || lastChar == VS15) {
            val pos = consumeEmojiZwjChain(textBeforeCursor, consumeEmojiUnit(textBeforeCursor, len))
            return len - pos
        }

        // Ordinary BMP character (including Sinhala letters/pili and BMP ZWJ tails like 🐈‍⬛):
        // if preceded by a non-Sinhala ZWJ (e.g. 🐈‍⬛), delete the full emoji ZWJ cluster;
        // if preceded by a Sinhala ZWJ (e.g. ක්‍ය, ක්‍ර), delete the trailing char + ZWJ only.
        var pos = len - 1
        if (pos > 1 && textBeforeCursor[pos - 1] == ZWJ) {
            val beforeZwjCp = Character.codePointBefore(textBeforeCursor, pos - 1)
            if (beforeZwjCp !in SINHALA_START..SINHALA_END) {
                pos = consumeEmojiZwjChain(textBeforeCursor, pos)
                return len - pos
            }
        }
        while (pos > 0 && textBeforeCursor[pos - 1] == ZWJ) {
            pos--
        }
        return len - pos
    }

    private fun consumeEmojiZwjChain(text: CharSequence, startPos: Int): Int {
        var pos = startPos
        while (pos > 1 && text[pos - 1] == ZWJ) {
            val beforeZwjCp = Character.codePointBefore(text, pos - 1)
            if (beforeZwjCp in SINHALA_START..SINHALA_END) break
            pos = consumeEmojiUnit(text, pos - 1)
        }
        return pos
    }

    /** Consumes one emoji unit (optional VS15/16, optional skin tone, and one base code point). */
    private fun consumeEmojiUnit(text: CharSequence, end: Int): Int {
        var pos = end
        if (pos <= 0) return 0

        if (text[pos - 1] == VS16 || text[pos - 1] == VS15) {
            pos--
        }
        if (pos > 0) {
            val cp = Character.codePointBefore(text, pos)
            if (cp in SKIN_TONE_START..SKIN_TONE_END) {
                pos -= Character.charCount(cp)
                if (pos > 0 && (text[pos - 1] == VS16 || text[pos - 1] == VS15)) {
                    pos--
                }
            }
        }
        if (pos > 0) {
            val baseCp = Character.codePointBefore(text, pos)
            pos -= Character.charCount(baseCp)
        }
        return pos
    }

    private fun isKeycapBase(ch: Char): Boolean = ch in '0'..'9' || ch == '#' || ch == '*'
}
