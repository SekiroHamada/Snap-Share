package com.someoddguy.snapshare.ui.receiveradvertiserscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes

@Composable
fun ReceiveFileScreen(
    navHostController: NavHostController,
    viewModel: ReceiverAdvertiserViewModel = viewModel()
) {
    // Observe the state from the ViewModel
    val isAdvertising by viewModel.isAdvertising.collectAsState()
    //TODO added status check to go to the next page
    //for ConnectionValidation
    val isConnecting by viewModel.startStatus.collectAsState()
    LaunchedEffect(isConnecting) {
        if(isConnecting){
            navHostController.navigate(Routes.ConnectionValidationScreen) {}
        }
    }

    //Prompt Window code for gattServer receiving a connection
    if (viewModel.showConnectionDialog) {
        AlertDialog(
            onDismissRequest = {
                // empty to force user to send connection
            },
            title = {
                Text(text = "Incoming Connection")
            },
            text = {
                Text(text = "Device ${viewModel.connectingDeviceAddress} wants to connect. Do you want to keep this connection?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onKeepClicked() }
                ) {
                    Text("Keep")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onRemoveClicked() }
                ) {
                    Text("Remove")
                }
            },
            // This prevents the user from dismissing the dialog by tapping outside of it
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
    //End of Prompt Window

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
                        // Permissions are now handled at Splash Screen
                        viewModel.startAdvertising()
                    } else {
                        // Stop advertising
                        viewModel.stopAdvertising()
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (isAdvertising) "Stop Broadcasting" else "Receive Files")
            }
        }
    }
}