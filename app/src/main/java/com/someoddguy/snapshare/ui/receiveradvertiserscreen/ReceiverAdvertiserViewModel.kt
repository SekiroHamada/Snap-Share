package com.someoddguy.snapshare.ui.receiveradvertiserscreen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiverAdvertiserViewModel : ViewModel() {

    // Observe the state from the Singleton object instead
    val isAdvertising: StateFlow<Boolean> = ReceiverAdvertiser.isAdvertising

    /*TODO check code*/
    //for Prompt Window
    var showConnectionDialog by mutableStateOf(false)
        private set

    // State to hold the incoming device address for the UI
    var connectingDeviceAddress by mutableStateOf("")
        private set

    // Temporary variables to hold the callbacks from the BLE Handler
    private var pendingKeepAction: (() -> Unit)? = null
    private var pendingRemoveAction: (() -> Unit)? = null

    init {
        BleGattConnectionHandler.onConnectionPromptRequested = { address, onKeep, onRemove ->
            // Update state to trigger the Compose dialog
            connectingDeviceAddress = address
            pendingKeepAction = onKeep
            pendingRemoveAction = onRemove
            showConnectionDialog = true
        }
    }

    fun onKeepClicked() {
        pendingKeepAction?.invoke()
        clearDialogState()
    }

    fun onRemoveClicked() {
        pendingRemoveAction?.invoke()
        clearDialogState()
    }

    private fun clearDialogState() {
        showConnectionDialog = false
        pendingKeepAction = null
        pendingRemoveAction = null
        connectingDeviceAddress = ""
    }

    override fun onCleared() {
        super.onCleared()
        // Prevent memory leaks if the ViewModel is destroyed
        BleGattConnectionHandler.onConnectionPromptRequested = null
    }
    /*TODO end*/

    // Proxy methods so the UI can still call the ViewModel to start/stop
    fun startAdvertising(context: Context) {
        ReceiverAdvertiser.startAdvertising(context)
    }

    fun stopAdvertising(context: Context) {
        ReceiverAdvertiser.stopAdvertising(context)
    }

    //TODO added status check for Connection validation
    //for starting connection
    private val _startStatus = MutableStateFlow(false)
    val startStatus : StateFlow<Boolean> = _startStatus.asStateFlow()

    init {
        viewModelScope.launch {
            ConnectionValidationString.start.collect { newStatus ->
                _startStatus.value = newStatus
            }
        }
    }
}