package com.someoddguy.snapshare.ui.connectionvalidationscreen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ConnectionValidationString {
    private val _start = MutableStateFlow(false)
    val start : StateFlow<Boolean> = _start.asStateFlow()
    fun updateStart(bool: Boolean){
        _start.value = bool
    }
    private val _statusString = MutableStateFlow("")

    val statusString: StateFlow<String> = _statusString.asStateFlow()

    fun updateStatus(status:String){
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000L)
            _statusString.value=status
        }

    }

    private val _initiateTransfer = MutableStateFlow(false)
    val initiateTransfer: StateFlow<Boolean> = _initiateTransfer.asStateFlow()

    fun updateInitiateTransfer(bool: Boolean){
        _initiateTransfer.value = bool
    }
}