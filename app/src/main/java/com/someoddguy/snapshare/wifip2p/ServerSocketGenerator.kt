package com.someoddguy.snapshare.wifip2p

import android.content.Context
import com.someoddguy.snapshare.filepackets.ReceiveFilePackets
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.utils.ConnectionValidationString
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ServerSocket

object ServerSocketGenerator {

    fun startServer( port: Int = 7878) {
        CoroutineScope(Dispatchers.IO).launch {
            val serverSocket = ServerSocket(port)
            ConnectionValidationString.updateStatus("Server waiting on port $port...")
            BleGattConnectionHandler.changeWifiCredential("ServerSocket")

            val client = serverSocket.accept() // blocks until client connects
            ConnectionValidationString.updateStatus("Client connected: ${client.inetAddress}")
            /*TODO read incoming data*/
            val context = GlobalContext.appContext
            ReceiveFilePackets.receiveFilesOverSocket(context, client)
            //socket will be closed in the ReceiveFilePackets
        }
    }
}