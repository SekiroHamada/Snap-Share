package com.someoddguy.snapshare.ui.filetransferprogress

import BleGattConnector
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.services.resetApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun FileTransferProgressScreen(
    navHostController: NavHostController= rememberNavController(),
    viewModel: FileTransferProgressViewModel = viewModel()
){
    // Collect the single state object
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isButtonClicked by remember { mutableStateOf(false) }


    var str1=""
    var str2=""
    if(uiState.isReceiving){
        str1 = "Receiving"
        str2 = "Received"
    }else{
        str1 = "Sending"
        str2 = "Sent"
    }

    BackHandler(enabled = true) {

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


            Text("$str1 ${uiState.totalFiles} File${if(uiState.totalFiles>1)"s" else ""}")
            Spacer(modifier = Modifier.height(16.dp))
            Text("$str2 ${uiState.filesDone}/${uiState.totalFiles} File${if (uiState.totalFiles > 1) "s" else ""}")
            Spacer(modifier = Modifier.height(8.dp))


            LinearProgressIndicator(
            progress = { uiState.filesDone.toFloat()/uiState.totalFiles.toFloat() },
            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(8.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )


            Spacer(modifier = Modifier.height(32.dp))
            Text("$str1 ${uiState.fileName}")
            Spacer(modifier = Modifier.height(2.dp))
            Text("${uiState.fileSize / 1024} MB")
            Spacer(modifier = Modifier.height(8.dp))


            LinearProgressIndicator(
                progress = {uiState.fileSizeReceived.toFloat()/uiState.fileSize.toFloat()},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(8.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
            if(uiState.isDone){
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    enabled = !isButtonClicked,
                    onClick = {
                        isButtonClicked = true
                        coroutineScope.launch{
                            resetApp()
                            delay(500L)
                            navHostController.navigate(Routes.HomeScreen) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.teal_700),
                        contentColor = colorResource(R.color.lightning)),
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
                    if(isButtonClicked){
                        Text("Processing...")
                    }else{
                        Text("DONE")
                    }

                }
            }else{
                Button(
                    enabled = !uiState.cancelTransfer,
                    onClick = {
                        coroutineScope.launch{
                            if(uiState.isReceiving){
                                BleGattConnectionHandler.sendIndication("Cancel Transfer")
                            }else{
                                BleGattConnector.sendIndication("Cancel Transfer")
                            }
                            resetApp()
                            delay(500L)
                            navHostController.navigate(Routes.HomeScreen) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.teal_700),
                    contentColor = colorResource(R.color.lightning)),
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
                    Text(if(uiState.cancelTransfer) "Processing" else "Cancel")
                }
            }
        }
    }
}