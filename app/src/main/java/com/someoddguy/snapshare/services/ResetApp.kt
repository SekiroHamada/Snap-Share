package com.someoddguy.snapshare.services

import BleGattConnectionHandler
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.ReceiverAdvertiser


fun resetApp(){
    BleGattConnectionHandler.stopServer()
    BleGattConnector.clearAll()

    ConnectionValidationString.updateStatus("")
    ConnectionValidationString.updateStart(false)
    ConnectionValidationString.updateInitiateTransfer(false)

    FileTransferProgress.resetProgress()

    ReceiverAdvertiser.stopAdvertising()

    SendFilePackets.clearFiles()
}