package com.someoddguy.snapshare.wifip2p

import android.content.Context
import com.someoddguy.snapshare.utils.showToast

object WifiP2pClient {
    fun connectToGroupOwner(context: Context, ssid: String, pass: String) {
        // Implement WifiP2pManager.connect() here using the provided credentials.
        // This will join the group created by your Receiver device.
        showToast("Joining Wi-Fi Network: $ssid", true)
    }
}
