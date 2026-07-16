package com.someoddguy.snapshare.ui.connectionvalidationscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.services.resetApp
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.receivesvg.ReceiveSvg
import com.someoddguy.snapshare.wifip2p.WifiP2PClient
import com.someoddguy.snapshare.wifip2p.WifiP2PGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConnectionValidationScreen(
    navHostController: NavHostController,
    viewModel: ConnectionValidationViewModel = viewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.initiateTransfer) {
        if (uiState.initiateTransfer) {
            navHostController.navigate(Routes.FileTransferProgressScreen) {}
        }
    }

    LaunchedEffect(uiState.cancel) {
        if(uiState.cancel){
            ConnectionValidationString.updateButtonClick(true)
            coroutineScope.launch{
                delay(3000L)
                if(uiState.isReceiving){
                    BleGattConnectionHandler.cancelConnection()
                    WifiP2PGenerator.killAllWifiGeneratorConnections()
                }else{
                    BleGattConnector.cancelConnection()
                    WifiP2PClient.killAllWifiClientConnections()
                }
                resetApp()
                delay(1000L)
                navHostController.navigate(Routes.HomeScreen) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),
        contentColor = colorResource(R.color.white)
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ReceiveSvg()

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = uiState.statusString)

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                enabled = !uiState.isButtonClicked,
                onClick ={
                    if(!uiState.isReceiving){
                        BleGattConnector.cancelConnectionIndication()
                    }
                    ConnectionValidationString.updateButtonClick(true)
                    ConnectionValidationString.updateCancelStatus(true)
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
            ){
                Text(if (uiState.isButtonClicked) "Processing" else "Cancel", fontSize = 20.sp)
            }
        }
    }
}