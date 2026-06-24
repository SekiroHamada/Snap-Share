package com.someoddguy.snapshare.ui.connectionvalidationscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R


@Composable
fun ConnectionValidationScreen(
    navHostController: NavHostController,
    viewModel: ConnectionValidationViewModel= viewModel()
){
    val statusText by viewModel.myViewModelString.collectAsState()
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
            Text(text = statusText)
        }
    }

}