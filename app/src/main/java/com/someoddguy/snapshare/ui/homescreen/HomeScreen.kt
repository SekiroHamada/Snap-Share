package com.someoddguy.snapshare.ui.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes

@Composable
fun HomeScreen(
    navHostController: NavHostController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),
        contentColor = colorResource(R.color.white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    // 3. Launch the picker. The system UI will now allow long-pressing to select multiple.
                    navHostController.navigate(Routes.SendFileScreen) {}
                }
            ) {
                Text("Send Files")
            }
            Button(
                onClick = {

                    navHostController.navigate(Routes.ReceiveFileScreen){}
                }
            ) {
                Text("Receive Files")
            }

        }
    }
}

