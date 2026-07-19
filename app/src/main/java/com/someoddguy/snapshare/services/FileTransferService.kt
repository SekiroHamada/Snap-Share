package com.someoddguy.snapshare.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FileTransferService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    //TODO
    private val notificationManager = (GlobalContext.appContext).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "file_transfer_channel"
        private const val NOTIFICATION_ID = 1

        // Helper to easily start the service from anywhere
        fun startService(context: Context) {
            val intent = Intent(context, FileTransferService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(GlobalContext.appContext,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_file)
            .setContentTitle("File Transfer")
            .setContentText("Starting File Transfer")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()


        // Android 14+ strict requirement: explicitly state the service type in code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        observeTransferProgress()
        return START_NOT_STICKY
    }

    private fun observeTransferProgress() {
        serviceScope.launch {
            var lastUpdateTime =0L

            combine<Any, TransferState>(
                FileTransferProgress.fileName,
                FileTransferProgress.filesDone,
                FileTransferProgress.totalFiles,
                FileTransferProgress.isReceiving,
                FileTransferProgress.isDone
            ) {args->
                TransferState(
                    name = args[0] as String,
                    done = args[1] as Int,
                    total = args[2] as Int,
                    isReceiving = args[3] as Boolean,
                    isDone = args[4] as Boolean

                )
            }.collect { state ->

                if (state.isDone) {
                    // Show a completion notification that sticks around after the service dies
                    val isReceiving = if (state.isReceiving) "Received" else "Sent"

                    val notification = NotificationCompat.Builder(GlobalContext.appContext,CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_file)
                        .setContentTitle("Transfer Complete")
                        .setContentText("All files ${isReceiving} successfully!")
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(NOTIFICATION_ID, notification)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        // For older devices, safely use the legacy boolean method
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }

                    stopSelf() // Kills the service when transfer is complete
                    return@collect
                }

                // Calculate both progress values (0 to 100)
                val overallProgress = if (state.total > 0) ((state.done.toFloat() / state.total.toFloat()) * 100).toInt() else 0
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastUpdateTime < 500 && overallProgress < 100) {
                    return@collect // Silently ignore the update so we don't spam the OS
                }
                //otherwise send the notification
                lastUpdateTime = currentTime

                val isReceiving = if(state.isReceiving) "Receiving" else "Sending"
                val progressNotification = NotificationCompat.Builder(GlobalContext.appContext,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_file)
                    .setContentTitle("Transfer in progress")
                    .setContentText("$isReceiving File(s)")
                    .setAutoCancel(true)
                    .setProgress(state.total, state.done, false)
                    .build()
                notificationManager.notify(NOTIFICATION_ID,progressNotification)
            }
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "File Transfer",
                NotificationManager.IMPORTANCE_LOW // LOW is required so it doesn't pop up and ring constantly
            ).apply {
                description = "Channel for basic transfer notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Clean up coroutines to prevent memory leaks
    }

    private data class TransferState(
        val name: String, val done: Int, val total: Int,
        val isReceiving: Boolean, val isDone: Boolean
    )
}