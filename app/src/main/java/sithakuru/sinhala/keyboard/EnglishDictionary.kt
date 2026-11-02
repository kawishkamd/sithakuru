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

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

data class SuggestionResult(
    val raw: String,
    val primary: String,
    val secondary: String? = null
)

/**
 * Pure suggestion engine that takes a frequency-ranked word list and word set.
 * Independent of Android framework classes for easy unit testing.
 *
 * Note: [words] MUST remain in frequency-ranked order (most frequent first)
 * so sequential prefix scans naturally yield the most common words first.
 */
class SuggestionEngine(
    val words: List<String>,
    val wordSet: Set<String>
) {
    /**
     * Given the currently typed [input], returns suggestions wrapped in a [SuggestionResult],
     * or null if no suggestions are found or input is blank.
     */
    fun getSuggestions(input: String): SuggestionResult? {
        if (input.isBlank() || words.isEmpty()) return null

        val lower = input.lowercase()
        val exactMatch = wordSet.contains(lower)
        val directFix = REPLACEMENTS[lower]

        val completions = mutableListOf<String>()
        if (directFix != null) {
            completions.add(directFix)
        }

        // Prefix match (words are frequency-ranked)
        for (w in words) {
            if (completions.size >= 4) break
            if (w.startsWith(lower) && w != lower && !completions.contains(w)) {
                completions.add(w)
            }
        }

        // Edit distance 1 if no completions found, length >= 3, and not an exact match
        if (completions.isEmpty() && lower.length >= 3 && !exactMatch) {
            for (w in words) {
                if (completions.size >= 3) break
                if (kotlin.math.abs(w.length - lower.length) <= 1 && isEditDistanceOne(lower, w)) {
                    completions.add(w)
                }
            }
        }

        if (exactMatch) {
            val styledInput = applyCasing(input, input)
            val secondary = completions.firstOrNull()?.let { applyCasing(input, it) }
            return SuggestionResult(raw = styledInput, primary = styledInput, secondary = secondary)
        }

        if (completions.isEmpty()) {
            return null
        }

        val best = applyCasing(input, completions[0])
        val alt = if (completions.size > 1) applyCasing(input, completions[1]) else null

        return SuggestionResult(raw = input, primary = best, secondary = alt)
    }

    fun isEditDistanceOne(s1: String, s2: String): Boolean {
        val len1 = s1.length
        val len2 = s2.length
        if (kotlin.math.abs(len1 - len2) > 1) return false

        var i = 0
        var j = 0
        var diff = 0

        while (i < len1 && j < len2) {
            if (s1[i] != s2[j]) {
                diff++
                if (diff > 1) return false
                when {
                    len1 > len2 -> i++
                    len2 > len1 -> j++
                    else -> {
                        // check transposition (e.g. thsi -> this)
                        if (i + 1 < len1 && j + 1 < len2 && s1[i] == s2[j + 1] && s1[i + 1] == s2[j]) {
                            i += 2
                            j += 2
                            continue
                        }
                        i++
                        j++
                    }
                }
            } else {
                i++
                j++
            }
        }
        if (i < len1 || j < len2) diff++
        return diff == 1
    }

    fun applyCasing(source: String, target: String): String {
        return when {
            source.all { it.isUpperCase() } && source.length > 1 -> target.uppercase()
            source.firstOrNull()?.isUpperCase() == true -> target.replaceFirstChar { it.uppercase() }
            else -> target
        }
    }

    companion object {
        val REPLACEMENTS: Map<String, String> = mapOf(
            "dont" to "don't",
            "cant" to "can't",
            "wont" to "won't",
            "didnt" to "didn't",
            "isnt" to "isn't",
            "arent" to "aren't",
            "wasnt" to "wasn't",
            "werent" to "weren't",
            "havent" to "haven't",
            "hasnt" to "hasn't",
            "hadnt" to "hadn't",
            "wouldnt" to "wouldn't",
            "couldnt" to "couldn't",
            "shouldnt" to "shouldn't",
            "im" to "I'm",
            "ive" to "I've",
            "ill" to "I'll",
            "id" to "I'd",
            "youre" to "you're",
            "youve" to "you've",
            "youll" to "you'll",
            "youd" to "you'd",
            "theyre" to "they're",
            "theyve" to "they've",
            "theyll" to "they'll",
            "weve" to "we've",
            "thats" to "that's",
            "whats" to "what's",
            "wheres" to "where's",
            "hows" to "how's",
            "whos" to "who's",
            "theres" to "there's",
            "heres" to "here's",
            "lets" to "let's",
            // common typos
            "teh" to "the",
            "thsi" to "this",
            "taht" to "that",
            "waht" to "what",
            "wnat" to "want",
            "wiht" to "with",
            "frmo" to "from",
            "hte" to "the",
            "adn" to "and",
            "nad" to "and",
            "cna" to "can",
            "becuase" to "because",
            "beacuse" to "because",
            "peopel" to "people",
            "poeple" to "people",
            "recieve" to "receive",
            "seperate" to "separate",
            "definately" to "definitely",
            "untill" to "until",
            "truely" to "truly",
            "wierd" to "weird",
            "alot" to "a lot"
        )
    }
}

object EnglishDictionary {
    private const val TAG = "EnglishDictionary"

    @Volatile
    private var engine: SuggestionEngine? = null
    private val isLoading = AtomicBoolean(false)

    fun load(context: Context) {
        if (engine != null || !isLoading.compareAndSet(false, true)) return
        Thread {
            try {
                val list = mutableListOf<String>()
                context.assets.open("words_en.txt").use { stream ->
                    BufferedReader(InputStreamReader(stream)).useLines { lines ->
                        for (line in lines) {
                            val word = line.trim()
                            if (word.isNotEmpty() && !word.startsWith("#")) {
                                list.add(word)
                            }
                        }
                    }
                }
                val immutableList = list.toList()
                engine = SuggestionEngine(immutableList, immutableList.toHashSet())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load words_en.txt from assets", e)
            } finally {
                isLoading.set(false)
            }
        }.start()
    }

    /**
     * Given the currently typed [input], returns suggestions wrapped in a [SuggestionResult],
     * or null if no suggestions are found or the dictionary is not yet loaded.
     */
    fun getSuggestions(input: String): SuggestionResult? = engine?.getSuggestions(input)
}
