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

fun isSwara(code: Int) = code in CharCode.AYANNA..CharCode.AUYANNA

fun isWyanjana(code: Int) = when (code) {
    in CharCode.ALPAPRAANA_KAYANNA..CharCode.DANTAJA_NAYANNA,
    in CharCode.SANYAKA_DAYANNA..CharCode.RAYANNA,
    CharCode.DANTAJA_LAYANNA,
    in CharCode.VAYANNA..CharCode.FAYANNA -> true

    else -> false
}

fun isPili(code: Int) = when (code) {
    in CharCode.AELA_PILLA..CharCode.KETTI_PAA_PILLA,
    CharCode.DIGA_PAA_PILLA,
    in CharCode.GAETTA_PILLA..CharCode.GAYANUKITTA,
    CharCode.DIGA_GAETTA_PILLA,
    CharCode.DIGA_GAYANUKITTA -> true

    else -> false
}

fun isSigns(code: Int) = when (code) {
    CharCode.SIGN_ANUSVARAYA,
    CharCode.SIGN_VISARGAYA,
    CharCode.SIGN_AL_LAKUNA,
    CharCode.SIGN_YANSHAYA,
    CharCode.SIGN_RAKARANSHAYA,
    CharCode.SIGN_REEPAYA -> true

    else -> false
}

fun getSwaraSigns(code: Int): Int? = when (code) {
    CharCode.AAYANNA -> CharCode.AELA_PILLA
    CharCode.AEYANNA -> CharCode.KETTI_AEDA_PILLA
    CharCode.AEEYANNA -> CharCode.DIGA_AEDA_PILLA
    CharCode.IYANNA -> CharCode.KETTI_IS_PILLA
    CharCode.IIYANNA -> CharCode.DIGA_IS_PILLA
    CharCode.UYANNA -> CharCode.KETTI_PAA_PILLA
    CharCode.UUYANNA -> CharCode.DIGA_PAA_PILLA
    CharCode.IRUYANNA -> CharCode.GAETTA_PILLA
    CharCode.IRUUYANNA -> CharCode.DIGA_GAETTA_PILLA
    CharCode.EYANNA -> CharCode.KOMBUVA
    CharCode.EEYANNA -> CharCode.DIGA_KOMBUVA
    CharCode.AIYANNA -> CharCode.KOMBU_DEKA
    CharCode.OYANNA -> CharCode.KOMBUVA_HAA_AELA_PILLA
    CharCode.OOYANNA -> CharCode.KOMBUVA_HAA_DIGA_AELA_PILLA
    CharCode.AUYANNA -> CharCode.KOMBUVA_HAA_GAYANUKITTA
    else -> null
}

