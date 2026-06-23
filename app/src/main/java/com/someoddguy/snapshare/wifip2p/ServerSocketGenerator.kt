package com.someoddguy.snapshare.wifip2p

import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ServerSocket

object ServerSocketGenerator {

    fun startServer(port: Int = 7878) {
        CoroutineScope(Dispatchers.IO).launch {
            val serverSocket = ServerSocket(port)
            showToast("Server waiting on port $port",true)
            BleGattConnectionHandler.changeWifiCredential("ServerSocket")

            val client = serverSocket.accept() // blocks until client connects
            showToast("Client connected: ${client.inetAddress}",true)
            /*TODO read incoming data*/
            //handleClient(client)
            serverSocket.close()
        }
    }
}