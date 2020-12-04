package com.gnet.lan_manager.search

import android.os.Build
import com.gnet.lan_manager.log.LanLogger
import com.gnet.lan_manager.search.DeviceBroadcastReceiver.BroadcastReceiverCallback
import com.gnet.lan_manager.search.broadcast.BroadcastHandler
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/27.
 */
class MasterSearchManager(
        private val broadcastHandler: BroadcastHandler,
        private val protocol: String,
        private val serverPort: Int
) : BroadcastReceiverCallback {

    interface MasterSearchManagerCallback {
        fun onFoundNewSlave(lanDevice: LanDevice)
    }

    private var broadcastSender: DeviceBroadcastSender? = null
    private var broadcastReceiver: DeviceBroadcastReceiver? = null

    @Volatile
    private var stop = false

    @Volatile
    private var ipList: MutableList<String> = ArrayList(5)
    private var callback: MasterSearchManagerCallback? = null
    private var timer: Timer? = null

    fun setSearchCallback(callback: MasterSearchManagerCallback?) {
        this.callback = callback
    }

    fun start() {
        stop = false
        broadcastReceiver = DeviceBroadcastReceiver(broadcastHandler.receivePort()).apply {
            setBroadcastReceiveCallback(this@MasterSearchManager)
            startBroadcastReceive()
        }
        broadcastSender = DeviceBroadcastSender(broadcastHandler.senderPort())
        if (timer == null) {
            timer = fixedRateTimer(TAG, false, 0, BROADCAST_INTERVAL.toLong()) {
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
            jsonObject.put("port", serverPort)
            jsonObject.put("protocol", protocol)
            jsonObject.put("deviceName", Build.MODEL)
            jsonObject.put("uuid", broadcastHandler.broadcastUUId())
            jsonObject.put("info", broadcastHandler.messageBroadcast())
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
            val uuid = jsonObj.optString("uuid")
            val type = jsonObj.optString("type")
            if (uuid == broadcastHandler.broadcastUUId() && type == "slave") {
                val info = jsonObj.optString("info")
                if (!ipList.contains(senderIp)) {
                    ipList.add(senderIp)
                    callback?.onFoundNewSlave(LanDevice(senderIp, serverPort, protocol, false, info))
                }
                broadcastHandler.onBroadcastMessageReceived(info)
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