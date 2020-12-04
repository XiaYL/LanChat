package com.gnet.lan_manager.websocket

import com.gnet.lan_manager.adapt.SocketManagerCallback
import com.gnet.lan_manager.log.LanLogger
import com.koushikdutta.async.AsyncNetworkSocket
import com.koushikdutta.async.callback.CompletedCallback
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServer.WebSocketRequestCallback

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/28.
 */
class MasterWebSocketManager(
        private val maxConnectCount: Int,
        private val serverPort: Int,
        private val protocol: String
) : SocketManagerCallback {

    private val server: AsyncHttpServer? = AsyncHttpServer()

    @Volatile
    private var socketClients = mutableListOf<ClientSocket>()
    private var socketManagerCallback: SocketManagerCallback? = null
    val clientCount: Int
        get() = socketClients.size

    private val callback = WebSocketRequestCallback { webSocket, request ->
        LanLogger.d(TAG, "server receive request= $request")
        if (socketClients.size >= maxConnectCount) {//超过最大连接数以后，不再连接
            LanLogger.e(TAG, "on max connect count reached")
            webSocket.close()
            return@WebSocketRequestCallback
        }
        val clientSocket = ClientSocket(webSocket, this)
        var slaveIndex = -1
        val asyncNetworkSocket = webSocket.socket as AsyncNetworkSocket
        for (i in socketClients.indices) {
            val ws = socketClients[i]
            val anws = ws.getAsyncSocket() as AsyncNetworkSocket
            if (asyncNetworkSocket.remoteAddress.hostName == anws.remoteAddress.hostName) {
                slaveIndex = i
                break
            }
        }
        if (slaveIndex == -1) {
            socketClients.add(clientSocket)
            onConnected(clientSocket)
        } else { //如果已经存在的连接，需要替换，解决slave端重新加入局域网
            socketClients[slaveIndex] = clientSocket
        }
    }

    fun setSocketManagerCallback(callback: SocketManagerCallback?) {
        this.socketManagerCallback = callback
    }

    fun start() {
        server?.websocket("/", protocol, callback)
        server?.listen(serverPort)
        server?.errorCallback = CompletedCallback { ex ->
            if (ex != null) {
                onConnectError(ex)
            }
        }
        LanLogger.d(TAG, "start server and listen")
    }

    fun stop() {
        LanLogger.d(TAG, "stop server")
        try {
            val sockets = mutableListOf<ClientSocket?>().apply { addAll(socketClients) }
            for (socket in sockets) {
                socket?.stopConnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            socketClients.clear()
            try {
                server?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(message: String?): Boolean {
        return try {
            for (clientSocket in socketClients) {
                clientSocket.sendMessage(message)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
        socketClients.remove(clientSocket)
        socketManagerCallback?.onDisconnected(clientSocket)
    }

    companion object {
        private val TAG = MasterWebSocketManager::class.java.simpleName
    }
}