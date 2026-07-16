package com.someoddguy.snapshare.ui.searchbluetoothusers

import android.app.Application
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchBluetoothViewModel(application: Application) : AndroidViewModel(application) {

    val isScanning: StateFlow<Boolean> = SearchBluetoothUsers.isScanning
    val scanResults: StateFlow<List<ScanResult>> = SearchBluetoothUsers.scanResults

    fun startBleScan() {
        SearchBluetoothUsers.startBleScan(getApplication())
    }

    fun stopBleScan() {
        SearchBluetoothUsers.stopBleScan()
    }

    fun startConnection(result: ScanResult) {
        if (isScanning.value) {
            stopBleScan()
        }
        val context = GlobalContext.appContext
        SearchBluetoothUsers.clearResults()
        BleGattConnector.startConnection(context, result)
    }

    private val _startStatus = MutableStateFlow(false)
    val startStatus: StateFlow<Boolean> = _startStatus.asStateFlow()

    init {
        viewModelScope.launch {
            ConnectionValidationString.start.collect { newStatus ->
                _startStatus.value = newStatus
            }
        }
    }
}