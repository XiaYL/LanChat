package com.gnet.lan_manager.bean

import com.gnet.lan_manager.ControlConstants
import com.gnet.lan_manager.log.ILogger
import com.gnet.lan_manager.search.broadcast.BroadcastHandler

/**
 * @Description: 局域网组配置
 * @Author: yanlei.xia
 * @CreateDate: 2020/11/25 9:35
 */
class LanConfiguration(val isSlave: Boolean? = true) {
    var maxClient: Int = ControlConstants.SLAVECOUNT
    var broadcastHandler: BroadcastHandler? = null
    var protocol: String = ControlConstants.PROTOCOL
    var serverPort: Int = ControlConstants.SERVER_SOCKET_PORT
    var logger: ILogger? = null
}