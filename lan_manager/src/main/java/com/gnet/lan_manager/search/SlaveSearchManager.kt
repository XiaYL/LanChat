package com.gnet.lan_manager.search

import android.os.Build
import com.gnet.lan_manager.search.DeviceBroadcastReceiver.BroadcastReceiverCallback
import com.gnet.lan_manager.utils.LanLogger
import org.json.JSONException
import org.json.JSONObject

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/27.
 */
class SlaveSearchManager(
        private val teamId: String?,
        private val taskId: String?,
        private val receiverPort: Int,
        private val senderPort: Int
) :
        BroadcastReceiverCallback {
    private var broadcastSender: DeviceBroadcastSender? = null
    private var broadcastReceiver: DeviceBroadcastReceiver? = null
    private var callback: SlaveSearchManagerCallback? = null

    interface SlaveSearchManagerCallback {
        fun onFoundMaster(masterIp: String, masterPort: Int, protocol: String)
    }

    fun setSlaveSearchCallback(callback: SlaveSearchManagerCallback?) {
        this.callback = callback
    }

    fun start() {
        broadcastReceiver = DeviceBroadcastReceiver(receiverPort).apply {
            setBroadcastReceiveCallback(this@SlaveSearchManager)
            startBroadcastReceive()
        }
        broadcastSender = DeviceBroadcastSender(senderPort)
    }

    fun stop() {
        broadcastReceiver?.setBroadcastReceiveCallback(null)
        broadcastReceiver?.stopReceive()
        broadcastReceiver = null
        broadcastSender = null
    }

    override fun onBroadcastError(errMsg: String?) {
        LanLogger.d(TAG, "errMsg=$errMsg")
    }

    override fun onReceive(senderIp: String, message: String) {
        LanLogger.d(TAG, "message:$message")
        try {
            val jsonObj = JSONObject(message)
            val type = jsonObj.optString("type")
            if (type == "master") {
                val team = jsonObj.optString("teamId")
                val task = jsonObj.optString("taskId")
                val port = jsonObj.optInt("port", 8080)
                val protocol = jsonObj.optString("protocol")
                broadcastSender?.sendBroadcastData(getBroadcastMessage())
                if (team == teamId && task == taskId) {
                    callback?.onFoundMaster(senderIp, port, protocol)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun getBroadcastMessage(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("type", "slave")
            jsonObject.put("teamId", teamId)
            jsonObject.put("taskId", taskId)
            jsonObject.put("deviceName", Build.MODEL)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject.toString()
    }

    companion object {
        private const val TAG = "SlaveSearchManager"
    }

}