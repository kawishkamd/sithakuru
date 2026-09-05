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

import sithakuru.sinhala.keyboard.data.Action
import sithakuru.sinhala.keyboard.R
import sithakuru.sinhala.keyboard.CharCode
import sithakuru.sinhala.keyboard.CharText
import sithakuru.sinhala.keyboard.datatype.KeyData
import sithakuru.sinhala.keyboard.datatype.KeyInputData

class Wijesekara : BaseLayout(indicator = "වි") {
    override val lower = mutableMapOf(
        1 to listOf(
            KeyData(CharCode.SIGN_RAKARANSHAYA, CharText.SIGN_RAKARANSHAYA),
            KeyData(CharCode.SIGN_REEPAYA, CharText.SIGN_REEPAYA),
            KeyData(CharCode.SIGN_VISARGAYA, CharText.SIGN_VISARGAYA),
            KeyData(CharCode.KANTAJA_NAASIKYAYA, CharText.KANTAJA_NAASIKYAYA),
            KeyData(CharCode.TAALUJA_NAASIKYAYA, CharText.TAALUJA_NAASIKYAYA),
            KeyData(CharCode.TAALUJA_SANYOOGA_NAAKSIKYAYA, CharText.TAALUJA_SANYOOGA_NAAKSIKYAYA),
            KeyData(CharCode.SANYAKA_GAYANNA, CharText.SANYAKA_GAYANNA),
            KeyData(CharCode.SANYAKA_JAYANNA, CharText.SANYAKA_JAYANNA),
            KeyData(CharCode.SANYAKA_DDAYANNA, CharText.SANYAKA_DDAYANNA),
            KeyData(CharCode.SANYAKA_DAYANNA, CharText.SANYAKA_DAYANNA)
        ),
        2 to listOf(
            KeyData(
                CharCode.KETTI_PAA_PILLA, CharText.KETTI_PAA_PILLA,
                popupInputData = KeyInputData(CharCode.DIGA_PAA_PILLA, CharText.DIGA_PAA_PILLA)
            ),
            KeyData(
                CharCode.AYANNA, CharText.AYANNA,
                popupInputData = KeyInputData(CharCode.UYANNA, CharText.UYANNA)
            ),
            KeyData(
                CharCode.KETTI_AEDA_PILLA, CharText.KETTI_AEDA_PILLA,
                popupInputData = KeyInputData(CharCode.DIGA_AEDA_PILLA, CharText.DIGA_AEDA_PILLA)
            ),
            KeyData(
                CharCode.RAYANNA, CharText.RAYANNA,
                popupInputData = KeyInputData(CharCode.IRUYANNA, CharText.IRUYANNA)
            ),
            KeyData(
                CharCode.EYANNA, CharText.EYANNA,
                popupInputData = KeyInputData(CharCode.OYANNA, CharText.OYANNA)
            ),
            KeyData(
                CharCode.HAYANNA, CharText.HAYANNA,
                popupInputData = KeyInputData(CharCode.TAALUJA_SAYANNA, CharText.TAALUJA_SAYANNA)
            ),
            KeyData(
                CharCode.MAYANNA, CharText.MAYANNA,
                popupInputData = KeyInputData(CharCode.AMBA_BAYANNA, CharText.AMBA_BAYANNA)
            ),
            KeyData(
                CharCode.DANTAJA_SAYANNA, CharText.DANTAJA_SAYANNA,
                popupInputData = KeyInputData(CharCode.MUURDHAJA_SAYANNA, CharText.MUURDHAJA_SAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_DAYANNA, CharText.ALPAPRAANA_DAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_DAYANNA, CharText.MAHAAPRAANA_DAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_CAYANNA, CharText.ALPAPRAANA_CAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_CAYANNA, CharText.MAHAAPRAANA_CAYANNA)
            )
        ),
        3 to listOf(
            KeyData(
                CharCode.SIGN_AL_LAKUNA, CharText.SIGN_AL_LAKUNA,
                popupInputData = KeyInputData(CharCode.GAYANUKITTA, CharText.GAYANUKITTA)
            ),
            KeyData(
                CharCode.KETTI_IS_PILLA, CharText.KETTI_IS_PILLA,
                popupInputData = KeyInputData(CharCode.DIGA_IS_PILLA, CharText.DIGA_IS_PILLA)
            ),
            KeyData(
                CharCode.AELA_PILLA, CharText.AELA_PILLA,
                popupInputData = KeyInputData(CharCode.GAETTA_PILLA, CharText.GAETTA_PILLA)
            ),
            KeyData(
                CharCode.KOMBUVA, CharText.KOMBUVA,
                popupInputData = KeyInputData(CharCode.FAYANNA, CharText.FAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_TTAYANNA, CharText.ALPAPRAANA_TTAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_TTAYANNA, CharText.MAHAAPRAANA_TTAYANNA)
            ),
            KeyData(
                CharCode.YAYANNA, CharText.YAYANNA,
                popupInputData = KeyInputData(CharCode.SIGN_YANSHAYA, CharText.SIGN_YANSHAYA)
            ),
            KeyData(
                CharCode.VAYANNA, CharText.VAYANNA,
                popupInputData = KeyInputData(CharCode.MUURDHAJA_LUYANNA, CharText.MUURDHAJA_LUYANNA)
            ),
            KeyData(
                CharCode.DANTAJA_NAYANNA, CharText.DANTAJA_NAYANNA,
                popupInputData = KeyInputData(CharCode.MUURDHAJA_NAYANNA, CharText.MUURDHAJA_NAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_KAYANNA, CharText.ALPAPRAANA_KAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_KAYANNA, CharText.MAHAAPRAANA_KAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_TAYANNA, CharText.ALPAPRAANA_TAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_TAYANNA, CharText.MAHAAPRAANA_TAYANNA)
            )
        ),
        4 to listOf(
            Action.SHIFT.toKeyData(iconRes = R.drawable.ic_shift, widthMultiplier = 1.5F),
            KeyData(CharCode.SMALL_LETTER_Z, CharText.SMALL_LETTER_Z),
            KeyData(
                CharCode.SIGN_ANUSVARAYA, CharText.SIGN_ANUSVARAYA,
                popupInputData = KeyInputData(CharCode.KANTAJA_NAASIKYAYA, CharText.KANTAJA_NAASIKYAYA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_JAYANNA, CharText.ALPAPRAANA_JAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_JAYANNA, CharText.MAHAAPRAANA_JAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_DDAYANNA, CharText.ALPAPRAANA_DDAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_DDAYANNA, CharText.MAHAAPRAANA_DDAYANNA)
            ),
            KeyData(
                CharCode.IYANNA, CharText.IYANNA,
                popupInputData = KeyInputData(CharCode.IIYANNA, CharText.IIYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_BAYANNA, CharText.ALPAPRAANA_BAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_BAYANNA, CharText.MAHAAPRAANA_BAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_PAYANNA, CharText.ALPAPRAANA_PAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_PAYANNA, CharText.MAHAAPRAANA_PAYANNA)
            ),
            Action.BACKSPACE.toKeyData(iconRes = R.drawable.ic_backspace, widthMultiplier = 1.5F)
        ),
        5 to listOf(
            rowBottom[0],
            rowBottom[1],
            rowBottom[3],
            KeyData(
                CharCode.DANTAJA_LAYANNA, CharText.DANTAJA_LAYANNA,
                popupInputData = KeyInputData(CharCode.MUURDHAJA_LAYANNA, CharText.MUURDHAJA_LAYANNA)
            ),
            KeyData(
                CharCode.ALPAPRAANA_GAYANNA, CharText.ALPAPRAANA_GAYANNA,
                popupInputData = KeyInputData(CharCode.MAHAAPRAANA_GAYANNA, CharText.MAHAAPRAANA_GAYANNA)
            ),
            rowBottom[5]
        )
    )

