package com.someoddguy.snapshare.ui.filetransferprogress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

@Composable
fun FileTransferProgressScreen(
    navHostController: NavHostController,
    viewModel: FileTransferProgressViewModel = viewModel()
){
    // Collect the single state object
    val uiState by viewModel.uiState.collectAsState()

    var str1=""
    var str2=""
    if(uiState.isReceiving){
        str1 = "Receiving"
        str2 = "Received"
    }else{
        str1 = "Sending"
        str2 = "Sent"
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
            Text("$str1 ${uiState.totalFiles} Files")

            Spacer(modifier = Modifier.height(16.dp))

            Text("$str2 ${uiState.filesDone}/${uiState.totalFiles} File/s")
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


            Text("$str1 ${uiState.fileName} : ${uiState.fileSize}")
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
        }
    }
    // Example of how you use it going forward:
    // Text(text = "Files remaining: ${uiState.totalFiles}")
    // Text(text = "Current file: ${uiState.fileName}")
}