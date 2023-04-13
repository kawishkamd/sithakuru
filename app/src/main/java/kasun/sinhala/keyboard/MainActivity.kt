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

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible

class MainActivity : Activity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

        actionBar?.title = resources.getString(R.string.app_name_si)

        val switchEnglish = findViewById<Switch>(R.id.switch_english)
        val switchWijesekara = findViewById<Switch>(R.id.switch_wijesekara)
        val switchSinglish = findViewById<Switch>(R.id.switch_singlish)
        val switchDark = findViewById<Switch>(R.id.switch_dark)
        val switchKeyBorders = findViewById<Switch>(R.id.switch_border)

        val prefs = Prefs(this)

        switchEnglish.isChecked = prefs.layoutEnglish
        switchWijesekara.isChecked = prefs.layoutWijesekara
        switchSinglish.isChecked = prefs.layoutSinglish
        switchDark.isChecked = prefs.darkTheme
        switchKeyBorders.isChecked = prefs.keyBorders

        switchEnglish.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                prefs.layoutEnglish = true
            else if (prefs.layoutWijesekara || prefs.layoutSinglish)
                prefs.layoutEnglish = false
            else
                buttonView.isChecked = true
        }
        switchWijesekara.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                prefs.layoutWijesekara = true
            else if (prefs.layoutEnglish || prefs.layoutSinglish)
                prefs.layoutWijesekara = false
            else
                buttonView.isChecked = true
        }
        switchSinglish.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                prefs.layoutSinglish = true
            else if (prefs.layoutEnglish || prefs.layoutWijesekara)
                prefs.layoutSinglish = false
            else
                buttonView.isChecked = true
        }
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkTheme = isChecked
        }
        switchKeyBorders.setOnCheckedChangeListener { _, isChecked ->
            prefs.keyBorders = isChecked
        }

        super.onCreate(savedInstanceState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            var keyboardEnabled = false
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            for (i in inputMethodManager.enabledInputMethodList)
                if (i.packageName == BuildConfig.APPLICATION_ID) keyboardEnabled = true

            val keyboardSelected =
                Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                    .contains(BuildConfig.APPLICATION_ID)

            findViewById<LinearLayout>(R.id.enable_help).isVisible = !keyboardEnabled
            findViewById<LinearLayout>(R.id.select_help).isVisible =
                keyboardEnabled && !keyboardSelected

            if (!keyboardEnabled)
                findViewById<Button>(R.id.enable_button).setOnClickListener {
                    startActivity(
                        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    )
                }
            else if (!keyboardSelected)
                findViewById<Button>(R.id.select_button).setOnClickListener { inputMethodManager.showInputMethodPicker() }
        }

        super.onWindowFocusChanged(hasFocus)
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}