package com.someoddguy.snapshare.ui.splashscreen // Defines the package inside the ui folder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column // Imports Column component to stack items vertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable // Imports Annotation to mark this as a UI building block
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import kotlinx.coroutines.delay


@Composable

fun SplashScreen(navHostController: NavHostController) {

    LaunchedEffect(Unit) {
        delay(timeMillis = 1000)

        navHostController.navigate(Routes.HomeScreen) {
            popUpTo(Routes.SplashScreen) { inclusive = true }
        }

    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),          // Sets the background to black
        contentColor = colorResource(R.color.white)    // Sets the default text color to white
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = "SnapShare",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color= colorResource(id= R.color.lightning)
            )
            Spacer(
                modifier= Modifier
                    .height(24.dp)
            )
            Text(
                text="By",
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier= Modifier
                    .height(7.dp)
            )
            Text(
                text="Some Odd Guy",
                fontWeight = FontWeight.Bold
            )


        }
    }


}