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

import android.content.res.Resources
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import kasun.sinhala.keyboard.KeyboardLayout.*
import java.util.Date

class InputMethodService : InputMethodService(), KeyboardView.ClickListener, KeyboardView.SwipeListener {
    private lateinit var prefs: Prefs
    private var darkTheme = true
    private var keyBorders = true
    private var heightPercentage = 100
    private var swipeToErase = true
    private var textSize = Prefs.DEFAULT_TEXT_SIZE
    private val swipeErasedContent = mutableListOf<String>()

    override fun onWindowHidden() {
        keyboardView.keyboardVisible = false
        super.onWindowHidden()
    }

    override fun onWindowShown() {
        keyboardView.keyboardVisible = true
        super.onWindowShown()
    }

    private var currentLayout: KeyboardLayout = SINGLISH
        set(value) {
            field = value
            toggleWijesekaraLayoutButtons(value)
            updateLangIndicator(value)
            resetInputMarkers()
            prefs.currentLayout = value.name
        }

    private var symbols: Boolean = false
        set(value) {
            shift = false
            field = value

            keyboardView.showSymbols(value, currentLayout, symbolsMap)
        }

    private lateinit var keyboardView: KeyboardView

    private val isCurrentLayoutEnabled: Boolean
        get() {
            return when (currentLayout) {
                ENGLISH -> prefs.layoutEnglish
                WIJESEKARA -> prefs.layoutWijesekara
                SINGLISH -> prefs.layoutSinglish
            }
        }

    private val nextAvailableLayout: KeyboardLayout
        get() {
            return when (currentLayout) {
                ENGLISH -> {
                    when {
                        prefs.layoutWijesekara -> WIJESEKARA
                        prefs.layoutSinglish -> SINGLISH
                        else -> ENGLISH
                    }
                }

                WIJESEKARA -> {
                    when {
                        prefs.layoutSinglish -> SINGLISH
                        prefs.layoutEnglish -> ENGLISH
                        else -> WIJESEKARA
                    }
                }

                SINGLISH -> {
                    when {
                        prefs.layoutEnglish -> ENGLISH
                        prefs.layoutWijesekara -> WIJESEKARA
                        else -> SINGLISH
                    }
                }
            }
        }

    private var shiftLastPressed = 0L

    private var shift = false
        set(value) {
            field = value
            if (symbols)
                keyboardView.showSymbols(
                    symbolsVisibility = true,
                    keyMap = if (value) symbolsMapShifted else symbolsMap,
                    currentLayout = currentLayout
                )
            else {
                keyboardView.buttonActionShift.setImageResource(if (field) R.drawable.ic_shift_pressed else R.drawable.ic_shift)
            }
        }

    private var caps = false

    private var positionFlag: CharSequence = ""
    private var lastText: String = ""
    private var lastChar: CHAR? = null
    private var lastLetter: CHAR? = null

    /* Without this, kombuva keeps going forward with each new letter in Wijesekara layout */
    private var kombuvaNew: Boolean = false

    override fun onCreate() {
        prefs = Prefs(this)
        super.onCreate()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        if (!::keyboardView.isInitialized)
            setInputView(onCreateInputView())
        if (!isCurrentLayoutEnabled)
            currentLayout = nextAvailableLayout

        if (darkTheme != prefs.darkTheme || keyBorders != prefs.keyBorders || heightPercentage != prefs.heightPercentage ||
            swipeToErase != prefs.swipeToErase || textSize != prefs.textSize
        ) setInputView(onCreateInputView())

        shift = false
        caps = false
        symbols = false
        resetInputMarkers()
        currentLayout = KeyboardLayout.valueOf(prefs.currentLayout)
        updateKeySet()
        super.onStartInput(attribute, restarting)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        symbols = false
        updateKeySet()
        keyboardView.buttonActionAction.setImageResource(
            when (currentInputEditorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_SEND ->
                    if (currentInputEditorInfo.inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE == 0)
                        R.drawable.ic_send
                    else
                        R.drawable.ic_keyboard_return

                EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT ->
                    R.drawable.ic_keyboard_arrow_right

                EditorInfo.IME_ACTION_SEARCH ->
                    R.drawable.ic_search

                EditorInfo.IME_ACTION_DONE ->
                    R.drawable.ic_check

                EditorInfo.IME_ACTION_PREVIOUS ->
                    R.drawable.ic_keyboard_arrow_left

                else ->
                    R.drawable.ic_keyboard_return
            }
        )


        super.onStartInputView(info, restarting)
    }

