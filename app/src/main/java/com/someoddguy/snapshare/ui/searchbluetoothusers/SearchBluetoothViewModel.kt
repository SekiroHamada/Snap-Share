package com.someoddguy.snapshare.ui.searchbluetoothusers

import android.app.Application
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.someoddguy.snapshare.utils.ConnectionValidationString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchBluetoothViewModel(application: Application) : AndroidViewModel(application) {

    // Directly expose the flows from the Singleton for the UI to observe
    val isScanning: StateFlow<Boolean> = SearchBluetoothUsers.isScanning
    val scanResults: StateFlow<List<ScanResult>> = SearchBluetoothUsers.scanResults

    fun clearResults() {
        SearchBluetoothUsers.clearResults()
    }

    fun startBleScan() {
        // Pass the application context down to the singleton
        SearchBluetoothUsers.startBleScan(getApplication())
    }

    fun stopBleScan() {
        SearchBluetoothUsers.stopBleScan()
    }

    fun startConnection(result: ScanResult) {
        if (isScanning.value) {
            stopBleScan()
        }
        val context = getApplication<Application>()
        BleGattConnector.startConnection(context, result)
    }

    // Connection validation state is kept in the ViewModel as it's UI specific
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