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
        return readCharacteristic?.getDescriptor(uuidNameReadDesc)
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

    fun write(value: String) :Boolean{
        getWrite()?.setValue(HexString.hexToBytes(value))
        return gatt?.writeCharacteristic(writeCharacteristic) == true
    }

    fun read():Boolean{
        getRead()
        return gatt?.readCharacteristic(readCharacteristic) == true
    }

}