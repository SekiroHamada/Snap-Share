package com.someoddguy.snapshare.navigation

import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object SplashScreen:Routes()

    @Serializable
    data object HomeScreen:Routes()

    @Serializable
    data object SearchBluetoothUsers:Routes()

}