package com.gnet.lan_manager.adapt

import android.os.Handler
import android.os.Looper
import com.gnet.lan_manager.bean.ConnectState
import com.gnet.lan_manager.search.SlaveSearchManager
import com.gnet.lan_manager.search.SlaveSearchManager.SlaveSearchManagerCallback
import com.gnet.lan_manager.utils.LanLogger
import com.gnet.lan_manager.websocket.ClientSocket
import com.gnet.lan_manager.websocket.SlaveWebSocketManager

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/10/9.
 */
class SlaveControlManager(
        private val teamId: String?,
        private val taskId: String?,
        private val receiverPort: Int,
        private val senderPort: Int
) : SlaveSearchManagerCallback, IControlManager, SocketManagerCallback {
    private var socketManagerCallback: SocketManagerCallback? = null
    private var searchManager: SlaveSearchManager? = null
    private var masterIp: String? = null
    private var masterPort: Int? = null
    private var protocol: String? = null
    private var socketManager: SlaveWebSocketManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var retryCount = 0
    private var mState = ConnectState.DISCONNECTED
    private var isReleased = false //释放被释放

    override fun start() {
        if (isReleased) {
            return
        }
        stop()
        startSearchMaster(teamId, taskId)
    }

    override fun release() {
        LanLogger.i(TAG, "client release")
        isReleased = true
        stop()
    }

    override fun setSocketManagerCallback(callback: SocketManagerCallback?) {
        this.socketManagerCallback = callback
    }

    override fun sendMessage(message: String): Boolean {
        LanLogger.d(TAG, "client send message= $message")
        return socketManager?.sendMessage(message) ?: false
    }

    override fun onFoundMaster(
            masterIp: String,
            masterPort: Int,
            protocol: String
    ) {
        LanLogger.i(TAG, "find server [ip = $masterIp, port= $masterPort]")
        this.masterIp = masterIp
        this.masterPort = masterPort
        this.protocol = protocol
        stopSearch()
        startConnectMaster()
    }

    override fun onConnected(clientSocket: ClientSocket) {
        LanLogger.i(TAG, "on server connected: $clientSocket")
        setState(ConnectState.CONNECTED)
        retryCount = 0
        handler.post { socketManagerCallback?.onConnected(clientSocket) }
    }

    override fun onConnectError(ex: Exception) {
        LanLogger.i(TAG, "connect server error: ${ex.message}")
        setState(ConnectState.DISCONNECTED)
        reconnect()
    }

    override fun onMessageReceived(message: String?) {
        LanLogger.i(TAG, "onMessageReceived from server= $message")
        handler.post {
            socketManagerCallback?.onMessageReceived(message)
        }
    }

    override fun onDisconnected(clientSocket: ClientSocket) {
        LanLogger.i(TAG, "on server disconnected: $clientSocket")
        setState(ConnectState.DISCONNECTED)
        handler.post { socketManagerCallback?.onDisconnected(clientSocket) }
        start() //断开连接以后，重新搜索，等待连接
    }

    private fun stop() {
        LanLogger.i(TAG, "client stop")
        stopSearch()
        stopConnect()
    }

    /**
     * 连接失败以后，尝试重连
     */
    private fun reconnect() {
        if (mState == ConnectState.DISCONNECTED && retryCount < 5) {
            retryCount++
            stopConnect()
            startConnectMaster()
        }
    }

    private fun stopSearch() {
        searchManager?.setSlaveSearchCallback(null)
        searchManager?.stop()
        searchManager = null
    }

    private fun stopConnect() {
        socketManager?.setSocketManagerCallback(null)
        socketManager?.stopConnect()
        socketManager = null
    }

    private fun startSearchMaster(teamId: String?, taskId: String?) {
        if (mState != ConnectState.DISCONNECTED) {
            return
        }
        LanLogger.i(TAG, "start search server")
        setState(ConnectState.SEARCHING)
        searchManager = SlaveSearchManager(teamId, taskId, receiverPort, senderPort).apply {
            setSlaveSearchCallback(this@SlaveControlManager)
            start()
        }
    }

    private fun startConnectMaster() {
        if (mState == ConnectState.CONNECTING) {
            return
        }
        if (masterIp == null || masterPort == null || protocol == null) {
            LanLogger.e(TAG, "no server found")
            return
        }
        setState(ConnectState.CONNECTING)
        socketManager = SlaveWebSocketManager(masterIp!!, masterPort!!, protocol!!).apply {
            setSocketManagerCallback(this@SlaveControlManager)
            startConnect()
        }
    }

    private fun setState(state: Int) {
        mState = state
    }

    companion object {
        private val TAG = SlaveControlManager::class.java.simpleName
    }
}