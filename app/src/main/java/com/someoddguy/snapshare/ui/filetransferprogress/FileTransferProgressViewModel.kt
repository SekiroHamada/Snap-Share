package com.someoddguy.snapshare.ui.filetransferprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TransferUiState(
    val totalFiles: Int = 0,
    val filesDone: Int = 0,
    val isReceiving: Boolean = true,
    val fileName: String = "",
    val fileSize: Long = 0L,
    val fileSizeReceived: Long = 0L,
    val isDone: Boolean = false
)

class FileTransferProgressViewModel: ViewModel() {

    //added <Any, TransferUiState>
    val uiState: StateFlow<TransferUiState> = combine<Any, TransferUiState>(
        FileTransferProgress.totalFiles,
        FileTransferProgress.filesDone,
        FileTransferProgress.isReceiving,
        FileTransferProgress.fileName,
        FileTransferProgress.fileSize,
        FileTransferProgress.fileSizeReceived,
        FileTransferProgress.isDone

    ) { args ->
        // Cast the array elements by index to their respective types
        TransferUiState(
            totalFiles = args[0] as Int,
            filesDone = args[1] as Int,
            isReceiving = args[2] as Boolean,
            fileName = args[3] as String,
            fileSize = args[4] as Long,
            fileSizeReceived = args[5] as Long,
            isDone = args[6] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransferUiState()
    )
}