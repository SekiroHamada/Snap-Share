package com.someoddguy.snapshare.ui.connectionvalidationscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionValidationViewModel: ViewModel() {
    private val _myViewModelString = MutableStateFlow("Waiting...")
    val myViewModelString : StateFlow<String> = _myViewModelString.asStateFlow()

    private val _initiateTransfer = MutableStateFlow(false)
    val initiateTransfer: StateFlow<Boolean> = _initiateTransfer.asStateFlow()
    init{
        viewModelScope.launch {
            ConnectionValidationString.statusString.collect{newString ->
                _myViewModelString.value = newString
            }
        }
        viewModelScope.launch {
            ConnectionValidationString.initiateTransfer.collect{initiate ->
                _initiateTransfer.value = initiate
            }
        }
    }

}