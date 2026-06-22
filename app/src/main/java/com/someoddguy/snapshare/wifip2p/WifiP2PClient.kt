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

object WifiP2PClient {

    fun connectToGroupOwner(context: Context, ssid: String, pass: String) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 1. Specify the network credentials received via BLE
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass)
            .build()

        // 2. Build the network request
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // Crucial: Tells Android this is a local P2P network, not for internet access
            .setNetworkSpecifier(specifier)
            .build()

        showToast("Joining Wi-Fi Network: $ssid", short = true)

        // 3. Request the connection
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d("WifiP2P", "Successfully connected to Group Owner!")

                // Bind the process to this network so your subsequent Socket connections route through the P2P WiFi and not mobile data.
                connectivityManager.bindProcessToNetwork(network)

                // TODO: Start your Socket client to send files here
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.e("WifiP2P", "Failed to connect to the network.")
            }
        })
    }
}