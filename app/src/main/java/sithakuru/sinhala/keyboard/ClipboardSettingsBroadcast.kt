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
import android.content.Intent
import sithakuru.sinhala.keyboard.clipboard.ClipboardSettings

/**
 * Tells a running keyboard about clipboard setting changes straight away.
 *
 * The keyboard runs in its own process and its copy of the settings file is
 * not refreshed while it runs; the settings screen restarts that process when
 * it closes, but turning history off or clearing it must not depend on that.
 * The new values travel in the broadcast itself. The receiver is registered
 * as not exported and the broadcast is limited to this package.
 */
object ClipboardSettingsBroadcast {
    const val ACTION = "sithakuru.sinhala.keyboard.action.CLIPBOARD_SETTINGS_CHANGED"
    private const val EXTRA_ENABLED = "enabled"
    private const val EXTRA_EXPIRY = "expiry"
    private const val EXTRA_CLEAR_REQUESTS = "clear_requests"

    fun send(context: Context, settings: ClipboardSettings) {
        context.sendBroadcast(
            Intent(ACTION)
                .setPackage(context.packageName)
                .putExtra(EXTRA_ENABLED, settings.enabled)
                .putExtra(EXTRA_EXPIRY, settings.expiryMillis)
                .putExtra(EXTRA_CLEAR_REQUESTS, settings.clearRequests)
        )
    }

    /** Null if the intent is not a complete clipboard settings broadcast. */
    fun read(intent: Intent): ClipboardSettings? {
        if (intent.action != ACTION) return null
        if (!intent.hasExtra(EXTRA_ENABLED) || !intent.hasExtra(EXTRA_EXPIRY) || !intent.hasExtra(EXTRA_CLEAR_REQUESTS)) return null
        return ClipboardSettings(
            enabled = intent.getBooleanExtra(EXTRA_ENABLED, true),
            expiryMillis = intent.getLongExtra(EXTRA_EXPIRY, Prefs.DEFAULT_CLIPBOARD_EXPIRY),
            clearRequests = intent.getIntExtra(EXTRA_CLEAR_REQUESTS, 0)
        )
    }
}
