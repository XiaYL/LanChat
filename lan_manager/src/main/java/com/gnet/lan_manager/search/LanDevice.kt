package com.gnet.lan_manager.search

/**
 *
 * @Description:     服务器
 * @Author:         yanlei.xia
 * @CreateDate:     2020/12/4 10:44
 */
data class LanDevice(
        val ip: String,
        val port: Int,
        val protocol: String,
        val server: Boolean
) {

    override fun toString(): String {
        return "[ip = $ip, port= $port, protocol= $protocol], server= $server"
    }
}