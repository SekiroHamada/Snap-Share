package com.someoddguy.snapshare.ui.splashscreen

import androidx.lifecycle.ViewModel
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.ReceiverAdvertiser

class SplashScreenViewModel: ViewModel() {
    var isEmpty: Boolean = SendFilePackets.isSelectedFilesEmpty()
    var isBackgroundIntentAdvertising : Boolean = ReceiverAdvertiser.checkBackgroundIntent()
}