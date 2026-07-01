package com.someoddguy.snapshare.wifip2p

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.services.FileTransferService
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString

object WifiP2PClient {

    var SSID : String = ""
    var PASS : String = ""
    val GO_IP : String = "192.168.49.1"


    // Hold the callback reference to unregister it later
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // Helper property to get the ConnectivityManager safely using GlobalContext
    private val connectivityManager: ConnectivityManager
        get() = GlobalContext.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun saveWifiCredentials(ssid: String, pass: String) {
        SSID = ssid
        PASS = pass
    }
    fun connectToGroupOwner() {

        // 1. Specify the network credentials received via BLE
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(SSID)
            .setWpa2Passphrase(PASS)
            .build()

        // Build the network request
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // Crucial: Tells Android this is a local P2P network, not for internet access
            .setNetworkSpecifier(specifier)
            .build()

        ConnectionValidationString.updateStatus("Joining Wi-Fi Network : $SSID")

        //for notification as well as foreground process
        FileTransferService.startService(GlobalContext.appContext)

        // Request the connection
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                ConnectionValidationString.updateStatus("Successfully connected to Group Owner!")

                connectivityManager.bindProcessToNetwork(network)
                ConnectionValidationString.updateStatus("Starting Socket with Network : $SSID")

                ClientSocket.startSocketClient(network, GO_IP)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                ConnectionValidationString.updateStatus("Failed to connect to the network.")
            }
        }
        networkCallback?.let {
            connectivityManager.requestNetwork(request, it)
        }
    }


    fun killAllWifiClientConnections(){
        try{
            connectivityManager.bindProcessToNetwork(null)

        }catch(e: Exception){
            //Failsafe
        }
        networkCallback?.let {callback ->
            try{
                connectivityManager.unregisterNetworkCallback(callback)
            }catch(e: IllegalArgumentException){
                //already unregistered
            }
            networkCallback =null
        }
    }

}