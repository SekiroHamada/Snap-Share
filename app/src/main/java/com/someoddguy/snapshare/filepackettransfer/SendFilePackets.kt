package com.someoddguy.snapshare.filepackettransfer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.Socket

object SendFilePackets {
    private val _selectedFileUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedFileUris: StateFlow<List<Uri>> = _selectedFileUris.asStateFlow()

    fun handleSelectedFiles(uris: List<Uri>) {
        _selectedFileUris.update { currentList ->
            if (uris.isNotEmpty()) {
                (currentList + uris).distinct()
            } else {
                currentList
            }
        }
    }

    fun isSelectedFilesEmpty(): Boolean{
        if(_selectedFileUris.value.isEmpty()){
            return true
        }else{
            return false
        }
    }

    fun removeFile(uri: Uri) {
        _selectedFileUris.update { currentList ->
            currentList.filter { it != uri }
        }
    }

    fun clearFiles() {
        _selectedFileUris.value = emptyList()
    }



    suspend fun sendFilesOverSocket(context: Context, socket: Socket) {
        withContext(Dispatchers.IO) {
            try {
                val uris = _selectedFileUris.value
                if (uris.isEmpty()) {
                    ConnectionValidationString.updateStatus("No files selected to send.")
                    return@withContext
                }

                ConnectionValidationString.updateStatus("Preparing to send ${uris.size} file(s)...")
                ConnectionValidationString.updateInitiateTransfer()

                val outputStream = DataOutputStream(socket.getOutputStream())

                // Tell receiver how many files are coming
                outputStream.writeInt(uris.size)

                //Send it to the object
                FileTransferProgress.updateTotalFiles(uris.size)

                for (uri in uris) {
                    var fileName = "SnapShare_File_${System.currentTimeMillis()}"
                    var fileSize = 0L

                    // Extract exact File Name and Size using ContentResolver
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                            if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                    FileTransferProgress.updateFileName(fileName)
                    FileTransferProgress.updateFileSize(fileSize)

                    // Send metadata
                    outputStream.writeUTF(fileName)
                    outputStream.writeLong(fileSize)

                    var bytesSent=0L
                    FileTransferProgress.updateFileSizeReceived(0L)
                    // Stream the file bytes
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val buffer = ByteArray(8192) // 8KB chunks
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            //file size sent
                            bytesSent += bytesRead
                            //sent it to the object
                            FileTransferProgress.updateFileSizeReceived(bytesSent)
                        }
                        outputStream.flush()
                    }
                    ConnectionValidationString.updateStatus("Successfully sent: $fileName")
                    FileTransferProgress.updateFilesDone()
                }

                FileTransferProgress.updateProgress(true)

            } catch (e: Exception) {
                ConnectionValidationString.updateStatus("Transfer Error: ${e.localizedMessage}")
            } finally {
                // Ensure socket is closed after transfer is complete
                socket.close()
            }
        }
    }
}