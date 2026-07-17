    import android.annotation.SuppressLint
    import android.bluetooth.BluetoothDevice
    import android.bluetooth.BluetoothGatt
    import android.bluetooth.BluetoothGattCallback
    import android.bluetooth.BluetoothGattCharacteristic
    import android.bluetooth.BluetoothGattDescriptor
    import android.bluetooth.BluetoothProfile
    import android.bluetooth.le.ScanResult
    import android.content.Context
    import com.someoddguy.snapshare.ble.BleConfig
    import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
    import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
    import com.someoddguy.snapshare.ui.searchbluetoothusers.SearchBluetoothUsers
    import com.someoddguy.snapshare.utils.showToast
    import com.someoddguy.snapshare.wifip2p.WifiP2PClient
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
                            addConnection(gatt)
                            gatt.discoverServices()

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            showToast("Successfully disconnected from $deviceName",true)
                            removeConnection(gatt)
                            gatt.disconnect()
                            gatt.close()
                            SearchBluetoothUsers.startBleScan(context)
                        }
                    } else {
                        showToast("Error $status encountered for $deviceName! Disconnecting...",true)
                        removeConnection(gatt)
                        gatt.disconnect()
                        gatt.close()
                        SearchBluetoothUsers.startBleScan(context)
                    }
                }


                @SuppressLint("MissingPermission")
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        showToast("Service Discovered!",true)
                        val service = gatt.getService(APP_SERVICE_UUID)
                        val characteristic = service?.getCharacteristic(DATA_CHARACTERISTIC_UUID)
                        if (characteristic != null) {
                            gatt.setCharacteristicNotification(characteristic, true)
                            val descriptor = characteristic.getDescriptor(BleConfig.CCCD_UUID)
                            if (descriptor != null) {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            }
                        } else {
                            showToast("Target characteristic not found!", true)
                            removeConnection(gatt)
                            gatt.disconnect()
                            gatt.close()
                            SearchBluetoothUsers.startBleScan(context)
                        }

                    }

                }


                @SuppressLint("MissingPermission")
                override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)
                    if(status== BluetoothGatt.GATT_SUCCESS){
                        ConnectionValidationString.updateStatus("MTU increased")
                    }
                }

                @Deprecated("Deprecated in Java")
                @SuppressLint("MissingPermission")
                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic
                ){
                    super.onCharacteristicChanged(gatt, characteristic)
                    val valueBytes = characteristic.value
                    if (valueBytes != null) {
                        val valueString = String(valueBytes, Charsets.UTF_8)

                        if (valueString == "DENIED") {
                            showToast("Connection rejected by host. Disconnecting...", true)
                            removeConnection(gatt)
                            gatt.disconnect()
                            gatt.close()
                            SearchBluetoothUsers.startBleScan(context)
                        }else if(valueString == "ACCEPTED"){
                            ConnectionValidationString.updateStart(true)
                            gatt.requestMtu(517)
                            ConnectionValidationString.updateStatus("Connected to Central")
                        }else if(valueString == "Cancel"){
                            removeConnection(gatt)
                            gatt.disconnect()
                            gatt.close()
                            cancelConnection()
                            SearchBluetoothUsers.startBleScan(context)
                            //TODO add the resetting functionality
                        }else if (valueString.contains("|")) {
                            val credentials = valueString.split("|")
                            if (credentials.size == 2) {
                                val ssid = credentials[0]
                                val pass = credentials[1]
                                ConnectionValidationString.updateStatus("Credentials received! Connecting to Wi-Fi...")
                                WifiP2PClient.saveWifiCredentials( ssid, pass)
                            }
                        }else if(valueString == "ServerSocket"){
                            WifiP2PClient.connectToGroupOwner()
                        }else if(valueString == "Cancel Transfer"){
                            SendFilePackets.cancelTransfer()
                            //TODO fix
                        }
                        else{
                            showToast("Received Unknown Indication!",true)
                        }
                    }
                }

            }
            @SuppressLint("MissingPermission")
            val bluetoothGatt = result.device.connectGatt(
                context,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        }

        fun clearAllConnections() {
            // Remember to actually disconnect them before clearing!
            @SuppressLint("MissingPermission")
            activeConnections.forEach { it.disconnect() }
            activeConnections.clear()
        }
        @SuppressLint("MissingPermission")
        fun cancelConnection(){
            sendIndication("Cancel")
            ConnectionValidationString.updateButtonClick(true)
            ConnectionValidationString.updateCancelStatus(true)
        }
        @SuppressLint("MissingPermission")
        fun sendIndication(msg:String){
            val message = msg.toByteArray(Charsets.UTF_8)
            activeConnections.forEach { gatt ->
                val service = gatt.getService(APP_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(DATA_CHARACTERISTIC_UUID)

                if (characteristic != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(
                            characteristic,
                            message,
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        characteristic.value = message
                        @Suppress("DEPRECATION")
                        gatt.writeCharacteristic(characteristic)
                    }
                }

            }
        }
    }