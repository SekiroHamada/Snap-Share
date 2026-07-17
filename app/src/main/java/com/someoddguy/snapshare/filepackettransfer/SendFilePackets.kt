package com.someoddguy.snapshare.filepackettransfer

import android.net.Uri
import android.provider.OpenableColumns
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import com.someoddguy.snapshare.utils.CustomException
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.Socket

object SendFilePackets {

    var activeSocket:Socket? = null
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


    var lastProgressUpdateTime = System.currentTimeMillis()
    suspend fun sendFilesOverSocket(socket: Socket) {
        val context = GlobalContext.appContext
        withContext(Dispatchers.IO) {
            try {
                activeSocket = socket
                socket.soTimeout = 15000

                FileTransferProgress.updateProgress(false)

                val uris = _selectedFileUris.value
                if (uris.isEmpty()) {
                    ConnectionValidationString.updateStatus("No files selected to send.")
                    return@withContext
                }

                ConnectionValidationString.updateStatus("Preparing to send ${uris.size} file(s)...")
                ConnectionValidationString.updateInitiateTransfer(true)

                val outputStream = DataOutputStream(socket.getOutputStream())

                // Tell receiver how many files are coming
                outputStream.writeInt(uris.size)

                //Send it to the object
                FileTransferProgress.updateTotalFiles(uris.size)

                for (uri in uris) {
                    if(!socket.isConnected || socket.isClosed){
                        throw CustomException("Socket Disconnected")
                    }
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
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val buffer = ByteArray(262144)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {

                            if(!socket.isConnected  || socket.isClosed){
                                throw CustomException("Socket Disconnected")
                            }

                            outputStream.write(buffer, 0, bytesRead)
                            //TODO
                            //outputStream.flush()
                            bytesSent += bytesRead
                            val currentTime = System.currentTimeMillis()
                            if(currentTime - lastProgressUpdateTime > 50 ){
                                FileTransferProgress.updateFileSizeReceived(bytesSent)
                                lastProgressUpdateTime = currentTime
                            }
                        }
                        outputStream.flush()
                    }
                    ConnectionValidationString.updateStatus("Successfully sent: $fileName")
                    FileTransferProgress.updateFilesDone()
                }

                FileTransferProgress.updateProgress(true)

            } catch (e: Exception) {
                showToast("Connection Dropped. Rollin Back",true)
            } finally {
                socket.close()

            }
        }
    }
    fun cancelTransfer(){
        FileTransferProgress.updateCancelTransfer(true)
        runCatching {
            activeSocket?.close()
        }
        activeSocket = null
    }
}