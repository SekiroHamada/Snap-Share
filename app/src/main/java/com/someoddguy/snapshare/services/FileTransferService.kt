package com.someoddguy.snapshare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
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
        val notification = buildNotification(title="Preparing transfer...", progress = 0,max = 100)

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

                val actionText = if (state.isReceiving) "Receiving" else "Sending"
                val progressMax = 100
                val currentProgress = if (state.size > 0) ((state.received.toFloat() / state.size) * 100).toInt() else 0

                val title = "$actionText file ${state.done + 1} of ${state.total}"
                val contentText = "${state.name} ($currentProgress%)"

                // Update the ongoing notification
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification(title, contentText, currentProgress, progressMax)
                )
            }
        }
    }

    private fun buildNotification(title: String, contentText: String = "", progress: Int, max: Int = 100) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload) // Replace with your own icon (e.g., R.drawable.ic_launcher_foreground)
            .setProgress(max, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true) // Prevents the phone from buzzing on every single byte transferred
            .build()

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