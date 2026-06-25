package com.someoddguy.snapshare.ui.searchbluetoothusers

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.ui.searchbluetoothusers.searchdevicecard.SearchDeviceCard

@SuppressLint("MissingPermission")
@Composable
fun SearchBluetoothUsers(
    navHostController: NavHostController,
    viewModel: SearchBluetoothViewModel = viewModel()
) {
    val scanResults by viewModel.scanResults.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    //TODO added status check to go to the next page
    //for ConnectionValidation
    val isConnecting by viewModel.startStatus.collectAsState()
    if(isConnecting){
        navHostController.navigate(Routes.ConnectionValidationScreen) {}
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
                        onClick={
                            viewModel.startConnection(result)
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
                        if(!isScanning){
                            // Permissions are now handled at Splash Screen
                            viewModel.startBleScan()
                        }else{
                            viewModel.stopBleScan()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if(!isScanning)"Scan" else "Stop Scan")
                }
            }
        }
    }
}