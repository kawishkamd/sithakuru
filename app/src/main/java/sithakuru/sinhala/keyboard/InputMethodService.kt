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

import android.content.ClipDescription
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.InputMethodService.Insets
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sithakuru.sinhala.keyboard.clipboard.ClipboardHistoryManager
import sithakuru.sinhala.keyboard.clipboard.ClipboardPolicy
import sithakuru.sinhala.keyboard.clipboard.ClipboardSettings
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
    private var currentActionKeyIconRes: Int = R.drawable.ic_keyboard_return
    private var currentActionKeyAction: Int = ActionKeyAction.NEWLINE

    private lateinit var keyboardLayoutEnglish: English
    private lateinit var keyboardLayoutSinglish: Singlish
    private lateinit var keyboardLayoutWijesekara: Wijesekara
    private lateinit var keyboardLayoutSymbols: Symbols

    private var currentLayout: Int = -1
    private lateinit var currentKeyboardLayout: BaseLayout

    private var symbols: Boolean = false

    private var hardwareInputViewBinding: HardwareInputViewBinding? = null
    private lateinit var inputViewContainer: FrameLayout
    private lateinit var keyboardContainer: LinearLayout
    private var suggestionStrip: SuggestionStripView? = null
    private lateinit var keyboardView: Keyboard
    private var emojiView: EmojiView? = null
    private var clipboardView: ClipboardView? = null
    private lateinit var clipboardHistoryManager: ClipboardHistoryManager
    private var clipboardManager: ClipboardManager? = null
    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        capturePrimaryClip()
    }
    private var insetBottom: Int = 0

    /**
     * The clipboard settings in force. Read from the settings file at startup,
     * then replaced by the settings screen's broadcasts: this process's copy of
     * the file is not refreshed while it runs.
     */
    private lateinit var clipboardSettings: ClipboardSettings
    private val clipboardSettingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            clipboardSettings = ClipboardSettingsBroadcast.read(intent) ?: return
            applyClipboardPolicy()
        }
    }
    private var builtLayoutOptions: Pair<Boolean, Boolean>? = null

    private var keyDownEventHandler: (keyCode: Int, event: KeyEvent) -> Boolean? = { _, _ -> false }
    private var keyCodeToCharCodeConverter: (keyCode: Int, isShiftPressed: Boolean) -> Int? = { _, _ -> null }

    private var shift = false
    private var shiftLastPressed = 0L
    private var caps = false

    private var positionFlag: CharSequence = ""
    private var appliedWord: String? = null

    private val wijesekara = WijesekaraComposer()
    private val singlish = SinglishComposer()

    // True while the Singlish path commits, so commitText does not end the word.
    private var singlishTyping = false

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        currentActionKeyIconRes = R.drawable.ic_keyboard_return
        clipboardHistoryManager = ClipboardHistoryManager(
            saveCallback = { serialized ->
                prefs.clipboardHistory = serialized
            }
        )
        clipboardHistoryManager.loadFromString(prefs.clipboardHistory)
        clipboardSettings = prefs.clipboardSettings
        applyClipboardPolicy()
        ContextCompat.registerReceiver(
            this,
            clipboardSettingsReceiver,
            IntentFilter(ClipboardSettingsBroadcast.ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        try {
            clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
        } catch (e: Exception) {
            // Some OEM builds restrict clipboard access; history just stays empty.
            Log.w(TAG, "Could not watch the clipboard", e)
        }
        initKeyboardLayouts()
        currentKeyboardLayout = keyboardLayoutWijesekara
        EnglishDictionary.load(this)
        updateWindowBackground()
    }

    override fun onDestroy() {
        unregisterReceiver(clipboardSettingsReceiver)
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipChangedListener)
        } catch (e: Exception) {
            Log.w(TAG, "Could not stop watching the clipboard", e)
        }
        super.onDestroy()
    }

    /**
     * Applies [clipboardSettings] to the saved history: at startup, and again
     * whenever the settings screen broadcasts a change, so turning history off
     * or clearing it takes effect even if this process is not restarted.
     */
    private fun applyClipboardPolicy() {
        val handled = ClipboardPolicy.apply(
            clipboardHistoryManager,
            clipboardSettings,
            appliedClearRequests = prefs.appliedClipboardClearRequests,
            now = System.currentTimeMillis()
        )
        if (handled != prefs.appliedClipboardClearRequests) prefs.appliedClipboardClearRequests = handled
        if (clipboardView?.visibility == View.VISIBLE) clipboardView?.updateClips()
    }

    private fun expireClipboardHistory() {
        clipboardHistoryManager.expireUnpinned(System.currentTimeMillis(), clipboardSettings.expiryMillis)
    }

    /**
     * Fields where nothing copied is remembered: the editor asked for
     * incognito typing, or it is a password field. A password copied from a
     * manager that does not flag it as sensitive is often on the clipboard
     * exactly when a password field opens the keyboard.
     */
    private fun isPrivateEditor(info: EditorInfo? = currentInputEditorInfo): Boolean {
        info ?: return false
        if ((info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0) return true
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        return when (info.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_TEXT ->
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    private fun capturePrimaryClip() {
        if (!clipboardSettings.enabled || isPrivateEditor()) return
        val text = readPrimaryClipText()
        if (text.isNullOrBlank()) return
        expireClipboardHistory()
        clipboardHistoryManager.addItem(text)
        if (clipboardView?.visibility == View.VISIBLE) {
            clipboardView?.updateClips()
        }
    }

    /**
     * The current clip as text, or null if it is empty, not text, flagged
     * sensitive, or cannot be read. Only the read is guarded: failures saving
     * or showing the clip are not clipboard errors and are not swallowed.
     */
    private fun readPrimaryClipText(): String? = try {
        val clip = clipboardManager?.primaryClip
        if (clip == null || clip.itemCount == 0) {
            null
        } else {
            val description = clip.description
            val extras = description?.extras
            @Suppress("DEPRECATION")
            val isSensitive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true
            } else {
                // EXTRA_IS_SENSITIVE only became public in API 33; the value is stable.
                extras?.getBoolean("android.content.extra.IS_SENSITIVE", false) == true
            }
            // Skip non-text clips (URI, Intent, image, ...). coerceToText() would
            // otherwise turn a content:// URI into plain text and persist it.
            val isTextClip = description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true ||
                description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true
            if (isSensitive || !isTextClip) null else clip.getItemAt(0).coerceToText(this)?.toString()
        }
    } catch (e: Exception) {
        // Only the exception type: a message could quote clip content.
        Log.w(TAG, "Could not read the clipboard: ${e.javaClass.name}")
        null
    }

    private fun initKeyboardLayouts() {
        val showEmoji = prefs.emojiKey
        val showClipboard = prefs.clipboardKey
        // The layouts depend only on these two settings, so rebuild them only
        // when one changed, not on every new text field.
        val options = showEmoji to showClipboard
        if (options == builtLayoutOptions) return
        builtLayoutOptions = options
        keyboardLayoutEnglish = English(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
        keyboardLayoutSinglish = Singlish(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
        keyboardLayoutWijesekara = Wijesekara(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
        keyboardLayoutSymbols = Symbols(showEmojiKey = showEmoji, showClipboardKey = showClipboard)
        if (currentLayout == KeyboardLayoutId.WIJESEKARA) {
            keyboardLayoutSymbols.setCustomRowBottom(keyboardLayoutWijesekara.wijesekaraSymbolsRowBottom)
        } else {
            keyboardLayoutSymbols.setCustomRowBottom(null)
        }
        if (::currentKeyboardLayout.isInitialized) {
            currentKeyboardLayout = when (currentLayout) {
                KeyboardLayoutId.WIJESEKARA -> keyboardLayoutWijesekara
                KeyboardLayoutId.SINGLISH -> keyboardLayoutSinglish
                else -> keyboardLayoutEnglish
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        darkTheme = prefs.darkTheme

        shift = false
        caps = false
        symbols = false
        resetInputMarkers()
        initKeyboardLayouts()
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
        } ?: run {
            currentActionKeyAction = ActionKeyAction.NEWLINE
            currentActionKeyIconRes = R.drawable.ic_keyboard_return
        }
        if (isLayoutEnabled(prefs.currentLayout)) {
            setCurrentLayout(prefs.currentLayout)
        } else {
            switchToNextAvailableLayout()
        }
        updateNavigationBarTheme()
        updateWindowBackground()
        suggestionStrip?.updateTheme()
        // The editor type (password, URL, number...) may have changed even when
        // the layout did not, so re-evaluate the strip's slot here as well.
        // Pass the new attribute explicitly: currentInputEditorInfo only updates
        // once super.onStartInput() runs below.
        updateSuggestionStripVisibility(attribute)

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
            emojiView = null
            clipboardView = null
            inputViewContainer = FrameLayout(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
            }
            keyboardContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            suggestionStrip = SuggestionStripView(this, prefs).apply {
                onSuggestionClick = { word ->
                    applySuggestion(word)
                }
            }
            keyboardContainer.addView(suggestionStrip)

            keyboardView = Keyboard(
                context = this,
                prefs = prefs,
                eventListener = this,
                insetBottom = insetBottom
            )
            keyboardContainer.addView(keyboardView)

            updateKeySet()
            updateSuggestionStripVisibility()
            inputViewContainer.addView(keyboardContainer)
            updateSuggestions()

            ViewCompat.setOnApplyWindowInsetsListener(inputViewContainer) { _, windowInsets ->
                // The views take the inset at construction, so a new value means
                // a rebuild. Only rebuild when it changed: insets are dispatched
                // again on every rotation, bar toggle and to the rebuilt view
                // itself, and rebuilding each time threw away the whole view tree.
                val bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                if (bottom != insetBottom) {
                    insetBottom = bottom
                    setInputView(onCreateInputView())
                }
                WindowInsetsCompat.CONSUMED
            }

            return inputViewContainer
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        // Resolve the strip's slot before the window is shown. Toggling it after
        // super.onStartInputView() resizes the IME mid-animation and flashes the
        // window background through the gap.
        hideEmojiView()
        hideClipboardView()
        capturePrimaryClip()
        switchSymbols(false)
        updateKeySet()
        updateSuggestionStripVisibility(info)
        // Blank the previous field's suggestions before the window paints; the
        // new ones can only be computed after super, once the input connection
        // exists.
        suggestionStrip?.clearSuggestions()

        super.onStartInputView(info, restarting)

        updateSuggestions()
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
            updateSuggestionStripVisibility()
            updateSuggestions()
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
        wijesekara.reset()
        appliedWord = null
        singlishReset()
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

    /**
     * Keeps the IME window and its root container transparent. Every visible
     * part (keys, an active suggestion strip, the emoji and clipboard panels)
     * paints its own background, so an inactive strip slot shows the app
     * behind it instead of a blank bar (see [onComputeInsets]). Painting the
     * window the theme's color instead would show through as a band there.
     */
    private fun updateWindowBackground() {
        window?.window?.let { w ->
            w.setFormat(PixelFormat.TRANSLUCENT)
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        if (::inputViewContainer.isInitialized) {
            inputViewContainer.setBackgroundColor(Color.TRANSPARENT)
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (emojiView?.visibility == View.VISIBLE) {
                hideEmojiView()
                return true
            }
            if (clipboardView?.visibility == View.VISIBLE) {
                hideClipboardView()
                return true
            }
        }

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

    private fun showEmojiView() {
        if (isHardwareKeyboardAvailable()) return
        if (!::inputViewContainer.isInitialized || !::keyboardView.isInitialized) return

        if (emojiView == null) {
            emojiView = EmojiView(
                context = this,
                prefs = prefs,
                eventListener = object : EmojiView.EventListener {
                    override fun onEmojiClick(emoji: String) {
                        commitText(emoji)
                        prefs.addRecentEmoji(emoji)
                        emojiView?.updateRecents()
                    }

                    override fun onBackToKeyboard() {
                        hideEmojiView()
                    }

                    override fun onBackspaceClick() {
                        backspaceClick()
                    }

                    override fun onSpaceClick() {
                        commitText(" ")
                    }

                    override fun onClipboardClick() {
                        hideEmojiView()
                        showClipboardView()
                    }
                },
                insetBottom = insetBottom
            )
            inputViewContainer.addView(emojiView, bottomPanelLayoutParams())
        } else {
            emojiView?.updateRecents()
        }
        // Keep the panel exactly as tall as the keyboard it replaces.
        emojiView?.let { view ->
            view.reserveStripHeight = panelCoversStrip()
            view.requestLayout()
        }
        clipboardView?.visibility = View.GONE
        hideKeyboardBehindPanel()
        emojiView?.visibility = View.VISIBLE
    }

    /**
     * A panel covers the strip slot only while the strip is active. Otherwise
     * it is as tall as the keys and the slot above it stays see-through, so
     * opening or closing a panel never changes the window's height or the
     * area the app sees as covered.
     */
    private fun panelCoversStrip(): Boolean =
        suggestionStrip?.let { it.visibility == View.VISIBLE && it.isActive } == true

    private fun bottomPanelLayoutParams() = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT,
        Gravity.BOTTOM
    )

    /**
     * INVISIBLE rather than GONE: the keyboard keeps its size under the panel,
     * so the container (and the IME window) keeps its height.
     */
    private fun hideKeyboardBehindPanel() {
        if (::keyboardContainer.isInitialized) {
            keyboardContainer.visibility = View.INVISIBLE
        } else {
            keyboardView.visibility = View.INVISIBLE
        }
    }

    private fun hideEmojiView() {
        emojiView?.visibility = View.GONE
        if (::keyboardContainer.isInitialized) {
            keyboardContainer.visibility = View.VISIBLE
        } else if (::keyboardView.isInitialized) {
            keyboardView.visibility = View.VISIBLE
        }
        updateSuggestionStripVisibility()
        updateSuggestions()
    }

    private fun showClipboardView() {
        if (isHardwareKeyboardAvailable()) return
        if (!::inputViewContainer.isInitialized || !::keyboardView.isInitialized) return

        expireClipboardHistory()
        capturePrimaryClip()

        if (clipboardView == null) {
            clipboardView = ClipboardView(
                context = this,
                prefs = prefs,
                historyManager = clipboardHistoryManager,
                eventListener = object : ClipboardView.EventListener {
                    override fun onClipClick(text: String) {
                        commitText(text)
                    }

                    override fun onBackToKeyboard() {
                        hideClipboardView()
                    }

                    override fun onSwitchToEmoji() {
                        hideClipboardView()
                        showEmojiView()
                    }

                    override fun onBackspaceClick() {
                        backspaceClick()
                    }

                    override fun onSpaceClick() {
                        commitText(" ")
                    }
                },
                insetBottom = insetBottom
            )
            inputViewContainer.addView(clipboardView, bottomPanelLayoutParams())
        } else {
            clipboardView?.updateClips()
        }

        // The strip is a sibling of the panel in tall canvases, so resolve its slot
        // here. Hiding keeps it untouched: returning to the keyboard must not
        // re-reveal a strip that the editor (e.g. a password field) suppressed.
        updateSuggestionStripVisibility()
        clipboardView?.let { view ->
            view.reserveStripHeight = panelCoversStrip()
            view.requestLayout()
        }

        emojiView?.visibility = View.GONE
        hideKeyboardBehindPanel()
        clipboardView?.visibility = View.VISIBLE
    }

    private fun hideClipboardView() {
        clipboardView?.visibility = View.GONE
        if (::keyboardContainer.isInitialized) {
            keyboardContainer.visibility = View.VISIBLE
        } else if (::keyboardView.isInitialized) {
            keyboardView.visibility = View.VISIBLE
        }
        updateSuggestions()
    }

    private fun switchSymbols(newSymbolsState: Boolean) {
        shift = false
        symbols = newSymbolsState
        updateSuggestionStripVisibility()
        updateSuggestions()
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
        // Inside a Singlish word the raw keys are the source of truth: drop
        // one and re-render instead of deleting a rendered character.
        if (currentLayout == KeyboardLayoutId.SINGLISH) {
            if (hasPositionChanged()) singlishReset()
            if (currentInputConnection.getSelectedText(0).isNullOrBlank()) {
                singlish.backspace()?.let {
                    applySinglishEdit(it)
                    return
                }
            }
        }
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
        updateSuggestions()
    }

    private fun clearLastTypedInfo() {
        appliedWord = null
        when (currentLayout) {
            KeyboardLayoutId.WIJESEKARA -> wijesekara.reset()
            KeyboardLayoutId.SINGLISH -> singlishReset()
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
        // Anything committed from outside the Singlish path (symbols, emoji,
        // a swipe, the emoji space bar) ends the word: the buffered keys no
        // longer describe what is on screen, so the next key must not erase it.
        if (!singlishTyping) singlishReset()
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
        val edit = wijesekara.type(
            input,
            charBeforeCursor = currentInputConnection.getTextBeforeCursor(1, 0)?.getOrNull(0),
            positionChanged = hasPositionChanged()
        )
        if (edit.erasePrevious)
            eraseAndCommit(edit.text)
        else
            commitText(edit.text)
    }

    private fun hasPositionChanged(): Boolean = currentInputConnection.getTextBeforeCursor(POSITION_FLAG_LENGTH, 0) != positionFlag

    /**
     * Helakuru-compatible phonetic input: keep the raw keys of the word being typed,
     * render the whole word again after every key, and replace what was
     * committed for it. A key that is not a letter ends the word.
     */
    private fun singlishInput(input: Int) {
        // A moved caret or a live selection means the buffered keys no longer
        // describe the text in front of the cursor, so the word starts over.
        if (hasPositionChanged() || !currentInputConnection.getSelectedText(0).isNullOrBlank()) {
            singlishReset()
        }

        val ch = input.toChar()
        if (!SinglishComposer.isLetter(ch)) {
            // A separator ends the word and is written like any other text.
            singlishReset()
            commitText(ch.toString())
            return
        }
        applySinglishEdit(singlish.type(ch))
    }

    private fun applySinglishEdit(edit: SinglishComposer.Edit) {
        if (edit.deleteBefore > 0) {
            currentInputConnection.deleteSurroundingText(edit.deleteBefore, 0)
        }
        singlishCommit(edit.insert)
    }

    private fun singlishReset() {
        singlish.reset()
    }

    /** commitText for the Singlish path, which owns the buffer it replaces. */
    private fun singlishCommit(text: String) {
        singlishTyping = true
        try {
            commitText(text)
        } finally {
            singlishTyping = false
        }
    }

    override fun onClickEvent(value: Int) {
        when {
            value < 0 -> when (value) {
                Action.ACTION -> actionClick()
                Action.BACKSPACE -> backspaceClick()
                Action.EMOJI -> showEmojiView()
                Action.CLIPBOARD -> showClipboardView()
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
                updateSuggestions()
            }

            else -> when (currentLayout) {
                KeyboardLayoutId.ENGLISH -> {
                    commitText(value.text)
                    updateSuggestions()
                }
                KeyboardLayoutId.WIJESEKARA -> wijesekaraInput(value)
                KeyboardLayoutId.SINGLISH -> singlishInput(value)
            }
        }
    }

    override fun swipeErase() {
        val erasedText = erasePrevious(1, true)
        swipeErasedContent.add(0, erasedText)
        updateSuggestions()
    }

    override fun swipeEraseUndo() {
        if (!swipeErasedContent.isEmpty()) {
            val erasedText = swipeErasedContent[0]
            commitText(erasedText)
            swipeErasedContent.removeAt(0)
            updateSuggestions()
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

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateSuggestions()
    }

    private fun shouldShowSuggestions(): Boolean {
        // The symbols layer has no words to suggest, but it shares the layout's
        // slot, so only the transient state changes there.
        if (symbols) return false
        return stripHasSlot()
    }

    /**
     * Whether the current layout and field can produce suggestions at all.
     * Unlike [shouldShowSuggestions] this ignores whether a suggestion happens
     * to be available right now, so it only changes when the strip's slot must
     * appear or disappear.
     */
    private fun stripHasSlot(info: EditorInfo? = currentInputEditorInfo): Boolean {
        if (currentLayout != KeyboardLayoutId.ENGLISH || !prefs.englishSuggestions) return false
        info ?: return false
        val textClass = info.inputType and InputType.TYPE_MASK_CLASS
        if (textClass != InputType.TYPE_CLASS_TEXT) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val isPassword = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        if (isPassword) return false
        val isNoSuggestions = (info.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0
        val isExcludedVariation = variation == InputType.TYPE_TEXT_VARIATION_URI ||
                variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == 0x000000d0 || // TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                variation == InputType.TYPE_TEXT_VARIATION_FILTER
        return !isNoSuggestions && !isExcludedVariation
    }

    /**
     * Whether the strip's slot exists at all. This depends only on settings,
     * which cannot change while this process runs (the settings screen
     * restarts it), so the keyboard window keeps one height across layout
     * switches and fields. Changing the window's height while it is shown
     * made the app behind it relayout and flash through the gap.
     */
    private fun stripReserved(): Boolean = prefs.englishSuggestions && prefs.layoutEnglish

    /**
     * The slot is reserved by settings ([stripReserved]); within it the strip
     * is only active where the layout and field can suggest words. Sinhala
     * layouts and excluded fields get an inactive, see-through strip, and
     * [onComputeInsets] hands that area back to the app.
     */
    private fun updateSuggestionStripVisibility(info: EditorInfo? = currentInputEditorInfo) {
        val strip = suggestionStrip ?: return
        val reserved = stripReserved()
        strip.visibility = if (reserved) View.VISIBLE else View.GONE
        strip.setActive(reserved && stripHasSlot(info))
    }

    /**
     * With an inactive strip, report only the keys as the keyboard's area: the
     * app lays out above the keys, sees its own content through the empty slot
     * and gets the touches there, while the IME window itself never resizes.
     */
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        if (isFullscreenMode || isHardwareKeyboardAvailable()) return
        val strip = suggestionStrip ?: return
        if (strip.visibility != View.VISIBLE || strip.isActive) return
        val decor = window?.window?.decorView ?: return
        // The opaque part below the slot: an open panel, otherwise the keys.
        val body: View = emojiView?.takeIf { it.visibility == View.VISIBLE }
            ?: clipboardView?.takeIf { it.visibility == View.VISIBLE }
            ?: keyboardView
        if (!body.isLaidOut) return

        val location = IntArray(2)
        body.getLocationInWindow(location)
        val keysTop = location[1]
        outInsets.contentTopInsets = keysTop
        outInsets.visibleTopInsets = keysTop
        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
        outInsets.touchableRegion.set(0, keysTop, decor.width, decor.height)
    }

    private fun updateSuggestions() {
        if (!shouldShowSuggestions()) {
            suggestionStrip?.clearSuggestions()
            appliedWord = null
            return
        }

        val ic = currentInputConnection
        if (ic == null) {
            suggestionStrip?.clearSuggestions()
            appliedWord = null
            return
        }

        // Selection guard: Never show suggestions when text is selected
        if (!ic.getSelectedText(0).isNullOrEmpty()) {
            suggestionStrip?.clearSuggestions()
            appliedWord = null
            return
        }

        val textBefore = ic.getTextBeforeCursor(40, 0)?.toString() ?: ""
        val word = textBefore.takeLastWhile { it.isLetter() || it == '\'' }

        if (word.isBlank()) {
            suggestionStrip?.clearSuggestions()
            appliedWord = null
            return
        }

        if (appliedWord != null) {
            if (word == appliedWord) {
                suggestionStrip?.clearSuggestions()
                return
            } else {
                appliedWord = null
            }
        }

        val suggestions = EnglishDictionary.getSuggestions(word)
        suggestionStrip?.setSuggestions(suggestions)
    }

    private fun applySuggestion(word: String) {
        val ic = currentInputConnection ?: return
        // Selection guard
        if (!ic.getSelectedText(0).isNullOrEmpty()) return

        val textBefore = ic.getTextBeforeCursor(40, 0)?.toString() ?: ""
        val textAfter = ic.getTextAfterCursor(40, 0)?.toString() ?: ""
        val prefix = textBefore.takeLastWhile { it.isLetter() || it == '\'' }
        val suffix = textAfter.takeWhile { it.isLetter() || it == '\'' }

        if (prefix.isNotEmpty() || suffix.isNotEmpty()) {
            ic.deleteSurroundingText(prefix.length, suffix.length)
        }

        // Smart spacing check: inspect remaining text AFTER the deleted suffix
        val remainingAfter = textAfter.drop(suffix.length)
        val appendSpace = remainingAfter.isEmpty() || (!remainingAfter.first().isWhitespace() && remainingAfter.first().isLetterOrDigit())

        commitText(if (appendSpace) "$word " else word)
        appliedWord = if (!appendSpace) word else null
        suggestionStrip?.clearSuggestions()
    }

    companion object {
        private const val TAG = "SithakuruIME"
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
