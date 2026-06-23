package com.someoddguy.snapshare.wifip2p

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WifiP2PClient {

    var SSID : String = ""
    var PASS : String = ""
    var GO_IP : String = ""

    fun saveWifiCredentials(ssid: String, pass: String, goIp:String) {
        SSID = ssid
        PASS = pass
        GO_IP = goIp
    }
    fun connectToGroupOwner(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 1. Specify the network credentials received via BLE
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(SSID)
            .setWpa2Passphrase(PASS)
            .build()

        // 2. Build the network request
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // Crucial: Tells Android this is a local P2P network, not for internet access
            .setNetworkSpecifier(specifier)
            .build()

        showToast("Joining Wi-Fi Network: $SSID", short = true)

        // 3. Request the connection
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                showToast("Successfully connected to Group Owner!",true)

                // Bind the process to this network so your subsequent Socket connections route through the P2P WiFi and not mobile data.
                connectivityManager.bindProcessToNetwork(network)

                showToast("$SSID|$PASS|$GO_IP",false)

                ClientSocket.startSocketClient(network,GO_IP)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                showToast("Failed to connect to the network.",true)
            }
        })
    }

}