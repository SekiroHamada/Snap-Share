package com.someoddguy.snapshare.ui.searchbluetoothusers // Make sure this matches your folder!

import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchBluetoothViewModel : ViewModel() {

    // Holds the reactive state of our scanned devices
    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()

    fun addOrUpdateDevice(result: ScanResult) {
        _scanResults.update { currentList ->
            val mutableList = currentList.toMutableList()
            val indexQuery = mutableList.indexOfFirst { it.device.address == result.device.address }

            if (indexQuery != -1) {
                // Device exists, update its signal strength (RSSI) or data
                mutableList[indexQuery] = result
            } else {
                // New device found, add to the list
                mutableList.add(result)
            }
            // Returning the new list triggers the StateFlow to emit, updating Compose
            mutableList
        }
    }

    fun clearResults() {
        _scanResults.value = emptyList()
    }
}