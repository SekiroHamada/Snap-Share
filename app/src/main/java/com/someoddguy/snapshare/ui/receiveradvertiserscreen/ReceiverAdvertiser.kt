package com.someoddguy.snapshare.ui.receiveradvertiserscreen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import com.someoddguy.snapshare.ble.BleConfig
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ReceiverAdvertiser {

    // Backing property moved to singleton
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private var advertiseCallback: AdvertiseCallback? = null

    @SuppressLint("MissingPermission")
    fun startAdvertising(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter?.bluetoothLeAdvertiser

        if (advertiser == null) {
            showToast("This device does not support BLE advertising.", true)
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
        val scanResponseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                showToast("Advertising Started!", true)
                _isAdvertising.value = true
            }

            override fun onStartFailure(errorCode: Int) {
                showToast("Advertising failed with error code: $errorCode", true)
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
            showToast("Advertising stopped!", true)
        }
    }
}