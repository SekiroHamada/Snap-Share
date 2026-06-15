package com.someoddguy.snapshare.ble
/*TODO EXPERIMENTAL CODE*/

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BleAdvertiser(private val bluetoothAdapter: BluetoothAdapter) {

    private val bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

    // Define your custom UUID so SnapShare scanners know it's your app, not a random smartwatch
    companion object {
        val SNAPSHARE_SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb") // Replace with your own generated UUID
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i("BleAdvertiser", "Successfully started advertising!")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BleAdvertiser", "Failed to start advertising. Error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission") // Suppressed assuming permissions are checked in MainActivity
    fun startAdvertising() {
        if (bleAdvertiser == null) {
            Log.e("BleAdvertiser", "Bluetooth LE Advertiser not supported on this device.")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SNAPSHARE_SERVICE_UUID))
            .build()

        bleAdvertiser.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        bleAdvertiser?.stopAdvertising(advertiseCallback)
    }
}