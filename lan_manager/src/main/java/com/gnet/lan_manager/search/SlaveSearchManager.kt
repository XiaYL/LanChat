package com.gnet.lan_manager.search

import android.os.Build
import com.gnet.lan_manager.search.DeviceBroadcastReceiver.BroadcastReceiverCallback
import com.gnet.lan_manager.search.broadcast.BroadcastHandler
import com.gnet.lan_manager.log.LanLogger
import org.json.JSONException
import org.json.JSONObject

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/27.
 */
class SlaveSearchManager(
        private val broadcastHandler: BroadcastHandler
) : BroadcastReceiverCallback {
    private var broadcastSender: DeviceBroadcastSender? = null
    private var broadcastReceiver: DeviceBroadcastReceiver? = null
    private var callback: SlaveSearchManagerCallback? = null

    interface SlaveSearchManagerCallback {
        fun onFoundMaster(device: LanDevice)
    }

    fun setSlaveSearchCallback(callback: SlaveSearchManagerCallback?) {
        this.callback = callback
    }

    fun start() {
        broadcastReceiver = DeviceBroadcastReceiver(broadcastHandler.receivePort()).apply {
            setBroadcastReceiveCallback(this@SlaveSearchManager)
            startBroadcastReceive()
        }
        broadcastSender = DeviceBroadcastSender(broadcastHandler.senderPort())
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
            val uuid = jsonObj.optString("uuid")
            val type = jsonObj.optString("type")
            if (uuid == broadcastHandler.broadcastUUId() && type == "master") {
                val port = jsonObj.optInt("port", 8080)
                val protocol = jsonObj.optString("protocol")
                broadcastSender?.sendBroadcastData(getBroadcastMessage())
                callback?.onFoundMaster(LanDevice(senderIp, port, protocol, true))
                broadcastHandler.onBroadcastMessageReceived(jsonObj.optString("info"))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun getBroadcastMessage(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("type", "slave")
            jsonObject.put("deviceName", Build.MODEL)
            jsonObject.put("uuid", broadcastHandler.broadcastUUId())
            jsonObject.put("info", broadcastHandler.messageBroadcast())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject.toString()
    }

    companion object {
        private const val TAG = "SlaveSearchManager"
    }

}