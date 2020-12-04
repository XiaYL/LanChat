package com.gnet.lan_manager.adapt

import android.os.Handler
import android.os.Looper
import com.gnet.lan_manager.search.MasterSearchManager
import com.gnet.lan_manager.search.MasterSearchManager.MasterSearchManagerCallback
import com.gnet.lan_manager.search.broadcast.BroadcastHandler
import com.gnet.lan_manager.log.LanLogger
import com.gnet.lan_manager.websocket.ClientSocket
import com.gnet.lan_manager.websocket.MasterWebSocketManager

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/10/9.
 */
class MasterControlManager(
        private val maxConnectCount: Int,
        private val broadcastHandler: BroadcastHandler,
        private val serverPort: Int,
        private val protocol: String
) : MasterSearchManagerCallback, IControlManager, SocketManagerCallback {
    private var socketManagerCallback: SocketManagerCallback? = null
    private var searchManager: MasterSearchManager? = null
    private var socketManager: MasterWebSocketManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isReleased = false //释放被释放

    override fun start() {
        searchManager =
                MasterSearchManager(broadcastHandler, protocol, serverPort).apply {
                    setSearchCallback(this@MasterControlManager)
                    start()
                }
        socketManager = MasterWebSocketManager(maxConnectCount, serverPort, protocol).apply {
            setSocketManagerCallback(this@MasterControlManager)
            start()
        }
    }

    override fun setSocketManagerCallback(callback: SocketManagerCallback?) {
        this.socketManagerCallback = callback
    }

    override fun release() {
        LanLogger.i(TAG, "server release")
        isReleased = true
        stop()
    }

    override fun sendMessage(message: String): Boolean {
        if (socketManager == null) {
            LanLogger.e(TAG, "socket manager is null")
            return false
        }
        LanLogger.i(TAG, "send message to client: $message")
        return socketManager?.sendMessage(message) ?: false
    }

    override fun onConnected(clientSocket: ClientSocket) {
        LanLogger.i(TAG, "on client connected= $clientSocket")
        if (socketManager?.clientCount == maxConnectCount) {
            stopSearch()
        }
        handler.post {
            socketManagerCallback?.onConnected(clientSocket)
        }
    }

    override fun onConnectError(ex: Exception) {
        LanLogger.i(TAG, "on client connect error")
        handler.post { socketManagerCallback?.onConnectError(ex) }
    }

    override fun onMessageReceived(message: String?) {
        LanLogger.i(TAG, "onMessageReceived from client= $message")
        handler.post { socketManagerCallback?.onMessageReceived(message) }
    }

    override fun onDisconnected(clientSocket: ClientSocket) {
        LanLogger.i(TAG, "on client disconnected= $clientSocket")
        handler.post { socketManagerCallback?.onDisconnected(clientSocket) }
    }

    override fun onFoundNewSlave(slaveIp: String) {

    }

    private fun stop() {
        LanLogger.i(TAG, "server stop")
        stopSearch()
        stopMaster()
    }

    private fun stopMaster() {
        socketManager?.setSocketManagerCallback(null)
        socketManager?.stop()
        socketManager = null
    }

    private fun stopSearch() {
        searchManager?.setSearchCallback(null)
        searchManager?.stop()
        searchManager = null
    }

    companion object {
        private val TAG = MasterControlManager::class.java.simpleName
    }
}