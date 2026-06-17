package com.someoddguy.snapshare.ui.searchbluetoothusers


import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// Changed to AndroidViewModel so we have safe access to the application context
// to get the Bluetooth System Service.
class SearchBluetoothViewModel(application: Application) : AndroidViewModel(application) {

    //for checking if app is scanning or not
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    //stores the nearby devices
    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()


    //for active Gatt Connections
    private var activeGatt: BluetoothGatt? = null

    // Lazily instantiate the Bluetooth Adapter using the Application Context
    private val bluetoothAdapter by lazy {
        val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    /*TODO change the UUID for target, also change it in the ReceiveFileViewModel as well*/
    /*TODO CHANGE THE UUID AND SAVE IT SOMEWHERE SAFE*/
    val targetServiceUuid = ParcelUuid(UUID.fromString("b8e1b517-97c9-464a-b8ff-60647e8cce2a"))
    val scanFilter = ScanFilter.Builder()
        .setServiceUuid(targetServiceUuid)
        .build()
    val filters = listOf(scanFilter)



    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addOrUpdateDevice(result)
            Log.i("ScanCallback", "Found BLE device! address: ${result.device.address}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
            _isScanning.value=false
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

    // Since the UI now handles the permission requests before this is called,
    // we can suppress the MissingPermission warning here.
    @SuppressLint("MissingPermission")
    fun startBleScan() {
        // Clear previous results when starting a new scan
        clearResults()
        bleScanner?.startScan(filters, scanSettings, scanCallback)
        _isScanning.value=true
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        bleScanner?.stopScan(scanCallback)
        _isScanning.value=false
    }

    fun startConnection(result: ScanResult){
        if(_isScanning.value){
            stopBleScan()
            _isScanning.value = false
        }
        val context=getApplication<Application>()
        BleGattConnection.startConnection(context,result)


    }
}