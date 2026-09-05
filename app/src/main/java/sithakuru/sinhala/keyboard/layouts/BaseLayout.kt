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

import androidx.annotation.DrawableRes
import sithakuru.sinhala.keyboard.data.Action
import sithakuru.sinhala.keyboard.datatype.KeyInputData
import sithakuru.sinhala.keyboard.R
import sithakuru.sinhala.keyboard.CharCode
import sithakuru.sinhala.keyboard.CharText
import sithakuru.sinhala.keyboard.datatype.KeyData

abstract class BaseLayout(val indicator: String) {
    abstract val lower: Map<Int, List<KeyData>>
    abstract val upper: Map<Int, List<KeyData>>

    internal val rowNumbers: List<KeyData> = listOf(
        KeyData(CharCode.ONE, CharText.ONE),
        KeyData(CharCode.TWO, CharText.TWO),
        KeyData(CharCode.THREE, CharText.THREE),
        KeyData(CharCode.FOUR, CharText.FOUR),
        KeyData(CharCode.FIVE, CharText.FIVE),
        KeyData(CharCode.SIX, CharText.SIX),
        KeyData(CharCode.SEVEN, CharText.SEVEN),
        KeyData(CharCode.EIGHT, CharText.EIGHT),
        KeyData(CharCode.NINE, CharText.NINE),
        KeyData(CharCode.ZERO, CharText.ZERO)
    )
    internal val rowBottom: MutableList<KeyData> = mutableListOf(
        Action.SYMBOLS.toKeyData(label = "#!", dim = true),
        // Todo: Action.EMOJI.toKeyData(label = ":)", dim = true),
        Action.LANG.toKeyData(label = indicator, dim = true),
        KeyData(CharCode.COMMA, CharText.COMMA),
        KeyData(
            KeyInputData(CharCode.SPACE, null, R.drawable.ic_space_bar),
            widthMultiplier = 5f,
            popupInputData = Action.IME.toKeyInputData()
        ),
        KeyData(CharCode.FULL_STOP, CharText.FULL_STOP),
        Action.ACTION.toKeyData()
    )

    internal fun @receiver:Action Int.toKeyInputData(label: String? = null, @DrawableRes iconRes: Int? = null, dim: Boolean = false) =
        KeyInputData(this, label = label, iconRes = iconRes, dim = dim)

    internal fun @receiver:Action Int.toKeyData(
        label: String? = null,
        @DrawableRes iconRes: Int? = null,
        dim: Boolean = false,
        widthMultiplier: Float = 1f
    ): KeyData = KeyData(toKeyInputData(label = label, iconRes = iconRes, dim = dim), widthMultiplier = widthMultiplier)
}
