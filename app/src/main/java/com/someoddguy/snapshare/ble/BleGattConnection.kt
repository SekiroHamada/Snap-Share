import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import com.someoddguy.snapshare.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.milliseconds

object BleGattConnection {
    val activeConnections: MutableList<BluetoothGatt> = CopyOnWriteArrayList()
    private var appContext: Context? = null
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
        @SuppressLint("Missing Permission")
        activeConnections.forEach { it.disconnect() }
        activeConnections.clear()
    }



    fun startConnection(context: Context,result: ScanResult){
        if (appContext == null) {
            appContext = context.applicationContext
        }
        @SuppressLint("MissingPermission")
        val bluetoothGatt = result.device.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    showToast("Connected to $deviceAddress",true)
                    addConnection(gatt)
                    /*TODO: Delete this code below after use*/
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L.milliseconds)
                        removeConnection(gatt)
                    }

                    /*TODO*/

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    showToast("Successfully disconnected from $deviceAddress",true)
                    removeConnection(gatt)
                    gatt.close()
                }
            } else {
                showToast("Error $status encountered for $deviceAddress! Disconnecting...",true)
                removeConnection(gatt)
                gatt.close()
            }
        }
    }

}