package com.someoddguy.snapshare.ui.receivefilescreen


import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ReceiveFileViewModel : ViewModel() {

    // Backing property to avoid state updates from outside the ViewModel
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private var advertiseCallback: AdvertiseCallback? = null

    @SuppressLint("MissingPermission")
    fun startAdvertising(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter?.bluetoothLeAdvertiser

        if (advertiser == null) {
            Toast.makeText(context,"This device does not support BLE advertising.",Toast.LENGTH_SHORT).show()
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val appServiceUuid = ParcelUuid(UUID.fromString("b8e1b517-97c9-464a-b8ff-60647e8cce2a"))

        // this data send UUID without the name, which ensures that packet doesn't exceed the data limit of 31byte
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(appServiceUuid)
            .build()

        val scanResponseData= AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()


        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                Toast.makeText(context,"Advertising Started!",Toast.LENGTH_SHORT).show()
                //Log.d("BLE", "Advertising started successfully.")
                _isAdvertising.value = true
            }

            override fun onStartFailure(errorCode: Int) {
                Toast.makeText(context,"Advertising failed with error code: $errorCode",Toast.LENGTH_SHORT).show()
                //Log.e("BLE", "Advertising failed with error code: $errorCode")
                _isAdvertising.value = false
            }
        }

        advertiser.startAdvertising(settings, advertiseData, scanResponseData, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter?.bluetoothLeAdvertiser

        advertiseCallback?.let {
            advertiser?.stopAdvertising(it)
            _isAdvertising.value = false
            Toast.makeText(context,"Advertising stopped!",Toast.LENGTH_SHORT).show()
            //Log.d("BLE", "Advertising stopped.")
        }
    }
}