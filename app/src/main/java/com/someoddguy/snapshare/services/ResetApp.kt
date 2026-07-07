package com.someoddguy.snapshare.services

import BleGattConnectionHandler
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.ReceiverAdvertiser
import com.someoddguy.snapshare.ui.searchbluetoothusers.SearchBluetoothUsers


fun resetApp(){
    BleGattConnectionHandler.stopServer()
    BleGattConnector.clearAll()
    ConnectionValidationString.resetValidation()

    FileTransferProgress.resetProgress()

    ReceiverAdvertiser.stopAdvertising()

    SendFilePackets.clearFiles()

    SearchBluetoothUsers.clearResults()
}