package com.someoddguy.snapshare.ui.searchbluetoothusers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.ui.searchdevicecard.SearchDeviceCard
import com.someoddguy.snapshare.utils.showPermanentDenyAlert // Ensure your utility is imported

// Helper function to safely extract the Activity from the Compose Context
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@SuppressLint("MissingPermission")
@Composable
fun SearchBluetoothUsers(
    navHostController: NavHostController,
    viewModel: SearchBluetoothViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity() // Extract activity for rationale checks
    val scanResults by viewModel.scanResults.collectAsState()

    // 1. Determine which permissions to ask for based on Android version
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // 2. Launcher to prompt user to turn on Bluetooth if it's disabled
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startBleScan()
        } else {
            Toast.makeText(context, "Bluetooth must be enabled to scan", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Launcher to request runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }

        if (allGranted) {
            // Permissions granted! Now check if Bluetooth is actually turned on.
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter

            if (adapter?.isEnabled == true) {
                viewModel.startBleScan()
            } else {
                // If permissions are good but BT is off, launch the enable intent
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }
        } else {
            // User denied the permissions. We need to check if it's a permanent denial.
            val containsPermanentDenial = perms.entries.any { (permission, isGranted) ->
                !isGranted && activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }

            if (containsPermanentDenial) {
                // If permanently denied, trigger the alert to send them to settings
                activity?.showPermanentDenyAlert()
            } else {
                // Standard denial (user just hit "Deny" once)
                Toast.makeText(context, "Permissions denied. Cannot scan.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),
        contentColor = colorResource(R.color.white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, bottom = 30.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (scanResults.isNotEmpty()) {
                            Modifier.border(width = 3.dp, color = colorResource(id = R.color.white))
                        } else {
                            Modifier
                        }
                    )
                    .padding(all = 5.dp)
            ) {
                items(
                    items = scanResults,
                    key = { result -> result.device.address }
                ) { result ->
                    SearchDeviceCard(
                        deviceName = result.device.name ?: "Unknown Device",
                        deviceAddress = result.device.address,
                        onClick = {
                            println("Connecting to yes...")
                        }
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        permissionLauncher.launch(permissionsToRequest)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Scan")
                }

                Button(
                    onClick = {
                        viewModel.stopBleScan()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Stop")
                }
            }
        }
    }
}