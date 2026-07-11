package com.someoddguy.snapshare.services.backgroundscanningintent

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.someoddguy.snapshare.ble.BleConfig
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.utils.showToast
import java.util.UUID

object BleBackgroundScanner {

    // Ensure this matches the UUID you are advertising in ReceiverAdvertiser.kt
    private val TARGET_UUID = UUID.fromString(BleConfig.APP_SERVICE_UUID.toString())

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BleScanReceiver::class.java)

        // CRITICAL: Must be FLAG_MUTABLE or FLAG_UPDATE_CURRENT so the OS can inject the scan results into the Intent.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(context, 1001, intent, flags)
    }

    @SuppressLint("MissingPermission") // Ensure permissions are checked before calling this
    fun startBackgroundScan() {
        val context = GlobalContext.appContext
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter?.bluetoothLeScanner

        if (scanner == null) {
            Log.e("BleBackgroundScanner", "Bluetooth is disabled or unsupported.")
            return
        }

        // 1. Tell the OS to only wake us up if it finds THIS specific UUID
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(TARGET_UUID))
            .build()

        // 2. Configure for background scanning (Low Power)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER) // Best for background
            .build()

        val pendingIntent = getPendingIntent(context)

        // 3. Hand the PendingIntent over to the OS
        scanner.startScan(listOf(filter), settings, pendingIntent)
        showToast( "Background scan started for UUID: $TARGET_UUID",true)
    }

    @SuppressLint("MissingPermission")
    fun stopBackgroundScan() {
        val context = GlobalContext.appContext
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter?.bluetoothLeScanner

        scanner?.stopScan(getPendingIntent(context))
        Log.d("BleBackgroundScanner", "Background scan stopped.")
    }
}