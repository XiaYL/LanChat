package com.gnet.lan_manager.websocket

import com.gnet.lan_manager.adapt.SocketManagerCallback
import com.gnet.lan_manager.utils.LanLogger
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/29.
 */
class SlaveWebSocketManager(
        private val serverIp: String,
        private val serverPort: Int,
        private val protocol: String
) : SocketManagerCallback {

    private var socketManagerCallback: SocketManagerCallback? = null
    private var slaveClientSocket: ClientSocket? = null
    private val isConnected: Boolean
        get() = slaveClientSocket != null

    fun setSocketManagerCallback(callback: SocketManagerCallback?) {
        this.socketManagerCallback = callback
    }

    fun startConnect() {
        if (isConnected) {
            return
        }
        val url = "ws://$serverIp:$serverPort"
        LanLogger.i(TAG, "start connect server= $url")

        AsyncHttpClient.getDefaultInstance().websocket(url, protocol)
        { ex: Exception?, webSocket: WebSocket? ->
            if (ex != null) {
                onConnectError(ex)
                return@websocket
            }
            if (webSocket == null) {
                onConnectError(NullPointerException("socket is null"))
                return@websocket
            }
            slaveClientSocket = ClientSocket(webSocket, this).also {
                onConnected(it)
            }
        }
    }

    fun sendMessage(message: String?): Boolean {
        if (message.isNullOrEmpty()) {
            return false
        }
        return slaveClientSocket?.sendMessage(message) ?: false
    }

    fun stopConnect() {
        slaveClientSocket?.stopConnect()
        slaveClientSocket = null
    }

    override fun onConnected(clientSocket: ClientSocket) {
        socketManagerCallback?.onConnected(clientSocket)
    }

    override fun onConnectError(ex: Exception) {
        socketManagerCallback?.onConnectError(ex)
    }

    override fun onMessageReceived(message: String?) {
        socketManagerCallback?.onMessageReceived(message)
    }

    override fun onDisconnected(clientSocket: ClientSocket) {
        socketManagerCallback?.onDisconnected(clientSocket)
        slaveClientSocket = null
    }

    companion object {
        private val TAG = SlaveWebSocketManager::class.java.simpleName
    }
}