    private fun toggleWijesekaraLayoutButtons(layout: KeyboardLayout) {
        (layout == WIJESEKARA).let { isWijesekara ->
            keyboardView.buttonSpecialComma.isVisible = isWijesekara.not()
            keyboardView.buttonSpecialCommaWijesekara.isVisible = isWijesekara
            keyboardView.buttonColon.isVisible = isWijesekara
            keyboardView.viewBlank1.isVisible = isWijesekara.not()
            keyboardView.viewBlank2.isVisible = isWijesekara.not()
        }
    }

    private fun updateLangIndicator(layout: KeyboardLayout) {
        keyboardView.setLangIndicator(
            when (layout) {
                ENGLISH -> "E"
                WIJESEKARA -> "වි"
                SINGLISH -> "සි"
            }
        )
    }

    private fun resetInputMarkers() {
        positionFlag = ""
        lastText = ""
        lastChar = null
        lastLetter = null
    }

    override fun onCreateInputView(): View {
        darkTheme = prefs.darkTheme
        keyBorders = prefs.keyBorders
        heightPercentage = prefs.heightPercentage
        swipeToErase = prefs.swipeToErase
        textSize = prefs.textSize

        val rowHeight: Int = ((48 * heightPercentage / 100) * Resources.getSystem().displayMetrics.density).toInt()

        setTheme(darkTheme)
        keyboardView = KeyboardView(this, this, this, rowHeight, darkTheme, keyBorders, swipeToErase, textSize)
        updateKeySet()
        currentLayout.let { layout ->
            toggleWijesekaraLayoutButtons(layout)
            updateLangIndicator(layout)
        }
        return keyboardView
    }

    private fun updateKeySet() {
        keyboardView.setNumberKeys(
            if (currentLayout == WIJESEKARA && !symbols)
                keyLabelsNumbersWijesekara
            else
                keyLabelsNumbers
        )
        keyboardView.setLetterKeys(
            if (currentLayout == WIJESEKARA && prefs.sinhalaKeyLabels && !symbols)
                if (shift) keyLabelsLettersWijesekaraShifted
                else keyLabelsLettersWijesekara
            else if (symbols)
                if (shift) symbolsMapShifted
                else symbolsMap
            else
                if (shift) keyLabelsLettersEnglishShifted
                else keyLabelsLettersEnglish
        )
        keyboardView.setSpecialKeys(
            if (currentLayout == WIJESEKARA && !symbols) {
                if (prefs.sinhalaKeyLabels)
                    if (shift) keyLabelsSpecialWijesekaraSinhalaShifted
                    else keyLabelsSpecialWijesekaraSinhala
                else
                    if (shift) keyLabelsSpecialWijesekaraShifted
                    else keyLabelsSpecialWijesekara
            } else keyLabelsSpecialEnglish
        )
    }

