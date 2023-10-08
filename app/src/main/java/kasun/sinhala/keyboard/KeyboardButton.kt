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
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.widget.TextView

class KeyboardButton : TextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var clickListener: (tag: String) -> Unit = { }


    init {
        gravity = CENTER
        isClickable = true
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10F, resources.displayMetrics)

        setOnClickListener { clickListener.invoke(it.tag.toString()) }

        background = resources.getDrawable(R.drawable.key_background, context.theme)
    }
}
