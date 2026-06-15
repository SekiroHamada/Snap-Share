package com.someoddguy.snapshare.ui.sendfilescreen

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SendFileViewModel : ViewModel() {

    // Internal mutable state
    private val _selectedFileUris = MutableStateFlow<List<Uri>>(emptyList())

    // External immutable state for the UI to observe
    val selectedFileUris: StateFlow<List<Uri>> = _selectedFileUris.asStateFlow()

    fun handleFilesSelected(uris: List<Uri>) {
        _selectedFileUris.update { currentList ->
            if (uris.isNotEmpty()) {
                (currentList + uris).distinct()
            } else {
                uris
                // Note: If the user opens the picker and cancels, 'uris' will be empty,
                // which clears the current list. If you want to keep previously selected
                // files when the user cancels, change this 'else' block to return 'currentList'.
            }
        }
    }

    fun removeFile(uriToRemove: Uri) {
        _selectedFileUris.update { currentList ->
            currentList.filter { it != uriToRemove }
        }
    }
}