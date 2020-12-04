package com.gnet.lan_manager.search.broadcast

/**
 *
 * @Description:     广播消息，服务端的发送端口对应客户端的接收端口，接收端口对应客户端的发送端口
 * @Author:         yanlei.xia
 * @CreateDate:     2020/12/4 10:14
 */
interface BroadcastHandler {
    fun broadcastUUId(): String //广播任务唯一标识
    fun messageBroadcast(): String
    fun onBroadcastMessageReceived(message: String)
    fun senderPort(): Int //发送广播端口
    fun receivePort(): Int//接收广播端口
}