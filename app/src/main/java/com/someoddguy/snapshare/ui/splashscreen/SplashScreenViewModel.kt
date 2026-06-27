package com.someoddguy.snapshare.ui.splashscreen

import androidx.lifecycle.ViewModel
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets

class SplashScreenViewModel: ViewModel() {
    var isEmpty: Boolean = SendFilePackets.isSelectedFilesEmpty()
}