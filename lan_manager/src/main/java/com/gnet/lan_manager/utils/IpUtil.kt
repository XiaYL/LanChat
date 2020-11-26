package com.gnet.lan_manager.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.lang.reflect.InvocationTargetException
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Copyright 2017 SpeakIn.Inc
 * Created by west on 2017/9/27.
 */
object IpUtil {
    @JvmStatic
    fun isWifiApEnabled(context: Context?): Boolean {
        if (context == null) {
            throw NullPointerException("you must call init first")
        }
        try {
            val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method = manager.javaClass.getMethod("isWifiApEnabled")
            return method.invoke(manager) as Boolean
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return false
    }// skip ipv6

    /**
     * 获取ip地址
     *
     * @return
     */
    @JvmStatic
    val hostIP: String?
        get() {
            var hostIp: String? = null
            try {
                val nis: Enumeration<*> = NetworkInterface.getNetworkInterfaces()
                var ia: InetAddress? = null
                while (nis.hasMoreElements()) {
                    val ni = nis.nextElement() as NetworkInterface
                    val ias = ni.inetAddresses
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement()
                        if (ia is Inet6Address) {
                            continue  // skip ipv6
                        }
                        val ip = ia.hostAddress
                        if ("127.0.0.1" != ip) {
                            hostIp = ia.hostAddress
                            break
                        }
                    }
                }
            } catch (e: SocketException) {
                Log.i("yao", "SocketException")
                e.printStackTrace()
            }
            return hostIp
        }
}