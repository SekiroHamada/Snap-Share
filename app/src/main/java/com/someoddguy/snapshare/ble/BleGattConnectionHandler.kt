
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
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.ble.BleConfig
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.ReceiverAdvertiser
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
    private fun sendIndication(message: String,targetDevice: BluetoothDevice? = null) {
        val data = message.toByteArray(Charsets.UTF_8)
        val characteristic = dataCharacteristic ?: return
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (targetDevice != null) {
                gattServer?.notifyCharacteristicChanged(targetDevice, characteristic, true, data)
            } else {
                connectedDevices.forEach { dev ->
                    gattServer?.notifyCharacteristicChanged(dev, characteristic, true, data)
                }
            }
        }else{
            characteristic.value = data
            if (targetDevice != null) {
                @Suppress("DEPRECATION")
                gattServer?.notifyCharacteristicChanged(targetDevice, characteristic, true)
            } else {
                connectedDevices.forEach { dev ->
                    @Suppress("DEPRECATION")
                    gattServer?.notifyCharacteristicChanged(dev, characteristic, true)
                }
            }
        }


    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val deviceAddress = device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //TODO check if this works for pendingIntent
                    if(ReceiverAdvertiser.isBackgroundIntentAdvertising.value){
                        sendWakeUpNotification(device.address)
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
            ConnectionValidationString.updateStatus("MTU updated to $mtu for ${device.address}")

            Handler(Looper.getMainLooper()).post {
                WifiP2PGenerator.startAsGroupOwner { changeWifiCredential(it) }
            }

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
                if(value != null && value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {


                    Handler(Looper.getMainLooper()).post {
                        onConnectionPromptRequested?.invoke(
                            device.address,
                            { // --- onKeep Clicked ---
                                addDevice(device)
                                ConnectionValidationString.updateStart(true)
                                ConnectionValidationString.updateStatus("Connected to Central ${device.address}")

                                // 100% safe to send now; the client is guaranteed to be listening
                                sendIndication("ACCEPTED")
                            },
                            { // --- onRemove Clicked ---
                                showToast("Connection rejected: ${device.address}", true)

                                sendIndication("DENIED", device)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    gattServer?.cancelConnection(device)
                                }, 500L)
                            }
                        )
                    }
                }
            }
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

    //for waking up the app using a high priority notification
    @SuppressLint("MissingPermission")
    private fun sendWakeUpNotification(deviceAddress: String) {
        val context = appContext ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "snapshare_incoming_connections"

        // 1. Create Notification Channel (Required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Incoming Connections",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create the Intent to launch MainActivity
        // FLAG_ACTIVITY_NEW_TASK is strictly required when launching from outside an Activity context.
        val intent = android.content.Intent(context, com.someoddguy.snapshare.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Optional: Pass data so MainActivity knows it needs to show the prompt immediately
            putExtra("CONNECTING_DEVICE_ADDRESS", deviceAddress)
        }

        // 3. Wrap it in a PendingIntent
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            1002, // Unique request code
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Build the High-Priority Notification
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with your actual drawable, e.g., R.drawable.ic_launcher_foreground
            .setContentTitle("Incoming SnapShare Connection")
            .setContentText("Device $deviceAddress wants to send files. Tap to open.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Attaches the MainActivity launcher
            .setAutoCancel(true) // Clears the notification when tapped
            .build()

        // 5. Fire the notification
        notificationManager.notify(deviceAddress.hashCode(), notification)
    }

}