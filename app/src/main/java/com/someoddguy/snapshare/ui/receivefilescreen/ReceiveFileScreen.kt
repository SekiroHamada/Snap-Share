package com.someoddguy.snapshare.ui.receivefilescreen


import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R

@Composable
fun ReceiveFileScreen(
    navHostController: NavHostController,
    viewModel: ReceiveFileViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe the state from the ViewModel
    val isAdvertising by viewModel.isAdvertising.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val canAdvertise = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_ADVERTISE] == true
        } else {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }

        // If permissions are granted, tell the ViewModel to start
        if (canAdvertise) {
            viewModel.startAdvertising(context)
        }else{
            Toast.makeText(context,"Bluetooth permissions are required",Toast.LENGTH_SHORT).show()
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(com.someoddguy.snapshare.R.color.black),
        contentColor = colorResource(R.color.white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (!isAdvertising) {
                        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        } else {
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                        //start advertising
                        permissionLauncher.launch(permissionsToRequest)
                    } else {
                        // Stop advertising
                        viewModel.stopAdvertising(context)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (isAdvertising) "Stop Broadcasting" else "Receive Files")
            }
        }
    }

}