package io.github.auag0.imnotadeveloper.app

import android.annotation.SuppressLint

object Prop {
    @SuppressLint("PrivateApi")
    fun getPropFromSystemProperties(key: String): String? {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val getMethod = systemProperties.getDeclaredMethod("get", String::class.java)
        return getMethod.invoke(null, key) as String?
    }

    fun getPropFromShell(key: String): String {
        val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
        process.waitFor()
        return process.inputStream.bufferedReader().readText()
    }
}