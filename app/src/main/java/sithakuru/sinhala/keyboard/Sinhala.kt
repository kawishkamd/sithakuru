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

fun isSwara(code: Int) = code >= CHAR.AYANNA.code && code <= CHAR.AUYANNA.code

fun isWyanjana(code: Int) =
    (code >= CHAR.ALPAPRAANA_KAYANNA.code && code <= CHAR.DANTAJA_NAYANNA.code) ||
            (code >= CHAR.SANYAKA_DAYANNA.code && code <= CHAR.RAYANNA.code) ||
            (code == CHAR.DANTAJA_LAYANNA.code) ||
            (code >= CHAR.VAYANNA.code && code <= CHAR.FAYANNA.code)

fun isPili(code: Int) =
    (code >= CHAR.AELA_PILLA.code && code <= CHAR.KETTI_PAA_PILLA.code) ||
            (code == CHAR.DIGA_PAA_PILLA.code) ||
            (code >= CHAR.GAETTA_PILLA.code && code <= CHAR.GAYANUKITTA.code) ||
            (code == CHAR.DIGA_GAETTA_PILLA.code) ||
            (code == CHAR.DIGA_GAYANUKITTA.code)

fun isSigns(code: Int) = code == CHAR.SIGN_ANUSVARAYA.code ||
        code == CHAR.SIGN_VISARGAYA.code ||
        code == CHAR.SIGN_AL_LAKUNA.code ||
        code == CHAR.SIGN_YANSHAYA.code ||
        code == CHAR.SIGN_RAKARANSHAYA.code ||
        code == CHAR.SIGN_REEPAYA.code

val swaraSignMap: Map<Int, CHAR> = mapOf(
    CHAR.AAYANNA.code to CHAR.AELA_PILLA,
    CHAR.AEYANNA.code to CHAR.KETTI_AEDA_PILLA,
    CHAR.AEEYANNA.code to CHAR.DIGA_AEDA_PILLA,
    CHAR.IYANNA.code to CHAR.KETTI_IS_PILLA,
    CHAR.IIYANNA.code to CHAR.DIGA_IS_PILLA,
    CHAR.UYANNA.code to CHAR.KETTI_PAA_PILLA,
    CHAR.UUYANNA.code to CHAR.DIGA_PAA_PILLA,
    CHAR.IRUYANNA.code to CHAR.GAETTA_PILLA,
    CHAR.IRUUYANNA.code to CHAR.DIGA_GAETTA_PILLA,
    CHAR.EYANNA.code to CHAR.KOMBUVA,
    CHAR.EEYANNA.code to CHAR.DIGA_KOMBUVA,
    CHAR.AIYANNA.code to CHAR.KOMBU_DEKA,
    CHAR.OYANNA.code to CHAR.KOMBUVA_HAA_AELA_PILLA,
    CHAR.OOYANNA.code to CHAR.KOMBUVA_HAA_DIGA_AELA_PILLA,
    CHAR.AUYANNA.code to CHAR.KOMBUVA_HAA_GAYANUKITTA
)

fun getCharType(text: String): CharType {
    val code: Int = when {
        text.length == 1 -> text.toCharArray()[0].code
        text == CHAR.SIGN_YANSHAYA.text -> CHAR.SIGN_YANSHAYA.code
        text == CHAR.SIGN_RAKARANSHAYA.text -> CHAR.SIGN_RAKARANSHAYA.code
        text == CHAR.SIGN_REEPAYA.text -> CHAR.SIGN_REEPAYA.code
        else -> 0
    }
    return getCharType(code)
}

fun getCharType(code: Int): CharType {
    return when {
        isSwara(code) -> CharType.SWARA
        isWyanjana(code) -> CharType.WYANJANA
        isPili(code) -> CharType.PILI
        isSigns(code) -> CharType.LAKUNU
        else -> CharType.UNKNOWN
    }
}