    override val upper = mutableMapOf(
        1 to rowNumbers,
        2 to listOf(
            KeyData(CharCode.DIGA_PAA_PILLA, CharText.DIGA_PAA_PILLA),
            KeyData(CharCode.UYANNA, CharText.UYANNA),
            KeyData(CharCode.DIGA_AEDA_PILLA, CharText.DIGA_AEDA_PILLA),
            KeyData(CharCode.IRUYANNA, CharText.IRUYANNA),
            KeyData(CharCode.OYANNA, CharText.OYANNA),
            KeyData(CharCode.TAALUJA_SAYANNA, CharText.TAALUJA_SAYANNA),
            KeyData(CharCode.AMBA_BAYANNA, CharText.AMBA_BAYANNA),
            KeyData(CharCode.MUURDHAJA_SAYANNA, CharText.MUURDHAJA_SAYANNA),
            KeyData(CharCode.MAHAAPRAANA_DAYANNA, CharText.MAHAAPRAANA_DAYANNA),
            KeyData(CharCode.MAHAAPRAANA_CAYANNA, CharText.MAHAAPRAANA_CAYANNA)
        ),
        3 to listOf(
            KeyData(CharCode.GAYANUKITTA, CharText.GAYANUKITTA),
            KeyData(CharCode.DIGA_IS_PILLA, CharText.DIGA_IS_PILLA),
            KeyData(CharCode.GAETTA_PILLA, CharText.GAETTA_PILLA),
            KeyData(CharCode.FAYANNA, CharText.FAYANNA),
            KeyData(CharCode.MAHAAPRAANA_TTAYANNA, CharText.MAHAAPRAANA_TTAYANNA),
            KeyData(CharCode.SIGN_YANSHAYA, CharText.SIGN_YANSHAYA),
            KeyData(CharCode.MUURDHAJA_LUYANNA, CharText.MUURDHAJA_LUYANNA),
            KeyData(CharCode.MUURDHAJA_NAYANNA, CharText.MUURDHAJA_NAYANNA),
            KeyData(CharCode.MAHAAPRAANA_KAYANNA, CharText.MAHAAPRAANA_KAYANNA),
            KeyData(CharCode.MAHAAPRAANA_TAYANNA, CharText.MAHAAPRAANA_TAYANNA)
        ),
        4 to listOf(
            Action.SHIFT.toKeyData(iconRes = R.drawable.ic_shift_pressed, widthMultiplier = 1.5F),
            KeyData(CharCode.SMALL_LETTER_Z, CharText.SMALL_LETTER_Z),
            KeyData(CharCode.KANTAJA_NAASIKYAYA, CharText.KANTAJA_NAASIKYAYA),
            KeyData(CharCode.MAHAAPRAANA_JAYANNA, CharText.MAHAAPRAANA_JAYANNA),
            KeyData(CharCode.MAHAAPRAANA_DDAYANNA, CharText.MAHAAPRAANA_DDAYANNA),
            KeyData(CharCode.IIYANNA, CharText.IIYANNA),
            KeyData(CharCode.MAHAAPRAANA_BAYANNA, CharText.MAHAAPRAANA_BAYANNA),
            KeyData(CharCode.MAHAAPRAANA_PAYANNA, CharText.MAHAAPRAANA_PAYANNA),
            Action.BACKSPACE.toKeyData(iconRes = R.drawable.ic_backspace, widthMultiplier = 1.5F)
        ),
        5 to listOf(
            rowBottom[0],
            rowBottom[1],
            rowBottom[3],
            KeyData(CharCode.MUURDHAJA_LAYANNA, CharText.MUURDHAJA_LAYANNA),
            KeyData(CharCode.MAHAAPRAANA_GAYANNA, CharText.MAHAAPRAANA_GAYANNA),
            rowBottom[5]
        )
    )

    val wijesekaraSymbolsRowBottom: MutableList<KeyData> = mutableListOf<KeyData>()
        .apply { addAll(rowBottom) }
        .apply {
            set(1, KeyData(CharCode.SOLIDUS, CharText.SOLIDUS))
            add(4, rowBottom[2])
            removeAt(2)
        }
}
