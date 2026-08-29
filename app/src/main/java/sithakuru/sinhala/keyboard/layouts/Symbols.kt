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

class Symbols : BaseLayout(indicator = "#!") {
    init {
        rowBottom.apply { set(1, KeyData(CharCode.SOLIDUS, CharText.SOLIDUS)) }
    }

    override val lower = mutableMapOf(
        1 to rowNumbers,
        2 to listOf(
            KeyData(CharCode.LOW_LINE, CharText.LOW_LINE),
            KeyData(CharCode.REVERSE_SOLIDUS, CharText.REVERSE_SOLIDUS),
            KeyData(CharCode.VERTICAL_LINE, CharText.VERTICAL_LINE),
            KeyData(CharCode.EQUALS_SIGN, CharText.EQUALS_SIGN),
            KeyData(CharCode.LEFT_SQUARE_BRACKET, CharText.LEFT_SQUARE_BRACKET),
            KeyData(CharCode.RIGHT_SQUARE_BRACKET, CharText.RIGHT_SQUARE_BRACKET),
            KeyData(CharCode.LESS_THAN_SIGN, CharText.LESS_THAN_SIGN),
            KeyData(CharCode.GREATER_THAN_SIGN, CharText.GREATER_THAN_SIGN),
            KeyData(CharCode.LEFT_CURLY_BRACKET, CharText.LEFT_CURLY_BRACKET),
            KeyData(CharCode.RIGHT_CURLY_BRACKET, CharText.RIGHT_CURLY_BRACKET)
        ),
        3 to listOf(
            KeyData(CharCode.COMMERCIAL_AT, CharText.COMMERCIAL_AT),
            KeyData(CharCode.NUMBER_SIGN, CharText.NUMBER_SIGN),
            KeyData(CharCode.CIRCUMFLEX_ACCENT, CharText.CIRCUMFLEX_ACCENT),
            KeyData(CharCode.PERCENT_SIGN, CharText.PERCENT_SIGN),
            KeyData(CharCode.AMPERSAND, CharText.AMPERSAND),
            KeyData(CharCode.HYPHEN_MINUS, CharText.HYPHEN_MINUS),
            KeyData(CharCode.PLUS_SIGN, CharText.PLUS_SIGN),
            KeyData(CharCode.LEFT_PARENTHESIS, CharText.LEFT_PARENTHESIS),
            KeyData(CharCode.RIGHT_PARENTHESIS, CharText.RIGHT_PARENTHESIS)
        ),
        4 to listOf(
            Action.SHIFT.toKeyData(iconRes = R.drawable.ic_shift, widthMultiplier = 1.5F),
            KeyData(CharCode.ASTERISK, CharText.ASTERISK),
            KeyData(CharCode.QUOTATION_MARK, CharText.QUOTATION_MARK),
            KeyData(CharCode.APOSTROPHE, CharText.APOSTROPHE),
            KeyData(CharCode.COLON, CharText.COLON),
            KeyData(CharCode.SEMICOLON, CharText.SEMICOLON),
            KeyData(CharCode.EXCLAMATION_MARK, CharText.EXCLAMATION_MARK),
            KeyData(CharCode.QUESTION_MARK, CharText.QUESTION_MARK),
            Action.BACKSPACE.toKeyData(iconRes = R.drawable.ic_backspace, widthMultiplier = 1.5F)
        ),
        5 to rowBottom
    )

    override val upper = mutableMapOf(
        1 to rowNumbers,
        2 to listOf(
            KeyData(CharCode.TILDE, CharText.TILDE),
            KeyData(CharCode.GRAVE_ACCENT, CharText.GRAVE_ACCENT),
            KeyData(CharCode.SQUARE_ROOT, CharText.SQUARE_ROOT),
            KeyData(CharCode.GREEK_SMALL_LETTER_PI, CharText.GREEK_SMALL_LETTER_PI),
            KeyData(CharCode.DIVISION_SIGN, CharText.DIVISION_SIGN),
            KeyData(CharCode.MULTIPLICATION_SIGN, CharText.MULTIPLICATION_SIGN),
            KeyData(CharCode.PILCROW_SIGN, CharText.PILCROW_SIGN),
            KeyData(CharCode.INCREMENT, CharText.INCREMENT),
            KeyData(CharCode.EURO_SIGN, CharText.EURO_SIGN),
            KeyData(CharCode.YEN_SIGN, CharText.YEN_SIGN)
        ),
        3 to listOf(
            KeyData(CharCode.DOLLAR_SIGN, CharText.DOLLAR_SIGN),
            KeyData(CharCode.POUND_SIGN, CharText.POUND_SIGN),
            KeyData(CharCode.CENT_SIGN, CharText.CENT_SIGN),
            KeyData(CharCode.DEGREE_SIGN, CharText.DEGREE_SIGN),
            KeyData(CharCode.COPYRIGHT_SIGN, CharText.COPYRIGHT_SIGN),
            KeyData(CharCode.REGISTERED_SIGN, CharText.REGISTERED_SIGN),
            KeyData(CharCode.TRADE_MARK_SIGN, CharText.TRADE_MARK_SIGN),
            KeyData(CharCode.CARE_OF, CharText.CARE_OF),
            KeyData(CharCode.PLUS_MINUS_SIGN, CharText.PLUS_MINUS_SIGN)
        ),
        4 to listOf(
            Action.SHIFT.toKeyData(iconRes = R.drawable.ic_shift_pressed, widthMultiplier = 1.5F),
            KeyData(CharCode.BULLET, CharText.BULLET),
            KeyData(CharCode.CIRCLED_CC, CharText.CIRCLED_CC),
            KeyData(CharCode.CIRCLED_ZERO_WITH_SLASH, CharText.CIRCLED_ZERO_WITH_SLASH),
            KeyData(CharCode.CIRCLED_HUMAN_FIGURE, CharText.CIRCLED_HUMAN_FIGURE),
            KeyData(CharCode.CIRCLED_ANTICLOCKWISE_ARROW, CharText.CIRCLED_ANTICLOCKWISE_ARROW),
            KeyData(CharCode.CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH, CharText.CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH),
            KeyData(CharCode.CIRCLED_EQUALS, CharText.CIRCLED_EQUALS),
            Action.BACKSPACE.toKeyData(iconRes = R.drawable.ic_backspace, widthMultiplier = 1.5F)
        ),
        5 to rowBottom
    )

    fun setCustomRowBottom(customRowBottom: MutableList<KeyData>?) {
        lower[5] = customRowBottom ?: rowBottom
        upper[5] = customRowBottom ?: rowBottom
    }
}
