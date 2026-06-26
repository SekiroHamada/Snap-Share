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
    init{
        viewModelScope.launch {
            ConnectionValidationString.statusString.collect{newString ->
                _myViewModelString.value = newString
            }
        }
    }

}