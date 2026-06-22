

    import android.Manifest
    import android.annotation.SuppressLint
    import android.bluetooth.BluetoothDevice
    import android.bluetooth.BluetoothGatt
    import android.bluetooth.BluetoothGattCallback
    import android.bluetooth.BluetoothGattCharacteristic
    import android.bluetooth.BluetoothGattDescriptor
    import android.bluetooth.BluetoothProfile
    import android.bluetooth.le.ScanResult
    import android.content.Context
    import android.net.wifi.aware.Characteristics
    import com.someoddguy.snapshare.ble.BleConfig
    import com.someoddguy.snapshare.utils.showToast
    import com.someoddguy.snapshare.wifip2p.WifiP2PClient
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.launch
    import java.util.UUID
    import java.util.concurrent.CopyOnWriteArrayList

    object BleGattConnector {
        val activeConnections: MutableList<BluetoothGatt> = CopyOnWriteArrayList()
        private var appContext: Context? = null

        val APP_SERVICE_UUID: UUID = BleConfig.APP_SERVICE_UUID
        val DATA_CHARACTERISTIC_UUID: UUID = BleConfig.DATA_CHARACTERISTIC_UUID

        fun addConnection(gatt: BluetoothGatt) {
            if (!activeConnections.contains(gatt)) {
                activeConnections.add(gatt)
            }
        }

        fun removeConnection(gatt: BluetoothGatt) {
            val device=gatt.device.address
            val isRemoved =activeConnections.remove(gatt)
            if(isRemoved){
                showToast("$device Disconnected",true)
            }else{
                showToast("Error: Couldn't Disconnect",true)
            }

        }


        fun clearAll() {
            // Remember to actually disconnect them before clearing!
            @SuppressLint("MissingPermission")
            activeConnections.forEach { it.disconnect() }
            activeConnections.clear()
        }



        fun startConnection(context: Context,result: ScanResult){
            if(appContext== null){
                appContext=context.applicationContext
            }
            val gattCallback = object : BluetoothGattCallback() {

                @SuppressLint("MissingPermission")
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    val deviceName = gatt.device.name

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            showToast("Connected to $deviceName",true)
                            addConnection(gatt)
                            gatt.requestMtu(512)

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            showToast("Successfully disconnected from $deviceName",true)
                            removeConnection(gatt)
                            gatt.disconnect()
                            gatt.close()
                        }
                    } else {
                        showToast("Error $status encountered for $deviceName! Disconnecting...",true)
                        removeConnection(gatt)
                        gatt.disconnect()
                        gatt.close()
                    }
                }
                //check when services are discovered
                @SuppressLint("MissingPermission")
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        showToast("Service Discovered!",true)
                        val service = gatt.getService(APP_SERVICE_UUID)
                        val characteristic = service?.getCharacteristic(DATA_CHARACTERISTIC_UUID)
                        if (characteristic != null) {
                            // Enable local notifications
                            gatt.setCharacteristicNotification(characteristic, true)
                            // Write to the CCCD descriptor to enable server-side indications
                            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                            if (descriptor != null) {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            }
                        } else {
                            showToast("Target characteristic not found!", true)
                        }

                    }

                }
                @SuppressLint("MissingPermission")
                override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)
                    if(status== BluetoothGatt.GATT_SUCCESS){
                        showToast("Mtu expanded to $mtu",true)
                        gatt.discoverServices()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic
                ){
                    super.onCharacteristicChanged(gatt, characteristic)
                    val valueBytes = characteristic.value
                    if (valueBytes != null) {
                        val valueString = String(valueBytes, Charsets.UTF_8)

                        // Check if Server denied the connection
                        if (valueString == "DENIED") {

                            showToast("Connection rejected by host. Disconnecting...", true)

                            removeConnection(gatt)
                            @SuppressLint("MissingPermission")
                            gatt.disconnect()
                        }
                        // Otherwise, we assume it's the Wi-Fi P2P credentials
                        else if (valueString.contains("|")) {
                            val credentials = valueString.split("|")
                            if (credentials.size == 2) {
                                val ssid = credentials[0]
                                val pass = credentials[1]

                                showToast("Credentials received! Connecting to Wi-Fi...", true)


                                appContext?.let { ctx ->
                                    WifiP2PClient.connectToGroupOwner(ctx, ssid, pass)
                                }
                            }
                        }else{
                            showToast("Received Unknown Indication!",true)
                        }
                    }
                }
                /*@Deprecated("onCharacteristicRead")
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val valueBytes = characteristic.value
                        if (valueBytes != null) {
                            val valueString = String(valueBytes, Charsets.UTF_8)

                            // 5. Check if the Receiver is still generating the credentials
                            if (valueString == "PENDING" || valueString.isEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    //2 sec delay
                                    delay(2000L)
                                    readWithRetry(gatt, characteristic)
                                }


                            } else {
                                // 6. Success! We have the credentials.
                                val credentials = valueString.split("|")
                                if (credentials.size == 2) {
                                    val ssid = credentials[0]
                                    val pass = credentials[1]

                                    showToast("Credentials received! Connecting to Wi-Fi...", true)

                                    // Trigger the Wi-Fi P2P connection logic
                                    appContext?.let { ctx ->
                                        WifiP2PClient.connectToGroupOwner(ctx, ssid, pass)
                                    }

                                    // Optional: You can disconnect the GATT connection here
                                    // if you strictly only need Wi-Fi P2P moving forward.
                                }
                            }
                        }
                    }
                }*/
            }
            @SuppressLint("MissingPermission")
            val bluetoothGatt = result.device.connectGatt(
                context,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        }
    }