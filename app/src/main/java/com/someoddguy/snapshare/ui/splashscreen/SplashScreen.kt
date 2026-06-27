package com.someoddguy.snapshare.ui.splashscreen

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun Context.ungrantedPermissions(permissions: Array<String>): Array<String> {
    return permissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }.toTypedArray()
}

@Composable
fun SplashScreen(
    navHostController: NavHostController,
    viewModel: SplashScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    // Grab the value from the ViewModel
    val isFilesEmpty = viewModel.isEmpty

    var showRetryButton by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Safely build the permissions array based on exact OS versions inside a remember block
    val permissionsToRequest = remember {
        val perms = mutableListOf<String>()
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        // 1. Bluetooth permissions (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
            perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        // 2. Wi-Fi permissions (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        perms.toTypedArray()
    }

    val navigateToHome = {
        if(isFilesEmpty){
            navHostController.navigate(Routes.HomeScreen) {
                popUpTo(Routes.SplashScreen) { inclusive = true }
            }
        }else{
            navHostController.navigate(Routes.SendFileScreen) {
                popUpTo(Routes.SplashScreen) { inclusive = true }
            }
        }

    }

    // Helper function to check hardware states sequentially
    val checkHardwareAndNavigate = {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val btAdapter = bluetoothManager.adapter
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (btAdapter?.isEnabled != true) {
            // Request Bluetooth Enable
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            // (Note: we use activity?.startActivityForResult if not using a launcher,
            // but since we have a launcher below, we'll let the launcher handle it)
        } else if (!wifiManager.isWifiEnabled) {
            // Request Wi-Fi Enable
        } else {
            navigateToHome()
        }
    }

    // 1. Launcher for enabling Wi-Fi
    val enableWifiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            navigateToHome()
        } else {
            Toast.makeText(context, "Wi-Fi must be enabled to continue.", Toast.LENGTH_SHORT).show()
            showRetryButton = true
        }
    }

    // 2. Launcher for enabling Bluetooth
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // BT is on, now check if Wi-Fi needs to be turned on
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                val enableWifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_WIFI)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
                enableWifiLauncher.launch(enableWifiIntent)
            } else {
                navigateToHome()
            }
        } else {
            Toast.makeText(context, "Bluetooth must be enabled to continue.", Toast.LENGTH_SHORT).show()
            showRetryButton = true
        }
    }

    // 3. Launcher for Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }

        if (allGranted) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val btAdapter = bluetoothManager.adapter
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (btAdapter?.isEnabled != true) {
                // First trigger BT prompt
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else if (!wifiManager.isWifiEnabled) {
                // If BT is already on, trigger Wi-Fi prompt
                val enableWifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_WIFI)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
                enableWifiLauncher.launch(enableWifiIntent)
            } else {
                // Both are already on
                navigateToHome()
            }
        } else {
            showRetryButton = true

            val containsPermanentDenial = perms.entries.any { (permission, isGranted) ->
                !isGranted && activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }

            if (containsPermanentDenial) {
                showSettingsDialog = true
            } else {
                Toast.makeText(context, "Required permissions were denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(timeMillis = 1000)
        val ungranted = context.ungrantedPermissions(permissionsToRequest)
        if (ungranted.isEmpty()) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val btAdapter = bluetoothManager.adapter
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (btAdapter?.isEnabled != true) {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else if (!wifiManager.isWifiEnabled) {
                val enableWifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_WIFI)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
                enableWifiLauncher.launch(enableWifiIntent)
            } else {
                navigateToHome()
            }
        } else {
            permissionLauncher.launch(ungranted)
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = "Permissions Required") },
            text = { Text(text = "You have permanently denied a required permission. SnapShare cannot function without it. Please click below to open your app settings and grant the required permissions manually.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) { Text("Go to Settings") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        Toast.makeText(context, "App cannot continue without permissions.", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Cancel") }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),
        contentColor = colorResource(R.color.white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SnapShare",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = colorResource(id = R.color.lightning)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "By", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(7.dp))
            Text(text = "Some Odd Guy", fontWeight = FontWeight.Bold)

            if (showRetryButton) {
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        showRetryButton = false
                        val ungranted = context.ungrantedPermissions(permissionsToRequest)
                        if (ungranted.isEmpty()) {
                            permissionLauncher.launch(permissionsToRequest)
                        } else {
                            permissionLauncher.launch(ungranted)
                        }
                    }
                ) {
                    Text("Grant Permissions & Continue")
                }
            }
        }
    }
}