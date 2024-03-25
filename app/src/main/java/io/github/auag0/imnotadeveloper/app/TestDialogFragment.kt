package io.github.auag0.imnotadeveloper.app

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.auag0.imnotadeveloper.R
import io.github.auag0.imnotadeveloper.app.Prop.getPropFromShell
import io.github.auag0.imnotadeveloper.app.Prop.getPropFromSystemProperties
import io.github.auag0.imnotadeveloper.common.PropKeys
import io.github.auag0.imnotadeveloper.common.PropKeys.INIT_SVC_ADBD
import io.github.auag0.imnotadeveloper.common.PropKeys.PERSIST_SYS_USB_CONFIG
import io.github.auag0.imnotadeveloper.common.PropKeys.SYS_USB_CONFIG
import io.github.auag0.imnotadeveloper.common.PropKeys.SYS_USB_STATE

class TestDialogFragment : DialogFragment() {
    private lateinit var dialogView: View
    private lateinit var tvDeveloperModeStatus: TextView
    private lateinit var tvUsbDebugStatus: TextView
    private lateinit var tvWirelessDebugStatus: TextView
    private lateinit var tvSystemProperties: TextView
    private lateinit var tvShellGetPropStatus: TextView
    private lateinit var tvSystemPropertyGetStatus: TextView
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = LayoutInflater.from(context)
        dialogView = layoutInflater.inflate(R.layout.test_dialog, (null))
        return AlertDialog.Builder(context)
            .setTitle(getString(R.string.test))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDeveloperModeStatus = dialogView.findViewById(R.id.tvDeveloperModeStatus)
        tvUsbDebugStatus = dialogView.findViewById(R.id.tvUsbDebugStatus)
        tvWirelessDebugStatus = dialogView.findViewById(R.id.tvWirelessDebugStatus)
        tvSystemProperties = dialogView.findViewById(R.id.tvSystemPropertiesStatus)
        tvShellGetPropStatus = dialogView.findViewById(R.id.tvShellGetPropStatus)
        tvSystemPropertyGetStatus = dialogView.findViewById(R.id.tvSystemPropertyGetStatus)

        val enabledDeveloperMode = Settings.Secure.getInt(
            context.contentResolver,
            PropKeys.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )
        tvDeveloperModeStatus.text = "$enabledDeveloperMode"

        val enabledUsbDebug = Settings.Global.getInt(
            context.contentResolver,
            PropKeys.ADB_ENABLED,
            0
        )
        tvUsbDebugStatus.text = "$enabledUsbDebug"

        val enabledWirelessDebug = Settings.Global.getInt(
            context.contentResolver,
            PropKeys.ADB_WIFI_ENABLED,
            0
        )
        tvWirelessDebugStatus.text = "$enabledWirelessDebug"

        val props = arrayOf(SYS_USB_CONFIG, PERSIST_SYS_USB_CONFIG, SYS_USB_STATE, INIT_SVC_ADBD)
        var strings = StringBuilder()
        props.forEach { key ->
            val value = getPropFromSystemProperties(key)
            with(strings) {
                appendLine("  $key -> ${value?.trim()}")
            }
        }
        tvSystemProperties.text = strings

        strings = StringBuilder()
        props.forEach { key ->
            val value = getPropFromShell(key)
            with(strings) {
                appendLine("  $key -> ${value.trim()}")
            }
        }
        tvShellGetPropStatus.text = strings


        tvSystemPropertyGetStatus.text = null
    }
}