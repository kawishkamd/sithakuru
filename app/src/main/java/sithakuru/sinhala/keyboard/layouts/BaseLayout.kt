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

abstract class BaseLayout(
    val indicator: String,
    val showEmojiKey: Boolean = true,
    val showClipboardKey: Boolean = false
) {
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
    internal val rowBottom: MutableList<KeyData> = mutableListOf<KeyData>().apply {
        add(Action.SYMBOLS.toKeyData(label = "#!", dim = true))
        if (showEmojiKey) {
            val emojiPopup = if (!showClipboardKey) Action.CLIPBOARD.toKeyInputData(iconRes = R.drawable.ic_clipboard) else null
            add(
                KeyData(
                    Action.EMOJI.toKeyInputData(iconRes = R.drawable.ic_emoji, dim = true),
                    popupInputData = emojiPopup
                )
            )
        }
        if (showClipboardKey) {
            val clipboardPopup = if (!showEmojiKey) Action.EMOJI.toKeyInputData(iconRes = R.drawable.ic_emoji) else null
            add(
                KeyData(
                    Action.CLIPBOARD.toKeyInputData(iconRes = R.drawable.ic_clipboard, dim = true),
                    popupInputData = clipboardPopup
                )
            )
        }
        add(Action.LANG.toKeyData(label = indicator, dim = true))
        add(
            KeyData(
                CharCode.COMMA,
                CharText.COMMA,
                popupInputData = commaPopup()
            )
        )
        val spaceMultiplier = (10 - 5 - (if (showEmojiKey) 1 else 0) - (if (showClipboardKey) 1 else 0)).toFloat()
        add(
            KeyData(
                KeyInputData(CharCode.SPACE, null, R.drawable.ic_space_bar),
                widthMultiplier = spaceMultiplier,
                popupInputData = Action.IME.toKeyInputData()
            )
        )
        add(KeyData(CharCode.FULL_STOP, CharText.FULL_STOP))
        add(Action.ACTION.toKeyData())
    }

    /**
     * Long-press popup for the bottom-row comma key. When the dedicated Emoji key is
     * disabled this exposes emoji through the comma, so emoji stays reachable either
     * way. Layouts that replace the comma with another key (see [Symbols]) override
     * this to null to avoid a duplicate emoji popup.
     */
    protected open fun commaPopup(): KeyInputData? =
        if (!showEmojiKey) Action.EMOJI.toKeyInputData(iconRes = R.drawable.ic_emoji) else null

    internal fun @receiver:Action Int.toKeyInputData(label: String? = null, @DrawableRes iconRes: Int? = null, dim: Boolean = false) =
        KeyInputData(this, label = label, iconRes = iconRes, dim = dim)

    internal fun @receiver:Action Int.toKeyData(
        label: String? = null,
        @DrawableRes iconRes: Int? = null,
        dim: Boolean = false,
        widthMultiplier: Float = 1f
    ): KeyData = KeyData(toKeyInputData(label = label, iconRes = iconRes, dim = dim), widthMultiplier = widthMultiplier)
}