val Int.text: String
    get() = when (this) {
        CharCode.AYANNA -> CharText.AYANNA
        CharCode.AAYANNA -> CharText.AAYANNA
        CharCode.AEYANNA -> CharText.AEYANNA
        CharCode.AEEYANNA -> CharText.AEEYANNA
        CharCode.IYANNA -> CharText.IYANNA
        CharCode.IIYANNA -> CharText.IIYANNA
        CharCode.UYANNA -> CharText.UYANNA
        CharCode.UUYANNA -> CharText.UUYANNA
        CharCode.IRUYANNA -> CharText.IRUYANNA
        CharCode.IRUUYANNA -> CharText.IRUUYANNA
        CharCode.ILUYANNA -> CharText.ILUYANNA
        CharCode.ILUUYANNA -> CharText.ILUUYANNA
        CharCode.EYANNA -> CharText.EYANNA
        CharCode.EEYANNA -> CharText.EEYANNA
        CharCode.AIYANNA -> CharText.AIYANNA
        CharCode.OYANNA -> CharText.OYANNA
        CharCode.OOYANNA -> CharText.OOYANNA
        CharCode.AUYANNA -> CharText.AUYANNA
        CharCode.ALPAPRAANA_KAYANNA -> CharText.ALPAPRAANA_KAYANNA
        CharCode.MAHAAPRAANA_KAYANNA -> CharText.MAHAAPRAANA_KAYANNA
        CharCode.ALPAPRAANA_GAYANNA -> CharText.ALPAPRAANA_GAYANNA
        CharCode.MAHAAPRAANA_GAYANNA -> CharText.MAHAAPRAANA_GAYANNA
        CharCode.KANTAJA_NAASIKYAYA -> CharText.KANTAJA_NAASIKYAYA
        CharCode.SANYAKA_GAYANNA -> CharText.SANYAKA_GAYANNA
        CharCode.ALPAPRAANA_CAYANNA -> CharText.ALPAPRAANA_CAYANNA
        CharCode.MAHAAPRAANA_CAYANNA -> CharText.MAHAAPRAANA_CAYANNA
        CharCode.ALPAPRAANA_JAYANNA -> CharText.ALPAPRAANA_JAYANNA
        CharCode.MAHAAPRAANA_JAYANNA -> CharText.MAHAAPRAANA_JAYANNA
        CharCode.TAALUJA_NAASIKYAYA -> CharText.TAALUJA_NAASIKYAYA
        CharCode.TAALUJA_SANYOOGA_NAAKSIKYAYA -> CharText.TAALUJA_SANYOOGA_NAAKSIKYAYA
        CharCode.SANYAKA_JAYANNA -> CharText.SANYAKA_JAYANNA
        CharCode.ALPAPRAANA_TTAYANNA -> CharText.ALPAPRAANA_TTAYANNA
        CharCode.MAHAAPRAANA_TTAYANNA -> CharText.MAHAAPRAANA_TTAYANNA
        CharCode.ALPAPRAANA_DDAYANNA -> CharText.ALPAPRAANA_DDAYANNA
        CharCode.MAHAAPRAANA_DDAYANNA -> CharText.MAHAAPRAANA_DDAYANNA
        CharCode.MUURDHAJA_NAYANNA -> CharText.MUURDHAJA_NAYANNA
        CharCode.SANYAKA_DDAYANNA -> CharText.SANYAKA_DDAYANNA
        CharCode.ALPAPRAANA_TAYANNA -> CharText.ALPAPRAANA_TAYANNA
        CharCode.MAHAAPRAANA_TAYANNA -> CharText.MAHAAPRAANA_TAYANNA
        CharCode.ALPAPRAANA_DAYANNA -> CharText.ALPAPRAANA_DAYANNA
        CharCode.MAHAAPRAANA_DAYANNA -> CharText.MAHAAPRAANA_DAYANNA
        CharCode.DANTAJA_NAYANNA -> CharText.DANTAJA_NAYANNA
        CharCode.SANYAKA_DAYANNA -> CharText.SANYAKA_DAYANNA
        CharCode.ALPAPRAANA_PAYANNA -> CharText.ALPAPRAANA_PAYANNA
        CharCode.MAHAAPRAANA_PAYANNA -> CharText.MAHAAPRAANA_PAYANNA
        CharCode.ALPAPRAANA_BAYANNA -> CharText.ALPAPRAANA_BAYANNA
        CharCode.MAHAAPRAANA_BAYANNA -> CharText.MAHAAPRAANA_BAYANNA
        CharCode.MAYANNA -> CharText.MAYANNA
        CharCode.AMBA_BAYANNA -> CharText.AMBA_BAYANNA
        CharCode.YAYANNA -> CharText.YAYANNA
        CharCode.RAYANNA -> CharText.RAYANNA
        CharCode.DANTAJA_LAYANNA -> CharText.DANTAJA_LAYANNA
        CharCode.VAYANNA -> CharText.VAYANNA
        CharCode.TAALUJA_SAYANNA -> CharText.TAALUJA_SAYANNA
        CharCode.MUURDHAJA_SAYANNA -> CharText.MUURDHAJA_SAYANNA
        CharCode.DANTAJA_SAYANNA -> CharText.DANTAJA_SAYANNA
        CharCode.HAYANNA -> CharText.HAYANNA
        CharCode.MUURDHAJA_LAYANNA -> CharText.MUURDHAJA_LAYANNA
        CharCode.FAYANNA -> CharText.FAYANNA
        CharCode.AELA_PILLA -> CharText.AELA_PILLA
        CharCode.KETTI_AEDA_PILLA -> CharText.KETTI_AEDA_PILLA
        CharCode.DIGA_AEDA_PILLA -> CharText.DIGA_AEDA_PILLA
        CharCode.KETTI_IS_PILLA -> CharText.KETTI_IS_PILLA
        CharCode.DIGA_IS_PILLA -> CharText.DIGA_IS_PILLA
        CharCode.KETTI_PAA_PILLA -> CharText.KETTI_PAA_PILLA
        CharCode.DIGA_PAA_PILLA -> CharText.DIGA_PAA_PILLA
        CharCode.GAETTA_PILLA -> CharText.GAETTA_PILLA
        CharCode.KOMBUVA -> CharText.KOMBUVA
        CharCode.DIGA_KOMBUVA -> CharText.DIGA_KOMBUVA
        CharCode.KOMBU_DEKA -> CharText.KOMBU_DEKA
        CharCode.KOMBUVA_HAA_AELA_PILLA -> CharText.KOMBUVA_HAA_AELA_PILLA
        CharCode.KOMBUVA_HAA_DIGA_AELA_PILLA -> CharText.KOMBUVA_HAA_DIGA_AELA_PILLA
        CharCode.KOMBUVA_HAA_GAYANUKITTA -> CharText.KOMBUVA_HAA_GAYANUKITTA
        CharCode.GAYANUKITTA -> CharText.GAYANUKITTA
        CharCode.DIGA_GAYANUKITTA -> CharText.DIGA_GAYANUKITTA
        CharCode.DIGA_GAETTA_PILLA -> CharText.DIGA_GAETTA_PILLA
        CharCode.SIGN_ANUSVARAYA -> CharText.SIGN_ANUSVARAYA
        CharCode.SIGN_VISARGAYA -> CharText.SIGN_VISARGAYA
        CharCode.SIGN_AL_LAKUNA -> CharText.SIGN_AL_LAKUNA
        CharCode.ZERO_WIDTH_JOINER -> CharText.ZERO_WIDTH_JOINER
        CharCode.SIGN_YANSHAYA -> CharText.SIGN_YANSHAYA
        CharCode.SIGN_RAKARANSHAYA -> CharText.SIGN_RAKARANSHAYA
        CharCode.SIGN_REEPAYA -> CharText.SIGN_REEPAYA
        CharCode.MUURDHAJA_LUYANNA -> CharText.MUURDHAJA_LUYANNA
        CharCode.CAPITAL_LETTER_A -> CharText.CAPITAL_LETTER_A
        CharCode.CAPITAL_LETTER_B -> CharText.CAPITAL_LETTER_B
        CharCode.CAPITAL_LETTER_C -> CharText.CAPITAL_LETTER_C
        CharCode.CAPITAL_LETTER_D -> CharText.CAPITAL_LETTER_D
        CharCode.CAPITAL_LETTER_E -> CharText.CAPITAL_LETTER_E
        CharCode.CAPITAL_LETTER_F -> CharText.CAPITAL_LETTER_F
        CharCode.CAPITAL_LETTER_G -> CharText.CAPITAL_LETTER_G
        CharCode.CAPITAL_LETTER_H -> CharText.CAPITAL_LETTER_H
        CharCode.CAPITAL_LETTER_I -> CharText.CAPITAL_LETTER_I
        CharCode.CAPITAL_LETTER_J -> CharText.CAPITAL_LETTER_J
        CharCode.CAPITAL_LETTER_K -> CharText.CAPITAL_LETTER_K
        CharCode.CAPITAL_LETTER_L -> CharText.CAPITAL_LETTER_L
        CharCode.CAPITAL_LETTER_M -> CharText.CAPITAL_LETTER_M
        CharCode.CAPITAL_LETTER_N -> CharText.CAPITAL_LETTER_N
        CharCode.CAPITAL_LETTER_O -> CharText.CAPITAL_LETTER_O
        CharCode.CAPITAL_LETTER_P -> CharText.CAPITAL_LETTER_P
        CharCode.CAPITAL_LETTER_Q -> CharText.CAPITAL_LETTER_Q
        CharCode.CAPITAL_LETTER_R -> CharText.CAPITAL_LETTER_R
        CharCode.CAPITAL_LETTER_S -> CharText.CAPITAL_LETTER_S
        CharCode.CAPITAL_LETTER_T -> CharText.CAPITAL_LETTER_T
        CharCode.CAPITAL_LETTER_U -> CharText.CAPITAL_LETTER_U
        CharCode.CAPITAL_LETTER_V -> CharText.CAPITAL_LETTER_V
        CharCode.CAPITAL_LETTER_W -> CharText.CAPITAL_LETTER_W
        CharCode.CAPITAL_LETTER_X -> CharText.CAPITAL_LETTER_X
        CharCode.CAPITAL_LETTER_Y -> CharText.CAPITAL_LETTER_Y
        CharCode.CAPITAL_LETTER_Z -> CharText.CAPITAL_LETTER_Z
        CharCode.SMALL_LETTER_A -> CharText.SMALL_LETTER_A
        CharCode.SMALL_LETTER_B -> CharText.SMALL_LETTER_B
        CharCode.SMALL_LETTER_C -> CharText.SMALL_LETTER_C
        CharCode.SMALL_LETTER_D -> CharText.SMALL_LETTER_D
        CharCode.SMALL_LETTER_E -> CharText.SMALL_LETTER_E
        CharCode.SMALL_LETTER_F -> CharText.SMALL_LETTER_F
        CharCode.SMALL_LETTER_G -> CharText.SMALL_LETTER_G
        CharCode.SMALL_LETTER_H -> CharText.SMALL_LETTER_H
        CharCode.SMALL_LETTER_I -> CharText.SMALL_LETTER_I
        CharCode.SMALL_LETTER_J -> CharText.SMALL_LETTER_J
        CharCode.SMALL_LETTER_K -> CharText.SMALL_LETTER_K
        CharCode.SMALL_LETTER_L -> CharText.SMALL_LETTER_L
        CharCode.SMALL_LETTER_M -> CharText.SMALL_LETTER_M
        CharCode.SMALL_LETTER_N -> CharText.SMALL_LETTER_N
        CharCode.SMALL_LETTER_O -> CharText.SMALL_LETTER_O
        CharCode.SMALL_LETTER_P -> CharText.SMALL_LETTER_P
        CharCode.SMALL_LETTER_Q -> CharText.SMALL_LETTER_Q
        CharCode.SMALL_LETTER_R -> CharText.SMALL_LETTER_R
        CharCode.SMALL_LETTER_S -> CharText.SMALL_LETTER_S
        CharCode.SMALL_LETTER_T -> CharText.SMALL_LETTER_T
        CharCode.SMALL_LETTER_U -> CharText.SMALL_LETTER_U
        CharCode.SMALL_LETTER_V -> CharText.SMALL_LETTER_V
        CharCode.SMALL_LETTER_W -> CharText.SMALL_LETTER_W
        CharCode.SMALL_LETTER_X -> CharText.SMALL_LETTER_X
        CharCode.SMALL_LETTER_Y -> CharText.SMALL_LETTER_Y
        CharCode.SMALL_LETTER_Z -> CharText.SMALL_LETTER_Z
        CharCode.ZERO -> CharText.ZERO
        CharCode.ONE -> CharText.ONE
        CharCode.TWO -> CharText.TWO
        CharCode.THREE -> CharText.THREE
        CharCode.FOUR -> CharText.FOUR
        CharCode.FIVE -> CharText.FIVE
        CharCode.SIX -> CharText.SIX
        CharCode.SEVEN -> CharText.SEVEN
        CharCode.EIGHT -> CharText.EIGHT
        CharCode.NINE -> CharText.NINE
        CharCode.SPACE -> CharText.SPACE
        CharCode.GRAVE_ACCENT -> CharText.GRAVE_ACCENT
        CharCode.TILDE -> CharText.TILDE
        CharCode.EXCLAMATION_MARK -> CharText.EXCLAMATION_MARK
        CharCode.COMMERCIAL_AT -> CharText.COMMERCIAL_AT
        CharCode.NUMBER_SIGN -> CharText.NUMBER_SIGN
        CharCode.DOLLAR_SIGN -> CharText.DOLLAR_SIGN
        CharCode.PERCENT_SIGN -> CharText.PERCENT_SIGN
        CharCode.CIRCUMFLEX_ACCENT -> CharText.CIRCUMFLEX_ACCENT
        CharCode.AMPERSAND -> CharText.AMPERSAND
        CharCode.ASTERISK -> CharText.ASTERISK
        CharCode.LEFT_PARENTHESIS -> CharText.LEFT_PARENTHESIS
        CharCode.RIGHT_PARENTHESIS -> CharText.RIGHT_PARENTHESIS
        CharCode.HYPHEN_MINUS -> CharText.HYPHEN_MINUS
        CharCode.LOW_LINE -> CharText.LOW_LINE
        CharCode.EQUALS_SIGN -> CharText.EQUALS_SIGN
        CharCode.PLUS_SIGN -> CharText.PLUS_SIGN
        CharCode.LEFT_SQUARE_BRACKET -> CharText.LEFT_SQUARE_BRACKET
        CharCode.RIGHT_SQUARE_BRACKET -> CharText.RIGHT_SQUARE_BRACKET
        CharCode.LEFT_CURLY_BRACKET -> CharText.LEFT_CURLY_BRACKET
        CharCode.RIGHT_CURLY_BRACKET -> CharText.RIGHT_CURLY_BRACKET
        CharCode.REVERSE_SOLIDUS -> CharText.REVERSE_SOLIDUS
        CharCode.VERTICAL_LINE -> CharText.VERTICAL_LINE
        CharCode.SEMICOLON -> CharText.SEMICOLON
        CharCode.COLON -> CharText.COLON
        CharCode.APOSTROPHE -> CharText.APOSTROPHE
        CharCode.QUOTATION_MARK -> CharText.QUOTATION_MARK
        CharCode.COMMA -> CharText.COMMA
        CharCode.FULL_STOP -> CharText.FULL_STOP
        CharCode.LESS_THAN_SIGN -> CharText.LESS_THAN_SIGN
        CharCode.GREATER_THAN_SIGN -> CharText.GREATER_THAN_SIGN
        CharCode.SOLIDUS -> CharText.SOLIDUS
        CharCode.QUESTION_MARK -> CharText.QUESTION_MARK
        CharCode.DIVISION_SIGN -> CharText.DIVISION_SIGN
        CharCode.MULTIPLICATION_SIGN -> CharText.MULTIPLICATION_SIGN
        CharCode.PLUS_MINUS_SIGN -> CharText.PLUS_MINUS_SIGN
        CharCode.BULLET -> CharText.BULLET
        CharCode.WHITE_BULLET -> CharText.WHITE_BULLET
        CharCode.BLACK_SMALL_SQUARE -> CharText.BLACK_SMALL_SQUARE
        CharCode.WHITE_SMALL_SQUARE -> CharText.WHITE_SMALL_SQUARE
        CharCode.TRIANGULAR_BULLET -> CharText.TRIANGULAR_BULLET
        CharCode.SQUARE_ROOT -> CharText.SQUARE_ROOT
        CharCode.GREEK_SMALL_LETTER_PI -> CharText.GREEK_SMALL_LETTER_PI
        CharCode.PILCROW_SIGN -> CharText.PILCROW_SIGN
        CharCode.INCREMENT -> CharText.INCREMENT
        CharCode.EURO_SIGN -> CharText.EURO_SIGN
        CharCode.YEN_SIGN -> CharText.YEN_SIGN
        CharCode.POUND_SIGN -> CharText.POUND_SIGN
        CharCode.CENT_SIGN -> CharText.CENT_SIGN
        CharCode.DEGREE_SIGN -> CharText.DEGREE_SIGN
        CharCode.COPYRIGHT_SIGN -> CharText.COPYRIGHT_SIGN
        CharCode.REGISTERED_SIGN -> CharText.REGISTERED_SIGN
        CharCode.TRADE_MARK_SIGN -> CharText.TRADE_MARK_SIGN
        CharCode.CARE_OF -> CharText.CARE_OF
        CharCode.CIRCLED_ZERO_WITH_SLASH -> CharText.CIRCLED_ZERO_WITH_SLASH
        CharCode.CIRCLED_ANTICLOCKWISE_ARROW -> CharText.CIRCLED_ANTICLOCKWISE_ARROW
        CharCode.CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH -> CharText.CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH
        CharCode.CIRCLED_CC -> CharText.CIRCLED_CC
        CharCode.CIRCLED_C_WITH_OVERLAID_BACKSLASH -> CharText.CIRCLED_C_WITH_OVERLAID_BACKSLASH
        CharCode.CIRCLED_HUMAN_FIGURE -> CharText.CIRCLED_HUMAN_FIGURE
        CharCode.CIRCLED_EQUALS -> CharText.CIRCLED_EQUALS

        else -> ""
    }

