package io.github.auag0.imnotadeveloper.common

import android.util.Log

object Logger {
    private const val TAG = "ImNotADeveloper"
    fun logD(msg: Any?) {
        Log.d(TAG, msg.toString())
    }

    fun logE(msg: Any?) {
        Log.e(TAG, msg.toString())
    }
}