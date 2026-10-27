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
 * Helakuru phonetic keymap.
 *
 * Transcribed from helakuru-ime's `helakuru_ime/keymap.py`, which is the 2018
 * Helakuru phonetic map with the shipped file's value/comment mismatches
 * corrected. Keys are ASCII and matched longest-first. A consonant is stored
 * as its bare base letter; [render] supplies the hal and turns it into a vowel
 * sign, a conjunct or a mark as the following keys demand.
 */
object SinglishKeymap {

    private const val HAL = "\u0DCA"      // ්  al-lakuna
    private const val ZWJ = "\u200D"      //     yansaya / rakaransaya / repaya
    private const val ZWNJ = "\u200C"     //     joiner

    private const val C = 0
    private const val V = 1
    private const val M = 2
    private const val J = 3
    private const val LIT = 4

    private class Tok(val kind: Int, val a: String, val b: String = "")

    private val TABLE: Map<String, Tok> = mapOf(
        // --- consonants: base letter, no hal -------------------------
        "k" to Tok(C, "ක"),
        "K" to Tok(C, "ඛ"),
        "kh" to Tok(C, "ඛ"),
        "g" to Tok(C, "ග"),
        "G" to Tok(C, "ඝ"),
        "gh" to Tok(C, "ඝ"),
        "c" to Tok(C, "ච"),
        "C" to Tok(C, "ච"),
        "ch" to Tok(C, "ච"),
        "Ch" to Tok(C, "ඡ"),
        "j" to Tok(C, "ජ"),
        "J" to Tok(C, "ඣ"),
        "t" to Tok(C, "ට"),
        "T" to Tok(C, "ඨ"),
        "d" to Tok(C, "ඩ"),
        "D" to Tok(C, "ඪ"),
        "N" to Tok(C, "ණ"),
        "th" to Tok(C, "ත"),
        "Th" to Tok(C, "ථ"),
        "dh" to Tok(C, "ද"),
        "Dh" to Tok(C, "ධ"),
        "q" to Tok(C, "ද"),
        "Q" to Tok(C, "ධ"),
        "n" to Tok(C, "න"),
        "p" to Tok(C, "ප"),
        "P" to Tok(C, "ඵ"),
        "ph" to Tok(C, "ඵ"),
        "b" to Tok(C, "බ"),
        "bh" to Tok(C, "භ"),
        "Bh" to Tok(C, "භ"),
        "m" to Tok(C, "ම"),
        "M" to Tok(C, "ම"),
        "y" to Tok(C, "ය"),
        "r" to Tok(C, "ර"),
        "R" to Tok(C, "ර"),
        "l" to Tok(C, "ල"),
        "w" to Tok(C, "ව"),
        "W" to Tok(C, "ව"),
        "v" to Tok(C, "ව"),
        "V" to Tok(C, "ව"),
        "sh" to Tok(C, "ශ"),
        "S" to Tok(C, "ෂ"),
        "s" to Tok(C, "ස"),
        "h" to Tok(C, "හ"),
        "L" to Tok(C, "ළ"),
        "f" to Tok(C, "ෆ"),
        "F" to Tok(C, "ෆ"),
        "zg" to Tok(C, "ඟ"),
        "Zg" to Tok(C, "ඟ"),
        "zD" to Tok(C, "ඞ"),
        "ZD" to Tok(C, "ඞ"),
        "X" to Tok(C, "ඞ"),
        "zj" to Tok(C, "ඦ"),
        "Zj" to Tok(C, "ඦ"),
        "zd" to Tok(C, "ඬ"),
        "Zd" to Tok(C, "ඬ"),
        "zq" to Tok(C, "ඳ"),
        "Zq" to Tok(C, "ඳ"),
        "zdh" to Tok(C, "ඳ"),
        "Zdh" to Tok(C, "ඳ"),
        "zk" to Tok(C, "ඤ"),
        "Zk" to Tok(C, "ඤ"),
        "zh" to Tok(C, "ඥ"),
        "Zh" to Tok(C, "ඥ"),
        "B" to Tok(C, "ඹ"),
        "NG" to Tok(C, "ක්‍ෂ"),
        "ksh" to Tok(C, "ක්‍ෂ"),

        // --- vowels: (independent letter, dependent sign) ------------
        "a" to Tok(V, "අ", ""),
        "aa" to Tok(V, "ආ", "ා"),
        "A" to Tok(V, "ඇ", "ැ"),
        "AA" to Tok(V, "ඈ", "ෑ"),
        "Aa" to Tok(V, "ඈ", "ෑ"),
        "i" to Tok(V, "ඉ", "ි"),
        "I" to Tok(V, "ඉ", "ි"),
        "ii" to Tok(V, "ඊ", "ී"),
        "u" to Tok(V, "උ", "ු"),
        "U" to Tok(V, "උ", "ු"),
        "uu" to Tok(V, "ඌ", "ූ"),
        "sRu" to Tok(V, "ඍ", "ෘ"),
        "sRuu" to Tok(V, "ඎ", "ෲ"),
        "e" to Tok(V, "එ", "ෙ"),
        "ee" to Tok(V, "ඒ", "ේ"),
        "ai" to Tok(V, "ඓ", "ෛ"),
        "E" to Tok(V, "ඓ", "ෛ"),
        "o" to Tok(V, "ඔ", "ො"),
        "O" to Tok(V, "ඔ", "ො"),
        "oo" to Tok(V, "ඕ", "ෝ"),
        "ou" to Tok(V, "ඖ", "ෞ"),
        "au" to Tok(V, "ඖ", "ෞ"),

        // --- marks: ං / ඃ, hal is swapped for the mark ----------------
        "x" to Tok(M, "ං"),
        "zn" to Tok(M, "ං"),
        "ng" to Tok(M, "ං"),
        "H" to Tok(M, "ඃ"),

        // --- joiners: zero width non-joiner -------------------------
        "z" to Tok(J, ZWNJ),
        "Z" to Tok(J, ZWNJ),
    )

