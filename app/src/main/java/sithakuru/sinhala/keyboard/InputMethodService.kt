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

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sithakuru.sinhala.keyboard.data.Action
import sithakuru.sinhala.keyboard.data.KeyboardLayoutId
import sithakuru.sinhala.keyboard.databinding.HardwareInputViewBinding
import sithakuru.sinhala.keyboard.layouts.BaseLayout
import sithakuru.sinhala.keyboard.layouts.English
import sithakuru.sinhala.keyboard.layouts.Singlish
import sithakuru.sinhala.keyboard.layouts.Symbols
import sithakuru.sinhala.keyboard.layouts.Wijesekara
import java.util.Date

class InputMethodService : InputMethodService(), Keyboard.EventListener {
    private lateinit var prefs: Prefs

    private var darkTheme: Boolean = Prefs.DEFAULT_DARK_THEME

    private val swipeErasedContent: MutableList<String> = mutableListOf()

    @DrawableRes
    private var currentActionKeyIconRes: Int = -1
    private var currentActionKeyAction: Int = ActionKeyAction.NEWLINE

    private val keyboardLayoutEnglish: English by lazy { English() }
    private val keyboardLayoutSinglish: Singlish by lazy { Singlish() }
    private val keyboardLayoutWijesekara: Wijesekara by lazy { Wijesekara() }
    private val keyboardLayoutSymbols: Symbols by lazy { Symbols() }

    private var currentLayout: Int = -1
    private var currentKeyboardLayout: BaseLayout = keyboardLayoutWijesekara

    private var symbols: Boolean = false

    private var hardwareInputViewBinding: HardwareInputViewBinding? = null
    private lateinit var keyboardView: Keyboard
    private var insetBottom: Int = 0

    private var keyDownEventHandler: (keyCode: Int, event: KeyEvent) -> Boolean? = { _, _ -> false }
    private var keyCodeToCharCodeConverter: (keyCode: Int, isShiftPressed: Boolean) -> Int? = { _, _ -> null }

    private var shift = false
    private var shiftLastPressed = 0L
    private var caps = false

    private var positionFlag: CharSequence = ""
    private var lastText: String = ""
    private var lastChar: Int? = null
    private var lastLetter: Int? = null

    /* Without this, kombuva keeps going forward with each new letter in Wijesekara layout */
    private var kombuvaNew: Boolean = false

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        darkTheme = prefs.darkTheme

        shift = false
        caps = false
        symbols = false
        resetInputMarkers()
        attribute?.let { editorInfo ->
            val action: Int = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
            val isSingleLine: Boolean = editorInfo.inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE == 0

            currentActionKeyAction = if (isSingleLine) when (action) {
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_PREVIOUS -> ActionKeyAction.ORIGINAL

                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_DONE -> ActionKeyAction.ENTER

                else -> ActionKeyAction.NEWLINE
            } else ActionKeyAction.NEWLINE

            currentActionKeyIconRes = if (isSingleLine) when (action) {
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_keyboard_arrow_right

                EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_search

                EditorInfo.IME_ACTION_SEND -> R.drawable.ic_send

                EditorInfo.IME_ACTION_DONE -> R.drawable.ic_check

                EditorInfo.IME_ACTION_PREVIOUS -> R.drawable.ic_keyboard_arrow_left

                else -> R.drawable.ic_keyboard_return
            } else R.drawable.ic_keyboard_return
        }
        if (isLayoutEnabled(prefs.currentLayout)) {
            setCurrentLayout(prefs.currentLayout)
        } else {
            switchToNextAvailableLayout()
        }
        updateNavigationBarTheme()

