package com.gnet.lan_manager.search.broadcast

import com.gnet.lan_manager.ControlConstants

/**
 *
 * @Description:     默认广播处理类
 * @Author:         yanlei.xia
 * @CreateDate:     2020/12/4 10:36
 */
abstract class DefaultBroadcastHandler : BroadcastHandler {
    override fun broadcastUUId(): String {
        return ControlConstants.TEAMID + ControlConstants.TASKID
    }

    override fun messageBroadcast(): String {
        return ""
    }

    override fun onBroadcastMessageReceived(message: String) {

    }
}