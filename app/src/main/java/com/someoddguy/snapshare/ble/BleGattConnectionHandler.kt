//package com.someoddguy.snapshare.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.ble.BleConfig
import com.someoddguy.snapshare.utils.showToast
import com.someoddguy.snapshare.wifip2p.WifiP2PGenerator
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

object BleGattConnectionHandler {

    val connectedDevices: MutableList<BluetoothDevice> = CopyOnWriteArrayList()

    // The server instance listening for connections
    private var gattServer: BluetoothGattServer? = null
    private var appContext: Context? = null
    //UUIDs
    val APP_SERVICE_UUID: UUID = BleConfig.APP_SERVICE_UUID
    val DATA_CHARACTERISTIC_UUID:UUID= BleConfig.DATA_CHARACTERISTIC_UUID
    //gattCharacteristics
    private var dataCharacteristic: BluetoothGattCharacteristic? = null

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
        setupGattService()
    }
    @SuppressLint("MissingPermission")
    private fun setupGattService() {
        // Initialize the characteristic with READ permission
        dataCharacteristic = BluetoothGattCharacteristic(
            DATA_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val service =
            BluetoothGattService(APP_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        service.addCharacteristic(dataCharacteristic)

        gattServer?.addService(service)
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
                                /*TODO setup WifiP2P*/

                                appContext?.let { ctx ->
                                    WifiP2PGenerator.startAsGroupOwner(ctx) { credentials ->
                                        // Once credentials are generated, load them into the characteristic
                                        dataCharacteristic?.value = credentials.toByteArray(Charsets.UTF_8)
                                    }
                                }

                                /*TODO end*/
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

        /*TODO setup characteristic read request from the sender*/
        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            //checking read request
            showToast("Got read request",true)
            if (characteristic.uuid == DATA_CHARACTERISTIC_UUID) {
                // If credentials aren't ready yet, send a placeholder or empty array
                // so the client knows to retry.
                val responseValue = characteristic.value ?: "PENDING".toByteArray(Charsets.UTF_8)
                showToast("MSG sent",true)
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    responseValue
                )
            } else {
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    offset,
                    null
                )
            }
        }
    }
}