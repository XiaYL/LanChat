package com.gnet.lan_manager.bean

import com.gnet.lan_manager.ControlConstants

/**
 * @Description: 局域网组配置
 * @Author: yanlei.xia
 * @CreateDate: 2020/11/25 9:35
 */
class LanConfiguration(val isSlave: Boolean? = true) {
    var maxClient: Int = ControlConstants.SLAVECOUNT
    var teamId: String? = ControlConstants.TEAMID
    var taskId: String? = ControlConstants.TASKID
    var protocol: String = ControlConstants.PROTOCOL
    var serverPort: Int = ControlConstants.SERVER_SOCKET_PORT
    var clientListenPort: Int = ControlConstants.SLAVE_LISTEN_PORT
    var serverListenPort: Int = ControlConstants.MASTER_LISTEN_PORT
}