package com.someoddguy.snapshare.wifip2p

import android.net.Network
import com.someoddguy.snapshare.utils.showToast
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
                showToast("Success Socket Connection!", true)
                /*TODO start sending files*/
                //sendFile(socket, fileUri)
                //socket.close()
            } catch (e: ConnectException) {
                showToast(
                    "Specific Failure: The IP is reachable, but nothing is listening on that port.",
                    true
                )
                // (e.g., The ServerSocket on the other device hasn't started yet).
            } catch (e: UnknownHostException) {
                showToast(
                    "Specific Failure: The groupOwnerIP is invalid or completely unreachable.",
                    true
                )
            } catch (e: IOException) {
                showToast(
                    "General Failure: The connection timed out, or the Wi-Fi Direct group collapsed.",
                    true
                )
            }catch(e: Exception){
                showToast(
                    "Some Exception Occured!",
                    true
                )
            }
        }


    }
}