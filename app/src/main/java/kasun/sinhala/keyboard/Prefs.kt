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

package kasun.sinhala.keyboard

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    var layoutEnglish: Boolean
        get() = preferences.getBoolean(keyEnglish, true)
        set(value) = preferences.edit().putBoolean(keyEnglish, value).apply()
    var layoutWijesekara: Boolean
        get() = preferences.getBoolean(keyWijesekara, false)
        set(value) = preferences.edit().putBoolean(keyWijesekara, value).apply()
    var layoutSinglish: Boolean
        get() = preferences.getBoolean(keySinglish, true)
        set(value) = preferences.edit().putBoolean(keySinglish, value).apply()
    var darkTheme: Boolean
        get() = preferences.getBoolean(keyDarkTheme, true)
        set(value) = preferences.edit().putBoolean(keyDarkTheme, value).apply()
    var currentLayout: String
        get() = preferences.getString(keyCurrentLayout, KeyboardLayout.ENGLISH.name)
            ?: KeyboardLayout.ENGLISH.name
        set(value) = preferences.edit().putString(keyCurrentLayout, value).apply()

    companion object {
        private const val keyEnglish = "LAYOUT_ENGLISH"
        private const val keyWijesekara = "LAYOUT_WIJESEKARA"
        private const val keySinglish = "LAYOUT_SINGLISH"
        const val keyDarkTheme = "DARK_THEME"
        const val keyCurrentLayout = "CURRENT_LAYOUT"
    }
}