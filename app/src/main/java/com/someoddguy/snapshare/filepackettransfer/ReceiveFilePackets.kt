package com.someoddguy.snapshare.filepackettransfer

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import com.someoddguy.snapshare.utils.CustomException
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.IOException
import java.net.Socket

object ReceiveFilePackets {
    var lastProgressUpdateTime = System.currentTimeMillis()

    var activeSocket :Socket? = null

    suspend fun receiveFilesOverSocket(socket: Socket) {
        val context = GlobalContext.appContext
        withContext(Dispatchers.IO) {
            try {
                socket.soTimeout = 15000
                FileTransferProgress.updateProgress(false)
                ConnectionValidationString.updateStatus("Listening for incoming files...")

                FileTransferProgress.updateIsReceiving(true)
                ConnectionValidationString.updateInitiateTransfer(true)

                val inputStream = DataInputStream(socket.getInputStream())

                // Read how many files are coming
                val fileCount = inputStream.readInt()
                FileTransferProgress.updateTotalFiles(fileCount)

                for (i in 0 until fileCount) {
                    if(!socket.isConnected || socket.isClosed){
                        throw CustomException("Socket Disconnected")
                    }

                    val fileName = inputStream.readUTF()
                    val fileSize = inputStream.readLong()

                    FileTransferProgress.updateFileName(fileName)
                    FileTransferProgress.updateFileSize(fileSize)

                    // Prepare MediaStore to save the file into Downloads/SnapShare
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SnapShare")
                        }
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            val buffer = ByteArray(262144) // 8KB chunks
                            var totalRead = 0L
                            FileTransferProgress.updateFileSizeReceived(0L)
                            while (totalRead < fileSize) {
                                if(!socket.isConnected || socket.isClosed){
                                    throw CustomException("Socket Disconnected")
                                }
                                // Calculate remaining bytes to ensure we don't bleed into the next file's data
                                val remainingBytes = fileSize - totalRead
                                val bytesToRead = minOf(buffer.size.toLong(), remainingBytes).toInt()

                                val bytesRead = inputStream.read(buffer, 0, bytesToRead)
                                if (bytesRead == -1) {
                                    throw IOException("Connection Broken")
                                }

                                outputStream.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                val currentTime = System.currentTimeMillis()
                                if(currentTime - lastProgressUpdateTime > 50 ){
                                    FileTransferProgress.updateFileSizeReceived(totalRead)
                                    lastProgressUpdateTime = currentTime
                                }
                            }
                            outputStream.flush()
                        }
                        ConnectionValidationString.updateStatus("Saved: $fileName in Downloads")
                        FileTransferProgress.updateFilesDone()
                    } else {
                        ConnectionValidationString.updateStatus("Failed to create file entry for: $fileName")
                    }
                }
                FileTransferProgress.updateProgress(true)
                ConnectionValidationString.updateStatus("All files received successfully!")

            } catch (e: Exception) {
                showToast("Socket Connection Interrupted. Rolling Back...",true)
            } finally {
                socket.close()
            }
        }
    }

    fun cancelTransfer(){
        runCatching {
            activeSocket?.close()
        }
        activeSocket = null
    }
}