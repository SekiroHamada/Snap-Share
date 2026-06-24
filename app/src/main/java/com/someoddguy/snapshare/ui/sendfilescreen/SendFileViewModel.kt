package com.someoddguy.snapshare.ui.sendfilescreen

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.someoddguy.snapshare.filepackets.SendFilePackets
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SendFileViewModel : ViewModel() {
    // External immutable state for the UI to observe
    val selectedFileUris: StateFlow<List<Uri>> = SendFilePackets.selectedFileUris.asStateFlow()

    fun addFiles(uris: List<Uri>) {
        SendFilePackets.handleFilesSelected(uris)
    }
    fun removeSelectedFile(uriToRemove: Uri) {
        SendFilePackets.removeFile(uriToRemove)
    }
}