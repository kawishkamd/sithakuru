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

package sithakuru.sinhala.keyboard.datatype

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

data class KeyInputData(
    var value: Int,
    var label: String?,
    @param:DrawableRes @field:DrawableRes var iconRes: Int? = null,
    var iconDrawable: Drawable? = null,
    var dim: Boolean = false
)
