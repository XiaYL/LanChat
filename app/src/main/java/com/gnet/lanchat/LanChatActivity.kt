package com.gnet.lanchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.gnet.lan_manager.LanManager
import com.gnet.lan_manager.adapt.SocketManagerCallback
import com.gnet.lan_manager.bean.LanConfiguration
import com.gnet.lan_manager.websocket.ClientSocket

/**
 * @Description: 测试类
 * @Author: yanlei.xia
 * @CreateDate: 2020/11/25 13:48
 */
class LanChatActivity : AppCompatActivity() {

    private var isSlave: Boolean = true
    private var msgTxt: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lanchat)
        initData()
        initView()
    }

    private fun initData() {
        isSlave = intent.getBooleanExtra("slave", true)
        LanManager.init(this, LanConfiguration(isSlave))
        LanManager.setSocketManagerCallback(object : SocketManagerCallback {
            override fun onConnected(clientSocket: ClientSocket) {
                showToast("socket connected")
            }

            override fun onConnectError(ex: Exception) {
                showToast("socket connect error: ${ex.message}")
            }

            override fun onMessageReceived(message: String?) {
                msgTxt?.text = message
            }

            override fun onDisconnected(clientSocket: ClientSocket) {
                showToast("socket disconnected")
            }
        })
        LanManager.start()
    }

    private fun initView() {
        findViewById<View>(R.id.message_send_btn).setOnClickListener {
            val message = if (isSlave) "Hello, I'm client: ${System.currentTimeMillis()}" else "Hello, I'm server: ${System.currentTimeMillis()}"
            LanManager.sendMessage(message)
        }
        msgTxt = findViewById(R.id.message_received)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LanManager.release()
    }

    companion object {
        fun openChat(context: Context, isSlave: Boolean) {
            val intent = Intent(context, LanChatActivity::class.java)
            intent.putExtra("slave", isSlave)
            context.startActivity(intent)
        }
    }
}