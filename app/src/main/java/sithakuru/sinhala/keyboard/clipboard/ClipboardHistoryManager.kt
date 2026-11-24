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

package sithakuru.sinhala.keyboard.clipboard

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

class ClipboardHistoryManager(
    private val maxItems: Int = MAX_ITEMS,
    private val saveCallback: ((String) -> Unit)? = null
) {
    private val items = mutableListOf<ClipboardItem>()

    @Synchronized
    fun getItems(): List<ClipboardItem> = items.toList()

    @Synchronized
    fun loadFromString(serialized: String) {
        // Parse into a fresh list first: a corrupt/unexpected blob must not wipe the
        // in-memory history before we know whether we actually read anything.
        val parsed = mutableListOf<ClipboardItem>()
        if (serialized.isNotBlank()) {
            for (line in serialized.split("\n")) {
                val parts = line.split("\t")
                if (parts.size < 4) continue
                try {
                    val id = parts[0]
                    val timestamp = parts[1].toLongOrNull() ?: System.currentTimeMillis()
                    val isPinned = parts[2] == "1" || parts[2].equals("true", ignoreCase = true)
                    val encoded = parts.subList(3, parts.size).joinToString("\t")
                    val text = URLDecoder.decode(encoded, "UTF-8")
                    if (text.isNotEmpty() && parsed.none { it.id == id || it.text == text }) {
                        parsed.add(ClipboardItem(id = id, text = text, timestamp = timestamp, isPinned = isPinned))
                    }
                } catch (_: IllegalArgumentException) {
                    // A malformed escape in one line: skip that clip, keep the rest.
                }
            }
        }
        items.clear()
        items.addAll(parsed)
        sortAndPrune()
    }

    @Synchronized
    fun serializeToString(): String {
        return items.joinToString("\n") { item ->
            val encodedText = try {
                URLEncoder.encode(item.text, "UTF-8")
            } catch (_: UnsupportedEncodingException) {
                // UTF-8 is always supported; this branch cannot run.
                item.text
            }
            "${item.id}\t${item.timestamp}\t${if (item.isPinned) 1 else 0}\t$encodedText"
        }
    }

    @Synchronized
    fun addItem(text: String): ClipboardItem? {
        // Trim first so duplicates differing only in surrounding whitespace collapse,
        // then truncate on the trimmed text without splitting a surrogate pair.
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        val processedText = if (trimmed.length > MAX_CLIP_LENGTH) {
            val cut = trimmed.substring(0, MAX_CLIP_LENGTH)
            if (cut.last().isHighSurrogate()) cut.dropLast(1) else cut
        } else {
            trimmed
        }

        val timestamp = nextTimestamp()
        val existingIndex = items.indexOfFirst { it.text == processedText }
        if (existingIndex != -1) {
            val existing = items.removeAt(existingIndex)
            val updated = existing.copy(timestamp = timestamp)
            items.add(0, updated)
            sortAndPrune()
            save()
            return updated
        }

        val newItem = ClipboardItem(
            text = processedText,
            timestamp = timestamp,
            isPinned = false
        )
        items.add(0, newItem)
        sortAndPrune()
        save()
        return newItem
    }

    @Synchronized
    fun togglePin(id: String): Boolean {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return false
        val current = items[index]
        // Refuse to pin beyond capacity; otherwise pinned items would grow without
        // bound and squeeze every unpinned clip out of the list.
        if (!current.isPinned && items.count { it.isPinned } >= maxItems) return false
        val updated = current.copy(isPinned = !current.isPinned)
        items[index] = updated
        sortAndPrune()
        save()
        return updated.isPinned
    }

    @Synchronized
    fun deleteItem(id: String): Boolean {
        val removed = items.removeAll { it.id == id }
        if (removed) {
            save()
        }
        return removed
    }

    @Synchronized
    fun clearUnpinned(): Int {
        val initialSize = items.size
        items.removeAll { !it.isPinned }
        val cleared = initialSize - items.size
        if (cleared > 0) {
            save()
        }
        return cleared
    }

    /** Removes every item, pinned ones included. */
    @Synchronized
    fun clearAll(): Int {
        val cleared = items.size
        if (cleared > 0) {
            items.clear()
            save()
        }
        return cleared
    }

    /**
     * Removes unpinned items copied more than [maxAgeMillis] before [now].
     * A [maxAgeMillis] of zero or less keeps everything.
     */
    @Synchronized
    fun expireUnpinned(now: Long, maxAgeMillis: Long): Int {
        if (maxAgeMillis <= 0) return 0
        val cutoff = now - maxAgeMillis
        val initialSize = items.size
        items.removeAll { !it.isPinned && it.timestamp < cutoff }
        val removed = initialSize - items.size
        if (removed > 0) {
            save()
        }
        return removed
    }

    /**
     * Strictly increasing timestamps: rapid copies within the same millisecond would
     * otherwise tie, making ordering (and the pinned/unpinned split) non-deterministic.
     */
    private fun nextTimestamp(): Long {
        val candidate = System.currentTimeMillis()
        val newest = items.maxOfOrNull { it.timestamp } ?: 0L
        return if (candidate <= newest) newest + 1 else candidate
    }

    private fun sortAndPrune() {
        // Tie-break equal timestamps by current list order so a pin/unpin within the
        // same millisecond does not reshuffle otherwise-identical entries.
        val order = items.withIndex().associate { (i, item) -> item.id to i }
        val comparator = compareByDescending<ClipboardItem> { it.timestamp }
            .thenBy { order[it.id] ?: Int.MAX_VALUE }
        val pinned = items.filter { it.isPinned }.sortedWith(comparator)
        val unpinned = items.filter { !it.isPinned }.sortedWith(comparator)

        val allowedUnpinned = if (pinned.size < maxItems) {
            maxItems - pinned.size
        } else {
            1
        }
        val prunedUnpinned = unpinned.take(allowedUnpinned)

        items.clear()
        items.addAll(pinned)
        items.addAll(prunedUnpinned)
    }

    private fun save() {
        saveCallback?.invoke(serializeToString())
    }

    companion object {
        const val MAX_ITEMS = 50
        const val MAX_CLIP_LENGTH = 10_000
    }
}
