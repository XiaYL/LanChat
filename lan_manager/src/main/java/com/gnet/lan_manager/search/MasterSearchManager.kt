package com.gnet.lan_manager.search

import android.os.Build
import com.gnet.lan_manager.search.DeviceBroadcastReceiver.BroadcastReceiverCallback
import com.gnet.lan_manager.utils.LanLogger
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/27.
 */
class MasterSearchManager(
        private val teamId: String?,
        private val taskId: String?,
        private val protocol: String,
        private val serverPort: Int,
        private val receiverPort: Int,
        private val senderPort: Int
) : BroadcastReceiverCallback {

    interface MasterSearchManagerCallback {
        fun onFoundNewSlave(slaveIp: String, slaveInfo: JSONObject)
    }

    private var broadcastSender: DeviceBroadcastSender? = null
    private var broadcastReceiver: DeviceBroadcastReceiver? = null

    @Volatile
    private var stop = false
    private val ipList: MutableList<String>
    private var callback: MasterSearchManagerCallback? = null
    private var timer: Timer? = null

    init {
        ipList = ArrayList(5)
    }

    fun setSearchCallback(callback: MasterSearchManagerCallback?) {
        this.callback = callback
    }

    fun start() {
        stop = false
        broadcastReceiver = DeviceBroadcastReceiver(receiverPort).apply {
            setBroadcastReceiveCallback(this@MasterSearchManager)
            startBroadcastReceive()
        }
        broadcastSender = DeviceBroadcastSender(senderPort)
        if (timer == null) {
            timer = fixedRateTimer(taskId, false, 0, BROADCAST_INTERVAL.toLong()) {
                if (!stop) {
                    broadcastSender?.sendBroadcastData(getBroadcastMessage())
                }
            }
        }
    }

    fun stop() {
        LanLogger.d(TAG, "stop search")
        stop = true
        broadcastReceiver?.setBroadcastReceiveCallback(null)
        broadcastReceiver?.stopReceive()
        broadcastReceiver = null
        broadcastSender = null
        timer?.cancel()
    }

    private fun getBroadcastMessage(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("type", "master")
            jsonObject.put("teamId", teamId)
            jsonObject.put("taskId", taskId)
            jsonObject.put("port", serverPort)
            jsonObject.put("protocol", protocol)
            jsonObject.put("deviceName", Build.MODEL)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject.toString()
    }

    override fun onBroadcastError(errMsg: String?) {
        LanLogger.d(TAG, "errMsg =$errMsg")
    }

    override fun onReceive(senderIp: String, message: String) {
        LanLogger.d(TAG, "message:$message")
        try {
            val jsonObj = JSONObject(message)
            val type = jsonObj.optString("type")
            if (type == "slave") {
                val team = jsonObj.optString("teamId")
                val task = jsonObj.optString("taskId")
                if (team == teamId && task == taskId) {
                    if (!ipList.contains(senderIp)) {
                        ipList.add(senderIp)
                        callback?.onFoundNewSlave(senderIp, jsonObj)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "MasterSearchManager"
        private const val BROADCAST_INTERVAL = 5 * 1000
    }
}