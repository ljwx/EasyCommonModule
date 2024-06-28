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
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID


object BaseBleConnectUtils {

    private val TAG = "蓝牙连接"

    private var mBluetoothGatt: BluetoothGatt? = null

    private var listener: BaseBleManager.BleStateListener? = null

    var serverList = ArrayList<UUIDInfo>()
    var readCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
    var writeCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
    private var selectServer: UUIDInfo? = null
    private var selectWrite: UUIDInfo? = null
    private var selectRead: UUIDInfo? = null

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

                serverList.clear()
                readCharaMap.clear()
                writeCharaMap.clear()
                var notify: Triple<UUID, UUID, UUID>? = null
                //获取服务列表
                gatt.services.forEach { server ->
                    val serverInfo = UUIDInfo(server.uuid)
                    serverInfo.strCharactInfo = "[Server]"
                    serverList.add(serverInfo)
                    val readArray = ArrayList<UUIDInfo>()
                    val writeArray = ArrayList<UUIDInfo>()
                    server.characteristics.forEach { character ->
                        val charaProp = character.properties
                        var isRead = false
                        var isWrite = false
                        // 具备读的特征
                        var strReadCharactInfo = ""
                        // 具备写的特征
                        var strWriteCharactInfo = ""
                        if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                            isRead = true
                            strReadCharactInfo += "[PROPERTY_READ]"
                            if (character.descriptors.size > 0) {
                                serverInfo.descriptor = character.descriptors[0]
                                Log.d(TAG, "1有desc:" + serverInfo.descriptor?.uuid)
                            }
                            Log.e(
                                TAG,
                                "read_chara=" + character.uuid + "----read_service=" + server.uuid
                            )
                        }
                        if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                            isWrite = true
                            strWriteCharactInfo += "[PROPERTY_WRITE]"
                            if (character.descriptors.size > 0) {
                                serverInfo.descriptor = character.descriptors[0]
                                Log.d(TAG, "2有desc:" + serverInfo.descriptor?.uuid)
                            }
                            Log.e(
                                TAG,
                                "write_chara=" + character.uuid + "----write_service=" + server.uuid
                            )
                        }
                        if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                            isWrite = true
                            strWriteCharactInfo += "[PROPERTY_WRITE_NO_RESPONSE]"
                            if (character.descriptors.size > 0) {
                                serverInfo.descriptor = character.descriptors[0]
                                Log.d(TAG, "3有desc:" + serverInfo.descriptor?.uuid)
                            }
                            Log.e(
                                TAG,
                                "write_chara=" + character.uuid + "----write_service=" + server.uuid
                            )
                        }
                        if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                            isRead = true
                            strReadCharactInfo += "[PROPERTY_NOTIFY]"
                            if (character.descriptors.size > 0) {
                                serverInfo.descriptor = character.descriptors[0]
                                Log.d(TAG, "4有desc:" + serverInfo.descriptor?.uuid)
                            }
                            Log.e(
                                TAG,
                                "notify_chara=" + character.uuid + "----notify_service=" + server.uuid
                            )
                        }
                        if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                            isRead = true
                            strReadCharactInfo += "[PROPERTY_INDICATE]"
                            if (character.descriptors.size > 0) {
                                serverInfo.descriptor = character.descriptors[0]
                                Log.d(TAG, "5有desc:" + serverInfo.descriptor?.uuid)
                            }
                            Log.e(
                                TAG,
                                "indicate_chara=" + character.uuid + "----indicate_service=" + server.uuid
                            )
                        }
                        if (isRead) {
                            val uuidInfo = UUIDInfo(character.uuid)
                            uuidInfo.strCharactInfo = strReadCharactInfo
                            uuidInfo.bluetoothGattCharacteristic = character
                            readArray.add(uuidInfo)
                        }
                        if (isWrite) {
                            val uuidInfo = UUIDInfo(character.uuid)
                            uuidInfo.strCharactInfo = strWriteCharactInfo
                            uuidInfo.bluetoothGattCharacteristic = character
                            writeArray.add(uuidInfo)
                        }
                        readCharaMap.put(server.uuid.toString(), readArray)
                        writeCharaMap.put(server.uuid.toString(), writeArray)
                    }
                    if (notify == null) {
                    }
                    notify = getReadNotify(server)
                }
                listener?.stateChange(
                    BaseBleConst.STATE_SERVICES_DISCOVERED,
                    "onServicesDiscovered"
                )
                notify?.apply {
                    Log.d("蓝牙", "创建服务：" + second)
                    BaseBleCommunicationUtils.enableNotifications(gatt, first, second, third)
                }
            } else {
                listener?.stateChange(
                    BaseBleConst.STATE_SERVICES_DISCOVERED_OTHER,
                    "服务发现不成功：$status"
                )
            }

        }

        private fun getReadNotify(service: BluetoothGattService): Triple<UUID, UUID, UUID>? {
            var result: Triple<UUID, UUID, UUID>? = null
            for (characteristic in service.characteristics) {
                val properties = characteristic.properties
                if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                    if (characteristic.descriptors.size > 0) {
                        val uuid = characteristic.descriptors[0].uuid
                        result = Triple(service.uuid, characteristic.uuid, uuid)
                        return result
                    }
                }
            }
            return result
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            //写入Characteristic
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener?.stateChange(BaseBleConst.STATE_WRITE_SUCCESS, "写入命令成功:" + characteristic?.uuid)
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
                    "读取成功:" + HexString.bytesToHex(characteristic.value) + "," + characteristic.uuid
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
                "回复的内容：" + HexString.bytesToHex(value) + "," + characteristic.uuid
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
                "写入监听成功，可以写入命令：" + descriptor?.uuid
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
                "接收监听成功，可以接收数据:" + descriptor.uuid
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