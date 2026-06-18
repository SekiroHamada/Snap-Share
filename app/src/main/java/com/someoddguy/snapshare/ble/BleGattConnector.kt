import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.ble.BleConfig
import com.someoddguy.snapshare.utils.showToast
import com.someoddguy.snapshare.wifip2p.WifiP2pClient
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

        val gattCallback = object : BluetoothGattCallback() {

            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val deviceName = gatt.device.name

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        showToast("Connected to $deviceName",true)
                        addConnection(gatt)
                        gatt.discoverServices()
                        /*TODO ask for larger data pipe line for wifi credentials gatt.requestMtu(128)
                        * TODO make a callback function -> override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int)
                        *TODO do the same for the receiver */

                        /*TODO when fixing connection issue after receiver declines, do gatt.disconnect()*/

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
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                showToast("service discovered",true)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(APP_SERVICE_UUID)
                    val characteristic = service?.getCharacteristic(DATA_CHARACTERISTIC_UUID)

                    if (characteristic != null) {
                        // 3. Initiate the first read request
                        CoroutineScope(Dispatchers.IO).launch {
                            showToast("trying read",true)
                            readWithRetry(gatt, characteristic)
                        }
                    } else {
                        showToast("Target characteristic not found!", true)
                    }
                }

            }
            @Deprecated("onCharacteristicRead")
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
                                    WifiP2pClient.connectToGroupOwner(ctx, ssid, pass)
                                }

                                // Optional: You can disconnect the GATT connection here
                                // if you strictly only need Wi-Fi P2P moving forward.
                            }
                        }
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

    /*TODO problem with this one sends a lot of requests but idk where these requests are coming from*/
    @SuppressLint("MissingPermission")
    private suspend fun readWithRetry(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        val maxRetries = 5
        var attempts=0

        while(attempts < maxRetries){
            val success = gatt.readCharacteristic(characteristic)
            delay(3000)
            if(!success){
                    attempts++
                showToast("Trying Again attempt:$attempts",true)
            }else

                return
        }
        showToast("Connection stalled. Please try again.", true)
        gatt.disconnect()


    }

}