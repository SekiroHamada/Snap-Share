package com.someoddguy.snapshare.ble

import java.util.UUID
/*TODO change the UUID for target, also change it in the ReceiveFileViewModel as well*/
/*TODO CHANGE THE UUID AND SAVE IT SOMEWHERE SAFE*/
object BleConfig {
    val APP_SERVICE_UUID: UUID=UUID.fromString("b8e1b517-97c9-464a-b8ff-60647e8cce2a")
    val DATA_CHARACTERISTIC_UUID: UUID=UUID.fromString("b8e1b518-97c9-464a-b8ff-60647e8cce2a")

}