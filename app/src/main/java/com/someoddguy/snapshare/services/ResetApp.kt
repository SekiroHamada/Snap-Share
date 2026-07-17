package com.someoddguy.snapshare.services

import BleGattConnectionHandler
import com.someoddguy.snapshare.filepackettransfer.ReceiveFilePackets
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
import com.someoddguy.snapshare.ui.connectionvalidationscreen.ConnectionValidationString
import com.someoddguy.snapshare.ui.filetransferprogress.FileTransferProgress
import com.someoddguy.snapshare.ui.receiveradvertiserscreen.ReceiverAdvertiser
import com.someoddguy.snapshare.ui.searchbluetoothusers.SearchBluetoothUsers
import com.someoddguy.snapshare.wifip2p.WifiP2PClient
import com.someoddguy.snapshare.wifip2p.WifiP2PGenerator


fun resetApp(){
    if(FileTransferProgress.isReceiving.value){
        ReceiveFilePackets.cancelTransfer()
        WifiP2PGenerator.killAllWifiGeneratorConnections()
    }else{
        SendFilePackets.cancelTransfer()
        WifiP2PClient.killAllWifiClientConnections()
    }
    BleGattConnectionHandler.stopServer()
    BleGattConnector.clearAllConnections()
    ConnectionValidationString.resetValidation()

    FileTransferProgress.resetProgress()

    ReceiverAdvertiser.stopAdvertising()

    SendFilePackets.clearFiles()

    SearchBluetoothUsers.clearResults()
}