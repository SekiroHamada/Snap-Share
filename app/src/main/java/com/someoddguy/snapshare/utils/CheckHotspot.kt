package com.someoddguy.snapshare.utils

import android.content.Context
import android.net.wifi.WifiManager
import com.someoddguy.snapshare.globalcontext.GlobalContext
import java.lang.reflect.Method

object CheckHotspot {
    /**
     * Checks if the device's mobile hotspot is currently enabled.
     * Uses reflection to access the hidden isWifiApEnabled method in WifiManager.
     */
    fun isHotspotOn(): Boolean {
        val context = GlobalContext.appContext
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method: Method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            // If reflection fails (e.g., due to extreme OEM restrictions on newer APIs),
            // fallback to false so it doesn't indefinitely block the user's flow.
            false
        }
    }
}