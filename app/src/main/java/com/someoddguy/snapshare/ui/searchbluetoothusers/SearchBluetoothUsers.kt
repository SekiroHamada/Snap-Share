package com.someoddguy.snapshare.ui.searchbluetoothusers

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.searchbluetoothusers.SearchBluetoothViewModel
import com.someoddguy.snapshare.ui.searchdevicecard.SearchDeviceCard

@SuppressLint("MissingPermission")
@Composable
@Preview(showSystemUi = true)
fun SearchBluetoothUsers(
    navHostController: NavHostController,
    viewModel: SearchBluetoothViewModel,
    onStartScanClick: () -> Unit,
    onStopScanClick: () -> Unit
){
    //change this to var if something happens
    var deviceList by remember { mutableStateOf(listOf<Int>()) }
    val scanResults by viewModel.scanResults.collectAsState()


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),          // Sets the background to black
        contentColor = colorResource(R.color.white)    // Sets the default text color to white
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top=30.dp,
                    bottom=30.dp,
                    start=10.dp,
                    end=10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Spacer(
                modifier=Modifier.height(30.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (deviceList.isNotEmpty()) {
                            Modifier.border(width = 3.dp, color = colorResource(id = R.color.white))
                        } else {
                            Modifier // An empty modifier that does nothing
                        }
                    )

                    .padding(all = 5.dp)

            ) {
                 items(
                     items = scanResults,
                     // Providing a key optimizes recomposition by helping Compose track items
                     key = { result -> result.device.address }
                 ) { result ->
                     SearchDeviceCard(

                         deviceName = result.device.name ?: "Unknown Device",
                         deviceAddress = result.device.address,
                         onClick = {
                             println("Connecting to yes...")
                         }
                     )
                     Spacer(
                         modifier = Modifier.height(5.dp)
                     )
                }
            }

            Spacer(
                modifier=Modifier.height(30.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        deviceList=listOf(1,2) + deviceList
                        onStartScanClick()
                              },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Scan")
                }

                Button(
                    onClick = {
                        onStopScanClick()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Stop")
                }
            }
        }
    }
}