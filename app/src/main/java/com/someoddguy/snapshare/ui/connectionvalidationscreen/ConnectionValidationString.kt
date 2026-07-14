package com.someoddguy.snapshare.ui.connectionvalidationscreen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ConnectionValidationString {

    // tells if validation started
    private val _start = MutableStateFlow(false)
    val start : StateFlow<Boolean> = _start.asStateFlow()
    fun updateStart(bool: Boolean){
        _start.value = bool
    }

    // tells current connection status
    private val _statusString = MutableStateFlow("")
    val statusString: StateFlow<String> = _statusString.asStateFlow()
    fun updateStatus(status:String){
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000L)
            _statusString.value = status
        }
    }

    // tells if transfer should be initiated
    private val _initiateTransfer = MutableStateFlow(false)
    val initiateTransfer: StateFlow<Boolean> = _initiateTransfer.asStateFlow()
    fun updateInitiateTransfer(bool: Boolean){
        _initiateTransfer.value = bool
    }

    private val _cancel = MutableStateFlow(false)
    val cancel : StateFlow<Boolean> = _cancel.asStateFlow()
    fun updateCancelStatus(bool : Boolean){
        _cancel.value = bool
    }

    private val _isButtonClicked = MutableStateFlow(false)
    val isButtonClicked : StateFlow<Boolean> = _isButtonClicked.asStateFlow()
    fun updateButtonClick(bool: Boolean){
        _isButtonClicked.value = bool
    }

    fun resetValidation() {
        updateStart(false)
        _statusString.value = ""
        updateInitiateTransfer(false)
        updateCancelStatus(false)
        updateButtonClick(false)
    }
}