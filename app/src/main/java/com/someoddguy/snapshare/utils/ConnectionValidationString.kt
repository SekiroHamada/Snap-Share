package com.someoddguy.snapshare.utils

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
        CoroutineScope(Dispatchers.Main).launch {
            delay(100L)
            _statusString.value=status
        }

    }
}