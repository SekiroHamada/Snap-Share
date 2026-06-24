package com.someoddguy.snapshare.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ConnectionValidationString {
    private val _start = MutableStateFlow(false)
    val start : StateFlow<Boolean> = _start.asStateFlow()
    fun updateStart(bool: Boolean){
        _start.value = bool
    }
    private val _statusString = MutableStateFlow("")

    val statusString: StateFlow<String> = _statusString.asStateFlow()

    fun updateStatus(status:String){
        _statusString.value=status
    }
}