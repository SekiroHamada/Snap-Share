package com.someoddguy.snapshare.ble

import android.bluetooth.BluetoothManager
import android.content.Context
import java.util.UUID
import android.annotation.SuppressLint
import com.someoddguy.snapshare.globalcontext.GlobalContext

/*TODO change the UUID for target, also change it in the ReceiveFileViewModel as well*/
/*TODO CHANGE THE UUID AND SAVE IT SOMEWHERE SAFE*/
object BleConfig {
    val APP_SERVICE_UUID: UUID=UUID.fromString("b8e1b517-97c9-464a-b8ff-60647e8cce2a")
    val DATA_CHARACTERISTIC_UUID: UUID=UUID.fromString("b8e1b518-97c9-464a-b8ff-60647e8cce2a")
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    var BluetoothUserName = ""

    fun getBluetoothUserName(){
        val bluetoothManager = GlobalContext.appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        @SuppressLint("MissingPermission")
        BluetoothUserName = bluetoothAdapter.name
    }
}