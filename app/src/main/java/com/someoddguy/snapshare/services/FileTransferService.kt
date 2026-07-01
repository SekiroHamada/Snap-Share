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
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FileTransferService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

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
        val initialNotification = buildBasicNotification(title="Preparing transfer...", "Connecting")

        // Android 14+ strict requirement: explicitly state the service type in code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        observeTransferProgress()
        return START_NOT_STICKY
    }

    private fun observeTransferProgress() {
        serviceScope.launch {
            var lastUpdateTime =0L

            combine<Any, TransferState>(
                FileTransferProgress.fileName,
                FileTransferProgress.fileSize,
                FileTransferProgress.fileSizeReceived,
                FileTransferProgress.filesDone,
                FileTransferProgress.totalFiles,
                FileTransferProgress.isReceiving,
                FileTransferProgress.isDone
            ) {args->
                TransferState(
                    name = args[0] as String,
                    size = args[1] as Long,
                    received = args[2] as Long,
                    done = args[3] as Int,
                    total = args[4] as Int,
                    isReceiving = args[5] as Boolean,
                    isDone = args[6] as Boolean
                )
            }.collect { state ->

                if (state.isDone) {
                    // Show a completion notification that sticks around after the service dies
                    val actionText = if (state.isReceiving) "Received" else "Sent"
                    val doneNotification = buildBasicNotification("Transfer Complete", "All files $actionText successfully.")
                        .apply { flags = Notification.FLAG_AUTO_CANCEL } // Allows user to swipe it away

                    notificationManager.notify(NOTIFICATION_ID + 1, doneNotification) // Use a different ID so it doesn't get cleared


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
                val currentProgress = if (state.size > 0) ((state.received.toFloat() / state.size) * 100).toInt() else 0
                val overallProgress = if (state.total > 0) ((state.done.toFloat() / state.total) * 100).toInt() else 0
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastUpdateTime < 500 && currentProgress < 100) {
                    return@collect // Silently ignore the update so we don't spam the OS
                }
                //otherwise send the notification
                lastUpdateTime = currentTime
                // Update the ongoing notification with the custom dual-bar layout
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildCustomNotification(state, overallProgress, currentProgress)
                )
            }
        }
    }

    private fun buildCustomNotification(state: TransferState, overallProgress: Int, currentProgress: Int): Notification {
        val actionText = if (state.isReceiving) "Receiving" else "Sending"

        // Inflate the XML layout we created
        val remoteViews = RemoteViews(packageName, R.layout.notification_transfer_progress)

        // Update the text fields
        remoteViews.setTextViewText(R.id.textOverallProgress, "$actionText: ${state.done}/${state.total} Files")
        remoteViews.setTextViewText(R.id.textCurrentFile, state.name)

        // Update both progress bars
        remoteViews.setProgressBar(R.id.progressOverall, 100, overallProgress, false)
        remoteViews.setProgressBar(R.id.progressCurrent, 100, currentProgress, false)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload) // Replace if you have a custom icon
            .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Tells Android to use your XML
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
    private fun buildBasicNotification(title: String, text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "File Transfers",
                NotificationManager.IMPORTANCE_LOW // LOW is required so it doesn't pop up and ring constantly
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Clean up coroutines to prevent memory leaks
    }

    // Simple data class to hold the combined flow data
    private data class TransferState(
        val name: String, val size: Long, val received: Long,
        val done: Int, val total: Int, val isReceiving: Boolean, val isDone: Boolean
    )
}