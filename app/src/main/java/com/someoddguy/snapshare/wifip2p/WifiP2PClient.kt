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
import com.someoddguy.snapshare.utils.ConnectionValidationString
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WifiP2PClient {

    var SSID : String = ""
    var PASS : String = ""
    val GO_IP : String = "192.168.49.1"

    fun saveWifiCredentials(ssid: String, pass: String) {
        SSID = ssid
        PASS = pass
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

        ConnectionValidationString.updateStatus("Joining Wi-Fi Network : $SSID")

        // 3. Request the connection
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                ConnectionValidationString.updateStatus("Successfully connected to Group Owner!")

                // Bind the process to this network so your subsequent Socket connections route through the P2P WiFi and not mobile data.
                connectivityManager.bindProcessToNetwork(network)
                ConnectionValidationString.updateStatus("Starting Socket with Network : $SSID")
                ClientSocket.startSocketClient(network,GO_IP)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                ConnectionValidationString.updateStatus("Failed to connect to the network.")
            }
        })
    }

}