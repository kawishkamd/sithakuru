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
import java.util.Date

@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class KeyboardView(context: Context?, clickListener: ClickListener, rowHeight: Int?, darkTheme: Boolean, keyBorders: Boolean) : LinearLayout(context) {

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

    private val buttonNumber0: KeyboardButton
    private val buttonNumber1: KeyboardButton
    private val buttonNumber2: KeyboardButton
    private val buttonNumber3: KeyboardButton
    private val buttonNumber4: KeyboardButton
    private val buttonNumber5: KeyboardButton
    private val buttonNumber6: KeyboardButton
    private val buttonNumber7: KeyboardButton
    private val buttonNumber8: KeyboardButton
    private val buttonNumber9: KeyboardButton

    private val buttonLetterA: KeyboardButton
    private val buttonLetterB: KeyboardButton
    private val buttonLetterC: KeyboardButton
    private val buttonLetterD: KeyboardButton
    private val buttonLetterE: KeyboardButton
    private val buttonLetterF: KeyboardButton
    private val buttonLetterG: KeyboardButton
    private val buttonLetterH: KeyboardButton
    private val buttonLetterI: KeyboardButton
    private val buttonLetterJ: KeyboardButton
    private val buttonLetterK: KeyboardButton
    private val buttonLetterL: KeyboardButton
    private val buttonLetterM: KeyboardButton
    private val buttonLetterN: KeyboardButton
    private val buttonLetterO: KeyboardButton
    private val buttonLetterP: KeyboardButton
    private val buttonLetterQ: KeyboardButton
    private val buttonLetterR: KeyboardButton
    private val buttonLetterS: KeyboardButton
    private val buttonLetterT: KeyboardButton
    private val buttonLetterU: KeyboardButton
    private val buttonLetterV: KeyboardButton
    private val buttonLetterW: KeyboardButton
    private val buttonLetterX: KeyboardButton
    private val buttonLetterY: KeyboardButton
    private val buttonLetterZ: KeyboardButton

    private val buttonSymbol1: KeyboardButton

    val viewBlank1: View
    val viewBlank2: View
    val buttonColon: KeyboardButton

    val buttonSpecialComma: KeyboardButton
    val buttonSpecialCommaWijesekara: KeyboardButton
    private var buttonSpecialSpace: ImageView
    private var buttonSpecialFullStop: KeyboardButton
    var buttonActionShift: ImageView
    private var buttonActionBackspace: ImageView
    private var buttonActionLang: KeyboardButton
    private var buttonActionPanel: KeyboardButton
    var buttonActionAction: ImageView

    init {
        val style = when {
            !darkTheme && keyBorders -> R.style.Light
            !darkTheme && !keyBorders -> R.style.LightNoBorder
            darkTheme && !keyBorders -> R.style.NightNoBorder
            else -> R.style.Night
        }

        val contextThemeWrapper = ContextThemeWrapper(context, style)
        val keyboard = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.keyboard_layout, this, true)

        if (rowHeight != null) {
            keyboard.findViewById<LinearLayout>(R.id.key_row_1).layoutParams.height = rowHeight
            keyboard.findViewById<LinearLayout>(R.id.key_row_2).layoutParams.height = rowHeight
            keyboard.findViewById<LinearLayout>(R.id.key_row_3).layoutParams.height = rowHeight
            keyboard.findViewById<LinearLayout>(R.id.key_row_4).layoutParams.height = rowHeight
            keyboard.findViewById<LinearLayout>(R.id.key_row_5).layoutParams.height = rowHeight
        }

        buttonNumber0 = keyboard.findViewById(R.id.n_0)
        buttonNumber1 = keyboard.findViewById(R.id.n_1)
        buttonNumber2 = keyboard.findViewById(R.id.n_2)
        buttonNumber3 = keyboard.findViewById(R.id.n_3)
        buttonNumber4 = keyboard.findViewById(R.id.n_4)
        buttonNumber5 = keyboard.findViewById(R.id.n_5)
        buttonNumber6 = keyboard.findViewById(R.id.n_6)
        buttonNumber7 = keyboard.findViewById(R.id.n_7)
        buttonNumber8 = keyboard.findViewById(R.id.n_8)
        buttonNumber9 = keyboard.findViewById(R.id.n_9)

        buttonNumber0.clickListener = { clickListener.numberClick(it) }
        buttonNumber1.clickListener = { clickListener.numberClick(it) }
        buttonNumber2.clickListener = { clickListener.numberClick(it) }
        buttonNumber3.clickListener = { clickListener.numberClick(it) }
        buttonNumber4.clickListener = { clickListener.numberClick(it) }
        buttonNumber5.clickListener = { clickListener.numberClick(it) }
        buttonNumber6.clickListener = { clickListener.numberClick(it) }
        buttonNumber7.clickListener = { clickListener.numberClick(it) }
        buttonNumber8.clickListener = { clickListener.numberClick(it) }
        buttonNumber9.clickListener = { clickListener.numberClick(it) }

        buttonLetterA = keyboard.findViewById(R.id.l_a)
        buttonLetterB = keyboard.findViewById(R.id.l_b)
        buttonLetterC = keyboard.findViewById(R.id.l_c)
        buttonLetterD = keyboard.findViewById(R.id.l_d)
        buttonLetterE = keyboard.findViewById(R.id.l_e)
        buttonLetterF = keyboard.findViewById(R.id.l_f)
        buttonLetterG = keyboard.findViewById(R.id.l_g)
        buttonLetterH = keyboard.findViewById(R.id.l_h)
        buttonLetterI = keyboard.findViewById(R.id.l_i)
        buttonLetterJ = keyboard.findViewById(R.id.l_j)
        buttonLetterK = keyboard.findViewById(R.id.l_k)
        buttonLetterL = keyboard.findViewById(R.id.l_l)
        buttonLetterM = keyboard.findViewById(R.id.l_m)
        buttonLetterN = keyboard.findViewById(R.id.l_n)
        buttonLetterO = keyboard.findViewById(R.id.l_o)
        buttonLetterP = keyboard.findViewById(R.id.l_p)
        buttonLetterQ = keyboard.findViewById(R.id.l_q)
        buttonLetterR = keyboard.findViewById(R.id.l_r)
        buttonLetterS = keyboard.findViewById(R.id.l_s)
        buttonLetterT = keyboard.findViewById(R.id.l_t)
        buttonLetterU = keyboard.findViewById(R.id.l_u)
        buttonLetterV = keyboard.findViewById(R.id.l_v)
        buttonLetterW = keyboard.findViewById(R.id.l_w)
        buttonLetterX = keyboard.findViewById(R.id.l_x)
        buttonLetterY = keyboard.findViewById(R.id.l_y)
        buttonLetterZ = keyboard.findViewById(R.id.l_z)

        buttonLetterA.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterB.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterC.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterD.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterE.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterF.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterG.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterH.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterI.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterJ.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterK.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterL.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterM.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterN.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterO.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterP.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterQ.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterR.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterS.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterT.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterU.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterV.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterW.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterX.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterY.clickListener = { clickListener.letterOrSymbolClick(it) }
        buttonLetterZ.clickListener = { clickListener.letterOrSymbolClick(it) }

        buttonSymbol1 = keyboard.findViewById(R.id.symbol_1)
        buttonSymbol1.clickListener = { clickListener.letterOrSymbolClick(it) }

        viewBlank1 = keyboard.findViewById(R.id.blank_1)
        viewBlank2 = keyboard.findViewById(R.id.blank_2)
        buttonColon = keyboard.findViewById(R.id.colon_wijesekara)
        buttonColon.clickListener = { clickListener.specialClick(it) }

        buttonSpecialComma = keyboard.findViewById(R.id.comma)
        buttonSpecialCommaWijesekara = keyboard.findViewById(R.id.comma_wijesekara)
        buttonSpecialSpace = keyboard.findViewById(R.id.space)
        buttonSpecialFullStop = keyboard.findViewById(R.id.dot)

        buttonSpecialComma.clickListener = { clickListener.specialClick(it) }
        buttonSpecialCommaWijesekara.clickListener = { clickListener.specialClick(it) }
        buttonSpecialFullStop.clickListener = { clickListener.specialClick(it) }

        buttonSpecialSpace.setOnLongClickListener {
            clickListener.functionClick(Function.IME)
            true
        }
        buttonSpecialSpace.setOnTouchListener { v, event ->
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

        buttonActionShift = keyboard.findViewById(R.id.shift)
        buttonActionBackspace = keyboard.findViewById(R.id.backspace)
        buttonActionLang = keyboard.findViewById(R.id.lang)
        buttonActionPanel = keyboard.findViewById(R.id.panel)
        buttonActionAction = keyboard.findViewById(R.id.action)

        buttonActionLang.clickListener = { clickListener.functionClick(Function.LANG) }
        buttonActionPanel.clickListener = { clickListener.functionClick(Function.PANEL) }

        buttonActionShift.setOnTouchListener { v, event ->
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

        buttonActionAction.setOnTouchListener { v, event ->
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

        buttonActionBackspace.setOnTouchListener { v, event ->
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
        buttonLetterA.text = keySet["a"]
        buttonLetterB.text = keySet["b"]
        buttonLetterC.text = keySet["c"]
        buttonLetterD.text = keySet["d"]
        buttonLetterE.text = keySet["e"]
        buttonLetterF.text = keySet["f"]
        buttonLetterG.text = keySet["g"]
        buttonLetterH.text = keySet["h"]
        buttonLetterI.text = keySet["i"]
        buttonLetterJ.text = keySet["j"]
        buttonLetterK.text = keySet["k"]
        buttonLetterL.text = keySet["l"]
        buttonLetterM.text = keySet["m"]
        buttonLetterN.text = keySet["n"]
        buttonLetterO.text = keySet["o"]
        buttonLetterP.text = keySet["p"]
        buttonLetterQ.text = keySet["q"]
        buttonLetterR.text = keySet["r"]
        buttonLetterS.text = keySet["s"]
        buttonLetterT.text = keySet["t"]
        buttonLetterU.text = keySet["u"]
        buttonLetterV.text = keySet["v"]
        buttonLetterW.text = keySet["w"]
        buttonLetterX.text = keySet["x"]
        buttonLetterY.text = keySet["y"]
        buttonLetterZ.text = keySet["z"]

        keySet[":"]?.let { buttonColon.text = it }
    }

    fun setNumberKeys(keySet: Array<String>) {
        buttonNumber1.text = keySet[0]
        buttonNumber2.text = keySet[1]
        buttonNumber3.text = keySet[2]
        buttonNumber4.text = keySet[3]
        buttonNumber5.text = keySet[4]
        buttonNumber6.text = keySet[5]
        buttonNumber7.text = keySet[6]
        buttonNumber8.text = keySet[7]
        buttonNumber9.text = keySet[8]
        buttonNumber0.text = keySet[9]
    }

    fun setLangIndicator(text: String) {
        buttonActionLang.text = text
    }

    fun showSymbols(
        symbolsVisibility: Boolean,
        currentLayout: KeyboardLayout,
        keyMap: Map<String, String>? = null
    ) {
        if (currentLayout == KeyboardLayout.WIJESEKARA) {
            viewBlank1.isVisible = symbolsVisibility
            viewBlank2.isVisible = symbolsVisibility
        }
        buttonColon.isVisible = !symbolsVisibility && currentLayout == KeyboardLayout.WIJESEKARA
        buttonSymbol1.isVisible = symbolsVisibility
        buttonActionLang.isVisible = !symbolsVisibility

        if (symbolsVisibility && keyMap != null) setLetterKeys(keyMap)
    }
}
