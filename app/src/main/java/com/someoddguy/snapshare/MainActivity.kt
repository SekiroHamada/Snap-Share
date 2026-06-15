package com.someoddguy.snapshare // Defines the package matching your project structure

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle // Imports Bundle to save/restore activity state
import android.util.Log
import androidx.activity.ComponentActivity // Imports base class for Compose activities
import androidx.activity.compose.setContent // Imports extension function to set Compose UI
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.someoddguy.snapshare.ble.BleAdvertiser
import com.someoddguy.snapshare.navigation.NavigationSystem
import com.someoddguy.snapshare.searchbluetoothusers.SearchBluetoothViewModel
import com.someoddguy.snapshare.utils.hasPermission
import com.someoddguy.snapshare.utils.hasRequiredBluetoothPermissions
import com.someoddguy.snapshare.utils.showPermanentDenyAlert

// Main entry point activity for your Android application
private const val PERMISSION_REQUEST_CODE = 1

class MainActivity : ComponentActivity() {

    // This method runs when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Calls the parent class implementation of onCreate
        super.onCreate(savedInstanceState)

        // Defines the UI layout using Jetpack Compose
        setContent {
            NavigationSystem(
                viewModel = viewModel,
                onStartScanClick={startBleScan()},
                onStopScanClick={stopBleScan()}
            )

        }
    }

    /*TODO Change this code */
    private val viewModel: SearchBluetoothViewModel by viewModels()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Push the result to the ViewModel.
            // The ViewModel handles the duplicate checking logic now.
            viewModel.addOrUpdateDevice(result)

            // Optional: Keep your logging if needed
            Log.i("ScanCallback", "Found BLE device! address: ${result.device.address}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }
    private fun startBleScan() {
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions()
        } else {
        /* TODO: Actually perform scan */
            //put a ? in here (wasn't there in the code)
            bleScanner?.startScan(null, scanSettings, scanCallback)
        }
    }
    private fun stopBleScan() {
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions()
        }else{
            //added if else, suppressLint and ?
            @SuppressLint("MissingPermission")
            bleScanner?.stopScan(scanCallback)
        }

    }
    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredBluetoothPermissions()) { return }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    //Requests Location Permission (for older versions)
    private fun requestLocationPermission() = runOnUiThread {
        AlertDialog.Builder(this)
            .setTitle("Location permission required")
            .setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
            )
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
            .show()
    }

    //Requests Bluetooth Permission
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothPermissions() = runOnUiThread {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth permission required")
            .setMessage(
                "Starting from Android 12, the system requires apps to be granted " +
                        "Bluetooth access in order to scan for and connect to BLE devices."
            )
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
            .show()
    }

    //If user denies permission,
    //Continuously Prompts the user to give access until permanent denial is not activated
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array< String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST_CODE) return

        val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
            it.second == PackageManager.PERMISSION_DENIED &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, it.first)
        }
        val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when {
            containsPermanentDenial -> {

                showPermanentDenyAlert()
            }
            containsDenial -> {
                requestRelevantRuntimePermissions()
            }
            allGranted && hasRequiredBluetoothPermissions() -> {
                startBleScan()
            }
            else -> {
                // Unexpected scenario encountered when handling permissions
                recreate()
            }
        }
    }


    //checking if Bluetooth is Enabled or not
    //if not prompt the user to enable it
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    /*TODO: Change the ScanSettings based on our preferences */

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    /* This has been commented as there is another object above which is same but this one is from the website
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i("ScanCallback", "Found BLE device! Address: $address")
            }
        }
    }
    */

    private val bluetoothEnablingResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Bluetooth is enabled, good to go
        } else {
            // User dismissed or denied Bluetooth prompt
            promptEnableBluetooth()
        }
    }
    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }
    /**
     * Prompts the user to enable Bluetooth via a system dialog.
     *
     * For Android 12+, [Manifest.permission.BLUETOOTH_CONNECT] is required to use
     * the [BluetoothAdapter.ACTION_REQUEST_ENABLE] intent.
     */
    private fun promptEnableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) {
            // Insufficient permission to prompt for Bluetooth enabling
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                bluetoothEnablingResult.launch(this)
            }
        }
    }


    /*TODO Experimental Code */
    // Initialize your new class
    private val appAdvertiser by lazy { BleAdvertiser(bluetoothAdapter) }
    private fun startBleScanAndAdvertise() {
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions()
        } else {
            // 1. Start looking for other SnapShare phones
            viewModel.clearResults()
            //put a ? here
            bleScanner?.startScan(null, scanSettings, scanCallback)

            // 2. Start shouting "I am here" to other SnapShare phones
            appAdvertiser.startAdvertising()
        }
    }


}




