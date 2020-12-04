package com.gnet.lan_manager.adapt

import com.gnet.lan_manager.search.LanDevice
import com.gnet.lan_manager.websocket.ClientSocket

/**
 *
 * @Description:    连接回调
 * @Author:         yanlei.xia
 * @CreateDate:     2020/11/24 16:52
 */
interface SocketManagerCallback {

    fun onConnected(clientSocket: ClientSocket)

    fun onConnectError(ex: Exception)//一直连接不上以后，会执行这个回调

    fun onMessageReceived(message: String?)

    fun onDisconnected(clientSocket: ClientSocket)

    fun onAvailableDeviceFound(lanDevice: LanDevice) {}
}