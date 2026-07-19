package com.someoddguy.snapshare.utils

import android.content.Context
import android.net.wifi.WifiManager
import com.someoddguy.snapshare.globalcontext.GlobalContext
import java.lang.reflect.Method

object CheckHotspot {

    fun isHotspotOn(): Boolean {
        val context = GlobalContext.appContext
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method: Method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}