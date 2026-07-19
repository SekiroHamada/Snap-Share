package com.someoddguy.snapshare.ui.filetransferprogress

import BleGattConnector
import android.app.NotificationManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.globalcontext.GlobalContext
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.services.FileTransferService
import com.someoddguy.snapshare.services.resetApp
import com.someoddguy.snapshare.ui.filetransferprogress.percentagebox.PercentageBox
import com.someoddguy.snapshare.ui.filetransferprogress.rhombusshape.RhombusShape
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
    val percentageTotal= uiState.filesDone.toFloat()/uiState.totalFiles.toFloat()
    val percentageFile = uiState.fileSizeReceived.toFloat()/uiState.fileSize.toFloat()

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
    LaunchedEffect(uiState.isDone) {
        if(uiState.isDone){
            val context = GlobalContext.appContext
            val status = if(uiState.isReceiving) "Received" else "Sent"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, FileTransferService.CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("SnapShare")
                .setContentText("Files Successfully $status")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1,notification)
        }
    }
    LaunchedEffect(uiState.cancelTransfer) {
        if(uiState.cancelTransfer){
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
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.paper_crush_background), // Replace with your filename
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Text("$str1 ${uiState.totalFiles} File${if(uiState.totalFiles>1)"s" else ""}", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier
                .width(200.dp)
                .height(2.dp)
                .background(color = Color.Black))
            Spacer(modifier = Modifier.height(16.dp))
            Text("$str2 ${uiState.filesDone}/${uiState.totalFiles} File${if (uiState.totalFiles > 1) "s" else ""}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))


            Row(horizontalArrangement = Arrangement.Center){
                PercentageBox(percentageTotal,0.1f)
                PercentageBox(percentageTotal,0.2f)
                PercentageBox(percentageTotal,0.3f)
                PercentageBox(percentageTotal,0.4f)
                PercentageBox(percentageTotal,0.5f)
                PercentageBox(percentageTotal,0.6f)
                PercentageBox(percentageTotal,0.7f)
                PercentageBox(percentageTotal,0.8f)
                PercentageBox(percentageTotal,0.9f)
                PercentageBox(percentageTotal,0.98f)
            }


            Spacer(modifier = Modifier.height(32.dp))
            Text("${uiState.fileName}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("${uiState.fileSize / 1024} MB", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.Center){
                PercentageBox(percentageFile,0.1f)
                PercentageBox(percentageFile,0.2f)
                PercentageBox(percentageFile,0.3f)
                PercentageBox(percentageFile,0.4f)
                PercentageBox(percentageFile,0.5f)
                PercentageBox(percentageFile,0.6f)
                PercentageBox(percentageFile,0.7f)
                PercentageBox(percentageFile,0.8f)
                PercentageBox(percentageFile,0.9f)
                PercentageBox(percentageFile,0.98f)
            }


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
                        containerColor = colorResource(R.color.custom_gray),
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
                            containerColor = colorResource(R.color.custom_gray),
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