val Int.type: Int
    get() = when {
        isSwara(this) -> CharType.SWARA
        isWyanjana(this) -> CharType.WYANJANA
        isPili(this) -> CharType.PILI
        isSigns(this) -> CharType.LAKUNU
        else -> CharType.UNKNOWN
    }

fun Int.toSinglish(): Int = when (this) {
    CharCode.SMALL_LETTER_A -> CharCode.AYANNA
    CharCode.CAPITAL_LETTER_A -> CharCode.AEYANNA
    CharCode.SMALL_LETTER_B -> CharCode.ALPAPRAANA_BAYANNA
    CharCode.CAPITAL_LETTER_B -> CharCode.MAHAAPRAANA_BAYANNA
    CharCode.SMALL_LETTER_C -> CharCode.ALPAPRAANA_CAYANNA
    CharCode.CAPITAL_LETTER_C -> CharCode.MAHAAPRAANA_CAYANNA
    CharCode.SMALL_LETTER_D -> CharCode.ALPAPRAANA_DDAYANNA
    CharCode.CAPITAL_LETTER_D -> CharCode.MAHAAPRAANA_DDAYANNA
    CharCode.SMALL_LETTER_E -> CharCode.EYANNA
    CharCode.CAPITAL_LETTER_E -> CharCode.EEYANNA
    CharCode.SMALL_LETTER_F -> CharCode.FAYANNA
    CharCode.CAPITAL_LETTER_F -> CharCode.FAYANNA
    CharCode.SMALL_LETTER_G -> CharCode.ALPAPRAANA_GAYANNA
    CharCode.CAPITAL_LETTER_G -> CharCode.MAHAAPRAANA_GAYANNA
    CharCode.SMALL_LETTER_H -> CharCode.HAYANNA
    CharCode.CAPITAL_LETTER_H -> CharCode.HAYANNA
    CharCode.SMALL_LETTER_I -> CharCode.IYANNA
    CharCode.CAPITAL_LETTER_I -> CharCode.IIYANNA
    CharCode.SMALL_LETTER_J -> CharCode.ALPAPRAANA_JAYANNA
    CharCode.CAPITAL_LETTER_J -> CharCode.MAHAAPRAANA_JAYANNA
    CharCode.SMALL_LETTER_K -> CharCode.ALPAPRAANA_KAYANNA
    CharCode.CAPITAL_LETTER_K -> CharCode.MAHAAPRAANA_KAYANNA
    CharCode.SMALL_LETTER_L -> CharCode.DANTAJA_LAYANNA
    CharCode.CAPITAL_LETTER_L -> CharCode.MUURDHAJA_LAYANNA
    CharCode.SMALL_LETTER_M -> CharCode.MAYANNA
    CharCode.CAPITAL_LETTER_M -> CharCode.MAYANNA
    CharCode.SMALL_LETTER_N -> CharCode.DANTAJA_NAYANNA
    CharCode.CAPITAL_LETTER_N -> CharCode.MUURDHAJA_NAYANNA
    CharCode.SMALL_LETTER_O -> CharCode.OYANNA
    CharCode.CAPITAL_LETTER_O -> CharCode.OOYANNA
    CharCode.SMALL_LETTER_P -> CharCode.ALPAPRAANA_PAYANNA
    CharCode.CAPITAL_LETTER_P -> CharCode.MAHAAPRAANA_PAYANNA
    CharCode.SMALL_LETTER_Q -> CharCode.ALPAPRAANA_DAYANNA
    CharCode.CAPITAL_LETTER_Q -> CharCode.MAHAAPRAANA_DAYANNA
    CharCode.SMALL_LETTER_R -> CharCode.RAYANNA
    CharCode.CAPITAL_LETTER_R -> CharCode.IRUYANNA
    CharCode.SMALL_LETTER_S -> CharCode.DANTAJA_SAYANNA
    CharCode.CAPITAL_LETTER_S -> CharCode.MUURDHAJA_SAYANNA
    CharCode.SMALL_LETTER_T -> CharCode.ALPAPRAANA_TTAYANNA
    CharCode.CAPITAL_LETTER_T -> CharCode.MAHAAPRAANA_TTAYANNA
    CharCode.SMALL_LETTER_U -> CharCode.UYANNA
    CharCode.CAPITAL_LETTER_U -> CharCode.UUYANNA
    CharCode.SMALL_LETTER_V -> CharCode.VAYANNA
    CharCode.CAPITAL_LETTER_V -> CharCode.VAYANNA
    CharCode.SMALL_LETTER_W -> CharCode.VAYANNA
    CharCode.CAPITAL_LETTER_W -> CharCode.VAYANNA
    CharCode.SMALL_LETTER_X -> CharCode.SIGN_ANUSVARAYA
    CharCode.CAPITAL_LETTER_X -> CharCode.SIGN_VISARGAYA
    CharCode.SMALL_LETTER_Y -> CharCode.YAYANNA
    CharCode.CAPITAL_LETTER_Y -> CharCode.YAYANNA
    else -> this
}

