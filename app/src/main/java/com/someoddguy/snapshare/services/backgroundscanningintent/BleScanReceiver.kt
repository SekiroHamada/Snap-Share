package com.someoddguy.snapshare.services.backgroundscanningintent

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.ReceiverAdvertiser
import com.someoddguy.snapshare.utils.showToast

class BleScanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Just check for errors so we don't crash on a bad intent.
        if (intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, -1) != -1) {
            return
        }
        val scanResults = intent.getParcelableArrayListExtra<ScanResult>(
            BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT
        )

        // If no results, do nothing and let the OS go back to sleep
        if (scanResults.isNullOrEmpty()) return
        showToast("scan result out",true)
        ReceiverAdvertiser.isBackgroundIntent(true)
        //BleBackgroundScanner.stopBackgroundScan()
        ReceiverAdvertiser.startAdvertising()


    }
}