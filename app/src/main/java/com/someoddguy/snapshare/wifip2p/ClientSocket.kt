package com.someoddguy.snapshare.wifip2p

import android.net.Network
import com.someoddguy.snapshare.filepackets.SendFilePackets
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.utils.ConnectionValidationString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

object ClientSocket {
    fun startSocketClient(
        network: Network,
        groupOwnerIP: String,
        /*TODO change the port*/
        port: Int=7878){
        //showToast("Socket connection initialization",true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = network.socketFactory.createSocket(groupOwnerIP, port)
                ConnectionValidationString.updateStatus("Socket Connection Successful!")
                /*TODO start sending files*/
                val context = GlobalContext.appContext
                SendFilePackets.sendFilesOverSocket(context, socket)
                //socket will be closed in the SendFilePackets
            } catch (e: ConnectException) {
                ConnectionValidationString.updateStatus("Specific Failure: The IP is reachable, but nothing is listening on that port.")
                delay(1000L)
                ConnectionValidationString.updateStatus("Retrying Connection...")
                // (e.g., The ServerSocket on the other device hasn't started yet).
            } catch (e: UnknownHostException) {
                ConnectionValidationString.updateStatus("Specific Failure: The groupOwnerIP is invalid or completely unreachable.")

            } catch (e: IOException) {
                ConnectionValidationString.updateStatus("General Failure: The connection timed out, or the Wi-Fi Direct group collapsed.")

            }catch(e: Exception){
                ConnectionValidationString.updateStatus("Some Exception Occured!")
            }
        }


    }
}