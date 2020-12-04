package com.gnet.lan_manager.search.broadcast

import com.gnet.lan_manager.ControlConstants

/**
 *
 * @Description:     服务器广播处理类
 * @Author:         yanlei.xia
 * @CreateDate:     2020/12/4 13:54
 */
open class MasterBroadcastHandler : DefaultBroadcastHandler() {
    override fun senderPort(): Int {
        return ControlConstants.SLAVE_LISTEN_PORT
    }

    override fun receivePort(): Int {
        return ControlConstants.MASTER_LISTEN_PORT
    }
}