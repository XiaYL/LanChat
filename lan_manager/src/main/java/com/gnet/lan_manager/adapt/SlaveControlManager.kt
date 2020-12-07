package com.gnet.lan_manager.adapt

import android.os.Handler
import android.os.Looper
import com.gnet.lan_manager.bean.ConnectState
import com.gnet.lan_manager.search.LanDevice
import com.gnet.lan_manager.search.SlaveSearchManager
import com.gnet.lan_manager.search.SlaveSearchManager.SlaveSearchManagerCallback
import com.gnet.lan_manager.search.broadcast.BroadcastHandler
import com.gnet.lan_manager.log.LanLogger
import com.gnet.lan_manager.websocket.ClientSocket
import com.gnet.lan_manager.websocket.SlaveWebSocketManager

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/10/9.
 */
class SlaveControlManager(
        private val broadcastHandler: BroadcastHandler
) : SlaveSearchManagerCallback, IControlManager, SocketManagerCallback {
    private var socketManagerCallback: SocketManagerCallback? = null
    private var searchManager: SlaveSearchManager? = null
    private var socketManager: SlaveWebSocketManager? = null
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var retryCount = 0
    private var mState = ConnectState.DISCONNECTED
    private var isReleased = false //释放被释放
    private var server: LanDevice? = null

    override fun start() {
        if (isReleased) {
            return
        }
        stop()
        startSearchMaster()
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
        if (socketManager == null) {
            LanLogger.e(TAG, "socket manager is null")
            return false
        }
        LanLogger.d(TAG, "client send message= $message")
        return socketManager?.sendMessage(message) ?: false
    }

    override fun connect(lanDevice: LanDevice) {
        disconnect()
        this.retryCount = 0
        this.server = lanDevice
        startConnectMaster()
    }

    override fun disconnect() {
        stopConnect()
    }

    override fun onFoundMaster(device: LanDevice) {
        LanLogger.i(TAG, "find server $device")
        handler.post { socketManagerCallback?.onAvailableDeviceFound(device) }
        if (device == server) {
            connect(device)
        }
    }

    override fun onConnected(clientSocket: ClientSocket) {
        LanLogger.i(TAG, "on server connected: $clientSocket")
        stopSearch()//连接成功以后，停止搜索
        setState(ConnectState.CONNECTED)
        retryCount = 0
        handler.post { socketManagerCallback?.onConnected(clientSocket) }
    }

    override fun onConnectError(ex: Exception) {
        LanLogger.i(TAG, "connect server error: ${ex.message}")
        setState(ConnectState.DISCONNECTED)
        if (!reconnect()) {
            handler.post { socketManagerCallback?.onConnectError(ex) }
        }
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
        startSearchMaster() //断开连接以后，重新搜索，等待连接
    }

    private fun stop() {
        LanLogger.i(TAG, "client stop")
        stopSearch()
        stopConnect()
    }

    /**
     * 连接失败以后，尝试重连
     */
    private fun reconnect(): Boolean {
        if (mState == ConnectState.DISCONNECTED && retryCount < 5) {
            retryCount++
            stopConnect()
            startConnectMaster()
            return true
        }
        return false
    }

    private fun stopSearch() {
        searchManager?.setSlaveSearchCallback(null)
        searchManager?.stop()
        searchManager = null
    }

    private fun stopConnect() {
        this.server = null
        socketManager?.setSocketManagerCallback(null)
        socketManager?.stopConnect()
        socketManager = null
    }

    private fun startSearchMaster() {
        LanLogger.i(TAG, "start search server")
        setState(ConnectState.SEARCHING)
        searchManager = SlaveSearchManager(broadcastHandler).apply {
            setSlaveSearchCallback(this@SlaveControlManager)
            start()
        }
    }

    private fun startConnectMaster() {
        if (mState == ConnectState.CONNECTING) {
            return
        }
        server?.let { device ->
            setState(ConnectState.CONNECTING)
            socketManager = SlaveWebSocketManager(device.ip, device.port, device.protocol).apply {
                setSocketManagerCallback(this@SlaveControlManager)
                startConnect()
            }
        }
    }

    private fun setState(state: Int) {
        mState = state
    }

    companion object {
        private val TAG = SlaveControlManager::class.java.simpleName
    }
}