    private fun setTheme(dark: Boolean) {
        if (dark) {
            window.window?.navigationBarColor = ResourcesCompat.getColor(resources, R.color.night_1, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                window.window?.insetsController?.systemBarsAppearance?.let { existingFlags ->
                    window.window?.insetsController?.setSystemBarsAppearance(
                        existingFlags and APPEARANCE_LIGHT_NAVIGATION_BARS.inv(),
                        APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                @Suppress("Deprecation")
                window.window?.decorView?.systemUiVisibility?.let { existingFlags ->
                    window.window?.decorView?.systemUiVisibility = existingFlags and SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
        } else {
            window.window?.navigationBarColor = ResourcesCompat.getColor(resources, R.color.light_1, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                window.window?.insetsController?.systemBarsAppearance?.let { existingFlags ->
                    window.window?.insetsController?.setSystemBarsAppearance(
                        existingFlags or APPEARANCE_LIGHT_NAVIGATION_BARS,
                        APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("Deprecation")
                window.window?.decorView?.systemUiVisibility?.let { existingFlags ->
                    window.window?.decorView?.systemUiVisibility = existingFlags or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
        }
    }

    private fun shiftClick() {
        if (shift && !symbols && Date().time - shiftLastPressed < 500)
            caps = true
        else {
            shiftLastPressed = Date().time
            caps = false
            shift = shift.not()
            updateKeySet()
        }
    }

    private fun symbolsInput(tag: String) {
        if (shift)
            symbolsMapShifted[tag]?.let { commitText(it) }
        else
            symbolsMap[tag]?.let { commitText(it) }
    }

    override fun letterOrSymbolClick(tag: String) {
        if (symbols)
            symbolsInput(tag)
        else
            (if (shift) tag.uppercase() else tag).let { char ->
                when (currentLayout) {
                    ENGLISH -> commitText(char)
                    WIJESEKARA -> wijesekaraInput(char)
                    SINGLISH -> singlishInput(char)
                }
            }
    }

    override fun numberClick(tag: String) {
        when (currentLayout) {
            ENGLISH, SINGLISH -> commitText(tag)
            WIJESEKARA -> if (!symbols) wijesekaraInput(tag) else commitText(tag)
        }
    }

    override fun specialClick(tag: String) {
        when (currentLayout) {
            WIJESEKARA -> when (tag) {
                "44" -> {
                    if (symbols) commitText(CHAR.COMMA.text)
                    else wijesekaraInput(if (shift) CHAR.LESS_THAN.text else CHAR.COMMA.text)
                }

                "46" -> {
                    if (symbols) commitText(CHAR.FULL_STOP.text)
                    else wijesekaraInput(if (shift) CHAR.GREATER_THAN.text else CHAR.FULL_STOP.text)
                }

                "59" -> wijesekaraInput(if (shift) CHAR.COLON.text else CHAR.SEMICOLON.text)
                else -> {
                    lastText = ""
                    commitText(tag.toInt())
                }
            }

            SINGLISH -> {
                lastChar = null
                lastLetter = null
                commitText(tag.toInt())
            }

            ENGLISH -> commitText(tag.toInt())
        }
    }

    private fun backspaceClick() {
        if (currentInputConnection.getSelectedText(0).isNullOrBlank()) {
            when (currentInputConnection.getTextBeforeCursor(1, 0)?.getOrElse(0) { ' ' }) {
                'ෲ' -> eraseAndCommit("ෘ")
                'ෛ' -> eraseAndCommit("ෙ")
                'ො' -> eraseAndCommit("ෙ")
                'ෞ' -> eraseAndCommit("ෙ")
                'ෝ' -> eraseAndCommit("ො")
                'ආ', 'ඇ', 'ඈ' -> eraseAndCommit("අ")
                'ඌ' -> eraseAndCommit("උ")
                'ඎ' -> eraseAndCommit("ඍ")
                'ඐ' -> eraseAndCommit("ඏ")
                'ඒ' -> eraseAndCommit("එ")
                'ඕ', 'ඖ' -> eraseAndCommit("ඔ")

                else -> erasePrevious()
            }
        } else commitText("")

        clearLastTypedInfo()
    }

    private fun clearLastTypedInfo() {
        if (currentLayout == SINGLISH) {
            lastLetter = null
            lastChar = null
        }
        if (currentLayout == WIJESEKARA)
            lastText = ""
    }

    override fun functionClick(type: Function) {
        when (type) {
            Function.ACTION -> actionClick()
            Function.SHIFT -> shiftClick()
            Function.LANG -> {
                currentLayout = nextAvailableLayout
                updateKeySet()
            }

            Function.IME -> (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            Function.BACKSPACE -> backspaceClick()
            Function.PANEL -> {
                symbols = symbols.not()
                updateKeySet()
            }
        }
    }

    private fun actionClick() {
        when (val action: Int = currentInputEditorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_PREVIOUS -> currentInputConnection.performEditorAction(action)

            EditorInfo.IME_ACTION_UNSPECIFIED,
            EditorInfo.IME_ACTION_NONE,
            EditorInfo.IME_ACTION_SEND -> {
                clearLastTypedInfo()
                commitText("\n")
            }

            EditorInfo.IME_ACTION_DONE -> currentInputConnection.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
            )
        }
    }

    private fun commitText(code: Int) = commitText(Char(code))
    private fun commitText(char: Char) = commitText(char.toString())
    private fun commitText(text: String) {
        currentInputConnection.commitText(text, 1)
        positionFlag = currentInputConnection.getTextBeforeCursor(5, 0).toString()
        if (!caps && shift && !symbols) {
            shift = false
            updateKeySet()
        }
    }

    private fun charBeforeCursorSurrogate(): Boolean {
        currentInputConnection.getTextBeforeCursor(1, 0)?.let {
            if (it.isNotEmpty() && it[0].isSurrogate()) return true
        }
        return false
    }

    private fun erasePrevious(count: Int = 1, returnDeletedText: Boolean = false): String {
        var erasedText = ""
        if (count == 1 && charBeforeCursorSurrogate()) {
            if (returnDeletedText) erasedText = currentInputConnection.getTextBeforeCursor(2, 0).toString()
            currentInputConnection.deleteSurroundingText(2, 0)
        } else {
            if (returnDeletedText) erasedText = currentInputConnection.getTextBeforeCursor(count, 0).toString()
            currentInputConnection.deleteSurroundingText(count, 0)
        }

        if (currentInputConnection.getTextBeforeCursor(1, 0) == CHAR.ZERO_WIDTH_JOINER.text) {
            val erasedTextNested = erasePrevious(1, returnDeletedText)
            if (returnDeletedText) erasedText = erasedTextNested + erasedText
        }
        return erasedText
    }

    private fun eraseAndCommit(text: String) {
        erasePrevious()
        commitText(text)
    }

    private fun wijesekaraInput(input: String) {
        var erasePrevious = false
        var lastTextOverride: String? = null
        var output = getWijesekaraChars(input)

        if (lastText.isBlank() or hasPositionChanged())
            lastText = currentInputConnection.getTextBeforeCursor(1, 0)?.getOrElse(0) { ' ' }.toString()


        when (lastText) {
            CHAR.AYANNA.text -> {
                when (output) {
                    CHAR.AELA_PILLA.text -> {
                        output = CHAR.AAYANNA.text
                        erasePrevious = true
                    }

                    CHAR.KETTI_AEDA_PILLA.text -> {
                        output = CHAR.AEYANNA.text
                        erasePrevious = true
                    }

                    CHAR.DIGA_AEDA_PILLA.text -> {
                        output = CHAR.AEEYANNA.text
                        erasePrevious = true
                    }
                }
            }

            CHAR.UYANNA.text ->
                if (output == CHAR.GAYANUKITTA.text) {
                    output = CHAR.UUYANNA.text
                    erasePrevious = true
                }

            CHAR.IRUYANNA.text ->
                if (output == CHAR.GAETTA_PILLA.text) {
                    output = CHAR.IRUUYANNA.text
                    erasePrevious = true
                }

            CHAR.ILUYANNA.text ->
                if (output == CHAR.GAYANUKITTA.text) {
                    output = CHAR.ILUUYANNA.text
                    erasePrevious = true
                }

            CHAR.EYANNA.text ->
                if (output == CHAR.SIGN_AL_LAKUNA.text) {
                    output = CHAR.EEYANNA.text
                    erasePrevious = true
                }

            CHAR.OYANNA.text -> {
                if (output == CHAR.SIGN_AL_LAKUNA.text) {
                    output = CHAR.OOYANNA.text
                    erasePrevious = true
                } else if (output == CHAR.GAYANUKITTA.text) {
                    output = CHAR.AUYANNA.text
                    erasePrevious = true
                }
            }

            CHAR.KOMBUVA.text -> {
                if (kombuvaNew) {
                    erasePrevious = true
                    if ((getCharType(output) == CharType.SWARA || getCharType(output) == CharType.WYANJANA)) {
                        lastTextOverride = CHAR.KOMBUVA.text
                        output += CHAR.KOMBUVA.text
                    } else {
                        when (output) {
                            CHAR.EYANNA.text -> output = CHAR.AIYANNA.text
                            CHAR.KOMBUVA.text -> output = CHAR.KOMBU_DEKA.text
                            CHAR.SIGN_AL_LAKUNA.text -> output = CHAR.DIGA_KOMBUVA.text
                            CHAR.AELA_PILLA.text -> output = CHAR.KOMBUVA_HAA_AELA_PILLA.text
                            CHAR.GAYANUKITTA.text -> output = CHAR.KOMBUVA_HAA_GAYANUKITTA.text
                        }
                    }
                } else if (output == CHAR.SIGN_RAKARANSHAYA.text) {
                    erasePrevious = true
                    output = CHAR.SIGN_RAKARANSHAYA.text + CHAR.KOMBUVA.text
                    lastTextOverride = CHAR.KOMBUVA.text
                }
            }

            CHAR.KOMBUVA_HAA_AELA_PILLA.text -> {
                if (output == CHAR.SIGN_AL_LAKUNA.text) {
                    output = CHAR.KOMBUVA_HAA_DIGA_AELA_PILLA.text
                    erasePrevious = true
                }
            }

            CHAR.KOMBU_DEKA.text -> {
                if ((getCharType(output) == CharType.SWARA || getCharType(output) == CharType.WYANJANA)) {
                    output += CHAR.KOMBU_DEKA.text
                    erasePrevious = true
                }
            }

            else -> {
                if (output == CHAR.SIGN_REEPAYA.text) {
                    output = CHAR.SIGN_REEPAYA.text + lastText
                    erasePrevious = true
                }
            }
        }

        kombuvaNew = output == CHAR.KOMBUVA.text

        if (erasePrevious)
            eraseAndCommit(output)
        else
            commitText(output)

        lastText = lastTextOverride ?: output
    }

    private fun getCombinedWijesekaraChars(input: String): String? = when (input) {
        "J" -> CHAR.MUURDHAJA_LAYANNA.text + CHAR.KETTI_PAA_PILLA.text
        else -> null
    }

    private fun getWijesekaraChars(input: String): String =
        wijesekaraMap[input]?.text ?: getCombinedWijesekaraChars(input) ?: input

    private fun hasPositionChanged(): Boolean =
        currentInputConnection.getTextBeforeCursor(5, 0) != positionFlag

    private fun singlishInput(input: String) {
        var output = ""
        var erasePreviousChars = 0
        var mLastChar: CHAR? = null
        var mLastLetter: CHAR? = null
        var tLastChar: CHAR? = null
        var tLastLetter: CHAR? = null

        if (!hasPositionChanged()) {
            mLastChar = lastChar
            mLastLetter = lastLetter
        }

        lastChar = null
        lastLetter = null

        var singlishChar: CHAR = getSinglishChars(input) ?: CHAR.EMPTY

        fun newLetter() {
            output = singlishChar.text
            if (singlishChar.type == CharType.WYANJANA) {
                output += CHAR.SIGN_AL_LAKUNA.text
                tLastChar = CHAR.SIGN_AL_LAKUNA
            }
        }

        if (mLastChar == null) {
            if (input == "z" || input == "Z") tLastChar = CHAR.MARK_SANYAKA
            else newLetter()
        } else {
            when {
                input == "z" || input == "Z" -> tLastChar = CHAR.MARK_SANYAKA
                mLastChar.type == CharType.WYANJANA ->
                    when (singlishChar.code) {
                        CHAR.AYANNA.code -> output = CHAR.AELA_PILLA.text
                        CHAR.IYANNA.code -> output = CHAR.KOMBU_DEKA.text
                        CHAR.UYANNA.code -> output = CHAR.KOMBUVA_HAA_GAYANUKITTA.text
                        else -> newLetter()
                    }

                mLastChar.code == CHAR.SIGN_AL_LAKUNA.code -> {
                    when (singlishChar.code) {
                        CHAR.AYANNA.code -> {
                            erasePreviousChars = 1
                            tLastChar = mLastLetter
                        }

                        CHAR.RAYANNA.code -> {
                            output =
                                CHAR.ZERO_WIDTH_JOINER.text + CHAR.RAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                            tLastChar = CHAR.SIGN_AL_LAKUNA
                        }

                        CHAR.YAYANNA.code -> {
                            output =
                                CHAR.ZERO_WIDTH_JOINER.text + CHAR.YAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                            tLastChar = CHAR.SIGN_AL_LAKUNA
                        }

                        CHAR.HAYANNA.code -> {
                            if (mLastLetter != null) {
                                when (mLastLetter.code) {
                                    CHAR.ALPAPRAANA_TTAYANNA.code -> {
                                        output =
                                            CHAR.ALPAPRAANA_TAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.ALPAPRAANA_TAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.MAHAAPRAANA_TTAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_TAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_TAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_DDAYANNA.code -> {
                                        output =
                                            CHAR.ALPAPRAANA_DAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.ALPAPRAANA_DAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.MAHAAPRAANA_DDAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_DAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_DAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_KAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_KAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_KAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_GAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_GAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_GAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_CAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_CAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_CAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_JAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_JAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_JAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_TAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_TAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_TAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_DAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_DAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_DAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_PAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_PAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_PAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.ALPAPRAANA_BAYANNA.code -> {
                                        output =
                                            CHAR.MAHAAPRAANA_BAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MAHAAPRAANA_BAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.DANTAJA_SAYANNA.code -> {
                                        output =
                                            CHAR.TAALUJA_SAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.TAALUJA_SAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.SANYAKA_DDAYANNA.code -> {
                                        output =
                                            CHAR.SANYAKA_DAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.SANYAKA_DAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    CHAR.MUURDHAJA_SAYANNA.code -> {
                                        output =
                                            CHAR.MUURDHAJA_SAYANNA.text + CHAR.SIGN_AL_LAKUNA.text
                                        erasePreviousChars = 2
                                        tLastLetter = CHAR.MUURDHAJA_SAYANNA
                                        tLastChar = CHAR.SIGN_AL_LAKUNA
                                    }

                                    else -> newLetter()
                                }
                            } else newLetter()
                        }

                        else -> {
                            when (singlishChar.type) {
                                CharType.SWARA -> {
                                    if (mLastLetter != null) {
                                        if (singlishChar.code == CHAR.AYANNA.code)
                                            erasePreviousChars = 1
                                        else {
                                            swaraSignMap[singlishChar.code].let { sign ->
                                                if (sign != null) {
                                                    output += sign.text
                                                    tLastChar = sign
                                                    erasePreviousChars = 1
                                                } else output = singlishChar.text
                                            }
                                        }
                                    } else output = singlishChar.text
                                }

                                else -> newLetter()
                            }
                        }
                    }
                }

                mLastChar.type == CharType.PILI -> {
                    when {
                        mLastChar.code == CHAR.KETTI_AEDA_PILLA.code && singlishChar.code == CHAR.AYANNA.code -> {
                            output = CHAR.DIGA_AEDA_PILLA.text
                            erasePreviousChars = 1
                            tLastChar = CHAR.DIGA_AEDA_PILLA
                        }

                        mLastChar.code == CHAR.KETTI_IS_PILLA.code && singlishChar.code == CHAR.IYANNA.code -> {
                            output = CHAR.DIGA_IS_PILLA.text
                            erasePreviousChars = 1
                            tLastChar = CHAR.DIGA_IS_PILLA
                        }

                        mLastChar.code == CHAR.KETTI_PAA_PILLA.code && singlishChar.code == CHAR.UYANNA.code -> {
                            output = CHAR.DIGA_PAA_PILLA.text
                            erasePreviousChars = 1
                            tLastChar = CHAR.DIGA_PAA_PILLA
                        }

                        mLastChar.code == CHAR.GAETTA_PILLA.code && singlishChar.code == CHAR.IYANNA.code -> {
                            output = CHAR.DIGA_GAETTA_PILLA.text
                            erasePreviousChars = 1
                            tLastChar = CHAR.DIGA_GAETTA_PILLA
                        }

                        mLastChar.code == CHAR.KOMBUVA.code && singlishChar.code == CHAR.EYANNA.code -> {
                            output = CHAR.DIGA_KOMBUVA.text
                            erasePreviousChars = 1
                            tLastChar = CHAR.DIGA_KOMBUVA
                        }

                        mLastChar.code == CHAR.KOMBUVA_HAA_AELA_PILLA.code && singlishChar.code == CHAR.OYANNA.code -> {
                            output = CHAR.KOMBUVA_HAA_DIGA_AELA_PILLA.text
                            erasePreviousChars = 1
                            tLastChar = CHAR.KOMBUVA_HAA_DIGA_AELA_PILLA
                        }

                        else -> newLetter()
                    }
                }

                mLastChar.code == CHAR.MARK_SANYAKA.code -> {
                    when (singlishChar.code) {
                        CHAR.ALPAPRAANA_KAYANNA.code -> singlishChar = CHAR.TAALUJA_NAASIKYAYA
                        CHAR.ALPAPRAANA_GAYANNA.code -> singlishChar = CHAR.SANYAKA_GAYANNA
                        CHAR.ALPAPRAANA_JAYANNA.code -> singlishChar = CHAR.SANYAKA_JAYANNA
                        CHAR.ALPAPRAANA_DDAYANNA.code -> singlishChar = CHAR.SANYAKA_DDAYANNA
                        CHAR.ALPAPRAANA_BAYANNA.code -> singlishChar = CHAR.AMBA_BAYANNA
                        CHAR.HAYANNA.code -> singlishChar = CHAR.TAALUJA_SANYOOGA_NAAKSIKYAYA
                    }
                    newLetter()
                }

                else -> {
                    if (mLastLetter != null) {
                        when (mLastLetter) {
                            CHAR.AYANNA -> {
                                when (singlishChar.code) {
                                    CHAR.AYANNA.code -> {
                                        output = CHAR.AAYANNA.text
                                        erasePreviousChars = 1
                                        tLastLetter = CHAR.AAYANNA
                                    }

                                    CHAR.IYANNA.code -> {
                                        output = CHAR.AIYANNA.text
                                        erasePreviousChars = 1
                                        tLastLetter = CHAR.AIYANNA
                                    }

                                    CHAR.UYANNA.code -> {
                                        output = CHAR.AUYANNA.text
                                        erasePreviousChars = 1
                                        tLastLetter = CHAR.AUYANNA
                                    }

                                    else -> newLetter()
                                }
                            }

                            CHAR.AEYANNA -> {
                                if (singlishChar.code == CHAR.AYANNA.code) {
                                    output = CHAR.AEEYANNA.text
                                    erasePreviousChars = 1
                                    tLastLetter = CHAR.AEEYANNA
                                } else newLetter()
                            }

                            CHAR.IYANNA -> {
                                if (singlishChar.code == CHAR.IYANNA.code) {
                                    output = CHAR.IIYANNA.text
                                    erasePreviousChars = 1
                                    tLastLetter = CHAR.IIYANNA
                                } else newLetter()
                            }

                            CHAR.UYANNA -> {
                                if (singlishChar.code == CHAR.UYANNA.code) {
                                    output = CHAR.UUYANNA.text
                                    erasePreviousChars = 1
                                    tLastLetter = CHAR.UUYANNA
                                } else newLetter()
                            }

                            CHAR.IRUYANNA -> {
                                if (singlishChar.code == CHAR.IYANNA.code) {
                                    output = CHAR.IRUUYANNA.text
                                    erasePreviousChars = 1
                                    tLastLetter = CHAR.IRUUYANNA
                                } else newLetter()
                            }

                            CHAR.EYANNA -> {
                                if (singlishChar.code == CHAR.EYANNA.code) {
                                    output = CHAR.EEYANNA.text
                                    erasePreviousChars = 1
                                    tLastLetter = CHAR.EEYANNA
                                } else newLetter()
                            }

                            CHAR.OYANNA -> {
                                if (singlishChar.code == CHAR.OYANNA.code) {
                                    output = CHAR.OOYANNA.text
                                    erasePreviousChars = 1
                                    tLastLetter = CHAR.OOYANNA
                                } else newLetter()
                            }

                            else -> newLetter()
                        }
                    } else newLetter()
                }
            }
        }

        if (erasePreviousChars > 0) erasePrevious(erasePreviousChars)

        commitText(output)

        lastLetter = tLastLetter ?: singlishChar
        lastChar = tLastChar ?: tLastLetter ?: singlishChar
    }

    private fun getSinglishChars(input: String): CHAR? = singlishMap[input]

    override fun eraseDo() {
        val erasedText = erasePrevious(1, true)
        swipeErasedContent.add(0, erasedText)
    }

    override fun eraseUndo() {
        val erasedText = swipeErasedContent[0]
        commitText(erasedText)
        swipeErasedContent.removeAt(0)
    }

    override fun eraseDone() {
        swipeErasedContent.clear()
    }

}