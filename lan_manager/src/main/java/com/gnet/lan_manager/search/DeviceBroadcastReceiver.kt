package com.gnet.lan_manager.search

import android.os.Handler
import android.os.Looper
import com.gnet.lan_manager.log.LanLogger
import com.gnet.lan_manager.utils.IpUtil.hostIP
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/26.
 */
class DeviceBroadcastReceiver(private val port: Int) {

    @Volatile
    private var needListen = true

    interface BroadcastReceiverCallback {
        fun onBroadcastError(errMsg: String?)
        fun onReceive(senderIp: String, message: String)
    }

    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private var server: DatagramSocket? = null
    private var callback: BroadcastReceiverCallback? = null

    fun setBroadcastReceiveCallback(callback: BroadcastReceiverCallback?) {
        this.callback = callback
    }

    @Throws(IOException::class)
    private fun startReceive() {
        val receive = DatagramPacket(ByteArray(BUFFER_LEN), BUFFER_LEN)
        if (server == null) {
            server = DatagramSocket(null)//此处一定要设置null，不然会自动绑定端口
                    .apply {
                        reuseAddress = true
                        bind(InetSocketAddress(inetAddress, this@DeviceBroadcastReceiver.port))
                    }
        }
        LanLogger.i(TAG, "---------- start listen ------------")
        while (needListen) {
            server?.receive(receive)
            val recvByte = Arrays.copyOfRange(receive.data, 0, receive.length)
            val receiveMsg = String(recvByte)
            LanLogger.d(TAG, "receive msg:$receiveMsg")
            val senderIp = receive.address.hostAddress
            val localIP = hostIP
            if (senderIp == localIP) {
                continue
            }
            handler.post { callback?.onReceive(senderIp, receiveMsg) }
        }
        server?.disconnect()
        server?.close()
        LanLogger.i(TAG, "---------- end listen ------------")
    }

    fun startBroadcastReceive() {
        server?.close()
        server = null
        Thread(Runnable {
            try {
                startReceive()
            } catch (e: IOException) {
                handler.post {
                    callback?.onBroadcastError(e.message)
                }
                server?.close()
                server = null
            }
        }).start()
    }

    fun stopReceive() {
        needListen = false
    }

    companion object {
        private const val TAG = "DeviceBroadcastReceiver"
        private const val BUFFER_LEN = 1024 * 4
    }
}