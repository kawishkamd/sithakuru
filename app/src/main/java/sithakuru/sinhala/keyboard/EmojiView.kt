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
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import sithakuru.sinhala.keyboard.data.EmojiCategory
import sithakuru.sinhala.keyboard.data.EmojiData

@SuppressLint("ViewConstructor")
class EmojiView(
    context: Context,
    private val prefs: Prefs,
    private val eventListener: EventListener,
    private val insetBottom: Int
) : LinearLayout(context) {

    interface EventListener {
        fun onEmojiClick(emoji: String)
        fun onBackToKeyboard()
        fun onBackspaceClick()
        fun onSpaceClick()
    }

    private val repeatHandler = Handler(Looper.getMainLooper())
    private val backspaceRepeatRunnable = object : Runnable {
        override fun run() {
            eventListener.onBackspaceClick()
            repeatHandler.postDelayed(this, 50)
        }
    }

    private val rowHeight = (48 * prefs.heightPercentage / 100).toFloat().dpToPx()
    private val keyBorderRadius = 4f.dpToPx()
    private val paddingBottom = 8f.dpToPx() + insetBottom
    private val paddingTop = 2f.dpToPx()

    private var selectedCategoryIndex = 0
    private val categoryViews = mutableListOf<ImageView>()
    private val allCategories: MutableList<EmojiCategory> = mutableListOf()
    private val categoryContainer: LinearLayout

    private val emojiGridAdapter = EmojiGridAdapter()
    private val gridView: GridView

    init {
        orientation = VERTICAL
        setBackgroundColor(prefs.colorBackground)

        // 1. Top Category Bar
        val categoryScrollView = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        categoryContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4f.dpToPx().toInt(), 4f.dpToPx().toInt(), 4f.dpToPx().toInt(), 4f.dpToPx().toInt())
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        buildCategoryTabs()

        categoryScrollView.addView(categoryContainer)
        addView(categoryScrollView)

        // 2. Middle Emoji GridView
        gridView = GridView(context).apply {
            numColumns = 7
            gravity = Gravity.CENTER
            stretchMode = GridView.STRETCH_COLUMN_WIDTH
            isVerticalScrollBarEnabled = false
            adapter = emojiGridAdapter
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            setOnItemClickListener { _, _, position, _ ->
                val emoji = emojiGridAdapter.getItem(position)
                eventListener.onEmojiClick(emoji)
            }
        }
        addView(gridView)

        // 3. Bottom Control Row (ABC, Space, Backspace)
        val bottomRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (rowHeight + paddingBottom).toInt()).apply {
                setPadding(2f.dpToPx().toInt(), 0, 2f.dpToPx().toInt(), paddingBottom.toInt())
            }
        }

        // ABC Key
        val abcKey = TextView(context).apply {
            text = "ABC"
            textSize = 15f
            setTextColor(prefs.colorText)
            gravity = Gravity.CENTER
            background = createKeyStatesDrawable(
                prefs.colorBackground,
                prefs.colorPressedKeyBackground,
                prefs.colorKeyBorder,
                keyBorderRadius
            )
            layoutParams = LayoutParams(0, rowHeight.toInt(), 1.5f).apply {
                setMargins(2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt())
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                eventListener.onBackToKeyboard()
            }
        }

        // Space Key
        val spaceKey = ImageView(context).apply {
            val spaceDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_space_bar, context.theme)?.mutate()
            spaceDrawable?.setTint(prefs.colorText)
            setImageDrawable(spaceDrawable)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = createKeyStatesDrawable(
                prefs.colorBackground,
                prefs.colorPressedKeyBackground,
                prefs.colorKeyBorder,
                keyBorderRadius
            )
            layoutParams = LayoutParams(0, rowHeight.toInt(), 4.5f).apply {
                setMargins(2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt())
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                eventListener.onSpaceClick()
            }
        }

        // Backspace Key
        val backspaceKey = ImageView(context).apply {
            val backspaceDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_backspace, context.theme)?.mutate()
            backspaceDrawable?.setTint(prefs.colorText)
            setImageDrawable(backspaceDrawable)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = createKeyStatesDrawable(
                prefs.colorBackground,
                prefs.colorPressedKeyBackground,
                prefs.colorKeyBorder,
                keyBorderRadius
            )
            layoutParams = LayoutParams(0, rowHeight.toInt(), 1.5f).apply {
                setMargins(2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt())
            }
            isClickable = true
            isFocusable = true
            setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        eventListener.onBackspaceClick()
                        repeatHandler.postDelayed(backspaceRepeatRunnable, 300)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        repeatHandler.removeCallbacks(backspaceRepeatRunnable)
                        true
                    }
                    else -> false
                }
            }
        }

        bottomRow.addView(abcKey)
        bottomRow.addView(spaceKey)
        bottomRow.addView(backspaceKey)
        addView(bottomRow)

        updateCategoryHighlight()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalKeyboardHeight = (rowHeight * 5 + paddingTop + paddingBottom).toInt()
        val exactHeightSpec = MeasureSpec.makeMeasureSpec(totalKeyboardHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, exactHeightSpec)
    }

    private fun buildCategoryTabs() {
        categoryContainer.removeAllViews()
        categoryViews.clear()
        allCategories.clear()

        val recentEmojis = prefs.recentEmojis
        if (recentEmojis.isNotEmpty()) {
            allCategories.add(
                EmojiCategory(
                    iconRes = R.drawable.ic_category_recent,
                    name = "Recent",
                    emojis = recentEmojis
                )
            )
        }
        allCategories.addAll(EmojiData.categories)

        if (selectedCategoryIndex >= allCategories.size) {
            selectedCategoryIndex = 0
        }

        for (i in allCategories.indices) {
            val category = allCategories[i]
            val tabView = ImageView(context).apply {
                val drawable = ResourcesCompat.getDrawable(resources, category.iconRes, context.theme)?.mutate()
                setImageDrawable(drawable)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                val padH = 12f.dpToPx().toInt()
                val padV = 8f.dpToPx().toInt()
                setPadding(padH, padV, padH, padV)
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, 36f.dpToPx().toInt())
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectCategory(i)
                }
            }
            categoryViews.add(tabView)
            categoryContainer.addView(tabView)
        }
        updateCategoryHighlight()
    }

    fun updateRecents() {
        val hadRecent = allCategories.firstOrNull()?.name == "Recent"
        val hasRecent = prefs.recentEmojis.isNotEmpty()
        if (hadRecent != hasRecent) {
            buildCategoryTabs()
        }
        emojiGridAdapter.notifyDataSetChanged()
    }

    private fun selectCategory(index: Int) {
        if (index in allCategories.indices) {
            selectedCategoryIndex = index
            updateCategoryHighlight()
            emojiGridAdapter.notifyDataSetChanged()
            gridView.setSelection(0)
        }
    }

    private fun updateCategoryHighlight() {
        for (i in categoryViews.indices) {
            val view = categoryViews[i]
            if (i == selectedCategoryIndex) {
                view.background = createKeyDrawable(prefs.colorPressedKeyBackground, prefs.colorKeyBorder, keyBorderRadius)
                view.setColorFilter(prefs.colorText)
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
                view.setColorFilter(prefs.colorTextDim)
            }
        }
    }

    private fun createKeyDrawable(backgroundColor: Int, borderColor: Int, radiusPx: Float): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(backgroundColor)
            setStroke(0.5f.dpToPx().toInt().coerceAtLeast(1), borderColor)
            cornerRadius = radiusPx
        }
    }

    private fun createKeyStatesDrawable(normalBg: Int, pressedBg: Int, borderColor: Int, radiusPx: Float): StateListDrawable {
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), createKeyDrawable(pressedBg, borderColor, radiusPx))
            addState(intArrayOf(), createKeyDrawable(normalBg, borderColor, radiusPx))
        }
    }

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

    private inner class EmojiGridAdapter : BaseAdapter() {
        private fun getCurrentList(): List<String> {
            if (allCategories.isEmpty()) return emptyList()
            val category = allCategories.getOrNull(selectedCategoryIndex) ?: allCategories[0]
            return if (category.name == "Recent") prefs.recentEmojis else category.emojis
        }

        override fun getCount(): Int = getCurrentList().size

        override fun getItem(position: Int): String = getCurrentList()[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView: TextView = (convertView as? TextView) ?: TextView(context).apply {
                textSize = 24f
                gravity = Gravity.CENTER
                val pad = 6f.dpToPx().toInt()
                setPadding(pad, pad, pad, pad)
            }
            textView.text = getItem(position)
            return textView
        }
    }
}
