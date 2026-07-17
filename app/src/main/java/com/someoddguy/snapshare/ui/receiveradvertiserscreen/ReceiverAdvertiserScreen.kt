package com.someoddguy.snapshare.ui.receiveradvertiserscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.receivesvg.ReceiveSvg

@Preview
@Composable
fun ReceiveFileScreen(
    navHostController: NavHostController= rememberNavController(),
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
            // prevents the user from dismissing the dialog by tapping outside of it
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(isAdvertising) {
        if(isAdvertising){
            val press= PressInteraction.Press(Offset(100f,100f))
            interactionSource.emit(press)

            interactionSource.emit(PressInteraction.Release(press))
        }
    }

    BackHandler() {
        ReceiverAdvertiser.stopAdvertising()
        navHostController.popBackStack()
    }

    Surface(
        modifier = Modifier.fillMaxSize()
        .indication(
            interactionSource = interactionSource,
            indication = ripple()
        ),
        color = colorResource(R.color.black),
        contentColor = colorResource(R.color.white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(isAdvertising){
                ReceiveSvg()
            }else{
                Spacer(modifier = Modifier
                    .height(30.dp))
            }
            Button(
                onClick = {
                    if (!isAdvertising) {
                        viewModel.startAdvertising()
                    } else {
                        viewModel.stopAdvertising()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.teal_700),
                    contentColor = colorResource(R.color.lightning)
                ),
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        top = 20.dp,
                        end = 20.dp,
                        bottom = 10.dp)
                    .width(260.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)

            ) {
                Text(if (isAdvertising) "Stop Broadcasting" else "Receive Files", fontSize = 20.sp)
            }
        }
    }
}