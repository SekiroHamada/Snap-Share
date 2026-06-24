package com.someoddguy.snapshare.filepackets

import kotlinx.coroutines.flow.MutableStateFlow
import android.net.Uri
import kotlinx.coroutines.flow.update
import kotlin.collections.distinct
import kotlin.collections.plus


object SendFilePackets {
    val selectedFileUris = MutableStateFlow<List<Uri>>(emptyList())


    fun handleFilesSelected(uris: List<Uri>) {
        selectedFileUris.update { currentList ->
            if (uris.isNotEmpty()) {
                (currentList + uris).distinct()
            } else {
                currentList
            }
        }
    }
    fun removeFile(uri: Uri) {
        selectedFileUris.update { currentList ->
            currentList.filter { it != uri }
        }
    }
}