        super.onStartInput(attribute, restarting)
    }

    override fun onCreateInputView(): View {
        if (isHardwareKeyboardAvailable()) {
            hardwareInputViewBinding = HardwareInputViewBinding.inflate(layoutInflater)

            hardwareInputViewBinding?.root?.setBackgroundColor(prefs.colorBackground)
            hardwareInputViewBinding?.layoutIndicatorContainer?.setBackgroundColor(prefs.colorKeyBorder)
            hardwareInputViewBinding?.layoutIndicator?.setTextColor(prefs.colorText)
            hardwareInputViewBinding?.layoutIndicator?.setBackgroundColor(prefs.colorBackground)
            hardwareInputViewBinding?.helpTextLine1?.setTextColor(prefs.colorTextDim)
            hardwareInputViewBinding?.helpTextLine2?.setTextColor(prefs.colorText)

            ViewCompat.setOnApplyWindowInsetsListener(hardwareInputViewBinding!!.root) { _, windowInsets ->
                insetBottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                hardwareInputViewBinding!!.root.let {
                    it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, insetBottom)
                }
                WindowInsetsCompat.CONSUMED
            }

            return hardwareInputViewBinding!!.root
        } else {
            keyboardView = Keyboard(
                context = this,
                prefs = prefs,
                eventListener = this,
                insetBottom = insetBottom
            )
            updateKeySet()

            ViewCompat.setOnApplyWindowInsetsListener(keyboardView) { _, windowInsets ->
                insetBottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                setInputView(onCreateInputView())
                WindowInsetsCompat.CONSUMED
            }

            return keyboardView
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        switchSymbols(false)
        updateKeySet()
    }

    private fun isHardwareKeyboardAvailable() = resources.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO

    private fun setCurrentLayout(newCurrentLayout: Int) {
        if (currentLayout != newCurrentLayout) {
            currentLayout = newCurrentLayout
            prefs.currentLayout = newCurrentLayout
            resetInputMarkers()

            if (newCurrentLayout == KeyboardLayoutId.WIJESEKARA) {
                keyboardLayoutSymbols.setCustomRowBottom(keyboardLayoutWijesekara.wijesekaraSymbolsRowBottom)
            } else {
                keyboardLayoutSymbols.setCustomRowBottom(null)
            }

            currentKeyboardLayout = when (newCurrentLayout) {
                KeyboardLayoutId.WIJESEKARA -> keyboardLayoutWijesekara
                KeyboardLayoutId.SINGLISH -> keyboardLayoutSinglish
                else -> keyboardLayoutEnglish
            }
            updateKeyDownEventHandlers(currentLayout)
        }
    }

    private fun isLayoutEnabled(keyboardLayoutId: Int): Boolean = when (keyboardLayoutId) {
        KeyboardLayoutId.ENGLISH -> prefs.layoutEnglish
        KeyboardLayoutId.SINGLISH -> prefs.layoutSinglish
        KeyboardLayoutId.WIJESEKARA -> prefs.layoutWijesekara
        else -> false
    }

    private fun switchToNextAvailableLayout() {
        setCurrentLayout(
            when (currentLayout) {
                KeyboardLayoutId.ENGLISH -> {
                    when {
                        prefs.layoutWijesekara -> KeyboardLayoutId.WIJESEKARA
                        prefs.layoutSinglish -> KeyboardLayoutId.SINGLISH
                        else -> KeyboardLayoutId.ENGLISH
                    }
                }

                KeyboardLayoutId.WIJESEKARA -> {
                    when {
                        prefs.layoutSinglish -> KeyboardLayoutId.SINGLISH
                        prefs.layoutEnglish -> KeyboardLayoutId.ENGLISH
                        else -> KeyboardLayoutId.WIJESEKARA
                    }
                }

                KeyboardLayoutId.SINGLISH -> {
                    when {
                        prefs.layoutEnglish -> KeyboardLayoutId.ENGLISH
                        prefs.layoutWijesekara -> KeyboardLayoutId.WIJESEKARA
                        else -> KeyboardLayoutId.SINGLISH
                    }
                }

                else -> {
                    KeyboardLayoutId.ENGLISH
                }
            }
        )
    }

    private fun resetInputMarkers() {
        positionFlag = ""
        lastText = ""
        lastChar = null
        lastLetter = null
    }

    private fun updateKeySet() {
        if (isHardwareKeyboardAvailable()) {
            hardwareInputViewBinding?.layoutIndicator?.text = currentKeyboardLayout.indicator
        } else {
            keyboardView.setActionKeyIcon(currentActionKeyIconRes)
            keyboardView.setRows(
                if (symbols) {
                    if (shift) keyboardLayoutSymbols.upper else keyboardLayoutSymbols.lower
                } else {
                    if (shift) currentKeyboardLayout.upper else currentKeyboardLayout.lower
                }
            )
        }
    }

    private fun updateNavigationBarTheme() {
        if (darkTheme) {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
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
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
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

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.isCtrlPressed == false) {
            if (event.isAltPressed && keyCode == KeyEvent.KEYCODE_I) {
                currentInputConnection.clearMetaKeyStates(KeyEvent.META_ALT_ON)
                switchToNextAvailableLayout()
                updateKeySet()
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_DEL) {
                backspaceClick()
                return true
            }

            keyDownEventHandler(keyCode, event)?.let { return it }
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun keyCodeToCharCodeWijesekara(keyCode: Int, isShiftPressed: Boolean): Int? =
        if (isShiftPressed) when (keyCode) {
            KeyEvent.KEYCODE_GRAVE -> CharCode.SIGN_REEPAYA
            KeyEvent.KEYCODE_Q -> CharCode.DIGA_PAA_PILLA
            KeyEvent.KEYCODE_W -> CharCode.UYANNA
            KeyEvent.KEYCODE_E -> CharCode.DIGA_AEDA_PILLA
            KeyEvent.KEYCODE_R -> CharCode.IRUYANNA
            KeyEvent.KEYCODE_T -> CharCode.OYANNA
            KeyEvent.KEYCODE_Y -> CharCode.TAALUJA_SAYANNA
            KeyEvent.KEYCODE_U -> CharCode.AMBA_BAYANNA
            KeyEvent.KEYCODE_I -> CharCode.MUURDHAJA_SAYANNA
            KeyEvent.KEYCODE_O -> CharCode.MAHAAPRAANA_DAYANNA
            KeyEvent.KEYCODE_P -> CharCode.MAHAAPRAANA_CAYANNA
            KeyEvent.KEYCODE_LEFT_BRACKET -> CharCode.TAALUJA_SANYOOGA_NAAKSIKYAYA
            KeyEvent.KEYCODE_RIGHT_BRACKET -> CharCode.COLON
            KeyEvent.KEYCODE_A -> CharCode.GAYANUKITTA
            KeyEvent.KEYCODE_S -> CharCode.DIGA_IS_PILLA
            KeyEvent.KEYCODE_D -> CharCode.GAETTA_PILLA
            KeyEvent.KEYCODE_F -> CharCode.FAYANNA
            KeyEvent.KEYCODE_G -> CharCode.MAHAAPRAANA_TTAYANNA
            KeyEvent.KEYCODE_H -> CharCode.SIGN_YANSHAYA
            KeyEvent.KEYCODE_J -> CharCode.MUURDHAJA_LUYANNA
            KeyEvent.KEYCODE_K -> CharCode.MUURDHAJA_NAYANNA
            KeyEvent.KEYCODE_L -> CharCode.MAHAAPRAANA_KAYANNA
            KeyEvent.KEYCODE_SEMICOLON -> CharCode.MAHAAPRAANA_TAYANNA
            KeyEvent.KEYCODE_APOSTROPHE -> CharCode.COMMA
            KeyEvent.KEYCODE_Z -> CharCode.QUOTATION_MARK
            KeyEvent.KEYCODE_X -> CharCode.KANTAJA_NAASIKYAYA
            KeyEvent.KEYCODE_C -> CharCode.MAHAAPRAANA_JAYANNA
            KeyEvent.KEYCODE_V -> CharCode.MAHAAPRAANA_DDAYANNA
            KeyEvent.KEYCODE_B -> CharCode.IIYANNA
            KeyEvent.KEYCODE_N -> CharCode.MAHAAPRAANA_BAYANNA
            KeyEvent.KEYCODE_M -> CharCode.MAHAAPRAANA_PAYANNA
            KeyEvent.KEYCODE_COMMA -> CharCode.MUURDHAJA_LAYANNA
            KeyEvent.KEYCODE_PERIOD -> CharCode.MAHAAPRAANA_GAYANNA
            else -> null
        } else when (keyCode) {
            KeyEvent.KEYCODE_GRAVE -> CharCode.SIGN_RAKARANSHAYA
            KeyEvent.KEYCODE_Q -> CharCode.KETTI_PAA_PILLA
            KeyEvent.KEYCODE_W -> CharCode.AYANNA
            KeyEvent.KEYCODE_E -> CharCode.KETTI_AEDA_PILLA
            KeyEvent.KEYCODE_R -> CharCode.RAYANNA
            KeyEvent.KEYCODE_T -> CharCode.EYANNA
            KeyEvent.KEYCODE_Y -> CharCode.HAYANNA
            KeyEvent.KEYCODE_U -> CharCode.MAYANNA
            KeyEvent.KEYCODE_I -> CharCode.DANTAJA_SAYANNA
            KeyEvent.KEYCODE_O -> CharCode.ALPAPRAANA_DAYANNA
            KeyEvent.KEYCODE_P -> CharCode.ALPAPRAANA_CAYANNA
            KeyEvent.KEYCODE_LEFT_BRACKET -> CharCode.TAALUJA_NAASIKYAYA
            KeyEvent.KEYCODE_RIGHT_BRACKET -> CharCode.SEMICOLON
            KeyEvent.KEYCODE_A -> CharCode.SIGN_AL_LAKUNA
            KeyEvent.KEYCODE_S -> CharCode.KETTI_IS_PILLA
            KeyEvent.KEYCODE_D -> CharCode.AELA_PILLA
            KeyEvent.KEYCODE_F -> CharCode.KOMBUVA
            KeyEvent.KEYCODE_G -> CharCode.ALPAPRAANA_TTAYANNA
            KeyEvent.KEYCODE_H -> CharCode.YAYANNA
            KeyEvent.KEYCODE_J -> CharCode.VAYANNA
            KeyEvent.KEYCODE_K -> CharCode.DANTAJA_NAYANNA
            KeyEvent.KEYCODE_L -> CharCode.ALPAPRAANA_KAYANNA
            KeyEvent.KEYCODE_SEMICOLON -> CharCode.ALPAPRAANA_TAYANNA
            KeyEvent.KEYCODE_APOSTROPHE -> CharCode.FULL_STOP
            KeyEvent.KEYCODE_Z -> CharCode.APOSTROPHE
            KeyEvent.KEYCODE_X -> CharCode.SIGN_ANUSVARAYA
            KeyEvent.KEYCODE_C -> CharCode.ALPAPRAANA_JAYANNA
            KeyEvent.KEYCODE_V -> CharCode.ALPAPRAANA_DDAYANNA
            KeyEvent.KEYCODE_B -> CharCode.IYANNA
            KeyEvent.KEYCODE_N -> CharCode.ALPAPRAANA_BAYANNA
            KeyEvent.KEYCODE_M -> CharCode.ALPAPRAANA_PAYANNA
            KeyEvent.KEYCODE_COMMA -> CharCode.DANTAJA_LAYANNA
            KeyEvent.KEYCODE_PERIOD -> CharCode.ALPAPRAANA_GAYANNA
            else -> null
        }

    private fun keyCodeToCharCodeSinglish(keyCode: Int, isShiftPressed: Boolean): Int? =
        if (isShiftPressed) when (keyCode) {
            KeyEvent.KEYCODE_Q -> CharCode.CAPITAL_LETTER_Q
            KeyEvent.KEYCODE_W -> CharCode.CAPITAL_LETTER_W
            KeyEvent.KEYCODE_E -> CharCode.CAPITAL_LETTER_E
            KeyEvent.KEYCODE_R -> CharCode.CAPITAL_LETTER_R
            KeyEvent.KEYCODE_T -> CharCode.CAPITAL_LETTER_T
            KeyEvent.KEYCODE_Y -> CharCode.CAPITAL_LETTER_Y
            KeyEvent.KEYCODE_U -> CharCode.CAPITAL_LETTER_U
            KeyEvent.KEYCODE_I -> CharCode.CAPITAL_LETTER_I
            KeyEvent.KEYCODE_O -> CharCode.CAPITAL_LETTER_O
            KeyEvent.KEYCODE_P -> CharCode.CAPITAL_LETTER_P
            KeyEvent.KEYCODE_A -> CharCode.CAPITAL_LETTER_A
            KeyEvent.KEYCODE_S -> CharCode.CAPITAL_LETTER_S
            KeyEvent.KEYCODE_D -> CharCode.CAPITAL_LETTER_D
            KeyEvent.KEYCODE_F -> CharCode.CAPITAL_LETTER_F
            KeyEvent.KEYCODE_G -> CharCode.CAPITAL_LETTER_G
            KeyEvent.KEYCODE_H -> CharCode.CAPITAL_LETTER_H
            KeyEvent.KEYCODE_J -> CharCode.CAPITAL_LETTER_J
            KeyEvent.KEYCODE_K -> CharCode.CAPITAL_LETTER_K
            KeyEvent.KEYCODE_L -> CharCode.CAPITAL_LETTER_L
            KeyEvent.KEYCODE_Z -> CharCode.CAPITAL_LETTER_Z
            KeyEvent.KEYCODE_X -> CharCode.CAPITAL_LETTER_X
            KeyEvent.KEYCODE_C -> CharCode.CAPITAL_LETTER_C
            KeyEvent.KEYCODE_V -> CharCode.CAPITAL_LETTER_V
            KeyEvent.KEYCODE_B -> CharCode.CAPITAL_LETTER_B
            KeyEvent.KEYCODE_N -> CharCode.CAPITAL_LETTER_N
            KeyEvent.KEYCODE_M -> CharCode.CAPITAL_LETTER_M
            else -> null
        } else when (keyCode) {
            KeyEvent.KEYCODE_Q -> CharCode.SMALL_LETTER_Q
            KeyEvent.KEYCODE_W -> CharCode.SMALL_LETTER_W
            KeyEvent.KEYCODE_E -> CharCode.SMALL_LETTER_E
            KeyEvent.KEYCODE_R -> CharCode.SMALL_LETTER_R
            KeyEvent.KEYCODE_T -> CharCode.SMALL_LETTER_T
            KeyEvent.KEYCODE_Y -> CharCode.SMALL_LETTER_Y
            KeyEvent.KEYCODE_U -> CharCode.SMALL_LETTER_U
            KeyEvent.KEYCODE_I -> CharCode.SMALL_LETTER_I
            KeyEvent.KEYCODE_O -> CharCode.SMALL_LETTER_O
            KeyEvent.KEYCODE_P -> CharCode.SMALL_LETTER_P
            KeyEvent.KEYCODE_A -> CharCode.SMALL_LETTER_A
            KeyEvent.KEYCODE_S -> CharCode.SMALL_LETTER_S
            KeyEvent.KEYCODE_D -> CharCode.SMALL_LETTER_D
            KeyEvent.KEYCODE_F -> CharCode.SMALL_LETTER_F
            KeyEvent.KEYCODE_G -> CharCode.SMALL_LETTER_G
            KeyEvent.KEYCODE_H -> CharCode.SMALL_LETTER_H
            KeyEvent.KEYCODE_J -> CharCode.SMALL_LETTER_J
            KeyEvent.KEYCODE_K -> CharCode.SMALL_LETTER_K
            KeyEvent.KEYCODE_L -> CharCode.SMALL_LETTER_L
            KeyEvent.KEYCODE_Z -> CharCode.SMALL_LETTER_Z
            KeyEvent.KEYCODE_X -> CharCode.SMALL_LETTER_X
            KeyEvent.KEYCODE_C -> CharCode.SMALL_LETTER_C
            KeyEvent.KEYCODE_V -> CharCode.SMALL_LETTER_V
            KeyEvent.KEYCODE_B -> CharCode.SMALL_LETTER_B
            KeyEvent.KEYCODE_N -> CharCode.SMALL_LETTER_N
            KeyEvent.KEYCODE_M -> CharCode.SMALL_LETTER_M
            else -> null
        }

    private fun updateKeyDownEventHandlers(keyboardLayoutId: Int) {
        keyCodeToCharCodeConverter = this::keyCodeToCharCodeWijesekara
        keyDownEventHandler = when (keyboardLayoutId) {
            KeyboardLayoutId.WIJESEKARA -> { keyCode: Int, event: KeyEvent ->
                keyCodeToCharCodeWijesekara(keyCode, event.isShiftPressed)?.let {
                    wijesekaraInput(it)
                    true
                }
            }

            KeyboardLayoutId.SINGLISH -> { keyCode: Int, event: KeyEvent ->
                keyCodeToCharCodeSinglish(keyCode, event.isShiftPressed)?.let {
                    singlishInput(it)
                    true
                }
            }

            else -> { _, _ -> null }
        }
    }

    private fun switchSymbols(newSymbolsState: Boolean) {
        shift = false
        symbols = newSymbolsState
    }

    private fun shiftClick() {
        if (shift && !symbols && Date().time - shiftLastPressed < DOUBLE_PRESS_TIME_RANGE) {
            caps = true
        } else {
            shiftLastPressed = Date().time
            if (caps) {
                caps = false
            }
            shift = !shift
            updateKeySet()
        }
    }

    private fun backspaceClick() {
        if (currentInputConnection.getSelectedText(0).isNullOrBlank()) {
            when (currentInputConnection.getTextBeforeCursor(1, 0)?.getOrNull(0)) {
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
                'ඓ' -> eraseAndCommit("එ")
                'ඕ', 'ඖ' -> eraseAndCommit("ඔ")

                else -> erasePrevious()
            }
        } else commitText("")

        clearLastTypedInfo()
    }

    private fun clearLastTypedInfo() {
        when (currentLayout) {
            KeyboardLayoutId.WIJESEKARA -> lastText = ""
            KeyboardLayoutId.SINGLISH -> {
                lastLetter = null
                lastChar = null
            }
        }
    }

    private fun actionClick() {
        when (currentActionKeyAction) {
            ActionKeyAction.ENTER -> currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))

            ActionKeyAction.NEWLINE -> {
                clearLastTypedInfo()
                commitText("\n")
            }

            ActionKeyAction.ORIGINAL -> currentInputConnection.performEditorAction(currentInputEditorInfo.imeOptions and EditorInfo.IME_MASK_ACTION)
        }
    }

    private fun commitText(text: String) {
        currentInputConnection.commitText(text, 1)
        positionFlag = currentInputConnection.getTextBeforeCursor(POSITION_FLAG_LENGTH, 0) ?: ""
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

        if (currentInputConnection.getTextBeforeCursor(1, 0) == CharText.ZERO_WIDTH_JOINER) {
            val erasedTextNested = erasePrevious(1, returnDeletedText)
            if (returnDeletedText) erasedText = erasedTextNested + erasedText
        }
        return erasedText
    }

    private fun eraseAndCommit(text: String) {
        erasePrevious()
        commitText(text)
    }

    private fun wijesekaraInput(input: Int) {
        var erasePrevious = false
        var lastTextOverride: String? = null

        if (lastText.isBlank() or hasPositionChanged())
            lastText = (currentInputConnection.getTextBeforeCursor(1, 0)?.getOrNull(0) ?: ' ').toString()

        val output: String = when (lastText) {
            CharText.AYANNA -> {
                when (input) {
                    CharCode.AELA_PILLA -> {
                        erasePrevious = true
                        CharText.AAYANNA
                    }

                    CharCode.KETTI_AEDA_PILLA -> {
                        erasePrevious = true
                        CharText.AEYANNA
                    }

                    CharCode.DIGA_AEDA_PILLA -> {
                        erasePrevious = true
                        CharText.AEEYANNA
                    }

                    else -> input.text
                }
            }

            CharText.UYANNA ->
                if (input == CharCode.GAYANUKITTA) {
                    erasePrevious = true
                    CharText.UUYANNA
                } else input.text

            CharText.IRUYANNA ->
                if (input == CharCode.GAETTA_PILLA) {
                    erasePrevious = true
                    CharText.IRUUYANNA
                } else input.text

            CharText.ILUYANNA ->
                if (input == CharCode.GAYANUKITTA) {
                    erasePrevious = true
                    CharText.ILUUYANNA
                } else input.text

            CharText.EYANNA ->
                if (input == CharCode.SIGN_AL_LAKUNA) {
                    erasePrevious = true
                    CharText.EEYANNA
                } else input.text

            CharText.OYANNA -> {
                when (input) {
                    CharCode.SIGN_AL_LAKUNA -> {
                        erasePrevious = true
                        CharText.OOYANNA
                    }

                    CharCode.GAYANUKITTA -> {
                        erasePrevious = true
                        CharText.AUYANNA
                    }

                    else -> input.text
                }
            }

            CharText.KOMBUVA -> {
                if (kombuvaNew) {
                    erasePrevious = true
                    when (input.type) {
                        CharType.SWARA,
                        CharType.WYANJANA -> {
                            lastTextOverride = CharText.KOMBUVA
                            if (input == CharCode.EYANNA)
                                CharText.AIYANNA
                            else
                                input.text + CharText.KOMBUVA
                        }

                        else -> {
                            when (input) {
                                CharCode.KOMBUVA -> CharText.KOMBU_DEKA
                                CharCode.SIGN_AL_LAKUNA -> CharText.DIGA_KOMBUVA
                                CharCode.AELA_PILLA -> CharText.KOMBUVA_HAA_AELA_PILLA
                                CharCode.GAYANUKITTA -> CharText.KOMBUVA_HAA_GAYANUKITTA
                                else -> input.text
                            }
                        }
                    }
                } else if (input == CharCode.SIGN_RAKARANSHAYA) {
                    erasePrevious = true
                    lastTextOverride = CharText.KOMBUVA
                    CharText.SIGN_RAKARANSHAYA + CharText.KOMBUVA
                } else input.text
            }

            CharText.KOMBUVA_HAA_AELA_PILLA -> {
                if (input == CharCode.SIGN_AL_LAKUNA) {
                    erasePrevious = true
                    CharText.KOMBUVA_HAA_DIGA_AELA_PILLA
                } else input.text
            }

            CharText.KOMBU_DEKA -> {
                when (input.type) {
                    CharType.SWARA, CharType.WYANJANA -> {
                        erasePrevious = true
                        input.text + CharText.KOMBU_DEKA
                    }

                    else -> input.text
                }
            }

            else -> {
                if (input == CharCode.SIGN_REEPAYA) {
                    erasePrevious = true
                    CharText.SIGN_REEPAYA + lastText
                } else input.text
            }
        }

        kombuvaNew = output == CharText.KOMBUVA

        if (erasePrevious)
            eraseAndCommit(output)
        else
            commitText(output)

        lastText = lastTextOverride ?: output
    }

    private fun hasPositionChanged(): Boolean = currentInputConnection.getTextBeforeCursor(POSITION_FLAG_LENGTH, 0) != positionFlag

    private fun singlishInput(input: Int) {
        var output = ""
        var erasePreviousChars = 0

        var tLastChar: Int? = null

        var tLastLetter: Int? = null

        val mLastChar: Int?

        val mLastLetter: Int?

        if (!hasPositionChanged()) {
            mLastChar = lastChar
            mLastLetter = lastLetter
        } else {
            mLastChar = null
            mLastLetter = null
        }

        lastChar = null
        lastLetter = null

        var singlishChar: Int = input.toSinglish()

        fun newLetter() {
            output = singlishChar.text
            if (singlishChar.type == CharType.WYANJANA) {
                output += CharText.SIGN_AL_LAKUNA
                tLastChar = CharCode.SIGN_AL_LAKUNA
            }
        }

        if (mLastChar == null) {
            when (input) {
                CharCode.SMALL_LETTER_Z,
                CharCode.CAPITAL_LETTER_Z ->
                    tLastChar = CharCode.MARK_SANYAKA

                else ->
                    newLetter()
            }
        } else {
            when {
                input == CharCode.SMALL_LETTER_Z || input == CharCode.CAPITAL_LETTER_Z -> tLastChar = CharCode.MARK_SANYAKA

                mLastChar.type == CharType.WYANJANA ->
                    when (singlishChar) {
                        CharCode.AYANNA -> output = CharText.AELA_PILLA
                        CharCode.IYANNA -> output = CharText.KOMBU_DEKA
                        CharCode.UYANNA -> output = CharText.KOMBUVA_HAA_GAYANUKITTA
                        else -> newLetter()
                    }

                mLastChar == CharCode.SIGN_AL_LAKUNA -> {
                    when (singlishChar) {
                        CharCode.AYANNA -> {
                            erasePreviousChars = 1
                            tLastChar = mLastLetter
                        }

                        CharCode.RAYANNA -> {
                            output = CharText.ZERO_WIDTH_JOINER + CharText.RAYANNA + CharText.SIGN_AL_LAKUNA
                            tLastChar = CharCode.SIGN_AL_LAKUNA
                        }

                        CharCode.YAYANNA -> {
                            output = CharText.ZERO_WIDTH_JOINER + CharText.YAYANNA + CharText.SIGN_AL_LAKUNA
                            tLastChar = CharCode.SIGN_AL_LAKUNA
                        }

                        CharCode.HAYANNA -> {
                            if (mLastLetter != null) {
                                when (mLastLetter) {
                                    CharCode.ALPAPRAANA_TTAYANNA -> {
                                        output = CharText.ALPAPRAANA_TAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.ALPAPRAANA_TAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.MAHAAPRAANA_TTAYANNA -> {
                                        output = CharText.MAHAAPRAANA_TAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_TAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_DDAYANNA -> {
                                        output = CharText.ALPAPRAANA_DAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.ALPAPRAANA_DAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.MAHAAPRAANA_DDAYANNA -> {
                                        output = CharText.MAHAAPRAANA_DAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_DAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_KAYANNA -> {
                                        output = CharText.MAHAAPRAANA_KAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_KAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_GAYANNA -> {
                                        output = CharText.MAHAAPRAANA_GAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_GAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_CAYANNA -> {
                                        output = CharText.MAHAAPRAANA_CAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_CAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_JAYANNA -> {
                                        output = CharText.MAHAAPRAANA_JAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_JAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_TAYANNA -> {
                                        output = CharText.MAHAAPRAANA_TAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_TAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_DAYANNA -> {
                                        output = CharText.MAHAAPRAANA_DAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_DAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_PAYANNA -> {
                                        output = CharText.MAHAAPRAANA_PAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_PAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.ALPAPRAANA_BAYANNA -> {
                                        output = CharText.MAHAAPRAANA_BAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MAHAAPRAANA_BAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.DANTAJA_SAYANNA -> {
                                        output = CharText.TAALUJA_SAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.TAALUJA_SAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.SANYAKA_DDAYANNA -> {
                                        output = CharText.SANYAKA_DAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.SANYAKA_DAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    CharCode.MUURDHAJA_SAYANNA -> {
                                        output = CharText.MUURDHAJA_SAYANNA + CharText.SIGN_AL_LAKUNA
                                        erasePreviousChars = 2
                                        tLastLetter = CharCode.MUURDHAJA_SAYANNA
                                        tLastChar = CharCode.SIGN_AL_LAKUNA
                                    }

                                    else -> newLetter()
                                }
                            } else newLetter()
                        }

                        else -> {
                            when (singlishChar.type) {
                                CharType.SWARA -> {
                                    if (mLastLetter != null) {
                                        getSwaraSigns(singlishChar).let { sign ->
                                            if (sign != null) {
                                                output += sign.text
                                                tLastChar = sign
                                                erasePreviousChars = 1
                                            } else output = singlishChar.text
                                        }
                                    } else output = singlishChar.text
                                }

                                else -> newLetter()
                            }
                        }
                    }
                }

                mLastChar.type == CharType.PILI -> {
                    when (mLastChar) {
                        CharCode.KETTI_AEDA_PILLA if singlishChar == CharCode.AYANNA -> {
                            output = CharText.DIGA_AEDA_PILLA
                            erasePreviousChars = 1
                            tLastChar = CharCode.DIGA_AEDA_PILLA
                        }

                        CharCode.KETTI_IS_PILLA if singlishChar == CharCode.IYANNA -> {
                            output = CharText.DIGA_IS_PILLA
                            erasePreviousChars = 1
                            tLastChar = CharCode.DIGA_IS_PILLA
                        }

                        CharCode.KETTI_PAA_PILLA if singlishChar == CharCode.UYANNA -> {
                            output = CharText.DIGA_PAA_PILLA
                            erasePreviousChars = 1
                            tLastChar = CharCode.DIGA_PAA_PILLA
                        }

                        CharCode.GAETTA_PILLA if singlishChar == CharCode.IYANNA -> {
                            output = CharText.DIGA_GAETTA_PILLA
                            erasePreviousChars = 1
                            tLastChar = CharCode.DIGA_GAETTA_PILLA
                        }

                        CharCode.KOMBUVA if singlishChar == CharCode.EYANNA -> {
                            output = CharText.DIGA_KOMBUVA
                            erasePreviousChars = 1
                            tLastChar = CharCode.DIGA_KOMBUVA
                        }

                        CharCode.KOMBUVA_HAA_AELA_PILLA if singlishChar == CharCode.OYANNA -> {
                            output = CharText.KOMBUVA_HAA_DIGA_AELA_PILLA
                            erasePreviousChars = 1
                            tLastChar = CharCode.KOMBUVA_HAA_DIGA_AELA_PILLA
                        }

                        else -> newLetter()
                    }
                }

                mLastChar == CharCode.MARK_SANYAKA -> {
                    when (singlishChar) {
                        CharCode.ALPAPRAANA_KAYANNA -> singlishChar = CharCode.TAALUJA_NAASIKYAYA
                        CharCode.ALPAPRAANA_GAYANNA -> singlishChar = CharCode.SANYAKA_GAYANNA
                        CharCode.ALPAPRAANA_JAYANNA -> singlishChar = CharCode.SANYAKA_JAYANNA
                        CharCode.ALPAPRAANA_DDAYANNA -> singlishChar = CharCode.SANYAKA_DDAYANNA
                        CharCode.ALPAPRAANA_BAYANNA -> singlishChar = CharCode.AMBA_BAYANNA
                        CharCode.HAYANNA -> singlishChar = CharCode.TAALUJA_SANYOOGA_NAAKSIKYAYA
                        else -> {}
                    }
                    newLetter()
                }

                else -> {
                    if (mLastLetter != null) {
                        when (mLastLetter) {
                            CharCode.AYANNA -> {
                                when (singlishChar) {
                                    CharCode.AYANNA -> {
                                        output = CharText.AAYANNA
                                        erasePreviousChars = 1
                                        tLastLetter = CharCode.AAYANNA
                                    }

                                    CharCode.IYANNA -> {
                                        output = CharText.AIYANNA
                                        erasePreviousChars = 1
                                        tLastLetter = CharCode.AIYANNA
                                    }

                                    CharCode.UYANNA -> {
                                        output = CharText.AUYANNA
                                        erasePreviousChars = 1
                                        tLastLetter = CharCode.AUYANNA
                                    }

                                    else -> newLetter()
                                }
                            }

                            CharCode.AEYANNA -> {
                                if (singlishChar == CharCode.AYANNA) {
                                    output = CharText.AEEYANNA
                                    erasePreviousChars = 1
                                    tLastLetter = CharCode.AEEYANNA
                                } else newLetter()
                            }

                            CharCode.IYANNA -> {
                                if (singlishChar == CharCode.IYANNA) {
                                    output = CharText.IIYANNA
                                    erasePreviousChars = 1
                                    tLastLetter = CharCode.IIYANNA
                                } else newLetter()
                            }

                            CharCode.UYANNA -> {
                                if (singlishChar == CharCode.UYANNA) {
                                    output = CharText.UUYANNA
                                    erasePreviousChars = 1
                                    tLastLetter = CharCode.UUYANNA
                                } else newLetter()
                            }

                            CharCode.IRUYANNA -> {
                                if (singlishChar == CharCode.IYANNA) {
                                    output = CharText.IRUUYANNA
                                    erasePreviousChars = 1
                                    tLastLetter = CharCode.IRUUYANNA
                                } else newLetter()
                            }

                            CharCode.EYANNA -> {
                                if (singlishChar == CharCode.EYANNA) {
                                    output = CharText.EEYANNA
                                    erasePreviousChars = 1
                                    tLastLetter = CharCode.EEYANNA
                                } else newLetter()
                            }

                            CharCode.OYANNA -> {
                                if (singlishChar == CharCode.OYANNA) {
                                    output = CharText.OOYANNA
                                    erasePreviousChars = 1
                                    tLastLetter = CharCode.OOYANNA
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

    override fun onClickEvent(value: Int) {
        when {
            value < 0 -> when (value) {
                Action.ACTION -> actionClick()
                Action.BACKSPACE -> backspaceClick()
                Action.EMOJI -> {}
                Action.IME -> (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
                Action.LANG -> {
                    switchToNextAvailableLayout()
                    updateKeySet()
                }

                Action.SHIFT -> shiftClick()
                Action.SYMBOLS -> {
                    switchSymbols(!symbols)
                    updateKeySet()
                }
            }

            symbols -> {
                val x = value.text
                commitText(x)
            }

            else -> when (currentLayout) {
                KeyboardLayoutId.ENGLISH -> commitText(value.text)
                KeyboardLayoutId.WIJESEKARA -> wijesekaraInput(value)
                KeyboardLayoutId.SINGLISH -> singlishInput(value)
            }
        }
    }

    override fun swipeErase() {
        val erasedText = erasePrevious(1, true)
        swipeErasedContent.add(0, erasedText)
    }

    override fun swipeEraseUndo() {
        if (!swipeErasedContent.isEmpty()) {
            val erasedText = swipeErasedContent[0]
            commitText(erasedText)
            swipeErasedContent.removeAt(0)
        }
    }

    override fun swipeMoveLeft() {
        if (!currentInputConnection.getTextBeforeCursor(1, 0).isNullOrEmpty())
            currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
    }

    override fun swipeMoveRight() {
        if (!currentInputConnection.getTextAfterCursor(1, 0).isNullOrEmpty())
            currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
    }

    companion object {
        private const val POSITION_FLAG_LENGTH: Int = 5
        private const val DOUBLE_PRESS_TIME_RANGE: Long = 500
    }

    @Retention(AnnotationRetention.SOURCE)
    private annotation class ActionKeyAction {
        companion object {
            const val ENTER: Int = 1
            const val NEWLINE: Int = 2
            const val ORIGINAL: Int = 3
        }
    }
}
