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
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sithakuru.sinhala.keyboard.databinding.ActivityMainBinding
import sithakuru.sinhala.keyboard.databinding.ColorSelectorDialogBinding
import sithakuru.sinhala.keyboard.databinding.HelpListItemBinding
import sithakuru.sinhala.keyboard.databinding.KeyboardHelpDialogBinding

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding

    private var settingsChanged: Boolean = false

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_about)
            Dialog(this, R.style.Night).let { dialog ->
                dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
                dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                dialog.window?.setDimAmount(0.8F)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setGravity(Gravity.CENTER)
                dialog.setContentView(R.layout.about_dialog)
                dialog.setCanceledOnTouchOutside(true)
                dialog.window?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                dialog.findViewById<TextView>(R.id.source_link).setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.source_link_url))
                    )
                    startActivity(intent)
                }
                dialog.findViewById<TextView>(R.id.license_link).setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.license_link_url))
                    )
                    startActivity(intent)
                }
                dialog.findViewById<Button>(R.id.dialog_close).setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }

        return super.onMenuItemSelected(featureId, item)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                insets.left,
                insets.top,
                insets.right,
                insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        actionBar?.title = resources.getString(R.string.app_name_si)

        val prefs = Prefs(this)

        binding.switchEnglish.isChecked = prefs.layoutEnglish
        binding.switchWijesekara.isChecked = prefs.layoutWijesekara
        binding.switchSinglish.isChecked = prefs.layoutSinglish
        binding.switchDark.isChecked = prefs.darkTheme
        binding.switchBorder.isChecked = prefs.keyBorders
        binding.longPressKeysLabels.isChecked = prefs.longPressKeysLabels
        binding.longPressKeys.isChecked = prefs.longPressKeys
        binding.swipeToErase.isChecked = prefs.swipeToErase
        binding.swipeToMoveCursor.isChecked = prefs.swipeToMoveCursor

        binding.switchEnglish.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                prefs.layoutEnglish = true
            else if (prefs.layoutWijesekara || prefs.layoutSinglish)
                prefs.layoutEnglish = false
            else
                buttonView.isChecked = true

            settingsChanged = true
        }
        binding.switchWijesekara.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                prefs.layoutWijesekara = true
            else if (prefs.layoutEnglish || prefs.layoutSinglish)
                prefs.layoutWijesekara = false
            else
                buttonView.isChecked = true

            settingsChanged = true
        }
        binding.switchSinglish.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                prefs.layoutSinglish = true
            else if (prefs.layoutEnglish || prefs.layoutWijesekara)
                prefs.layoutSinglish = false
            else
                buttonView.isChecked = true

            settingsChanged = true
        }
        binding.switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkTheme = isChecked
            settingsChanged = true
        }
        binding.switchBorder.setOnCheckedChangeListener { _, isChecked ->
            prefs.keyBorders = isChecked
            settingsChanged = true
        }
        binding.longPressKeysLabels.setOnCheckedChangeListener { _, isChecked ->
            prefs.longPressKeysLabels = isChecked
            settingsChanged = true
        }

        binding.height.max = 12
        binding.height.progress = (prefs.heightPercentage - KEYBOARD_HEIGHT_VALUE_OFFSET) / KEYBOARD_HEIGHT_STEP_MULTIPLIER
        binding.heightPercentage.text = "${prefs.heightPercentage}%"
        binding.height.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress * KEYBOARD_HEIGHT_STEP_MULTIPLIER + KEYBOARD_HEIGHT_VALUE_OFFSET
                binding.heightPercentage.text = "$value%"
                prefs.heightPercentage = value
                settingsChanged = true
            }
        })

        binding.textSize.max = 20
        binding.textSize.progress = prefs.textSize - TEXT_SIZE_VALUE_OFFSET
        binding.textSizeValue.text = "${prefs.textSize}"
        binding.textSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + TEXT_SIZE_VALUE_OFFSET
                binding.textSizeValue.text = "$value"
                prefs.textSize = value
                settingsChanged = true
            }
        })

        fun updateThemeButtons() {
            when (checkCurrentTheme(prefs)) {
                NIGHT -> binding.colorsNight.isChecked = true
                LIGHT -> binding.colorsLight.isChecked = true
                else -> binding.colorsCustom.isChecked = true
            }
        }

        fun updateColorButtonsColors() {
            binding.colorBackground.setBackgroundColor(prefs.colorBackground)
            binding.colorPressedKeyBackground.setBackgroundColor(prefs.colorPressedKeyBackground)
            binding.colorLongPressedKeyBackground.setBackgroundColor(prefs.colorLongPressedKeyBackground)
            binding.colorKeyBorder.setBackgroundColor(prefs.colorKeyBorder)
            binding.colorText.setBackgroundColor(prefs.colorText)
            binding.colorTextDim.setBackgroundColor(prefs.colorTextDim)
            binding.colorTextLongPress.setBackgroundColor(prefs.colorTextLongPress)
        }

        updateThemeButtons()
        updateColorButtonsColors()

        binding.colorsNight.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.colorsLight.isChecked = false
                binding.colorsCustom.isChecked = false

                prefs.colorBackground = getColorInt(R.color.night_1)
                prefs.colorPressedKeyBackground = getColorInt(R.color.night_2)
                prefs.colorLongPressedKeyBackground = getColorInt(R.color.yellow)
                prefs.colorKeyBorder = getColorInt(R.color.night_2)
                prefs.colorText = getColorInt(R.color.light_1)
                prefs.colorTextDim = getColorInt(R.color.night_2)
                prefs.colorTextLongPress = getColorInt(R.color.light_1)

                binding.switchDark.isChecked = true

                updateColorButtonsColors()
                settingsChanged = true
            }
        }

        binding.colorsLight.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.colorsNight.isChecked = false
                binding.colorsCustom.isChecked = false

                prefs.colorBackground = getColorInt(R.color.light_1)
                prefs.colorPressedKeyBackground = getColorInt(R.color.light_2)
                prefs.colorLongPressedKeyBackground = getColorInt(R.color.blue)
                prefs.colorKeyBorder = getColorInt(R.color.light_2)
                prefs.colorText = getColorInt(R.color.night_1)
                prefs.colorTextDim = getColorInt(R.color.light_2)
                prefs.colorTextLongPress = getColorInt(R.color.night_1)

                binding.switchDark.isChecked = false

                updateColorButtonsColors()
                settingsChanged = true
            }
        }

        binding.colorsCustom.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.colorsNight.isChecked = false
                binding.colorsLight.isChecked = false

                prefs.colorBackground = prefs.colorCustomBackground
                prefs.colorPressedKeyBackground = prefs.colorCustomPressedKeyBackground
                prefs.colorLongPressedKeyBackground = prefs.colorCustomLongPressedKeyBackground
                prefs.colorKeyBorder = prefs.colorCustomKeyBorder
                prefs.colorText = prefs.colorCustomText
                prefs.colorTextDim = prefs.colorCustomTextDim
                prefs.colorTextLongPress = prefs.colorCustomTextLongPress

                updateColorButtonsColors()
                settingsChanged = true
            }
        }

        binding.colorBackground.setOnClickListener {
            showColorSelectorDialog(prefs.colorBackground) { color ->
                prefs.colorBackground = color
                prefs.colorCustomBackground = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }
        binding.colorPressedKeyBackground.setOnClickListener {
            showColorSelectorDialog(prefs.colorPressedKeyBackground) { color ->
                prefs.colorPressedKeyBackground = color
                prefs.colorCustomPressedKeyBackground = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }
        binding.colorLongPressedKeyBackground.setOnClickListener {
            showColorSelectorDialog(prefs.colorLongPressedKeyBackground) { color ->
                prefs.colorLongPressedKeyBackground = color
                prefs.colorCustomLongPressedKeyBackground = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }
        binding.colorKeyBorder.setOnClickListener {
            showColorSelectorDialog(prefs.colorKeyBorder) { color ->
                prefs.colorKeyBorder = color
                prefs.colorCustomKeyBorder = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }
        binding.colorText.setOnClickListener {
            showColorSelectorDialog(prefs.colorText) { color ->
                prefs.colorText = color
                prefs.colorCustomText = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }
        binding.colorTextDim.setOnClickListener {
            showColorSelectorDialog(prefs.colorTextDim) { color ->
                prefs.colorTextDim = color
                prefs.colorCustomTextDim = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }
        binding.colorTextLongPress.setOnClickListener {
            showColorSelectorDialog(prefs.colorTextLongPress) { color ->
                prefs.colorTextLongPress = color
                prefs.colorCustomTextLongPress = color
                updateThemeButtons()
                updateColorButtonsColors()
                settingsChanged = true
            }
        }

        binding.longPressKeys.setOnCheckedChangeListener { _, isChecked ->
            prefs.longPressKeys = isChecked
            settingsChanged = true
        }
        binding.swipeToErase.setOnCheckedChangeListener { _, isChecked ->
            prefs.swipeToErase = isChecked
            settingsChanged = true
        }
        binding.swipeToMoveCursor.setOnCheckedChangeListener { _, isChecked ->
            prefs.swipeToMoveCursor = isChecked
            settingsChanged = true
        }

        binding.wijesekaraHelp.setOnClickListener { showHelpDialog(getString(R.string.wijesekara), helpMapWijesekara) }
        binding.singlishHelp.setOnClickListener { showHelpDialog(getString(R.string.singlish), helpMapSinglish) }
    }

    private fun checkCurrentTheme(prefs: Prefs): Int {
        if (
            prefs.colorBackground == getColorInt(R.color.night_1) &&
            prefs.colorPressedKeyBackground == getColorInt(R.color.night_2) &&
            prefs.colorLongPressedKeyBackground == getColorInt(R.color.yellow) &&
            prefs.colorKeyBorder == getColorInt(R.color.night_2) &&
            prefs.colorText == getColorInt(R.color.light_1) &&
            prefs.colorTextDim == getColorInt(R.color.night_2) &&
            prefs.colorTextLongPress == getColorInt(R.color.light_1)
        ) return NIGHT

        if (
            prefs.colorBackground == getColorInt(R.color.light_1) &&
            prefs.colorPressedKeyBackground == getColorInt(R.color.light_2) &&
            prefs.colorLongPressedKeyBackground == getColorInt(R.color.blue) &&
            prefs.colorKeyBorder == getColorInt(R.color.light_2) &&
            prefs.colorText == getColorInt(R.color.night_1) &&
            prefs.colorTextDim == getColorInt(R.color.light_2) &&
            prefs.colorTextLongPress == getColorInt(R.color.night_1)
        ) return LIGHT

        return CUSTOM
    }

    private fun Context.getColorInt(@ColorRes id: Int) = ResourcesCompat.getColor(resources, id, null)

    override fun onPause() {
        super.onPause()
        if (settingsChanged) (getSystemService(ACTIVITY_SERVICE) as ActivityManager?).let { activityManager ->
            activityManager?.runningAppProcesses?.let { processes ->
                for (info in processes)
                    if (info.processName.contains("sithakuru.sinhala.keyboard.input.method.service"))
                        Process.killProcess(info.pid)
            }
            settingsChanged = false
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            var keyboardEnabled = false
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            for (i in inputMethodManager.enabledInputMethodList)
                if (i.packageName == packageName) keyboardEnabled = true

            val keyboardSelected = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).contains(packageName)

            if (keyboardEnabled)
                binding.enableHelp.visibility = View.GONE
            else {
                binding.enableHelp.visibility = View.VISIBLE
                binding.enableHelpStep2.text = getString(R.string.enable_help_step_2, getString(R.string.app_name))
            }
            binding.selectHelp.visibility = if (keyboardEnabled && !keyboardSelected) View.VISIBLE else View.GONE

            if (!keyboardEnabled)
                binding.enableButton.setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
            else if (!keyboardSelected)
                binding.selectButton.setOnClickListener { inputMethodManager.showInputMethodPicker() }
        }

        super.onWindowFocusChanged(hasFocus)
    }

    private fun showHelpDialog(keyboardType: String, helpMap: List<Pair<String, String>>) {
        val dialog = Dialog(this)
        val binding = KeyboardHelpDialogBinding.inflate(layoutInflater)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.8F)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        binding.keyboardType.text = keyboardType
        binding.list.adapter = HelpListAdapter(this, helpMap)
        binding.dialogClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showColorSelectorDialog(@ColorInt currentColor: Int, onColorSelected: (color: Int) -> Unit) {
        val dialog = Dialog(this)
        val binding = ColorSelectorDialogBinding.inflate(layoutInflater)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.8F)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        var colorHex: String
        var colorRed = 0
        var colorGreen = 0
        var colorBlue = 0

        val hexFormat = HexFormat {
            upperCase = false
            bytes {
                bytesPerLine = 4
                byteSeparator = ""
                bytePrefix = ""
            }
        }

        fun setColorInts(@ColorInt colorInt: Int) {
            colorRed = (colorInt shr 16) and 0xFF
            colorGreen = (colorInt shr 8) and 0xFF
            colorBlue = colorInt and 0xFF

            binding.redValue.progress = colorRed
            binding.greenValue.progress = colorGreen
            binding.blueValue.progress = colorBlue
        }

        fun updateHexFromInts() {
            val colorHex = byteArrayOf(
                (colorRed).toByte(),
                (colorGreen).toByte(),
                colorBlue.toByte()
            ).toHexString(hexFormat)
            @SuppressLint("SetTextI18n")
            binding.colorHex.setText("#$colorHex")
        }

        binding.colorHex.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                colorHex = if (s?.startsWith("#") == true) s.toString() else "#$s"
                colorHexToInt(colorHex)?.let { colorInt ->
                    binding.colorPreview.setBackgroundColor(colorInt)
                    setColorInts(colorInt)
                }
            }
        })

        binding.redValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                colorRed = progress
                binding.redText.text = colorRed.toString()
                if (fromUser) updateHexFromInts()
            }
        })

        binding.greenValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                colorGreen = progress
                binding.greenText.text = colorGreen.toString()
                if (fromUser) updateHexFromInts()
            }
        })
        binding.blueValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                colorBlue = progress
                binding.blueText.text = colorBlue.toString()
                if (fromUser) updateHexFromInts()
            }
        })

        setColorInts(currentColor)
        updateHexFromInts()

        binding.dialogClose.setOnClickListener { dialog.dismiss() }
        binding.save.setOnClickListener {
            onColorSelected((0xFF shl 24 or (colorRed shl 16) or (colorGreen shl 8) or colorBlue))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun colorHexToInt(hex: String): Int? {
        try {
            return Color.parseColor(hex)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private companion object {
        private const val NIGHT = 1
        private const val LIGHT = 2
        private const val CUSTOM = 3
        const val KEYBOARD_HEIGHT_VALUE_OFFSET = 70
        const val KEYBOARD_HEIGHT_STEP_MULTIPLIER = 5
        const val TEXT_SIZE_VALUE_OFFSET = 10
    }

    class HelpListAdapter(context: Context, items: List<Pair<String, String>>) :
        ArrayAdapter<Pair<String, String>>(context, R.layout.help_list_item, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val binding = convertView?.let { HelpListItemBinding.bind(it) } ?: HelpListItemBinding.inflate(LayoutInflater.from(context))
            val item = getItem(position)
            binding.first.text = item?.first
            binding.second.text = item?.second
            return binding.root
        }
    }

    private val helpMapWijesekara: List<Pair<String, String>> = listOf(
        "q" to CharText.KETTI_PAA_PILLA,
        "Q" to CharText.DIGA_PAA_PILLA,
        "w" to CharText.AYANNA,
        "W" to CharText.UYANNA,
        "e" to CharText.KETTI_AEDA_PILLA,
        "E" to CharText.DIGA_AEDA_PILLA,
        "r" to CharText.RAYANNA,
        "R" to CharText.IRUYANNA,
        "t" to CharText.EYANNA,
        "T" to CharText.OYANNA,
        "y" to CharText.HAYANNA,
        "Y" to CharText.TAALUJA_SAYANNA,
        "u" to CharText.MAYANNA,
        "U" to CharText.AMBA_BAYANNA,
        "i" to CharText.DANTAJA_SAYANNA,
        "I" to CharText.MUURDHAJA_SAYANNA,
        "o" to CharText.ALPAPRAANA_DAYANNA,
        "O" to CharText.MAHAAPRAANA_DAYANNA,
        "p" to CharText.ALPAPRAANA_CAYANNA,
        "P" to CharText.MAHAAPRAANA_CAYANNA,
        "a" to CharText.SIGN_AL_LAKUNA,
        "A" to CharText.GAYANUKITTA,
        "s" to CharText.KETTI_IS_PILLA,
        "S" to CharText.DIGA_IS_PILLA,
        "d" to CharText.AELA_PILLA,
        "D" to CharText.GAETTA_PILLA,
        "f" to CharText.KOMBUVA,
        "F" to CharText.FAYANNA,
        "g" to CharText.ALPAPRAANA_TTAYANNA,
        "G" to CharText.MAHAAPRAANA_TTAYANNA,
        "h" to CharText.YAYANNA,
        "H" to CharText.SIGN_YANSHAYA,
        "j" to CharText.VAYANNA,
        "J" to CharText.MUURDHAJA_LAYANNA + CharText.KETTI_PAA_PILLA,
        "k" to CharText.DANTAJA_NAYANNA,
        "K" to CharText.MUURDHAJA_NAYANNA,
        "l" to CharText.ALPAPRAANA_KAYANNA,
        "L" to CharText.MAHAAPRAANA_KAYANNA,
        ";" to CharText.ALPAPRAANA_TAYANNA,
        ":" to CharText.MAHAAPRAANA_TAYANNA,
        "x" to CharText.SIGN_ANUSVARAYA,
        "X" to CharText.KANTAJA_NAASIKYAYA,
        "c" to CharText.ALPAPRAANA_JAYANNA,
        "C" to CharText.MAHAAPRAANA_JAYANNA,
        "v" to CharText.ALPAPRAANA_DDAYANNA,
        "V" to CharText.MAHAAPRAANA_DDAYANNA,
        "b" to CharText.IYANNA,
        "B" to CharText.IIYANNA,
        "n" to CharText.ALPAPRAANA_BAYANNA,
        "N" to CharText.MAHAAPRAANA_BAYANNA,
        "m" to CharText.ALPAPRAANA_PAYANNA,
        "M" to CharText.MAHAAPRAANA_PAYANNA,
        "," to CharText.DANTAJA_LAYANNA,
        "<" to CharText.MUURDHAJA_LAYANNA,
        "." to CharText.ALPAPRAANA_GAYANNA,
        ">" to CharText.MAHAAPRAANA_GAYANNA
    )

    private val helpMapSinglish: List<Pair<String, String>> = listOf(
        CharText.AYANNA to "a",
        CharText.AAYANNA to "aa",
        CharText.AEYANNA to "A",
        CharText.AEEYANNA to "Aa",
        CharText.IYANNA to "i",
        CharText.IIYANNA to "ii / I",
        CharText.UYANNA to "u",
        CharText.UUYANNA to "uu / U",
        CharText.IRUYANNA to "R",
        CharText.IRUUYANNA to "Ri",
        CharText.EYANNA to "e",
        CharText.EEYANNA to "ee / E",
        CharText.AIYANNA to "ai",
        CharText.OYANNA to "o",
        CharText.OOYANNA to "oo / O",
        CharText.AUYANNA to "au",
        CharText.SIGN_ANUSVARAYA to "x",
        CharText.SIGN_VISARGAYA to "X",
        CharText.ALPAPRAANA_KAYANNA + CharText.SIGN_AL_LAKUNA to "k",
        CharText.MAHAAPRAANA_KAYANNA + CharText.SIGN_AL_LAKUNA to "kh / K",
        CharText.ALPAPRAANA_GAYANNA + CharText.SIGN_AL_LAKUNA to "g",
        CharText.MAHAAPRAANA_GAYANNA + CharText.SIGN_AL_LAKUNA to "gh / G",
        CharText.KANTAJA_NAASIKYAYA + CharText.SIGN_AL_LAKUNA to "",
        CharText.SANYAKA_GAYANNA + CharText.SIGN_AL_LAKUNA to "zg",
        CharText.ALPAPRAANA_CAYANNA + CharText.SIGN_AL_LAKUNA to "gh / G",
        CharText.MAHAAPRAANA_CAYANNA + CharText.SIGN_AL_LAKUNA to "c",
        CharText.MAHAAPRAANA_GAYANNA + CharText.SIGN_AL_LAKUNA to "ch / C",
        CharText.ALPAPRAANA_JAYANNA + CharText.SIGN_AL_LAKUNA to "j",
        CharText.MAHAAPRAANA_JAYANNA + CharText.SIGN_AL_LAKUNA to "jh / J",
        CharText.TAALUJA_NAASIKYAYA + CharText.SIGN_AL_LAKUNA to "zk",
        CharText.SANYAKA_JAYANNA + CharText.SIGN_AL_LAKUNA to "zj",
        CharText.ALPAPRAANA_TTAYANNA + CharText.SIGN_AL_LAKUNA to "t",
        CharText.MAHAAPRAANA_TTAYANNA + CharText.SIGN_AL_LAKUNA to "T",
        CharText.ALPAPRAANA_DDAYANNA + CharText.SIGN_AL_LAKUNA to "d",
        CharText.MAHAAPRAANA_DDAYANNA + CharText.SIGN_AL_LAKUNA to "D",
        CharText.MUURDHAJA_NAYANNA + CharText.SIGN_AL_LAKUNA to "N",
        CharText.SANYAKA_DDAYANNA + CharText.SIGN_AL_LAKUNA to "zD",
        CharText.ALPAPRAANA_TAYANNA + CharText.SIGN_AL_LAKUNA to "th",
        CharText.MAHAAPRAANA_TAYANNA + CharText.SIGN_AL_LAKUNA to "thh / Th",
        CharText.ALPAPRAANA_DAYANNA + CharText.SIGN_AL_LAKUNA to "dh / q",
        CharText.MAHAAPRAANA_DAYANNA + CharText.SIGN_AL_LAKUNA to "dhh / Dh / qh / Q",
        CharText.DANTAJA_NAYANNA + CharText.SIGN_AL_LAKUNA to "n",
        CharText.SANYAKA_DAYANNA + CharText.SIGN_AL_LAKUNA to "zdh",
        CharText.ALPAPRAANA_PAYANNA + CharText.SIGN_AL_LAKUNA to "p",
        CharText.MAHAAPRAANA_PAYANNA + CharText.SIGN_AL_LAKUNA to "ph / P",
        CharText.ALPAPRAANA_BAYANNA + CharText.SIGN_AL_LAKUNA to "b",
        CharText.MAHAAPRAANA_BAYANNA + CharText.SIGN_AL_LAKUNA to "bh / B",
        CharText.MAYANNA + CharText.SIGN_AL_LAKUNA to "m",
        CharText.AMBA_BAYANNA + CharText.SIGN_AL_LAKUNA to "zb",
        CharText.YAYANNA + CharText.SIGN_AL_LAKUNA to "y",
        CharText.RAYANNA + CharText.SIGN_AL_LAKUNA to "r",
        CharText.DANTAJA_LAYANNA + CharText.SIGN_AL_LAKUNA to "l",
        CharText.VAYANNA + CharText.SIGN_AL_LAKUNA to "v / w",
        CharText.TAALUJA_SAYANNA + CharText.SIGN_AL_LAKUNA to "sh",
        CharText.MUURDHAJA_SAYANNA + CharText.SIGN_AL_LAKUNA to "S / Sh",
        CharText.DANTAJA_SAYANNA + CharText.SIGN_AL_LAKUNA to "S",
        CharText.HAYANNA + CharText.SIGN_AL_LAKUNA to "h",
        CharText.MUURDHAJA_LAYANNA + CharText.SIGN_AL_LAKUNA to "L",
        CharText.FAYANNA + CharText.SIGN_AL_LAKUNA to "f / F",
        CharText.TAALUJA_SANYOOGA_NAAKSIKYAYA + CharText.SIGN_AL_LAKUNA to "zh",
        "" to "",
        CharText.ALPAPRAANA_KAYANNA to "ka",
        CharText.ALPAPRAANA_KAYANNA + CharText.AELA_PILLA to "kaa",
        CharText.ALPAPRAANA_KAYANNA + CharText.KETTI_AEDA_PILLA to "kA",
        CharText.ALPAPRAANA_KAYANNA + CharText.DIGA_AEDA_PILLA to "kAa",
        CharText.ALPAPRAANA_KAYANNA + CharText.KETTI_IS_PILLA to "ki",
        CharText.ALPAPRAANA_KAYANNA + CharText.DIGA_IS_PILLA to "kii / kI",
        CharText.ALPAPRAANA_KAYANNA + CharText.KETTI_PAA_PILLA to "ku",
        CharText.ALPAPRAANA_KAYANNA + CharText.DIGA_PAA_PILLA to "kuu / kU",
        CharText.ALPAPRAANA_KAYANNA + CharText.GAETTA_PILLA to "kR",
        CharText.ALPAPRAANA_KAYANNA + CharText.DIGA_GAETTA_PILLA to "kRi",
        CharText.ALPAPRAANA_KAYANNA + CharText.KOMBUVA to "ke",
        CharText.ALPAPRAANA_KAYANNA + CharText.DIGA_KOMBUVA to "kee / kE",
        CharText.ALPAPRAANA_KAYANNA + CharText.KOMBU_DEKA to "kai",
        CharText.ALPAPRAANA_KAYANNA + CharText.KOMBUVA_HAA_AELA_PILLA to "ko",
        CharText.ALPAPRAANA_KAYANNA + CharText.KOMBUVA_HAA_DIGA_AELA_PILLA to "koo / kO",
        CharText.ALPAPRAANA_KAYANNA + CharText.KOMBUVA_HAA_GAYANUKITTA to "kau",
        CharText.ALPAPRAANA_KAYANNA + CharText.SIGN_ANUSVARAYA to "kax",
        CharText.ALPAPRAANA_KAYANNA + CharText.SIGN_VISARGAYA to "kaX",
        "" to "",
        CharText.ALPAPRAANA_KAYANNA + CharText.SIGN_YANSHAYA + CharText.SIGN_AL_LAKUNA to "ky / kY",
        CharText.ALPAPRAANA_KAYANNA + CharText.SIGN_RAKARANSHAYA + CharText.SIGN_AL_LAKUNA to "kr"
    )
}
