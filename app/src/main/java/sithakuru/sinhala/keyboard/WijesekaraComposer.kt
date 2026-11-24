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
 * Wijesekara composing rules, kept free of Android types so they can be
 * unit tested. Wijesekara types some letters as two keys (a vowel and a
 * modifier, or a kombuva typed before its consonant); [type] says whether the
 * previous character must be replaced and what to write.
 */
class WijesekaraComposer {

    /** [erasePrevious]: replace the character before the cursor with [text]. */
    data class Edit(val erasePrevious: Boolean, val text: String)

    /** What the last key produced, as far as composing is concerned. */
    private var lastText: String = ""

    /* Without this, kombuva keeps going forward with each new letter. */
    private var kombuvaNew: Boolean = false

    /** Forget the last key, so the next one reads the text before the cursor. */
    fun reset() {
        lastText = ""
    }

    /**
     * @param input the key's character code.
     * @param charBeforeCursor the character in front of the cursor, used when
     *   there is no last key to go on (after [reset] or [positionChanged]).
     * @param positionChanged the cursor moved since the last key.
     */
    fun type(input: Int, charBeforeCursor: Char?, positionChanged: Boolean): Edit {
        var erasePrevious = false
        var lastTextOverride: String? = null

        if (lastText.isBlank() || positionChanged)
            lastText = (charBeforeCursor ?: ' ').toString()

        val output: String = when (lastText) {
            CharText.AYANNA -> {
                when (input) {
                    CharCode.AELA_PILLA -> {
                        erasePrevious = true
                        CharText.AAYANNA
                    }

                    CharCode.KETTI_AEDA_PILLA -> {
                        erasePrevious = true
                        CharText.AEYANNA
                    }

                    CharCode.DIGA_AEDA_PILLA -> {
                        erasePrevious = true
                        CharText.AEEYANNA
                    }

                    else -> input.text
                }
            }

            CharText.UYANNA ->
                if (input == CharCode.GAYANUKITTA) {
                    erasePrevious = true
                    CharText.UUYANNA
                } else input.text

            CharText.IRUYANNA ->
                if (input == CharCode.GAETTA_PILLA) {
                    erasePrevious = true
                    CharText.IRUUYANNA
                } else input.text

            CharText.ILUYANNA ->
                if (input == CharCode.GAYANUKITTA) {
                    erasePrevious = true
                    CharText.ILUUYANNA
                } else input.text

            CharText.EYANNA ->
                if (input == CharCode.SIGN_AL_LAKUNA) {
                    erasePrevious = true
                    CharText.EEYANNA
                } else input.text

            CharText.OYANNA -> {
                when (input) {
                    CharCode.SIGN_AL_LAKUNA -> {
                        erasePrevious = true
                        CharText.OOYANNA
                    }

                    CharCode.GAYANUKITTA -> {
                        erasePrevious = true
                        CharText.AUYANNA
                    }

                    else -> input.text
                }
            }

            CharText.KOMBUVA -> {
                if (kombuvaNew) {
                    erasePrevious = true
                    when (input.type) {
                        CharType.SWARA,
                        CharType.WYANJANA -> {
                            lastTextOverride = CharText.KOMBUVA
                            if (input == CharCode.EYANNA)
                                CharText.AIYANNA
                            else
                                input.text + CharText.KOMBUVA
                        }

                        else -> {
                            when (input) {
                                CharCode.KOMBUVA -> CharText.KOMBU_DEKA
                                CharCode.SIGN_AL_LAKUNA -> CharText.DIGA_KOMBUVA
                                CharCode.AELA_PILLA -> CharText.KOMBUVA_HAA_AELA_PILLA
                                CharCode.GAYANUKITTA -> CharText.KOMBUVA_HAA_GAYANUKITTA
                                else -> input.text
                            }
                        }
                    }
                } else if (input == CharCode.SIGN_RAKARANSHAYA) {
                    erasePrevious = true
                    lastTextOverride = CharText.KOMBUVA
                    CharText.SIGN_RAKARANSHAYA + CharText.KOMBUVA
                } else input.text
            }

            CharText.KOMBUVA_HAA_AELA_PILLA -> {
                if (input == CharCode.SIGN_AL_LAKUNA) {
                    erasePrevious = true
                    CharText.KOMBUVA_HAA_DIGA_AELA_PILLA
                } else input.text
            }

            CharText.KOMBU_DEKA -> {
                when (input.type) {
                    CharType.SWARA, CharType.WYANJANA -> {
                        erasePrevious = true
                        input.text + CharText.KOMBU_DEKA
                    }

                    else -> input.text
                }
            }

            else -> {
                if (input == CharCode.SIGN_REEPAYA) {
                    erasePrevious = true
                    CharText.SIGN_REEPAYA + lastText
                } else input.text
            }
        }

        kombuvaNew = output == CharText.KOMBUVA
        lastText = lastTextOverride ?: output
        return Edit(erasePrevious, output)
    }
}
