package com.gnet.lan_manager

import android.content.Context
import com.gnet.lan_manager.adapt.IControlManager
import com.gnet.lan_manager.adapt.MasterControlManager
import com.gnet.lan_manager.adapt.SlaveControlManager
import com.gnet.lan_manager.adapt.SocketManagerCallback
import com.gnet.lan_manager.bean.LanConfiguration
import com.gnet.lan_manager.log.LanLogger
import com.gnet.lan_manager.search.LanDevice
import com.gnet.lan_manager.search.broadcast.BroadcastHandler
import com.gnet.lan_manager.search.broadcast.MasterBroadcastHandler
import com.gnet.lan_manager.search.broadcast.SlaveBroadcastHandler

/**
 *
 * @Description:     局域网组通信
 * @Author:         yanlei.xia
 * @CreateDate:     2020/11/23 18:02
 */
// TODO: 2020/11/25 1）socket连接断开，对端无法知道，需要发送新消息才能知道对端断开连接
object LanManager {
    private const val TAG = "LanManager"

    private var context: Context? = null

    private var iControlManager: IControlManager? = null

    private var configuration: LanConfiguration? = null

    fun init(context: Context, configuration: LanConfiguration) {
        if (iControlManager != null) {
            LanLogger.i(TAG, "instance already initialed")
            return
        }
        this.context = context
        this.configuration = configuration
        LanLogger.init(configuration.logger)
        initManager()
    }

    private fun initManager() {
        if (configuration == null) {
            LanLogger.e(TAG, "must call init first")
            return
        }
        configuration?.let {
            val broadcastHandler = getBroadcastHandler()
            iControlManager = if (it.isSlave != false) {
                SlaveControlManager(broadcastHandler)
            } else {
                MasterControlManager(
                        it.maxClient,
                        broadcastHandler,
                        it.serverPort,
                        it.protocol
                )
            }
        }
    }

    private fun getBroadcastHandler(): BroadcastHandler {
        var broadcastHandler = configuration?.broadcastHandler
        if (broadcastHandler == null) {
            broadcastHandler = if (configuration?.isSlave == true) SlaveBroadcastHandler() else MasterBroadcastHandler()
        }
        return broadcastHandler
    }

    fun setSocketManagerCallback(socketManagerCallback: SocketManagerCallback?) {
        iControlManager?.setSocketManagerCallback(socketManagerCallback)
    }

    fun start() {
        iControlManager?.start()
    }

    fun connect(lanDevice: LanDevice) {
        iControlManager?.connect(lanDevice)
    }

    fun disconnect() {
        iControlManager?.disconnect()
    }

    fun release() {
        setSocketManagerCallback(null)
        iControlManager?.release()
        iControlManager = null
    }

    fun getContext(): Context? {
        return context
    }

    fun sendMessage(message: String): Boolean {
        return iControlManager?.sendMessage(message) ?: false
    }
}