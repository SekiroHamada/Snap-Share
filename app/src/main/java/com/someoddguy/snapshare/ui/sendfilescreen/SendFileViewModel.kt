package com.someoddguy.snapshare.ui.sendfilescreen

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
import kotlinx.coroutines.flow.StateFlow

class SendFileViewModel : ViewModel() {

    val selectedFileUris: StateFlow<List<Uri>> = SendFilePackets.selectedFileUris

    fun addFiles(uris: List<Uri>) {
        SendFilePackets.handleSelectedFiles(uris)
    }

    fun removeSelectedFile(uriToRemove: Uri) {
        SendFilePackets.removeFile(uriToRemove)
    }
}