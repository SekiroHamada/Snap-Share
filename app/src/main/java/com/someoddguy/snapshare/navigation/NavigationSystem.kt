package com.someoddguy.snapshare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationScreen
import com.someoddguy.snapshare.ui.homescreen.HomeScreen
import com.someoddguy.snapshare.ui.receivefilescreen.ReceiveFileScreen
import com.someoddguy.snapshare.ui.searchbluetoothusers.SearchBluetoothUsers
import com.someoddguy.snapshare.ui.sendfilescreen.SendFileScreen
import com.someoddguy.snapshare.ui.splashscreen.SplashScreen

@Composable
fun NavigationSystem(){
    val navController= rememberNavController()

    NavHost(startDestination = Routes.SplashScreen,navController=navController){

        composable<Routes.SplashScreen> {
            SplashScreen(navController)
        }

        composable<Routes.HomeScreen> {
            HomeScreen(navController)
        }
        composable<Routes.SearchBluetoothUsers>{
            SearchBluetoothUsers(navController)
        }
        composable<Routes.SendFileScreen>{
            SendFileScreen(navHostController = navController)
        }
        composable<Routes.ReceiveFileScreen>{
            ReceiveFileScreen(navHostController = navController)
        }
        composable<Routes.ConnectionValidationScreen>{
            ConnectionValidationScreen(navHostController = navController)
        }
    }
}