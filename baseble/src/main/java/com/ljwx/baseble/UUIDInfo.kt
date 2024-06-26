package com.ljwx.baseble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

class UUIDInfo(var uuid: UUID) {
    var strCharactInfo: String? = null
    var bluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    var descriptor: BluetoothGattDescriptor? = null

    val uUIDString: String
        get() = uuid.toString()
}