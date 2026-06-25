package com.someoddguy.snapshare.ui.searchbluetoothusers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.someoddguy.snapshare.ble.BleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SearchBluetoothUsers {

    // Global state for checking if app is scanning or not
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Global state storing the nearby devices
    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()

    private var bleScanner: BluetoothLeScanner? = null

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val targetServiceUuid = ParcelUuid(BleConfig.APP_SERVICE_UUID)
    private val scanFilter = ScanFilter.Builder()
        .setServiceUuid(targetServiceUuid)
        .build()
    private val filters = listOf(scanFilter)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addOrUpdateDevice(result)
            Log.i("ScanCallback", "Found BLE device! address: ${result.device.address}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
            _isScanning.value = false
        }
    }

    private fun addOrUpdateDevice(result: ScanResult) {
        _scanResults.update { currentList ->
            val mutableList = currentList.toMutableList()
            val indexQuery = mutableList.indexOfFirst { it.device.address == result.device.address }

            if (indexQuery != -1) {
                mutableList[indexQuery] = result
            } else {
                mutableList.add(result)
            }
            mutableList
        }
    }

    fun clearResults() {
        _scanResults.value = emptyList()
    }

    @SuppressLint("MissingPermission")
    fun startBleScan(context: Context) {
        // Initialize scanner if it hasn't been created yet
        if (bleScanner == null) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bleScanner = bluetoothManager.adapter.bluetoothLeScanner
        }

        clearResults()
        bleScanner?.startScan(filters, scanSettings, scanCallback)
        _isScanning.value = true
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        bleScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }
}