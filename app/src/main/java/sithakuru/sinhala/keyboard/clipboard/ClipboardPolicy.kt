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

/**
 * The clipboard settings the keyboard acts on.
 *
 * @param clearRequests how many times "clear all" was confirmed in settings.
 *   A count rather than a time, so a wrong or changed clock cannot make a
 *   request miss clips or apply twice.
 */
data class ClipboardSettings(
    val enabled: Boolean,
    val expiryMillis: Long,
    val clearRequests: Int
)

object ClipboardPolicy {
    /**
     * Brings [history] in line with [settings].
     *
     * @param appliedClearRequests the [ClipboardSettings.clearRequests] value
     *   handled last time; any other value means a new "clear all".
     * @return the value to store as handled.
     */
    fun apply(history: ClipboardHistoryManager, settings: ClipboardSettings, appliedClearRequests: Int, now: Long): Int {
        if (!settings.enabled || settings.clearRequests != appliedClearRequests) {
            history.clearAll()
        }
        if (settings.enabled) {
            history.expireUnpinned(now, settings.expiryMillis)
        }
        return settings.clearRequests
    }
}
