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
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import sithakuru.sinhala.keyboard.clipboard.ClipboardHistoryManager
import sithakuru.sinhala.keyboard.clipboard.ClipboardItem

@SuppressLint("ViewConstructor")
class ClipboardView(
    context: Context,
    private val prefs: Prefs,
    private val historyManager: ClipboardHistoryManager,
    private val eventListener: EventListener,
    private val insetBottom: Int
) : LinearLayout(context) {

    interface EventListener {
        fun onClipClick(text: String)
        fun onBackToKeyboard()
        fun onSwitchToEmoji()
        fun onBackspaceClick()
        fun onSpaceClick()
    }

    private val repeatHandler = Handler(Looper.getMainLooper())
    private val rowHeight = (48 * prefs.heightPercentage / 100).toFloat().dpToPx()
    private val keyBorderRadius = 4f.dpToPx()
    private val paddingBottom = 8f.dpToPx() + insetBottom
    private val paddingTop = 2f.dpToPx()

    var reserveStripHeight: Boolean = false

    private val clearAllButton: TextView
    private val contentContainer: FrameLayout
    private val scrollView: ScrollView
    private val itemsLayout: LinearLayout
    private val emptyView: LinearLayout

    init {
        orientation = VERTICAL
        setBackgroundColor(prefs.colorBackground)

        // 1. Top Header Row
        val headerRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 38f.dpToPx().toInt()).apply {
                setPadding(12f.dpToPx().toInt(), 4f.dpToPx().toInt(), 12f.dpToPx().toInt(), 4f.dpToPx().toInt())
            }
        }

        val headerIcon = ImageView(context).apply {
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_clipboard, context.theme)?.mutate()
            drawable?.setTint(prefs.colorText)
            setImageDrawable(drawable)
            layoutParams = LayoutParams(20f.dpToPx().toInt(), 20f.dpToPx().toInt()).apply {
                setMargins(0, 0, 8f.dpToPx().toInt(), 0)
            }
        }

        val titleView = TextView(context).apply {
            text = context.getString(R.string.clipboard)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(prefs.colorText)
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        clearAllButton = TextView(context).apply {
            text = context.getString(R.string.clipboard_clear_all)
            textSize = 13f
            setTextColor(prefs.colorText)
            val padH = 8f.dpToPx().toInt()
            val padV = 4f.dpToPx().toInt()
            setPadding(padH, padV, padH, padV)
            background = createKeyStatesDrawable(
                Color.TRANSPARENT,
                prefs.colorPressedKeyBackground,
                prefs.colorKeyBorder,
                keyBorderRadius
            )
            isClickable = true
            isFocusable = true
            setOnClickListener {
                historyManager.clearUnpinned()
                updateClips()
            }
        }

        headerRow.addView(headerIcon)
        headerRow.addView(titleView)
        headerRow.addView(clearAllButton)
        addView(headerRow)

        // 2. Middle Content Area
        contentContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
        }

        // Empty state view
        emptyView = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

            val emptyIcon = ImageView(context).apply {
                val iconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_clipboard, context.theme)?.mutate()
                iconDrawable?.setTint(prefs.colorTextDim)
                setImageDrawable(iconDrawable)
                val iconSize = 44f.dpToPx().toInt()
                layoutParams = LayoutParams(iconSize, iconSize).apply {
                    setMargins(0, 0, 0, 10f.dpToPx().toInt())
                }
            }

            val emptyTitle = TextView(context).apply {
                text = context.getString(R.string.clipboard_empty)
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(prefs.colorText)
                gravity = Gravity.CENTER
            }

            val emptyHint = TextView(context).apply {
                text = context.getString(R.string.clipboard_empty_hint)
                textSize = 12f
                setTextColor(prefs.colorTextDim)
                gravity = Gravity.CENTER
                setPadding(0, 4f.dpToPx().toInt(), 0, 0)
            }

            addView(emptyIcon)
            addView(emptyTitle)
            addView(emptyHint)
        }
        contentContainer.addView(emptyView)

        // Items list view
        scrollView = ScrollView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            isVerticalScrollBarEnabled = true
            clipToPadding = false
        }

        itemsLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            val padH = 8f.dpToPx().toInt()
            val padV = 6f.dpToPx().toInt()
            setPadding(padH, padV, padH, padV)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        scrollView.addView(itemsLayout)
        contentContainer.addView(scrollView)

        addView(contentContainer)

        // 3. Bottom Control Row
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

        // Emoji Key
        val emojiKey = ImageView(context).apply {
            val emojiDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_emoji, context.theme)?.mutate()
            emojiDrawable?.setTint(prefs.colorText)
            setImageDrawable(emojiDrawable)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = createKeyStatesDrawable(
                prefs.colorBackground,
                prefs.colorPressedKeyBackground,
                prefs.colorKeyBorder,
                keyBorderRadius
            )
            layoutParams = LayoutParams(0, rowHeight.toInt(), 1.2f).apply {
                setMargins(2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt(), 2f.dpToPx().toInt())
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                eventListener.onSwitchToEmoji()
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
            layoutParams = LayoutParams(0, rowHeight.toInt(), 3.3f).apply {
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
                        repeatHandler.postDelayed(object : Runnable {
                            override fun run() {
                                eventListener.onBackspaceClick()
                                repeatHandler.postDelayed(this, 50)
                            }
                        }, 400)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        repeatHandler.removeCallbacksAndMessages(null)
                        true
                    }
                    else -> false
                }
            }
        }

        bottomRow.addView(abcKey)
        bottomRow.addView(emojiKey)
        bottomRow.addView(spaceKey)
        bottomRow.addView(backspaceKey)
        addView(bottomRow)

        updateClips()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        repeatHandler.removeCallbacksAndMessages(null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val stripHeight = if (reserveStripHeight) SuggestionStripView.heightFor(context, prefs) else 0
        val totalKeyboardHeight = (rowHeight * 5 + paddingTop + paddingBottom).toInt() + stripHeight
        val exactHeightSpec = MeasureSpec.makeMeasureSpec(totalKeyboardHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, exactHeightSpec)
    }

    fun updateClips() {
        val items = historyManager.getItems()
        val hasUnpinned = items.any { !it.isPinned }
        clearAllButton.visibility = if (hasUnpinned) View.VISIBLE else View.GONE

        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            scrollView.visibility = View.GONE
            return
        }

        emptyView.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
        itemsLayout.removeAllViews()

        for (item in items) {
            val card = createItemCard(item)
            itemsLayout.addView(card)
        }
    }

    private fun createItemCard(item: ClipboardItem): View {
        val card = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = 10f.dpToPx().toInt()
            setPadding(pad, pad, pad, pad)
            background = createKeyStatesDrawable(
                prefs.colorBackground,
                prefs.colorPressedKeyBackground,
                if (item.isPinned) prefs.colorLongPressedKeyBackground else prefs.colorKeyBorder,
                keyBorderRadius
            )
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                val marginV = 3f.dpToPx().toInt()
                setMargins(0, marginV, 0, marginV)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                eventListener.onClipClick(item.text)
            }
        }

        // Text preview
        val textView = TextView(context).apply {
            text = item.text
            textSize = 14f
            maxLines = 3
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(prefs.colorText)
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8f.dpToPx().toInt(), 0)
            }
        }
        card.addView(textView)

        // Actions container (Pin + Delete)
        val actionsLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        // Pin Button
        val pinButton = ImageView(context).apply {
            val iconRes = if (item.isPinned) R.drawable.ic_pin else R.drawable.ic_pin_outline
            val pinDrawable = ResourcesCompat.getDrawable(resources, iconRes, context.theme)?.mutate()
            val tintColor = if (item.isPinned) prefs.colorLongPressedKeyBackground else prefs.colorTextDim
            pinDrawable?.setTint(tintColor)
            setImageDrawable(pinDrawable)
            background = createKeyStatesDrawable(
                Color.TRANSPARENT,
                prefs.colorPressedKeyBackground,
                Color.TRANSPARENT,
                keyBorderRadius
            )
            val btnSize = 36f.dpToPx().toInt()
            val padBtn = 7f.dpToPx().toInt()
            setPadding(padBtn, padBtn, padBtn, padBtn)
            layoutParams = LayoutParams(btnSize, btnSize).apply {
                setMargins(0, 0, 4f.dpToPx().toInt(), 0)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                historyManager.togglePin(item.id)
                updateClips()
            }
        }
        actionsLayout.addView(pinButton)

        // Delete Button
        val deleteButton = ImageView(context).apply {
            val delDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, context.theme)?.mutate()
            delDrawable?.setTint(prefs.colorTextDim)
            setImageDrawable(delDrawable)
            background = createKeyStatesDrawable(
                Color.TRANSPARENT,
                prefs.colorPressedKeyBackground,
                Color.TRANSPARENT,
                keyBorderRadius
            )
            val btnSize = 36f.dpToPx().toInt()
            val padBtn = 7f.dpToPx().toInt()
            setPadding(padBtn, padBtn, padBtn, padBtn)
            layoutParams = LayoutParams(btnSize, btnSize)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                historyManager.deleteItem(item.id)
                updateClips()
            }
        }
        actionsLayout.addView(deleteButton)

        card.addView(actionsLayout)
        return card
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
}
