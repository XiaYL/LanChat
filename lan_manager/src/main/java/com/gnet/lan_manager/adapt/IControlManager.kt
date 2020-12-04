package com.gnet.lan_manager.adapt

import com.gnet.lan_manager.search.LanDevice

/**
 *
 * @Description:     管理类
 * @Author:         yanlei.xia
 * @CreateDate:     2020/11/24 10:03
 */
interface IControlManager {

    fun setSocketManagerCallback(callback: SocketManagerCallback?) {}

    fun start()

    fun sendMessage(message: String): Boolean

    fun connect(lanDevice: LanDevice) {}

    fun disconnect() {}

    fun release()
}