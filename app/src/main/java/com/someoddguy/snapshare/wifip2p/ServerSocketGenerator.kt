package com.someoddguy.snapshare.wifip2p

import com.someoddguy.snapshare.filepackettransfer.ReceiveFilePackets
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ServerSocket

object ServerSocketGenerator {

    fun startServer( port: Int = 7878) {
        CoroutineScope(Dispatchers.IO).launch {

            ServerSocket(port).use { serverSocket ->
                ConnectionValidationString.updateStatus("Server waiting on port $port...")
                BleGattConnectionHandler.changeWifiCredential("ServerSocket")
                val client = serverSocket.accept()
                ConnectionValidationString.updateStatus("Client connected: ${client.inetAddress}")

                ReceiveFilePackets.receiveFilesOverSocket(client)
                //socket will be closed in the ReceiveFilePackets

                //after this is done serverSocket.close() will be called automatically
            }

        }
    }
}