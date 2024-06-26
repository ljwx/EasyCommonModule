package com.ljwx.baseble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log
import java.util.UUID

object BaseBleCommunicationUtils {

    private var uuidServiceName = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private var uuidNameRead = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    private var uuidNameReadDesc = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    private var uuidNameWrite = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb")
    private var uuid_characteristic = UUID.fromString("00001355-0000-1000-8000-00805f9b34fb");
    private var uuid_descriptor = UUID.fromString("00001356-0000-1000-8000-00805f9b34fb");
    private var gatt: BluetoothGatt? = null
    private var serviceCharacteristic: BluetoothGattService? = null
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    fun setUUIDName(service: String, read: String, write: String, descriptor: String) {
        uuidServiceName = UUID.fromString(service)
        uuidNameRead = UUID.fromString(read)
        uuidNameReadDesc = UUID.fromString(descriptor)
        uuidNameWrite = UUID.fromString(write)
    }

    private fun getServiceUUID(): UUID {
        return uuidServiceName
    }

    private fun getReadUUID(): UUID {
        return uuidNameRead
    }

    private fun getWriteUUID(): UUID {
        return uuidNameWrite
    }

    private fun getService(): BluetoothGattService? {
        serviceCharacteristic = serviceCharacteristic ?: gatt?.getService(getServiceUUID())
        Log.d("蓝牙", "服务是否为空：$serviceCharacteristic")
        return serviceCharacteristic
    }

    fun init(gatt: BluetoothGatt?) {
        this.gatt = gatt
    }

    private fun getRead(): BluetoothGattDescriptor? {
        readCharacteristic = getService()?.getCharacteristic(getReadUUID())
        readCharacteristic?.let {
            gatt?.setCharacteristicNotification(it, true)
        }
        Log.d("蓝牙", "read是否为空：$readCharacteristic")
        return readCharacteristic?.getDescriptor(UUID.randomUUID())
    }

    private fun getWrite(): BluetoothGattDescriptor? {
        writeCharacteristic = getService()?.getCharacteristic(getWriteUUID())
        writeCharacteristic?.let {
            gatt?.setCharacteristicNotification(it, true)
        }
        val desc = writeCharacteristic?.getDescriptor(UUID.randomUUID())
        Log.d("蓝牙", "write是否为空：$writeCharacteristic,desc是否为空：" + desc)
        return desc
    }

    fun write(value: String): Boolean {
        getWrite()?.setValue(HexString.hexToBytes(value))
        return gatt?.writeCharacteristic(writeCharacteristic) == true
    }

    fun read(): Boolean {
        getRead()
        return gatt?.readCharacteristic(readCharacteristic) == true
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String?
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
            mutableListOf()
        val mGattCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
//            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
//            currentServiceData[LIST_UUID] = uuid
            gattServiceData += currentServiceData

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristics = gattService.characteristics
            val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

            // Loops through available Characteristics.
            gattCharacteristics.forEach { gattCharacteristic ->
                charas += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                uuid = gattCharacteristic.uuid.toString()
//                currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
//                currentCharaData[LIST_UUID] = uuid
                gattCharacteristicGroupData += currentCharaData
            }
            mGattCharacteristics += charas
            gattCharacteristicData += gattCharacteristicGroupData
        }
    }

    fun enableNotifications(
        gatt: BluetoothGatt,
        SERVICE_UUID: UUID,
        CHARACTERISTIC_UUID: UUID,
        DESCRIPTOR_UUID: UUID
    ) {
        val service = gatt.getService(SERVICE_UUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID)
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }
    }

}