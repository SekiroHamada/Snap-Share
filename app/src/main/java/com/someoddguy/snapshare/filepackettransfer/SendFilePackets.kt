package com.someoddguy.snapshare.filepackettransfer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.someoddguy.snapshare.utils.ConnectionValidationString
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
                val outputStream = DataOutputStream(socket.getOutputStream())

                // 1. Tell receiver how many files are coming
                outputStream.writeInt(uris.size)

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

                    ConnectionValidationString.updateStatus("Sending metadata: $fileName")

                    // 2. Send metadata
                    outputStream.writeUTF(fileName)
                    outputStream.writeLong(fileSize)

                    // 3. Stream the file bytes
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val buffer = ByteArray(8192) // 8KB chunks
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                    ConnectionValidationString.updateStatus("Successfully sent: $fileName")
                }

                ConnectionValidationString.updateStatus("All files transfered! Closing stream.")

            } catch (e: Exception) {
                ConnectionValidationString.updateStatus("Transfer Error: ${e.localizedMessage}")
            } finally {
                // Ensure socket is closed after transfer is complete
                socket.close()
                ConnectionValidationString.updateStatus("Socket closed.")
            }
        }
    }
}