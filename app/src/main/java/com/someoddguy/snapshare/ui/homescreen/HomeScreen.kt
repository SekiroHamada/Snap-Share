package com.someoddguy.snapshare.ui.homescreen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import com.someoddguy.snapshare.ui.homescreen.animateddropbox.DownwardArrow
import com.someoddguy.snapshare.ui.homescreen.animateddropbox.UpwardArrow


@Preview
@Composable
fun HomeScreen(
    navHostController: NavHostController=rememberNavController()
) {

    BackHandler() {
        FileTransferProgress.updateIsReceiving(false)
        navHostController.popBackStack()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_background), // Replace with your filename
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    FileTransferProgress.updateIsReceiving(false)
                    navHostController.navigate(Routes.SendFileScreen) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.custom_gray),
                    contentColor = colorResource(R.color.lightning)
                ),
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        top = 20.dp,
                        end = 20.dp,
                        bottom = 10.dp)
                    .width(300.dp)
                    .height(90.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Send", fontSize = 50.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    UpwardArrow()
                }

            }


            Spacer(modifier = Modifier.height(5.dp))
            Box(modifier = Modifier
                .height(1.dp)
                .width(320.dp)
                .background(color = colorResource(R.color.lightning)))
            Spacer(modifier = Modifier.height(5.dp))


            Button(
                onClick = {
                    FileTransferProgress.updateIsReceiving(true)
                    navHostController.navigate(Routes.ReceiveFileScreen){}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.custom_gray),
                    contentColor = colorResource(R.color.lightning)
                ),
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        top = 10.dp,
                        end =20.dp,
                        bottom = 20.dp)
                    .width(300.dp)
                    .height(90.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Receive", fontSize = 50.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    DownwardArrow()
                }
            }

        }
    }
}

