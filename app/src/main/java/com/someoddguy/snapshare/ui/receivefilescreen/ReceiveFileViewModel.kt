package com.someoddguy.snapshare.ui.receivefilescreen


import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.someoddguy.snapshare.ble.BleConfig
import com.someoddguy.snapshare.utils.ConnectionValidationString
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiveFileViewModel : ViewModel() {

    // Backing property to avoid state updates from outside the ViewModel
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private var advertiseCallback: AdvertiseCallback? = null

    /*TODO check code*/
    //for Prompt Window
    var showConnectionDialog by mutableStateOf(false)
        private set

    // State to hold the incoming device address for the UI
    var connectingDeviceAddress by mutableStateOf("")
        private set

    // Temporary variables to hold the callbacks from the BLE Handler
    private var pendingKeepAction: (() -> Unit)? = null
    private var pendingRemoveAction: (() -> Unit)? = null

    init {
        BleGattConnectionHandler.onConnectionPromptRequested = { address, onKeep, onRemove ->
            // Update state to trigger the Compose dialog
            connectingDeviceAddress = address
            pendingKeepAction = onKeep
            pendingRemoveAction = onRemove
            showConnectionDialog = true
        }
    }

    fun onKeepClicked() {
        pendingKeepAction?.invoke()
        clearDialogState()
    }

    fun onRemoveClicked() {
        pendingRemoveAction?.invoke()
        clearDialogState()
    }

    private fun clearDialogState() {
        showConnectionDialog = false
        pendingKeepAction = null
        pendingRemoveAction = null
        connectingDeviceAddress = ""
    }

    override fun onCleared() {
        super.onCleared()
        // Prevent memory leaks if the ViewModel is destroyed
        BleGattConnectionHandler.onConnectionPromptRequested = null
    }

    /*TODO end*/





    @SuppressLint("MissingPermission")
    fun startAdvertising(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter?.bluetoothLeAdvertiser

        if (advertiser == null) {
            showToast("This device does not support BLE advertising.",true)
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val appServiceUuid = ParcelUuid(BleConfig.APP_SERVICE_UUID)

        // only sends UUID not name
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(appServiceUuid)
            .build()
        //this sends the name
        val scanResponseData= AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()


        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                showToast("Advertising Started!",true)

                _isAdvertising.value = true
            }

            override fun onStartFailure(errorCode: Int) {
                showToast("Advertising failed with error code: $errorCode",true)

                _isAdvertising.value = false
            }
        }
        BleGattConnectionHandler.startServer(context)
        advertiser.startAdvertising(settings, advertiseData, scanResponseData, advertiseCallback)

    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter?.bluetoothLeAdvertiser

        advertiseCallback?.let {
            advertiser?.stopAdvertising(it)
            _isAdvertising.value = false
            BleGattConnectionHandler.stopServer()
            showToast("Advertising stopped!",true)

        }
    }

    //TODO added status check for Connection validation
    //for starting connection
    private val _startStatus = MutableStateFlow(false)
    val startStatus : StateFlow<Boolean> = _startStatus.asStateFlow()
    init{
        viewModelScope.launch {
            ConnectionValidationString.start.collect{ newStatus ->
                _startStatus.value = newStatus
            }
        }
    }
}