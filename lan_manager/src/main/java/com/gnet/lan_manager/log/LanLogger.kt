package com.gnet.lan_manager.log

import android.util.Log

/**
 *
 * @Description:     日志管理类
 * @Author:         yanlei.xia
 * @CreateDate:     2020/11/24 11:31
 */
object LanLogger : ILogger {
    private const val TAG = "LanManager"
    private var iLogger: ILogger? = null

    fun init(iLogger: ILogger?) {
        this.iLogger = iLogger
    }

    override fun d(tag: String, message: String?) {
        getLogger()?.d(tag, message)
    }

    override fun i(tag: String, message: String?) {
        getLogger()?.i(tag, message)
    }

    override fun e(tag: String, message: String?) {
        getLogger()?.i(tag, message)
    }

    private fun getLogger(): ILogger? {
        if (iLogger == null) {
            return defaultLogger()
        }
        return iLogger
    }

    private fun defaultLogger(): ILogger {
        return object : ILogger {
            override fun d(tag: String, message: String?) {
                Log.d(TAG, "[$tag] d: $message")
            }

            override fun i(tag: String, message: String?) {
                Log.i(TAG, "[$tag] i: $message")
            }

            override fun e(tag: String, message: String?) {
                Log.i(TAG, "[$tag] e: $message")
            }
        }
    }
}