@Retention(AnnotationRetention.SOURCE)
annotation class CharCode {
    companion object {
        const val AYANNA: Int = 3461
        const val AAYANNA: Int = 3462
        const val AEYANNA: Int = 3463
        const val AEEYANNA: Int = 3464
        const val IYANNA: Int = 3465
        const val IIYANNA: Int = 3466
        const val UYANNA: Int = 3467
        const val UUYANNA: Int = 3468
        const val IRUYANNA: Int = 3469
        const val IRUUYANNA: Int = 3470
        const val ILUYANNA: Int = 3471
        const val ILUUYANNA: Int = 3472
        const val EYANNA: Int = 3473
        const val EEYANNA: Int = 3474
        const val AIYANNA: Int = 3475
        const val OYANNA: Int = 3476
        const val OOYANNA: Int = 3477
        const val AUYANNA: Int = 3478
        const val ALPAPRAANA_KAYANNA: Int = 3482
        const val MAHAAPRAANA_KAYANNA: Int = 3483
        const val ALPAPRAANA_GAYANNA: Int = 3484
        const val MAHAAPRAANA_GAYANNA: Int = 3485
        const val KANTAJA_NAASIKYAYA: Int = 3486
        const val SANYAKA_GAYANNA: Int = 3487
        const val ALPAPRAANA_CAYANNA: Int = 3488
        const val MAHAAPRAANA_CAYANNA: Int = 3489
        const val ALPAPRAANA_JAYANNA: Int = 3490
        const val MAHAAPRAANA_JAYANNA: Int = 3491
        const val TAALUJA_NAASIKYAYA: Int = 3492
        const val TAALUJA_SANYOOGA_NAAKSIKYAYA: Int = 3493
        const val SANYAKA_JAYANNA: Int = 3494
        const val ALPAPRAANA_TTAYANNA: Int = 3495
        const val MAHAAPRAANA_TTAYANNA: Int = 3496
        const val ALPAPRAANA_DDAYANNA: Int = 3497
        const val MAHAAPRAANA_DDAYANNA: Int = 3498
        const val MUURDHAJA_NAYANNA: Int = 3499
        const val SANYAKA_DDAYANNA: Int = 3500
        const val ALPAPRAANA_TAYANNA: Int = 3501
        const val MAHAAPRAANA_TAYANNA: Int = 3502
        const val ALPAPRAANA_DAYANNA: Int = 3503
        const val MAHAAPRAANA_DAYANNA: Int = 3504
        const val DANTAJA_NAYANNA: Int = 3505
        const val SANYAKA_DAYANNA: Int = 3507
        const val ALPAPRAANA_PAYANNA: Int = 3508
        const val MAHAAPRAANA_PAYANNA: Int = 3509
        const val ALPAPRAANA_BAYANNA: Int = 3510
        const val MAHAAPRAANA_BAYANNA: Int = 3511
        const val MAYANNA: Int = 3512
        const val AMBA_BAYANNA: Int = 3513
        const val YAYANNA: Int = 3514
        const val RAYANNA: Int = 3515
        const val DANTAJA_LAYANNA: Int = 3517
        const val VAYANNA: Int = 3520
        const val TAALUJA_SAYANNA: Int = 3521
        const val MUURDHAJA_SAYANNA: Int = 3522
        const val DANTAJA_SAYANNA: Int = 3523
        const val HAYANNA: Int = 3524
        const val MUURDHAJA_LAYANNA: Int = 3525
        const val FAYANNA: Int = 3526
        const val AELA_PILLA: Int = 3535
        const val KETTI_AEDA_PILLA: Int = 3536
        const val DIGA_AEDA_PILLA: Int = 3537
        const val KETTI_IS_PILLA: Int = 3538
        const val DIGA_IS_PILLA: Int = 3539
        const val KETTI_PAA_PILLA: Int = 3540
        const val DIGA_PAA_PILLA: Int = 3542
        const val GAETTA_PILLA: Int = 3544
        const val KOMBUVA: Int = 3545
        const val DIGA_KOMBUVA: Int = 3546
        const val KOMBU_DEKA: Int = 3547
        const val KOMBUVA_HAA_AELA_PILLA: Int = 3548
        const val KOMBUVA_HAA_DIGA_AELA_PILLA: Int = 3549
        const val KOMBUVA_HAA_GAYANUKITTA: Int = 3550
        const val GAYANUKITTA: Int = 3551
        const val DIGA_GAYANUKITTA: Int = 3571
        const val DIGA_GAETTA_PILLA: Int = 3570
        const val SIGN_ANUSVARAYA: Int = 3458
        const val SIGN_VISARGAYA: Int = 3459
        const val SIGN_AL_LAKUNA: Int = 3530

        /** Zero Width Joiner | 8205 */
        const val ZERO_WIDTH_JOINER: Int = 8205

        /** යංශය (් + ZWJ + ය) | 3530 + 8205 + 3514 */
        const val SIGN_YANSHAYA: Int = 15249

        /** රකාරාංශය (් + ZWJ + ර) | 3530 + 8205 + 3515 */
        const val SIGN_RAKARANSHAYA: Int = 152501

        /** රේපය (ර + ් + ZWJ) | 3515 + 3530 + 8205 */
        const val SIGN_REEPAYA: Int = 152502

        /** ළු (ළ + ු) | 3525 + 3540 */
        const val MUURDHAJA_LUYANNA: Int = 7065

        const val CAPITAL_LETTER_A: Int = 65
        const val CAPITAL_LETTER_B: Int = 66
        const val CAPITAL_LETTER_C: Int = 67
        const val CAPITAL_LETTER_D: Int = 68
        const val CAPITAL_LETTER_E: Int = 69
        const val CAPITAL_LETTER_F: Int = 70
        const val CAPITAL_LETTER_G: Int = 71
        const val CAPITAL_LETTER_H: Int = 72
        const val CAPITAL_LETTER_I: Int = 73
        const val CAPITAL_LETTER_J: Int = 74
        const val CAPITAL_LETTER_K: Int = 75
        const val CAPITAL_LETTER_L: Int = 76
        const val CAPITAL_LETTER_M: Int = 77
        const val CAPITAL_LETTER_N: Int = 78
        const val CAPITAL_LETTER_O: Int = 79
        const val CAPITAL_LETTER_P: Int = 80
        const val CAPITAL_LETTER_Q: Int = 81
        const val CAPITAL_LETTER_R: Int = 82
        const val CAPITAL_LETTER_S: Int = 83
        const val CAPITAL_LETTER_T: Int = 84
        const val CAPITAL_LETTER_U: Int = 85
        const val CAPITAL_LETTER_V: Int = 86
        const val CAPITAL_LETTER_W: Int = 87
        const val CAPITAL_LETTER_X: Int = 88
        const val CAPITAL_LETTER_Y: Int = 89
        const val CAPITAL_LETTER_Z: Int = 90

        const val SMALL_LETTER_A: Int = 97
        const val SMALL_LETTER_B: Int = 98
        const val SMALL_LETTER_C: Int = 99
        const val SMALL_LETTER_D: Int = 100
        const val SMALL_LETTER_E: Int = 101
        const val SMALL_LETTER_F: Int = 102
        const val SMALL_LETTER_G: Int = 103
        const val SMALL_LETTER_H: Int = 104
        const val SMALL_LETTER_I: Int = 105
        const val SMALL_LETTER_J: Int = 106
        const val SMALL_LETTER_K: Int = 107
        const val SMALL_LETTER_L: Int = 108
        const val SMALL_LETTER_M: Int = 109
        const val SMALL_LETTER_N: Int = 110
        const val SMALL_LETTER_O: Int = 111
        const val SMALL_LETTER_P: Int = 112
        const val SMALL_LETTER_Q: Int = 113
        const val SMALL_LETTER_R: Int = 114
        const val SMALL_LETTER_S: Int = 115
        const val SMALL_LETTER_T: Int = 116
        const val SMALL_LETTER_U: Int = 117
        const val SMALL_LETTER_V: Int = 118
        const val SMALL_LETTER_W: Int = 119
        const val SMALL_LETTER_X: Int = 120
        const val SMALL_LETTER_Y: Int = 121
        const val SMALL_LETTER_Z: Int = 122

        const val ZERO: Int = 48
        const val ONE: Int = 49
        const val TWO: Int = 50
        const val THREE: Int = 51
        const val FOUR: Int = 52
        const val FIVE: Int = 53
        const val SIX: Int = 54
        const val SEVEN: Int = 55
        const val EIGHT: Int = 56
        const val NINE: Int = 57
        const val SPACE: Int = 32
        const val GRAVE_ACCENT: Int = 96
        const val TILDE: Int = 126
        const val EXCLAMATION_MARK: Int = 33
        const val COMMERCIAL_AT: Int = 64
        const val NUMBER_SIGN: Int = 35
        const val DOLLAR_SIGN: Int = 36
        const val PERCENT_SIGN: Int = 37
        const val CIRCUMFLEX_ACCENT: Int = 94
        const val AMPERSAND: Int = 38
        const val ASTERISK: Int = 42
        const val LEFT_PARENTHESIS: Int = 40
        const val RIGHT_PARENTHESIS: Int = 41
        const val HYPHEN_MINUS: Int = 45
        const val LOW_LINE: Int = 95
        const val EQUALS_SIGN: Int = 61
        const val PLUS_SIGN: Int = 43
        const val LEFT_SQUARE_BRACKET: Int = 91
        const val RIGHT_SQUARE_BRACKET: Int = 93
        const val LEFT_CURLY_BRACKET: Int = 123
        const val RIGHT_CURLY_BRACKET: Int = 125
        const val REVERSE_SOLIDUS: Int = 92
        const val VERTICAL_LINE: Int = 124
        const val SEMICOLON: Int = 59
        const val COLON: Int = 58
        const val APOSTROPHE: Int = 39
        const val QUOTATION_MARK: Int = 34
        const val COMMA: Int = 44
        const val FULL_STOP: Int = 46
        const val LESS_THAN_SIGN: Int = 60
        const val GREATER_THAN_SIGN: Int = 62
        const val SOLIDUS: Int = 47
        const val QUESTION_MARK: Int = 63
        const val DIVISION_SIGN: Int = 247
        const val MULTIPLICATION_SIGN: Int = 215
        const val PLUS_MINUS_SIGN: Int = 177
        const val BULLET: Int = 8226
        const val WHITE_BULLET: Int = 9702
        const val BLACK_SMALL_SQUARE: Int = 9642
        const val WHITE_SMALL_SQUARE: Int = 9643
        const val TRIANGULAR_BULLET: Int = 8227
        const val SQUARE_ROOT: Int = 8730
        const val GREEK_SMALL_LETTER_PI: Int = 960
        const val PILCROW_SIGN: Int = 182
        const val INCREMENT: Int = 8710
        const val EURO_SIGN: Int = 8364
        const val YEN_SIGN: Int = 165
        const val POUND_SIGN: Int = 163
        const val CENT_SIGN: Int = 162
        const val DEGREE_SIGN: Int = 176
        const val COPYRIGHT_SIGN: Int = 169
        const val REGISTERED_SIGN: Int = 174
        const val TRADE_MARK_SIGN: Int = 8482
        const val CARE_OF: Int = 8453
        const val CIRCLED_ZERO_WITH_SLASH: Int = 127245
        const val CIRCLED_ANTICLOCKWISE_ARROW: Int = 127246
        const val CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH: Int = 127247
        const val CIRCLED_CC: Int = 127341
        const val CIRCLED_C_WITH_OVERLAID_BACKSLASH: Int = 127342
        const val CIRCLED_HUMAN_FIGURE: Int = 127343
        const val CIRCLED_EQUALS: Int = 8860

        const val MARK_SANYAKA: Int = -25
    }
}

