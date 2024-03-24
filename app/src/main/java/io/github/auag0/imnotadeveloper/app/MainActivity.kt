package io.github.auag0.imnotadeveloper.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global
import android.provider.Settings.Secure
import android.text.Html
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import io.github.auag0.imnotadeveloper.BuildConfig
import io.github.auag0.imnotadeveloper.R
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEVELOPER_MODE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_USB_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_WIRELESS_DEBUG
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_WIFI_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.DEVELOPMENT_SETTINGS_ENABLED

class MainActivity : Activity() {
    private val options by lazy {
        listOf(
            Option(getString(R.string.hide_developer_mode), HIDE_DEVELOPER_MODE, true),
            Option(getString(R.string.hide_usb_debug), HIDE_USB_DEBUG, true),
            Option(getString(R.string.hide_wireless_debug), HIDE_WIRELESS_DEBUG, true)
        )
    }

    private data class Option(
        val title: String,
        val key: String,
        val defaultValue: Boolean
    )

    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = try {
            @Suppress("DEPRECATION")
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_preferences",
                Context.MODE_WORLD_READABLE
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, R.string.sp_is_not_available, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val container = LinearLayout(this).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL
            val paddingSize = 16f.dpToPx()
            setPadding(paddingSize, paddingSize, paddingSize, paddingSize)
        }

        options.forEach { option ->
            val switch = Switch(this).apply {
                text = option.title
                textSize = 18f
                isChecked = prefs.getBoolean(option.key, option.defaultValue)
                setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit().putBoolean(option.key, isChecked).apply()
                }
            }
            container.addView(switch)
        }

        val testBtn = Button(this).apply {
            text = getString(R.string.test)
            setOnClickListener {
                val developerMode = Secure.getString(contentResolver, DEVELOPMENT_SETTINGS_ENABLED)
                val usbDebug = Global.getString(contentResolver, ADB_ENABLED)
                val wirelessDebug = Global.getString(contentResolver, ADB_WIFI_ENABLED)
                val result = buildString {
                    appendLine("<p>developer mode status: $developerMode</p>")
                    appendLine("<p>usb debug status: $usbDebug</p>")
                    appendLine("<p>wireless debug status: $wirelessDebug</p>")
                    appendLine("<p>0 = <font color=\"#00ff00\">OFF</font> / 1 = <font color=\"#ff0000\">ON</font></p>")
                }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.test)
                    .setMessage(Html.fromHtml(result))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
        container.addView(testBtn)

        setContentView(container)
        with(getColor(android.R.color.transparent)) {
            window.navigationBarColor = this
            window.statusBarColor = this
        }
    }

    private fun Float.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        ).toInt()
    }
}