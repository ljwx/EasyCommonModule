package com.ljwx.baseble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.util.Log

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
                listener?.stateChange(BaseBleConst.STATE_CONNECT_SUCCESS, "连接成功:$deviceName")
                // 连接成功后，开始扫描服务
                // 扫描BLE设备服务是安卓系统中关于BLE蓝牙开发的重要一步，一般在设备连接成功后调用，
                //扫描到设备服务后回调onServicesDiscovered()函数
                mBluetoothGatt?.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // 连接断开
                listener?.stateChange(BaseBleConst.STATE_DISCONNECT, "连接断开:$deviceName")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            listener?.stateChange(BaseBleConst.STATE_SERVICES_DISCOVERED, "onServicesDiscovered")
            //服务发现成功的Callback
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                //获取服务列表
                val servicesList = mBluetoothGatt?.services;

            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            //写入Characteristic
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            //读取Characteristic
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            //通知Characteristic
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            //写入Descriptor
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)
            //读取Descriptor
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


}