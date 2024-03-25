package io.github.auag0.imnotadeveloper.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import io.github.auag0.imnotadeveloper.BuildConfig
import io.github.auag0.imnotadeveloper.R
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES_IN_NATIVE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEVELOPER_MODE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_USB_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_WIRELESS_DEBUG

class MainActivity : Activity() {
    private val options by lazy {
        listOf(
            Option(getString(R.string.hide_developer_mode), HIDE_DEVELOPER_MODE, true),
            Option(getString(R.string.hide_usb_debug), HIDE_USB_DEBUG, true),
            Option(getString(R.string.hide_wireless_debug), HIDE_WIRELESS_DEBUG, true),
            Option(getString(R.string.hide_debug_properties), HIDE_DEBUG_PROPERTIES, true),
            Option(getString(R.string.hide_debug_properties_in_native), HIDE_DEBUG_PROPERTIES_IN_NATIVE, true),
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
                val dialog = TestDialogFragment()
                dialog.show(fragmentManager, "")
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