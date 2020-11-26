package com.gnet.lan_manager.search

import com.gnet.lan_manager.LanManager.getContext
import com.gnet.lan_manager.utils.IpUtil.isWifiApEnabled
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/26.
 */
class DeviceBroadcastSender(private val port: Int) {

    @Throws(IOException::class)
    private fun sendBroadcast(message: String) {
        val broadcastIp = if (isWifiApEnabled(getContext())) {
            "192.168.43.255"
        } else {
            BROADCAST_IP
        }
        val msg: ByteArray = message.toByteArray()
        /*
         * 在Java UDP中单播与广播的代码是相同的,要实现具有广播功能的程序只需要使用广播地址即可, 例如：这里使用了本地的广播地址
         */
        val inetAddr = InetAddress.getByName(broadcastIp)
        val client = DatagramSocket()
        val sendPack = DatagramPacket(msg, msg.size, inetAddr, port)
        client.send(sendPack)
        client.close()
    }

    fun sendBroadcastData(message: String) {
        Thread(Runnable {
            try {
                sendBroadcast(message)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    companion object {
        private const val BROADCAST_IP = "255.255.255.255"
    }
}