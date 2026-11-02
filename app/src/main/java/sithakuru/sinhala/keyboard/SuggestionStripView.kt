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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

@SuppressLint("ViewConstructor")
class SuggestionStripView(
    context: Context,
    private val prefs: Prefs
) : LinearLayout(context) {

    var onSuggestionClick: ((String) -> Unit)? = null

    private val leftView: TextView
    private val centerView: TextView
    private val rightView: TextView
    private val divider1: View
    private val divider2: View

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val heightPx = (42 * prefs.heightPercentage / 100).toFloat().dpToPx().toInt().coerceAtLeast(38f.dpToPx().toInt())
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightPx)
        setBackgroundColor(prefs.colorBackground)

        val selectableRes = resolveSelectableBackground()

        leftView = createSuggestionTextView(selectableRes, isCenter = false)
        divider1 = createDividerView()
        centerView = createSuggestionTextView(selectableRes, isCenter = true)
        divider2 = createDividerView()
        rightView = createSuggestionTextView(selectableRes, isCenter = false)

        addView(leftView, LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f))
        addView(divider1)
        addView(centerView, LayoutParams(0, LayoutParams.MATCH_PARENT, 1.2f))
        addView(divider2)
        addView(rightView, LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f))

        clearSuggestions()
    }

    private fun resolveSelectableBackground(): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)) {
            typedValue.resourceId
        } else {
            0
        }
    }

    private fun createSuggestionTextView(selectableRes: Int, isCenter: Boolean): TextView {
        return TextView(context).apply {
            gravity = Gravity.CENTER
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            textSize = if (isCenter) (prefs.textSize * 0.8f) else (prefs.textSize * 0.75f)
            setTextColor(prefs.colorText)
            typeface = if (isCenter) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (selectableRes != 0) {
                setBackgroundResource(selectableRes)
            }
            val padH = 4f.dpToPx().toInt()
            setPadding(padH, 0, padH, 0)
            setOnClickListener {
                val suggestion = text.toString()
                if (suggestion.isNotEmpty()) {
                    onSuggestionClick?.invoke(suggestion)
                }
            }
        }
    }

    private fun createDividerView(): View {
        return View(context).apply {
            layoutParams = LayoutParams(1f.dpToPx().toInt().coerceAtLeast(1), LayoutParams.MATCH_PARENT).apply {
                val marginV = 8f.dpToPx().toInt()
                setMargins(0, marginV, 0, marginV)
            }
            setBackgroundColor(prefs.colorKeyBorder)
        }
    }

    fun setSuggestions(result: SuggestionResult?) {
        if (result == null) {
            clearSuggestions()
            return
        }

        val left: String
        val center: String
        val right: String

        if (result.primary == result.raw) {
            // Exact word already typed; keep center as primary, show completion on right
            left = ""
            center = result.primary
            right = result.secondary ?: ""
        } else {
            // Typo or prefix completion; left offers raw input, center offers best fix
            left = result.raw
            center = result.primary
            right = result.secondary ?: ""
        }

        updateSlot(leftView, left)
        updateSlot(centerView, center)
        updateSlot(rightView, right)

        updateDividerVisibility(left, center, right)
    }

    private fun updateSlot(view: TextView, newText: String) {
        if (view.text.toString() != newText) {
            view.text = newText
        }
        view.isClickable = newText.isNotEmpty()
    }

    private fun updateDividerVisibility(left: String, center: String, right: String) {
        val showDividers = prefs.keyBorders
        divider1.visibility = if (showDividers && left.isNotEmpty() && center.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        divider2.visibility = if (showDividers && center.isNotEmpty() && right.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    fun clearSuggestions() {
        updateSlot(leftView, "")
        updateSlot(centerView, "")
        updateSlot(rightView, "")
        divider1.visibility = View.INVISIBLE
        divider2.visibility = View.INVISIBLE
    }

    fun updateTheme() {
        setBackgroundColor(prefs.colorBackground)
        centerView.setTextColor(prefs.colorText)
        leftView.setTextColor(prefs.colorText)
        rightView.setTextColor(prefs.colorText)
        divider1.setBackgroundColor(prefs.colorKeyBorder)
        divider2.setBackgroundColor(prefs.colorKeyBorder)
        updateDividerVisibility(leftView.text.toString(), centerView.text.toString(), rightView.text.toString())
    }

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)
}
