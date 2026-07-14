package com.someoddguy.snapshare.ui.homescreen

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.ui.homescreen.animateddropbox.DownwardArrow
import com.someoddguy.snapshare.ui.homescreen.animateddropbox.UpwardArrow


@Preview

@Composable
fun HomeScreen(
    navHostController: NavHostController=rememberNavController()
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
                    .fillMaxWidth()
                    .height(100.dp),
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


            Spacer(modifier = Modifier.height(20.dp))


            Button(
                onClick = {

                    navHostController.navigate(Routes.ReceiveFileScreen){}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.teal_700),
                    contentColor = colorResource(R.color.lightning)
                ),
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        top = 10.dp,
                        end =20.dp,
                        bottom = 20.dp)
                    .fillMaxWidth()
                    .height(100.dp),
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

