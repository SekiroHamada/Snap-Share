package com.someoddguy.snapshare.wifip2p

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.utils.showToast

object WifiP2PGenerator {

    @SuppressLint("MissingPermission")


    fun startAsGroupOwner(
        context: Context,
        changeWifiCredentials: (String) -> Unit
    ) {
        val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val channel = manager.initialize(context, Looper.getMainLooper(), null)

        //remove existing group
        /*TODO fix it*/
        //manager.removeGroup(channel, listener)
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                showToast( "Existing group removed successfully.",true)
                // Now safe to create a new group
                createNewGroup(manager, channel, changeWifiCredentials)
            }

            override fun onFailure(reason: Int) {
                when (reason) {
                WifiP2pManager.BUSY -> {
                    // Reason 2: The framework is busy. We must back off.
                    showToast("removeGroup failed: Framework is BUSY (2).",true)
                    // Assuming you have access to the context to show a toast here
                    // showToast("Wi-Fi is busy. Please wait a moment and try again.", true)

                    // Do NOT call createNewGroup() here. The framework needs a breather.
                }
                else -> {
                    // Reason 0 (ERROR): Usually means there was no group to remove in the first place.
                    showToast("removeGroup failed (Reason: $reason). Proceeding to create group...",true)
                    createNewGroup(manager, channel, changeWifiCredentials)
                }
            }
            }
        })
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun createNewGroup(
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel,
        changeWifiCredentials: (String) -> Unit
    ){

        // 1. Create the Wi-Fi Direct Group
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            @SuppressLint("MissingPermission")
            override fun onSuccess() {
                showToast("Group created successfully",true)

                // 2. Request Group Info to get the credentials
                manager.requestGroupInfo(channel) { group ->
                    if (group != null && group.isGroupOwner) {
                        val ssid = group.networkName
                        val pass = group.passphrase

                        if (ssid != null && pass != null) {
                            // 3. Format and pass back the credentials
                            val credentialsString = "$ssid|$pass"
                            changeWifiCredentials(credentialsString)
                        } else {
                            showToast("SSID or Passphrase is null",true)
                        }
                    }
                }
            }

            override fun onFailure(reason: Int) {
                showToast("Failed to create group. Reason code: $reason",true)
            }
        })
    }
}