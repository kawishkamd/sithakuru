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

package sithakuru.sinhala.keyboard.data

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(Action.ACTION, Action.BACKSPACE, Action.EMOJI, Action.IME, Action.LANG, Action.SHIFT, Action.SYMBOLS)
annotation class Action {
    companion object {
        const val ACTION: Int = -1
        const val BACKSPACE: Int = -2
        const val EMOJI: Int = -3
        const val IME: Int = -4
        const val LANG: Int = -5
        const val SHIFT: Int = -6
        const val SYMBOLS: Int = -7
    }
}
