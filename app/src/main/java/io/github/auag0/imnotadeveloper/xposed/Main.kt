package io.github.auag0.imnotadeveloper.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.auag0.imnotadeveloper.BuildConfig
import io.github.auag0.imnotadeveloper.common.Logger.logD
import io.github.auag0.imnotadeveloper.common.Logger.logE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES_IN_NATIVE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEVELOPER_MODE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_USB_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_WIRELESS_DEBUG
import io.github.auag0.imnotadeveloper.common.PropKeys
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_WIFI_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.DEVELOPMENT_SETTINGS_ENABLED
import java.lang.reflect.Method

class Main : IXposedHookLoadPackage {
    private val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

    val propOverrides = mapOf(
        PropKeys.SYS_USB_FFS_READY to "0",
        PropKeys.SYS_USB_CONFIG to "mtp",
        PropKeys.PERSIST_SYS_USB_CONFIG to "mtp",
        PropKeys.SYS_USB_STATE to "mtp",
        PropKeys.INIT_SVC_ADBD to "stopped"
    )

    override fun handleLoadPackage(param: LoadPackageParam) {
        hookSettingsMethods(param.classLoader)
        hookSystemPropertiesMethods(param.classLoader)
        hookProcessMethods(param.classLoader)
        hookNativeMethods(param.classLoader)
    }

    private fun hookNativeMethods(classLoader: ClassLoader) {
        prefs.reload()
        if (prefs.getBoolean(HIDE_DEBUG_PROPERTIES_IN_NATIVE, true)) {
            try {
                System.loadLibrary("ImNotADeveloper")
                NativeFun.setProps(propOverrides)
            } catch (e: Exception) {
                logE(e.message)
            }
        }
    }

    private fun hookProcessMethods(classLoader: ClassLoader) {
        val hookCmd = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                hookedLog(param)
                val cmdarray = (param.args[0] as Array<*>).filterIsInstance<String>()
                val firstCmd = cmdarray.getOrNull(0)
                val secondCmd = cmdarray.getOrNull(1)
                if (firstCmd == "getprop" && propOverrides.containsKey(secondCmd)) {
                    prefs.reload()
                    if (prefs.getBoolean(HIDE_DEBUG_PROPERTIES, true)) {
                        val writableCmdArray = ArrayList(cmdarray)
                        writableCmdArray[1] = "Dummy${System.currentTimeMillis()}"
                        val a: Array<String> = writableCmdArray.toTypedArray()
                        param.args[0] = a
                    }
                }
            }
        }
        val processImpl = findClass("java.lang.ProcessImpl", classLoader)
        hookAllMethods(processImpl, "start", hookCmd)

        val processManager = findClass("java.lang.ProcessManager", classLoader)
        hookAllMethods(processManager, "exec", hookCmd)
    }

    private fun hookSystemPropertiesMethods(classLoader: ClassLoader) {
        val methods = arrayOf(
            "native_get",
            "native_get_int",
            "native_get_long",
            "native_get_boolean",
        )
        val systemProperties = findClass("android.os.SystemProperties", classLoader)
        methods.forEach { methodName ->
            hookAllMethods(systemProperties, methodName, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    if (param.args[0] !is String) {
                        return param.invokeOriginalMethod()
                    }
                    hookedLog(param)
                    val key = param.args[0] as String
                    val method = param.method as Method

                    val value = propOverrides[key]
                    if (value != null) {
                        prefs.reload()
                        if (prefs.getBoolean(HIDE_DEBUG_PROPERTIES, true)) {
                            return try {
                                when (method.returnType) {
                                    String::class.java -> value
                                    Int::class.java -> value.toInt()
                                    Long::class.java -> value.toLong()
                                    Boolean::class.java -> value.toBoolean()
                                    else -> param.invokeOriginalMethod()
                                }
                            } catch (e: NumberFormatException) {
                                logE(e.message)
                            }
                        }
                    }

                    return param.invokeOriginalMethod()
                }
            })
        }
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
                    hookedLog(param)
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

    private fun hookedLog(param: MethodHookParam) {
        val method = param.method as Method
        val message = buildString {
            appendLine("Hooked ${method.declaringClass.name}$${param.method.name} -> ${method.returnType.name}")
            param.args.forEachIndexed { index, any: Any? ->
                appendLine("    $index:${any.string()}")
            }
        }
        logD(message)
    }

    private fun Any?.string(): String {
        return when (this) {
            is List<*> -> joinToString(prefix = "[", postfix = "]")
            is Array<*> -> joinToString(prefix = "[", postfix = "]")
            else -> toString()
        }
    }

    private fun String.toBoolean(): Boolean {
        return when {
            equals("true", true) || equals("1", true) -> true
            equals("false", true) || equals("0", true) -> false
            else -> throw NumberFormatException(this)
        }
    }

    private fun MethodHookParam.invokeOriginalMethod(): Any? {
        return XposedBridge.invokeOriginalMethod(method, thisObject, args)
    }
}