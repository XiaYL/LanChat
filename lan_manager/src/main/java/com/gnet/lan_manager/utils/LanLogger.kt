package com.gnet.lan_manager.utils

import android.util.Log

/**
 *
 * @Description:     日志管理类
 * @Author:         yanlei.xia
 * @CreateDate:     2020/11/24 11:31
 */
object LanLogger {
    private const val TAG = "LanManager"

    fun d(tag: String, message: String?) {
        Log.d(TAG, "[$tag] d: $message")
    }

    fun i(tag: String, message: String?) {
        Log.i(TAG, "[$tag] i: $message")
    }

    fun e(tag: String, message: String?) {
        Log.i(TAG, "[$tag] e: $message")
    }
}