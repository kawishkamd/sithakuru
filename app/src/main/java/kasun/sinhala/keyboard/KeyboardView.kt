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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import kasun.sinhala.keyboard.databinding.KeyboardLayoutBinding
import java.util.Date

@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class KeyboardView(context: Context?, clickListener: ClickListener, rowHeight: Int, darkTheme: Boolean, keyBorders: Boolean) : LinearLayout(context) {

    interface ClickListener {
        fun letterOrSymbolClick(tag: String)
        fun numberClick(tag: String)
        fun functionClick(type: Function)
        fun specialClick(tag: String)
    }

    private lateinit var backspaceRepeater: Runnable
    private var backspaceRepeating = false
    private var lastBackspacePressTime = 0L

    var keyboardVisible = false

    private val binding: KeyboardLayoutBinding

    val viewBlank1: View get() = binding.blank1
    val viewBlank2: View get() = binding.blank2
    val buttonColon: KeyboardButton get() = binding.colonWijesekara
    val buttonActionShift: ImageView get() = binding.shift

    val buttonSpecialComma: KeyboardButton get() = binding.comma
    val buttonSpecialCommaWijesekara: KeyboardButton get() = binding.commaWijesekara
    val buttonActionAction: ImageView get() = binding.action

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

        binding.space.setOnLongClickListener {
            clickListener.functionClick(Function.IME)
            true
        }
        binding.space.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    clickListener.specialClick(v.tag.toString())
                    v.performClick()
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background_pressed)
                }
                MotionEvent.ACTION_UP ->
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background)
            }
            false
        }

        binding.lang.clickListener = { clickListener.functionClick(Function.LANG) }
        binding.panel.clickListener = { clickListener.functionClick(Function.PANEL) }

        binding.shift.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background_pressed)
                    clickListener.functionClick(Function.SHIFT)
                    v.performClick()
                }
                MotionEvent.ACTION_UP -> v.background = contextThemeWrapper.getDrawable(R.drawable.key_background)
            }
            true
        }

        binding.action.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background_pressed)
                    clickListener.functionClick(Function.ACTION)
                    v.performClick()
                }
                MotionEvent.ACTION_UP -> v.background = contextThemeWrapper.getDrawable(R.drawable.key_background)
            }
            true
        }

        backspaceRepeater = Runnable {
            if (backspaceRepeating && keyboardVisible) {
                clickListener.functionClick(Function.BACKSPACE)
                Handler(Looper.getMainLooper()).postDelayed(
                    backspaceRepeater,
                    (Date().time - lastBackspacePressTime).let {
                        when {
                            it > 4500 -> 40
                            it > 3000 -> 60
                            it > 1500 -> 80
                            else -> 100
                        }
                    }
                )
            }
        }

        binding.backspace.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background_pressed)
                    clickListener.functionClick(Function.BACKSPACE)
                    lastBackspacePressTime = Date().time
                    v.performClick()
                }
                MotionEvent.ACTION_UP -> {
                    v.background = contextThemeWrapper.getDrawable(R.drawable.key_background)
                    backspaceRepeating = false
                }
                else ->
                    if (Date().time - lastBackspacePressTime > 400 && !backspaceRepeating) {
                        backspaceRepeating = true
                        Handler(Looper.getMainLooper()).post(backspaceRepeater)
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

        keySet[":"]?.let { binding.colonWijesekara.text = it }
    }

    fun setNumberKeys(keySet: Array<String>) {
        binding.n1.text = keySet[0]
        binding.n2.text = keySet[1]
        binding.n3.text = keySet[2]
        binding.n4.text = keySet[3]
        binding.n5.text = keySet[4]
        binding.n6.text = keySet[5]
        binding.n7.text = keySet[6]
        binding.n8.text = keySet[7]
        binding.n9.text = keySet[8]
        binding.n0.text = keySet[9]
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
