package com.gnet.lan_manager.websocket

import com.gnet.lan_manager.adapt.SocketManagerCallback
import com.koushikdutta.async.AsyncSocket
import com.koushikdutta.async.ByteBufferList
import com.koushikdutta.async.DataEmitter
import com.koushikdutta.async.callback.CompletedCallback
import com.koushikdutta.async.callback.DataCallback
import com.koushikdutta.async.http.WebSocket

/**
 *
 * @Description:    socket连接类
 * @Author:         yanlei.xia
 * @CreateDate:     2020/11/24 17:14
 */
class ClientSocket(
        private var mSocket: WebSocket?,
        private val socketManagerCallback: SocketManagerCallback?
) : CompletedCallback, WebSocket.StringCallback, DataCallback {

    init {
        mSocket?.apply {
            stringCallback = this@ClientSocket
            closedCallback = this@ClientSocket
            dataCallback = this@ClientSocket
        }
    }

    override fun onCompleted(ex: Exception?) {
        socketManagerCallback?.onDisconnected(this)
    }

    override fun onStringAvailable(s: String?) {
        socketManagerCallback?.onMessageReceived(s)
    }

    override fun onDataAvailable(emitter: DataEmitter?, bb: ByteBufferList?) {

    }

    fun sendMessage(message: String?): Boolean {
        if (message.isNullOrEmpty()) {
            return false
        }
        return try {
            mSocket?.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopConnect() {
        try {
            mSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mSocket = null
        }
    }

    fun getAsyncSocket(): AsyncSocket? {
        return mSocket?.socket
    }

    override fun toString(): String {
        return "${getAsyncSocket()}"
    }
}