//package com.someoddguy.snapshare.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.utils.showToast
import com.someoddguy.snapshare.wifip2p.WifiP2PGenerator
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

object BleGattConnectionHandler {

    val connectedDevices: MutableList<BluetoothDevice> = CopyOnWriteArrayList()
    val pendingRejections: MutableList<String> = CopyOnWriteArrayList()
    // The server instance listening for connections
    private var gattServer: BluetoothGattServer? = null
    private var appContext: Context? = null
    //UUIDs
    val APP_SERVICE_UUID: UUID = BleConfig.APP_SERVICE_UUID
    val DATA_CHARACTERISTIC_UUID : UUID = BleConfig.DATA_CHARACTERISTIC_UUID
    val CCCD_UUID : UUID = BleConfig.CCCD_UUID




    //gattCharacteristics
    private var dataCharacteristic: BluetoothGattCharacteristic? = null

    //getting SSID and pass


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
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val cccd = BluetoothGattDescriptor(
            CCCD_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )

        dataCharacteristic?.addDescriptor(cccd)

        val service =
            BluetoothGattService(APP_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        service.addCharacteristic(dataCharacteristic)

        gattServer?.addService(service)
    }

    var WifiCredentials: String =""
    fun changeWifiCredential(stringValue:String){
        WifiCredentials=stringValue
        if(stringValue.isNotEmpty()){
            sendIndication(WifiCredentials)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendIndication(message: String) {
        val data = message.toByteArray(Charsets.UTF_8)
        dataCharacteristic?.value = data
        connectedDevices.forEach { device ->
            // true flag signifies an Indication rather than a Notification
            gattServer?.notifyCharacteristicChanged(device, dataCharacteristic, true)
        }
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
                                addDevice(device)
                                //for the new screen
                                ConnectionValidationString.updateStart(true)
                                ConnectionValidationString.updateStatus("Connected to Central $deviceAddress")


                                WifiP2PGenerator.startAsGroupOwner({changeWifiCredential(it)})

                            },
                            {
                                showToast("Connection rejected: $deviceAddress", true)
                                pendingRejections.add(deviceAddress)
                                val data = "DENIED".toByteArray(Charsets.UTF_8)
                                dataCharacteristic?.value = data
                                gattServer?.notifyCharacteristicChanged(device, dataCharacteristic, true)


                                // if there is a problem in subscribing to indication, remove them after 4s
                                Handler(Looper.getMainLooper()).postDelayed({
                                    gattServer?.cancelConnection(device)
                                    pendingRejections.remove(deviceAddress)
                                }, 4000L)

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
        //TODO remove this redundant fun
        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
            super.onMtuChanged(device, mtu)
            //showToast("MTU updated to $mtu for ${device.address}", true)

        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            if (descriptor.uuid == CCCD_UUID) {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    val address = device.address

                    // 1. Check if they were rejected while they were setting up
                    if (pendingRejections.contains(address)) {
                        val data = "DENIED".toByteArray(Charsets.UTF_8)
                        dataCharacteristic?.value = data
                        gattServer?.notifyCharacteristicChanged(device, dataCharacteristic, true)

                        // Disconnect after sending
                        Handler(Looper.getMainLooper()).postDelayed({
                            gattServer?.cancelConnection(device)
                            pendingRejections.remove(address)
                        }, 500L)
                    }
                    // 2. Or, check if we already have the Wi-Fi credentials ready for them
                    else if (WifiCredentials.isNotEmpty()) {
                        val data = WifiCredentials.toByteArray(Charsets.UTF_8)
                        dataCharacteristic?.value = data
                        gattServer?.notifyCharacteristicChanged(device, dataCharacteristic, true)
                    }
                }, 250L)
            }
        }

    }
}