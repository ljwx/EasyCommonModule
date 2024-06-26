package com.ljwx.baseble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.ArrayList
import java.util.UUID


object BaseBleConnectUtils {

    private val TAG = "蓝牙连接"

    private var mBluetoothGatt: BluetoothGatt? = null

    private var listener: BaseBleManager.BleStateListener? = null

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceName = gatt?.device?.name
            Log.e(TAG, "连接状态发生改变:$newState")
            //连接状态改变的Callback
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                mBluetoothGatt?.discoverServices();
                listener?.stateChange(
                    BaseBleConst.STATE_CONNECT_SUCCESS,
                    "连接成功:$deviceName",
                    gatt?.device,
                    gatt
                )
                // 连接成功后，开始扫描服务
                // 扫描BLE设备服务是安卓系统中关于BLE蓝牙开发的重要一步，一般在设备连接成功后调用，
                //扫描到设备服务后回调onServicesDiscovered()函数
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // 连接断开
                listener?.stateChange(BaseBleConst.STATE_DISCONNECT, "连接断开:$deviceName")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            //服务发现成功的Callback
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功

//                //获取指定GATT服务，UUID 由远程设备提供
//                val bleGattService = gatt?.getService(UUID.fromString("4FC148DD-FD5B-215C-F69D-E4914CB88E74"))
//                //获取指定GATT特征，UUID 由远程设备提供
//                val bleGattCharacteristic = bleGattService?.getCharacteristic(UUID.fromString("393399B5-53A2-EA72-713F-AE9F539D682C"))
//                //启用特征通知，如果远程设备修改了特征，则会触发 onCharacteristicChange() 回调
//                gatt?.setCharacteristicNotification(bleGattCharacteristic, true)
//                //启用客户端特征配置【固定写法】
//                val bleGattDescriptor = bleGattCharacteristic?.getDescriptor(UUID.fromString("2610B73F-77F6-2542-30B2-6A36A5677588"))
//                bleGattDescriptor?.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
//                gatt?.writeDescriptor(bleGattDescriptor)


                //获取服务列表
                val service = gatt.services.map { it.uuid }
                for (service in gatt.services) {
                    service.characteristics.forEach {
                        if ((it.properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                        }
                    }
                    if (service.characteristics.size > 1) {
                        val ser = service.uuid.toString()
                        val read = service.characteristics[0].uuid.toString()
//                        val readDes = service.characteristics[0].descriptors[0].uuid.toString()
                        val write = service.characteristics[1].uuid.toString()
                        BaseBleCommunicationUtils.setUUIDName(ser, read, write, "BluetoothGattCallback")
                    }
                }
                listener?.stateChange(
                    BaseBleConst.STATE_SERVICES_DISCOVERED,
                    "onServicesDiscovered"
                )
            } else {
                listener?.stateChange(
                    BaseBleConst.STATE_SERVICES_DISCOVERED_OTHER,
                    "服务发现不成功：$status"
                )
            }

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            //写入Characteristic
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener?.stateChange(BaseBleConst.STATE_WRITE_SUCCESS, "写入命令成功")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            //读取Characteristic
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener?.stateChange(
                    BaseBleConst.STATE_READ_SUCCESS,
                    "读取成功:" + HexString.bytesToHex(characteristic.value)
                )
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            // value为设备发送的数据，根据数据协议进行解析
            val value = characteristic.value
            listener?.stateChange(
                BaseBleConst.STATE_CHARACTERISTIC_CHANGED,
                "回复的内容：" + HexString.bytesToHex(value)
            )
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            //写入Descriptor
            listener?.stateChange(
                BaseBleConst.STATE_DESCRIPTOR_WRITE_SUCCESS,
                "写入监听成功，可以写入命令"
            )
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)
            //读取Descriptor
            listener?.stateChange(
                BaseBleConst.STATE_DESCRIPTOR_READ_SUCCESS,
                "接收监听成功，可以接收数据"
            )
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.d("蓝牙", "onMtuChanged:" + mtu)
        }



    }

    @SuppressLint("MissingPermission")
    fun connectGATT(
        context: Context,
        bluetoothDevice: BluetoothDevice?,
        autoConnect: Boolean = false,
        listener: BaseBleManager.BleStateListener?
    ): BluetoothGatt? {
        this.listener = listener
        //示例
        mBluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice?.connectGatt(
                context,
                autoConnect,
                mBluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            bluetoothDevice?.connectGatt(context, autoConnect, mBluetoothGattCallback);
        }
        return mBluetoothGatt
    }

    fun disconnect() {
        mBluetoothGatt?.disconnect()
        mBluetoothGatt?.close()
    }

}