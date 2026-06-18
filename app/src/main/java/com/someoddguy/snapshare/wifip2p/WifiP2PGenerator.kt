package com.someoddguy.snapshare.wifip2p

import android.annotation.SuppressLint
import android.content.Context

object WifiP2PGenerator {
    /*TODO change this code to an actual wifip2p connector*/
    @SuppressLint("MissingPermission")
    fun startAsGroupOwner(context: Context, onCredentialsReady: (String) -> Unit) {
        // 1. Call WifiP2pManager.createGroup() here
        // 2. Call WifiP2pManager.requestGroupInfo() to get the details
        // 3. Extract the network details and pass them back

        // Mocking the generated credentials for demonstration:
        val simulatedSsid = "DIRECT-Xy-SnapShare"
        val simulatedPass = "12345678"
        val credentialsString = "$simulatedSsid|$simulatedPass"

        onCredentialsReady(credentialsString)
    }
}