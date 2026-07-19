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
        const val CHANNEL_ID = "file_transfer_channel"
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
        val status = if(FileTransferProgress.isReceiving.value) "Receiving" else "Sending"
        val notification = NotificationCompat.Builder(GlobalContext.appContext,CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("SnapShare")
            .setContentText("$status Files")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
        return START_NOT_STICKY
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