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
 * Helakuru-compatible phonetic composing, kept free of Android types so it
 * can be unit tested. The raw keys of the word being typed are kept, the
 * whole word is rendered again after every key, and what was written for it
 * is replaced. A key that is not a letter ends the word.
 */
class SinglishComposer(private val render: (String) -> String = SinglishKeymap::render) {

    /** Delete [deleteBefore] UTF-16 units before the cursor, then write [insert]. */
    data class Edit(val deleteBefore: Int, val insert: String)

    /** Raw keys of the word being typed. */
    var keys: String = ""
        private set

    /** UTF-16 units written for [keys]: what the next edit must delete. */
    private var shown: Int = 0

    val isComposing: Boolean get() = keys.isNotEmpty()

    /** Ends the word: the next key starts a new one and leaves this one alone. */
    fun reset() {
        keys = ""
        shown = 0
    }

    fun type(ch: Char): Edit {
        if (!isLetter(ch)) {
            // Word boundary. The word written so far is already correct, so
            // only the separator is new.
            reset()
            return Edit(0, ch.toString())
        }
        keys += ch
        return rerender()
    }

    /**
     * Inside a word the raw keys are the source of truth: drop the last one
     * and render again. Returns null outside a word, where backspace deletes
     * a written character as usual.
     */
    fun backspace(): Edit? {
        if (!isComposing) return null
        keys = keys.dropLast(1)
        return rerender()
    }

    private fun rerender(): Edit {
        val text = render(keys)
        val edit = Edit(shown, text)
        shown = text.length
        return edit
    }

    companion object {
        fun isLetter(ch: Char): Boolean = ch in 'a'..'z' || ch in 'A'..'Z'
    }
}