    private val MAX_KEY = TABLE.keys.maxOf { it.length }

    /** Transliterate one raw word. Deterministic and side-effect free. */
    fun render(keys: String): String {
        val out = StringBuilder()
        var pending = false
        var i = 0
        while (i < keys.length) {
            val (tok, used) = token(keys, i)
            i += used
            when (tok.kind) {
                C -> {
                    if (pending) {
                        when (tok.a) {
                            // consonant + y / r -> yansaya / rakaransaya
                            "ය" -> out.replace(out.length - 1, out.length, HAL + ZWJ + "ය" + HAL)
                            "ර" -> out.replace(out.length - 1, out.length, HAL + ZWJ + "ර" + HAL)
                            else -> {
                                // repaya: ර් + consonant
                                if (out.endsWith("ර" + HAL)) out.append(ZWJ)
                                out.append(tok.a).append(HAL)
                            }
                        }
                    } else {
                        out.append(tok.a).append(HAL)
                    }
                    pending = true
                }

                V -> {
                    if (pending) {
                        out.setLength(out.length - 1)   // swap the hal for the sign
                        out.append(tok.b)
                    } else {
                        out.append(tok.a)               // word-initial or after a vowel
                    }
                    pending = false
                }

                M -> {
                    if (pending) out.setLength(out.length - 1)  // ං replaces the hal
                    out.append(tok.a)
                    pending = false
                }

                else -> {
                    out.append(tok.a)
                    pending = false
                }
            }
        }
        return out.toString()
    }

    /** Longest match at [i]; an unmapped key becomes a one-character literal. */
    private fun token(keys: String, i: Int): Pair<Tok, Int> {
        var len = minOf(MAX_KEY, keys.length - i)
        while (len > 0) {
            val tok = TABLE[keys.substring(i, i + len)]
            if (tok != null) return tok to len
            len--
        }
        return Tok(LIT, keys[i].toString()) to 1
    }
}
