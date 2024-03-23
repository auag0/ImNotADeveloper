package io.github.auag0.imnotadeveloper.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.auag0.imnotadeveloper.BuildConfig
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEVELOPER_MODE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_USB_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_WIRELESS_DEBUG
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_WIFI_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.DEVELOPMENT_SETTINGS_ENABLED

class Main : IXposedHookLoadPackage {
    private val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)
    override fun handleLoadPackage(param: LoadPackageParam) {
        hookSettingsMethods(param.classLoader)
    }

    private fun hookSettingsMethods(classLoader: ClassLoader) {
        val settingsClassNames = arrayOf(
            "android.provider.Settings.Secure",
            "android.provider.Settings.System",
            "android.provider.Settings.Global",
            "android.provider.Settings.NameValueCache"
        )
        settingsClassNames.forEach {
            val clazz = findClass(it, classLoader)
            hookAllMethods(clazz, "getStringForUser", object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    val name: String? = param.args[1] as? String?
                    return when (name) {
                        DEVELOPMENT_SETTINGS_ENABLED -> {
                            prefs.reload()
                            if (prefs.getBoolean(HIDE_DEVELOPER_MODE, true)) "0" else null
                        }

                        ADB_ENABLED -> {
                            prefs.reload()
                            if (prefs.getBoolean(HIDE_USB_DEBUG, true)) "0" else null
                        }

                        ADB_WIFI_ENABLED -> {
                            prefs.reload()
                            if (prefs.getBoolean(HIDE_WIRELESS_DEBUG, true)) "0" else null
                        }

                        else -> null
                    } ?: param.invokeOriginalMethod()
                }
            })
        }
    }

    private fun MethodHookParam.invokeOriginalMethod(): Any? {
        return XposedBridge.invokeOriginalMethod(method, thisObject, args)
    }
}