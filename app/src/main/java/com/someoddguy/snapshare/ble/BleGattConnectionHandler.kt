//package com.someoddguy.snapshare.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.utils.showToast
import java.util.concurrent.CopyOnWriteArrayList

object BleGattConnectionHandler {

    val connectedDevices: MutableList<BluetoothDevice> = CopyOnWriteArrayList()

    // The server instance listening for connections
    private var gattServer: BluetoothGattServer? = null
    private var appContext: Context? = null

    /*TODO */
    var onConnectionPromptRequested: ((String, onKeep: () -> Unit, onRemove: () -> Unit) -> Unit)? = null


    fun addDevice(device: BluetoothDevice) {
        if (!connectedDevices.contains(device)) {
            connectedDevices.add(device)
        }
    }

    fun removeDevice(device: BluetoothDevice) {
        val address = device.address
        val isRemoved = connectedDevices.remove(device)
        if (isRemoved) {
            showToast("$address Disconnected", true)
        } else {
            showToast("Error: Couldn't Disconnect $address", true)
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        gattServer = bluetoothManager.openGattServer(appContext, gattServerCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopServer() {
        gattServer?.let { server ->
            // Disconnect all tracked devices before closing
            connectedDevices.forEach { device ->
                server.cancelConnection(device)
            }
            server.close()
        }
        gattServer = null
        connectedDevices.clear()
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val deviceAddress = device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Handler(Looper.getMainLooper()).post {
                        onConnectionPromptRequested?.invoke(
                            deviceAddress,
                            {
                                showToast("Connected to Central: $deviceAddress", true)
                                addDevice(device)
                            },
                            {
                                showToast("Connection rejected: $deviceAddress", true)
                                // Actively disconnect the device!
                                gattServer?.cancelConnection(device)
                            }
                        )
                    }


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    showToast("Disconnected from Central: $deviceAddress", true)
                    removeDevice(device)
                }
            } else {
                showToast("Error $status encountered for $deviceAddress!", true)
                removeDevice(device)
            }
        }
    }
}