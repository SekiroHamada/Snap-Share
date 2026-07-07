package com.someoddguy.snapshare.ui.connectionvalidationscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
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
import com.someoddguy.snapshare.wifip2p.WifiP2PClient
import com.someoddguy.snapshare.wifip2p.WifiP2PGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConnectionValidationScreen(
    navHostController: NavHostController,
    viewModel: ConnectionValidationViewModel = viewModel()
){
    // Collect the single state object
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isButtonClicked = false

    LaunchedEffect(uiState.initiateTransfer) {
        if (uiState.initiateTransfer) {
            navHostController.navigate(Routes.FileTransferProgressScreen) {}
        }
    }

    LaunchedEffect(uiState.cancel) {
        if(uiState.cancel){
            coroutineScope.launch{
                delay(3000L)

                if(uiState.cancel){
                    WifiP2PGenerator.killAllWifiGeneratorConnections()
                    WifiP2PClient.killAllWifiClientConnections()
                    resetApp()
                }else{
                    WifiP2PGenerator.killAllWifiGeneratorConnections()
                    WifiP2PClient.killAllWifiClientConnections()
                    resetApp()
                }

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
            // The standard Jetpack Compose rotating loading icon
            CircularProgressIndicator()

            // Adds a little space between the icon and the text
            Spacer(modifier = Modifier.height(16.dp))

            // The text that automatically updates when the ViewModel changes
            Text(text = uiState.statusString)

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                enabled = !isButtonClicked,
                onClick ={
                    coroutineScope.launch{
                        delay(3000L)

                        if(uiState.cancel){
                            WifiP2PGenerator.killAllWifiGeneratorConnections()
                            resetApp()
                        }else{
                            WifiP2PClient.killAllWifiClientConnections()
                            resetApp()
                        }

                        delay(1000L)
                        navHostController.navigate(Routes.HomeScreen) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            ){
                if(isButtonClicked){
                    Text("Processing")
                }else{
                    Text("Cancel")
                }
            }
        }
    }
}