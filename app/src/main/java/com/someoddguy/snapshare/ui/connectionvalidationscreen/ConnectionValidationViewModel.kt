package com.someoddguy.snapshare.ui.connectionvalidationscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ValidationUiState(
    val start: Boolean = false,
    val statusString: String = "Waiting...",
    val initiateTransfer: Boolean = false,
    val cancel: Boolean = false,
    val isReceiving : Boolean = false,
    val isButtonClicked : Boolean = false
)

class ConnectionValidationViewModel: ViewModel() {

    val uiState: StateFlow<ValidationUiState> = combine<Any, ValidationUiState>(
        ConnectionValidationString.start,
        ConnectionValidationString.statusString,
        ConnectionValidationString.initiateTransfer,
        ConnectionValidationString.cancel,
        ConnectionValidationString.isButtonClicked,
        FileTransferProgress.isReceiving,

    ) { args ->
        ValidationUiState(
            start = args[0] as Boolean,
           statusString = (args[1] as String).ifEmpty { "Waiting..." },
            initiateTransfer = args[2] as Boolean,
            cancel = args[3] as Boolean,
            isButtonClicked = args[4] as Boolean,
            isReceiving = args[5] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ValidationUiState()
    )
}