@Retention(AnnotationRetention.SOURCE)
annotation class CharText {
    companion object {
        const val AYANNA: String = "අ"
        const val AAYANNA: String = "ආ"
        const val AEYANNA: String = "ඇ"
        const val AEEYANNA: String = "ඈ"
        const val IYANNA: String = "ඉ"
        const val IIYANNA: String = "ඊ"
        const val UYANNA: String = "උ"
        const val UUYANNA: String = "ඌ"
        const val IRUYANNA: String = "ඍ"
        const val IRUUYANNA: String = "ඎ"
        const val ILUYANNA: String = "ඏ"
        const val ILUUYANNA: String = "ඐ"
        const val EYANNA: String = "එ"
        const val EEYANNA: String = "ඒ"
        const val AIYANNA: String = "ඓ"
        const val OYANNA: String = "ඔ"
        const val OOYANNA: String = "ඕ"
        const val AUYANNA: String = "ඖ"
        const val ALPAPRAANA_KAYANNA: String = "ක"
        const val MAHAAPRAANA_KAYANNA: String = "ඛ"
        const val ALPAPRAANA_GAYANNA: String = "ග"
        const val MAHAAPRAANA_GAYANNA: String = "ඝ"
        const val KANTAJA_NAASIKYAYA: String = "ඞ"
        const val SANYAKA_GAYANNA: String = "ඟ"
        const val ALPAPRAANA_CAYANNA: String = "ච"
        const val MAHAAPRAANA_CAYANNA: String = "ඡ"
        const val ALPAPRAANA_JAYANNA: String = "ජ"
        const val MAHAAPRAANA_JAYANNA: String = "ඣ"
        const val TAALUJA_NAASIKYAYA: String = "ඤ"
        const val TAALUJA_SANYOOGA_NAAKSIKYAYA: String = "ඥ"
        const val SANYAKA_JAYANNA: String = "ඦ"
        const val ALPAPRAANA_TTAYANNA: String = "ට"
        const val MAHAAPRAANA_TTAYANNA: String = "ඨ"
        const val ALPAPRAANA_DDAYANNA: String = "ඩ"
        const val MAHAAPRAANA_DDAYANNA: String = "ඪ"
        const val MUURDHAJA_NAYANNA: String = "ණ"
        const val SANYAKA_DDAYANNA: String = "ඬ"
        const val ALPAPRAANA_TAYANNA: String = "ත"
        const val MAHAAPRAANA_TAYANNA: String = "ථ"
        const val ALPAPRAANA_DAYANNA: String = "ද"
        const val MAHAAPRAANA_DAYANNA: String = "ධ"
        const val DANTAJA_NAYANNA: String = "න"
        const val SANYAKA_DAYANNA: String = "ඳ"
        const val ALPAPRAANA_PAYANNA: String = "ප"
        const val MAHAAPRAANA_PAYANNA: String = "ඵ"
        const val ALPAPRAANA_BAYANNA: String = "බ"
        const val MAHAAPRAANA_BAYANNA: String = "භ"
        const val MAYANNA: String = "ම"
        const val AMBA_BAYANNA: String = "ඹ"
        const val YAYANNA: String = "ය"
        const val RAYANNA: String = "ර"
        const val DANTAJA_LAYANNA: String = "ල"
        const val VAYANNA: String = "ව"
        const val TAALUJA_SAYANNA: String = "ශ"
        const val MUURDHAJA_SAYANNA: String = "ෂ"
        const val DANTAJA_SAYANNA: String = "ස"
        const val HAYANNA: String = "හ"
        const val MUURDHAJA_LAYANNA: String = "ළ"
        const val FAYANNA: String = "ෆ"
        const val AELA_PILLA: String = "ා"
        const val KETTI_AEDA_PILLA: String = "ැ"
        const val DIGA_AEDA_PILLA: String = "ෑ"
        const val KETTI_IS_PILLA: String = "ි"
        const val DIGA_IS_PILLA: String = "ී"
        const val KETTI_PAA_PILLA: String = "ු"
        const val DIGA_PAA_PILLA: String = "ූ"
        const val GAETTA_PILLA: String = "ෘ"
        const val KOMBUVA: String = "ෙ"
        const val DIGA_KOMBUVA: String = "ේ"
        const val KOMBU_DEKA: String = "ෛ"
        const val KOMBUVA_HAA_AELA_PILLA: String = "ො"
        const val KOMBUVA_HAA_DIGA_AELA_PILLA: String = "ෝ"
        const val KOMBUVA_HAA_GAYANUKITTA: String = "ෞ"
        const val GAYANUKITTA: String = "ෟ"
        const val DIGA_GAYANUKITTA: String = "ෳ"
        const val DIGA_GAETTA_PILLA: String = "ෲ"
        const val SIGN_ANUSVARAYA: String = "ං"
        const val SIGN_VISARGAYA: String = "ඃ"
        const val SIGN_AL_LAKUNA: String = "්"
        const val ZERO_WIDTH_JOINER: String = "\u200D"

        /** (් + ZWJ + ය) | 3530 + 8205 + 3514 */
        const val SIGN_YANSHAYA: String = "්\u200Dය"

        /** (් + ZWJ + ර) | 3530 + 8205 + 3515 */
        const val SIGN_RAKARANSHAYA: String = "්\u200Dර"

        /** රේපය (ර + ් + ZWJ) | 3515 + 3530 + 8205 */
        const val SIGN_REEPAYA: String = "ර්\u200D"

        /** ළු (ළ + ු) | 3525 + 3540 */
        const val MUURDHAJA_LUYANNA: String = "ළු"

        const val CAPITAL_LETTER_A: String = "A"
        const val CAPITAL_LETTER_B: String = "B"
        const val CAPITAL_LETTER_C: String = "C"
        const val CAPITAL_LETTER_D: String = "D"
        const val CAPITAL_LETTER_E: String = "E"
        const val CAPITAL_LETTER_F: String = "F"
        const val CAPITAL_LETTER_G: String = "G"
        const val CAPITAL_LETTER_H: String = "H"
        const val CAPITAL_LETTER_I: String = "I"
        const val CAPITAL_LETTER_J: String = "J"
        const val CAPITAL_LETTER_K: String = "K"
        const val CAPITAL_LETTER_L: String = "L"
        const val CAPITAL_LETTER_M: String = "M"
        const val CAPITAL_LETTER_N: String = "N"
        const val CAPITAL_LETTER_O: String = "O"
        const val CAPITAL_LETTER_P: String = "P"
        const val CAPITAL_LETTER_Q: String = "Q"
        const val CAPITAL_LETTER_R: String = "R"
        const val CAPITAL_LETTER_S: String = "S"
        const val CAPITAL_LETTER_T: String = "T"
        const val CAPITAL_LETTER_U: String = "U"
        const val CAPITAL_LETTER_V: String = "V"
        const val CAPITAL_LETTER_W: String = "W"
        const val CAPITAL_LETTER_X: String = "X"
        const val CAPITAL_LETTER_Y: String = "Y"
        const val CAPITAL_LETTER_Z: String = "Z"

        const val SMALL_LETTER_A: String = "a"
        const val SMALL_LETTER_B: String = "b"
        const val SMALL_LETTER_C: String = "c"
        const val SMALL_LETTER_D: String = "d"
        const val SMALL_LETTER_E: String = "e"
        const val SMALL_LETTER_F: String = "f"
        const val SMALL_LETTER_G: String = "g"
        const val SMALL_LETTER_H: String = "h"
        const val SMALL_LETTER_I: String = "i"
        const val SMALL_LETTER_J: String = "j"
        const val SMALL_LETTER_K: String = "k"
        const val SMALL_LETTER_L: String = "l"
        const val SMALL_LETTER_M: String = "m"
        const val SMALL_LETTER_N: String = "n"
        const val SMALL_LETTER_O: String = "o"
        const val SMALL_LETTER_P: String = "p"
        const val SMALL_LETTER_Q: String = "q"
        const val SMALL_LETTER_R: String = "r"
        const val SMALL_LETTER_S: String = "s"
        const val SMALL_LETTER_T: String = "t"
        const val SMALL_LETTER_U: String = "u"
        const val SMALL_LETTER_V: String = "v"
        const val SMALL_LETTER_W: String = "w"
        const val SMALL_LETTER_X: String = "x"
        const val SMALL_LETTER_Y: String = "y"
        const val SMALL_LETTER_Z: String = "z"

        const val ZERO: String = "0"
        const val ONE: String = "1"
        const val TWO: String = "2"
        const val THREE: String = "3"
        const val FOUR: String = "4"
        const val FIVE: String = "5"
        const val SIX: String = "6"
        const val SEVEN: String = "7"
        const val EIGHT: String = "8"
        const val NINE: String = "9"

        const val SPACE: String = " "
        const val GRAVE_ACCENT: String = "`"
        const val TILDE: String = "~"
        const val EXCLAMATION_MARK: String = "!"
        const val COMMERCIAL_AT: String = "@"
        const val NUMBER_SIGN: String = "#"
        const val DOLLAR_SIGN: String = "$"
        const val PERCENT_SIGN: String = "%"
        const val CIRCUMFLEX_ACCENT: String = "^"
        const val AMPERSAND: String = "&"
        const val ASTERISK: String = "*"
        const val LEFT_PARENTHESIS: String = "("
        const val RIGHT_PARENTHESIS: String = ")"
        const val HYPHEN_MINUS: String = "-"
        const val LOW_LINE: String = "_"
        const val EQUALS_SIGN: String = "="
        const val PLUS_SIGN: String = "+"
        const val LEFT_SQUARE_BRACKET: String = "["
        const val RIGHT_SQUARE_BRACKET: String = "]"
        const val LEFT_CURLY_BRACKET: String = "{"
        const val RIGHT_CURLY_BRACKET: String = "}"
        const val REVERSE_SOLIDUS: String = "\\"
        const val VERTICAL_LINE: String = "|"
        const val SEMICOLON: String = ";"
        const val COLON: String = ":"
        const val APOSTROPHE: String = "'"
        const val QUOTATION_MARK: String = "\""
        const val COMMA: String = ","
        const val FULL_STOP: String = "."
        const val LESS_THAN_SIGN: String = "<"
        const val GREATER_THAN_SIGN: String = ">"
        const val SOLIDUS: String = "/"
        const val QUESTION_MARK: String = "?"
        const val DIVISION_SIGN: String = "÷"
        const val MULTIPLICATION_SIGN: String = "×"
        const val PLUS_MINUS_SIGN: String = "±"
        const val BULLET: String = "•"
        const val WHITE_BULLET: String = "◦"
        const val BLACK_SMALL_SQUARE: String = "▪"
        const val WHITE_SMALL_SQUARE: String = "▫"
        const val TRIANGULAR_BULLET: String = "‣"
        const val SQUARE_ROOT: String = "√"
        const val GREEK_SMALL_LETTER_PI: String = "π"
        const val PILCROW_SIGN: String = "¶"
        const val INCREMENT: String = "∆"
        const val EURO_SIGN: String = "€"
        const val YEN_SIGN: String = "¥"
        const val POUND_SIGN: String = "£"
        const val CENT_SIGN: String = "¢"
        const val DEGREE_SIGN: String = "°"
        const val COPYRIGHT_SIGN: String = "©"
        const val REGISTERED_SIGN: String = "®"
        const val TRADE_MARK_SIGN: String = "™"
        const val CARE_OF: String = "℅"
        const val CIRCLED_ZERO_WITH_SLASH: String = "🄍"
        const val CIRCLED_ANTICLOCKWISE_ARROW: String = "🄎"
        const val CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH: String = "🄏"
        const val CIRCLED_CC: String = "🅭"
        const val CIRCLED_C_WITH_OVERLAID_BACKSLASH: String = "🅮"
        const val CIRCLED_HUMAN_FIGURE: String = "🅯"
        const val CIRCLED_EQUALS: String = "⊜"
    }
}

@Retention(AnnotationRetention.SOURCE)
annotation class CharType {
    companion object {
        const val LAKUNU: Int = 1
        const val PILI: Int = 2
        const val SWARA: Int = 3
        const val UNKNOWN: Int = 4
        const val WYANJANA: Int = 5
    }
}
