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
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import sithakuru.sinhala.keyboard.databinding.KeyboardLayoutBinding

@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class KeyboardView(
    context: Context?,
    clickListener: ClickListener,
    private val swipeListener: SwipeListener,
    private val rowHeight: Int,
    darkTheme: Boolean,
    keyBorders: Boolean,
    private val swipeToErase: Boolean,
    private val swipeToMoveCursor: Boolean,
    textSize: Int
) : LinearLayout(context) {

    interface ClickListener {
        fun letterOrSymbolClick(tag: String)
        fun numberClick(tag: String)
        fun functionClick(type: Function)
        fun specialClick(tag: String)
    }

    interface SwipeListener {
        fun eraseDo()
        fun eraseUndo()
        fun eraseDone()
        fun moveRight()
        fun moveLeft()
    }

    private var lastBackspaceDownTime = 0L
    private var lastBackspaceEraseTime = 0L

    var keyboardVisible = false

    private val binding: KeyboardLayoutBinding

    val viewBlank1: View get() = binding.blank1
    val viewBlank2: View get() = binding.blank2
    val buttonColon: KeyboardButton get() = binding.colonWijesekara
    val buttonActionShift: ImageView get() = binding.shift

    val buttonSpecialComma: KeyboardButton get() = binding.comma
    val buttonSpecialCommaWijesekara: KeyboardButton get() = binding.commaWijesekara
    val buttonActionAction: ImageView get() = binding.action

    private var swipeStepStartX: Float = 0F
    private val swipeStepDistance = resources.displayMetrics.widthPixels / 15
    private var startIgnoreSwipe = false
    private var currentSwipeActionType = SwipeActionType.NONE

    private enum class SwipeActionType { ERASE, MOVE_CURSOR, NONE }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (swipeToErase || swipeToMoveCursor) {
            if (ev != null && ev.pointerCount > 1) startIgnoreSwipe = true
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentSwipeActionType = SwipeActionType.NONE
                    swipeStepStartX = ev.x
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!startIgnoreSwipe) {

                        // මේ වන විට swipeStepStartXහි තිබෙන්නේ ඉහළ MotionEvent.ACTION_DOWN සිදුවීමේදී ආදේශ කළ අගය යි.
                        val distanceFromDownX: Float = swipeStepStartX - ev.x

                        if (swipeToErase && ev.y < rowHeight * 4 && distanceFromDownX > swipeStepDistance)
                            currentSwipeActionType = SwipeActionType.ERASE
                        else if (swipeToMoveCursor && ev.y >= rowHeight * 4 && (distanceFromDownX > swipeStepDistance || distanceFromDownX < -swipeStepDistance))
                            currentSwipeActionType = SwipeActionType.MOVE_CURSOR

                        return currentSwipeActionType != SwipeActionType.NONE
                    }
                }

                MotionEvent.ACTION_UP -> if (ev.pointerCount == 1) startIgnoreSwipe = false
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                val swipeDistance = swipeStepStartX - event.x
                if (swipeDistance > swipeStepDistance) {
                    when (currentSwipeActionType) {
                        SwipeActionType.ERASE -> swipeListener.eraseDo()
                        SwipeActionType.MOVE_CURSOR -> swipeListener.moveLeft()
                        SwipeActionType.NONE -> {}
                    }
                    swipeStepStartX = event.x
                } else if (swipeDistance < -swipeStepDistance) {
                    when (currentSwipeActionType) {
                        SwipeActionType.ERASE -> swipeListener.eraseUndo()
                        SwipeActionType.MOVE_CURSOR -> swipeListener.moveRight()
                        SwipeActionType.NONE -> {}
                    }
                    swipeStepStartX = event.x
                }
            }

            MotionEvent.ACTION_UP -> {
                swipeListener.eraseDone()
                if (event.pointerCount == 1) startIgnoreSwipe = false
                return false
            }
        }
        return true
    }

    init {
        val style = when {
            !darkTheme && keyBorders -> R.style.Light
            !darkTheme && !keyBorders -> R.style.LightNoBorder
            darkTheme && !keyBorders -> R.style.NightNoBorder
            else -> R.style.Night
        }

        val contextThemeWrapper = ContextThemeWrapper(context, style)
        binding = KeyboardLayoutBinding.inflate(LayoutInflater.from(contextThemeWrapper), this, true)

        binding.keyRow1.layoutParams.height = rowHeight
        binding.keyRow2.layoutParams.height = rowHeight
        binding.keyRow3.layoutParams.height = rowHeight
        binding.keyRow4.layoutParams.height = rowHeight
        binding.keyRow5.layoutParams.height = rowHeight

        for (row in binding.root.children)
            if (row is LinearLayout)
                for (button in row.children)
                    if (button is KeyboardButton)
                        button.textSize = textSize.toFloat()

        binding.n0.clickListener = { clickListener.numberClick(it) }
        binding.n1.clickListener = { clickListener.numberClick(it) }
        binding.n2.clickListener = { clickListener.numberClick(it) }
        binding.n3.clickListener = { clickListener.numberClick(it) }
        binding.n4.clickListener = { clickListener.numberClick(it) }
        binding.n5.clickListener = { clickListener.numberClick(it) }
        binding.n6.clickListener = { clickListener.numberClick(it) }
        binding.n7.clickListener = { clickListener.numberClick(it) }
        binding.n8.clickListener = { clickListener.numberClick(it) }
        binding.n9.clickListener = { clickListener.numberClick(it) }

        binding.lA.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lB.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lC.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lD.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lE.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lF.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lG.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lH.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lI.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lJ.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lK.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lL.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lM.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lN.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lO.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lP.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lQ.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lR.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lS.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lT.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lU.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lV.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lW.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lX.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lY.clickListener = { clickListener.letterOrSymbolClick(it) }
        binding.lZ.clickListener = { clickListener.letterOrSymbolClick(it) }

        binding.symbol1.clickListener = { clickListener.letterOrSymbolClick(it) }

        binding.colonWijesekara.clickListener = { clickListener.specialClick(it) }

        binding.comma.clickListener = { clickListener.specialClick(it) }
        binding.commaWijesekara.clickListener = { clickListener.specialClick(it) }
        binding.dot.clickListener = { clickListener.specialClick(it) }

        binding.space.setOnClickListener { clickListener.specialClick(it.tag.toString()) }
        binding.space.setOnLongClickListener {
            clickListener.functionClick(Function.IME)
            true
        }

        binding.lang.setOnClickListener { clickListener.functionClick(Function.LANG) }
        binding.panel.setOnClickListener { clickListener.functionClick(Function.PANEL) }
        binding.shift.setOnClickListener { clickListener.functionClick(Function.SHIFT) }
        binding.action.setOnClickListener { clickListener.functionClick(Function.ACTION) }

        binding.backspace.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background_pressed)
                    clickListener.functionClick(Function.BACKSPACE)
                    lastBackspaceDownTime = System.currentTimeMillis()
                    v.performClick()
                }

                MotionEvent.ACTION_MOVE -> {
                    val currentTimeMillis = System.currentTimeMillis()
                    val timeSinceLastDown = currentTimeMillis - lastBackspaceDownTime
                    if (timeSinceLastDown > 500) {
                        val delay = when {
                            timeSinceLastDown > 6000 -> 4
                            timeSinceLastDown > 5000 -> 8
                            timeSinceLastDown > 4000 -> 16
                            timeSinceLastDown > 3000 -> 32
                            timeSinceLastDown > 2000 -> 64
                            timeSinceLastDown > 1000 -> 128
                            else -> 256
                        }
                        if (currentTimeMillis - lastBackspaceEraseTime > delay) {
                            clickListener.functionClick(Function.BACKSPACE)
                            lastBackspaceEraseTime = currentTimeMillis
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background)
                }
            }
            true
        }
    }

    fun setLetterKeys(keySet: Map<String, String>) {
        binding.lA.text = keySet["a"]
        binding.lB.text = keySet["b"]
        binding.lC.text = keySet["c"]
        binding.lD.text = keySet["d"]
        binding.lE.text = keySet["e"]
        binding.lF.text = keySet["f"]
        binding.lG.text = keySet["g"]
        binding.lH.text = keySet["h"]
        binding.lI.text = keySet["i"]
        binding.lJ.text = keySet["j"]
        binding.lK.text = keySet["k"]
        binding.lL.text = keySet["l"]
        binding.lM.text = keySet["m"]
        binding.lN.text = keySet["n"]
        binding.lO.text = keySet["o"]
        binding.lP.text = keySet["p"]
        binding.lQ.text = keySet["q"]
        binding.lR.text = keySet["r"]
        binding.lS.text = keySet["s"]
        binding.lT.text = keySet["t"]
        binding.lU.text = keySet["u"]
        binding.lV.text = keySet["v"]
        binding.lW.text = keySet["w"]
        binding.lX.text = keySet["x"]
        binding.lY.text = keySet["y"]
        binding.lZ.text = keySet["z"]
    }

    fun setNumberKeys(keyLabels: Map<String, String>) {
        binding.n1.text = keyLabels["1"]
        binding.n2.text = keyLabels["2"]
        binding.n3.text = keyLabels["3"]
        binding.n4.text = keyLabels["4"]
        binding.n5.text = keyLabels["5"]
        binding.n6.text = keyLabels["6"]
        binding.n7.text = keyLabels["7"]
        binding.n8.text = keyLabels["8"]
        binding.n9.text = keyLabels["9"]
        binding.n0.text = keyLabels["0"]
    }

    fun setSpecialKeys(keyLabels: Map<String, String>) {
        keyLabels[";"]?.let { binding.colonWijesekara.text = it }
        keyLabels[","]?.let { binding.commaWijesekara.text = it }
        keyLabels["."]?.let { binding.dot.text = it }
    }

    fun setLangIndicator(text: String) {
        binding.lang.text = text
    }

    fun showSymbols(
        symbolsVisibility: Boolean,
        currentLayout: KeyboardLayout,
        keyMap: Map<String, String>? = null
    ) {
        if (currentLayout == KeyboardLayout.WIJESEKARA) {
            binding.blank1.isVisible = symbolsVisibility
            binding.blank2.isVisible = symbolsVisibility
        }
        binding.colonWijesekara.isVisible = !symbolsVisibility && currentLayout == KeyboardLayout.WIJESEKARA
        binding.symbol1.isVisible = symbolsVisibility
        binding.lang.isVisible = !symbolsVisibility

        if (symbolsVisibility && keyMap != null) setLetterKeys(keyMap)
    }
}
