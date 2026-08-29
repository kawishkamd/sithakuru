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

package sithakuru.sinhala.keyboard.layouts

import sithakuru.sinhala.keyboard.R
import sithakuru.sinhala.keyboard.data.Action
import sithakuru.sinhala.keyboard.CharCode
import sithakuru.sinhala.keyboard.CharText
import sithakuru.sinhala.keyboard.datatype.KeyData

open class English(indicator: String = "E") : BaseLayout(indicator) {
    override val lower = mutableMapOf(
        1 to rowNumbers,
        2 to listOf(
            KeyData(CharCode.SMALL_LETTER_Q, CharText.SMALL_LETTER_Q),
            KeyData(CharCode.SMALL_LETTER_W, CharText.SMALL_LETTER_W),
            KeyData(CharCode.SMALL_LETTER_E, CharText.SMALL_LETTER_E),
            KeyData(CharCode.SMALL_LETTER_R, CharText.SMALL_LETTER_R),
            KeyData(CharCode.SMALL_LETTER_T, CharText.SMALL_LETTER_T),
            KeyData(CharCode.SMALL_LETTER_Y, CharText.SMALL_LETTER_Y),
            KeyData(CharCode.SMALL_LETTER_U, CharText.SMALL_LETTER_U),
            KeyData(CharCode.SMALL_LETTER_I, CharText.SMALL_LETTER_I),
            KeyData(CharCode.SMALL_LETTER_O, CharText.SMALL_LETTER_O),
            KeyData(CharCode.SMALL_LETTER_P, CharText.SMALL_LETTER_P)
        ),
        3 to listOf(
            KeyData(CharCode.SMALL_LETTER_A, CharText.SMALL_LETTER_A),
            KeyData(CharCode.SMALL_LETTER_S, CharText.SMALL_LETTER_S),
            KeyData(CharCode.SMALL_LETTER_D, CharText.SMALL_LETTER_D),
            KeyData(CharCode.SMALL_LETTER_F, CharText.SMALL_LETTER_F),
            KeyData(CharCode.SMALL_LETTER_G, CharText.SMALL_LETTER_G),
            KeyData(CharCode.SMALL_LETTER_H, CharText.SMALL_LETTER_H),
            KeyData(CharCode.SMALL_LETTER_J, CharText.SMALL_LETTER_J),
            KeyData(CharCode.SMALL_LETTER_K, CharText.SMALL_LETTER_K),
            KeyData(CharCode.SMALL_LETTER_L, CharText.SMALL_LETTER_L)
        ),
        4 to listOf(
            Action.SHIFT.toKeyData(iconRes = R.drawable.ic_shift, widthMultiplier = 1.5F),
            KeyData(CharCode.SMALL_LETTER_Z, CharText.SMALL_LETTER_Z),
            KeyData(CharCode.SMALL_LETTER_X, CharText.SMALL_LETTER_X),
            KeyData(CharCode.SMALL_LETTER_C, CharText.SMALL_LETTER_C),
            KeyData(CharCode.SMALL_LETTER_V, CharText.SMALL_LETTER_V),
            KeyData(CharCode.SMALL_LETTER_B, CharText.SMALL_LETTER_B),
            KeyData(CharCode.SMALL_LETTER_N, CharText.SMALL_LETTER_N),
            KeyData(CharCode.SMALL_LETTER_M, CharText.SMALL_LETTER_M),
            Action.BACKSPACE.toKeyData(iconRes = R.drawable.ic_backspace, widthMultiplier = 1.5F)
        ),
        5 to rowBottom
    )

    override val upper = mutableMapOf(
        1 to rowNumbers,
        2 to listOf(
            KeyData(CharCode.CAPITAL_LETTER_Q, CharText.CAPITAL_LETTER_Q),
            KeyData(CharCode.CAPITAL_LETTER_W, CharText.CAPITAL_LETTER_W),
            KeyData(CharCode.CAPITAL_LETTER_E, CharText.CAPITAL_LETTER_E),
            KeyData(CharCode.CAPITAL_LETTER_R, CharText.CAPITAL_LETTER_R),
            KeyData(CharCode.CAPITAL_LETTER_T, CharText.CAPITAL_LETTER_T),
            KeyData(CharCode.CAPITAL_LETTER_Y, CharText.CAPITAL_LETTER_Y),
            KeyData(CharCode.CAPITAL_LETTER_U, CharText.CAPITAL_LETTER_U),
            KeyData(CharCode.CAPITAL_LETTER_I, CharText.CAPITAL_LETTER_I),
            KeyData(CharCode.CAPITAL_LETTER_O, CharText.CAPITAL_LETTER_O),
            KeyData(CharCode.CAPITAL_LETTER_P, CharText.CAPITAL_LETTER_P)
        ),
        3 to listOf(
            KeyData(CharCode.CAPITAL_LETTER_A, CharText.CAPITAL_LETTER_A),
            KeyData(CharCode.CAPITAL_LETTER_S, CharText.CAPITAL_LETTER_S),
            KeyData(CharCode.CAPITAL_LETTER_D, CharText.CAPITAL_LETTER_D),
            KeyData(CharCode.CAPITAL_LETTER_F, CharText.CAPITAL_LETTER_F),
            KeyData(CharCode.CAPITAL_LETTER_G, CharText.CAPITAL_LETTER_G),
            KeyData(CharCode.CAPITAL_LETTER_H, CharText.CAPITAL_LETTER_H),
            KeyData(CharCode.CAPITAL_LETTER_J, CharText.CAPITAL_LETTER_J),
            KeyData(CharCode.CAPITAL_LETTER_K, CharText.CAPITAL_LETTER_K),
            KeyData(CharCode.CAPITAL_LETTER_L, CharText.CAPITAL_LETTER_L)
        ),
        4 to listOf(
            Action.SHIFT.toKeyData(iconRes = R.drawable.ic_shift_pressed, widthMultiplier = 1.5F),
            KeyData(CharCode.CAPITAL_LETTER_Z, CharText.CAPITAL_LETTER_Z),
            KeyData(CharCode.CAPITAL_LETTER_X, CharText.CAPITAL_LETTER_X),
            KeyData(CharCode.CAPITAL_LETTER_C, CharText.CAPITAL_LETTER_C),
            KeyData(CharCode.CAPITAL_LETTER_V, CharText.CAPITAL_LETTER_V),
            KeyData(CharCode.CAPITAL_LETTER_B, CharText.CAPITAL_LETTER_B),
            KeyData(CharCode.CAPITAL_LETTER_N, CharText.CAPITAL_LETTER_N),
            KeyData(CharCode.CAPITAL_LETTER_M, CharText.CAPITAL_LETTER_M),
            Action.BACKSPACE.toKeyData(iconRes = R.drawable.ic_backspace, widthMultiplier = 1.5F)
        ),
        5 to rowBottom
    )
}
