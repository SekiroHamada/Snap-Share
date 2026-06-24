package com.someoddguy.snapshare.wifip2p

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.utils.ConnectionValidationString
import com.someoddguy.snapshare.utils.showToast

object WifiP2PGenerator {

    // Increase settling time between operations
    private const val OPERATION_DELAY_MS = 1500L
    private const val CREATE_GROUP_DELAY_MS = 2000L

    @SuppressLint("MissingPermission")
    fun startAsGroupOwner(context: Context, changeWifiCredentials: (String) -> Unit) {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val channel = manager.initialize(appContext, Looper.getMainLooper(), null)

        // Check P2P state before doing anything
        manager.requestP2pState(channel) { state ->
            if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                ConnectionValidationString.updateStatus("WiFi P2P is not enabled on this device")
                return@requestP2pState
            }
            stopDiscoveryStep(manager, channel, changeWifiCredentials)
        }
    }

    private fun stopDiscoveryStep(
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel,
        changeWifiCredentials: (String) -> Unit
    ) {
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiP2P", "Peer discovery stopped")
                delayThen { removeExistingGroup(manager, channel, changeWifiCredentials) }
            }
            override fun onFailure(reason: Int) {
                // Failure here just means discovery wasn't running — safe to continue
                Log.d("WifiP2P", "stopPeerDiscovery failed (reason $reason) — likely not running, continuing")
                delayThen { removeExistingGroup(manager, channel, changeWifiCredentials) }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun removeExistingGroup(
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel,
        changeWifiCredentials: (String) -> Unit,
        retries: Int = 2  // Increase retries
    ) {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                ConnectionValidationString.updateStatus("Group removed. Waiting for hardware to settle...")
                // Always wait after removeGroup — hardware needs time to reset
                delayThen(CREATE_GROUP_DELAY_MS) {
                    createNewGroup(manager, channel, changeWifiCredentials)
                }
            }

            override fun onFailure(reason: Int) {
                ConnectionValidationString.updateStatus("removeGroup failed reason=$reason, retries left=$retries")
                when {
                    // BUSY (2) or ERROR (0) — both warrant a retry with delay
                    (reason == WifiP2pManager.BUSY || reason == WifiP2pManager.ERROR) && retries > 0 -> {
                        delayThen(OPERATION_DELAY_MS) {
                            removeExistingGroup(manager, channel, changeWifiCredentials, retries - 1)
                        }
                    }
                    // No group existed — safe to create directly, but still delay
                    else -> {
                        ConnectionValidationString.updateStatus("No existing group to remove (or unrecoverable). Proceeding to create.")
                        delayThen(CREATE_GROUP_DELAY_MS) {
                            createNewGroup(manager, channel, changeWifiCredentials)
                        }
                    }
                }
            }
        })
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun createNewGroup(
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel,
        changeWifiCredentials: (String) -> Unit,
        retries: Int = 3
    ) {
        ConnectionValidationString.updateStatus("Trying to create group")
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            @SuppressLint("MissingPermission")
            override fun onSuccess() {
                ConnectionValidationString.updateStatus("Group created. Fetching credentials...")
                // Small delay before requestGroupInfo — group info may not be populated instantly
                delayThen(1000L) {
                    manager.requestGroupInfo(channel) { group ->
                        if (group != null && group.isGroupOwner) {
                            val ssid = group.networkName
                            val pass = group.passphrase
                            if (ssid != null && pass != null) {
                                //send characteristics to the sender
                                changeWifiCredentials("$ssid|$pass")
                                ConnectionValidationString.updateStatus("Socket Info : $ssid")

                                //start server
                                ServerSocketGenerator.startServer()
                            } else {
                                showToast("SSID or Passphrase is null", true)
                            }
                        } else {
                            showToast("Not group owner or group is null", true)
                        }
                    }
                }
            }
            @SuppressLint("MissingPermission")
            override fun onFailure(reason: Int) {
                ConnectionValidationString.updateStatus("createGroup failed reason=$reason")
                if (reason == WifiP2pManager.BUSY && retries > 0) {
                    delayThen(OPERATION_DELAY_MS) {
                        createNewGroup(manager, channel, changeWifiCredentials, retries - 1)
                    }
                } else {
                    ConnectionValidationString.updateStatus("Failed to create group. Reason: $reason")
                }
            }
        })
    }

    private fun delayThen(ms: Long = OPERATION_DELAY_MS, action: () -> Unit) {
        android.os.Handler(Looper.getMainLooper()).postDelayed(action, ms)
    }
}