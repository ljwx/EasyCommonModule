package com.ljwx.baseble.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import com.ljwx.baseble.ConstUUID
import java.util.UUID


object BroadcastUtils {

    private var bluetoothManager: BluetoothManager? = null
    private var gattServer: BluetoothGattServer? = null

    private val TAG = "蓝牙"

    private val UUID_SERVICE = UUID.fromString(ConstUUID.READ_DESCRIPTOR_UUID)

    /**
     * 服务事件的回调
     */
    private val mBluetoothGattServerCallback: BluetoothGattServerCallback =
        object : BluetoothGattServerCallback() {
            /**
             * 1.连接状态发生变化时
             */
            override fun onConnectionStateChange(
                device: BluetoothDevice,
                status: Int,
                newState: Int
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "1.onConnectionStateChange：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(
                    TAG,
                    String.format(
                        "1.onConnectionStateChange：status = %s, newState =%s ",
                        status,
                        newState
                    )
                )
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService) {
                Log.e(TAG, String.format("onServiceAdded：status = %s", status))
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "onCharacteristicReadRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(
                    TAG,
                    String.format(
                        "onCharacteristicReadRequest：requestId = %s, offset = %s",
                        requestId,
                        offset
                    )
                )
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    characteristic.value
                )
            }

            /**
             * 3. onCharacteristicWriteRequest,接收具体的字节
             */
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                requestBytes: ByteArray
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "3.onCharacteristicWriteRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(
                    TAG,
                    String.format(
                        "3.onCharacteristicWriteRequest：requestId = %s, preparedWrite=%s, responseNeeded=%s, offset=%s, value=%s",
                        requestId,
                        preparedWrite,
                        responseNeeded,
                        offset,
                        requestBytes.toString()
                    )
                )
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    requestBytes
                )

                //4.处理响应内容
                onResponseToClient(requestBytes, device, requestId, characteristic)
            }

            /**
             * 2.描述被写入时，在这里执行 bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS...  收，触发 onCharacteristicWriteRequest
             */
            override fun onDescriptorWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "2.onDescriptorWriteRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(
                    TAG,
                    String.format(
                        "2.onDescriptorWriteRequest：requestId = %s, preparedWrite = %s, responseNeeded = %s, offset = %s, value = %s,",
                        requestId,
                        preparedWrite,
                        responseNeeded,
                        offset,
                        value.toString()
                    )
                )

                // now tell the connected device that this was all successfull
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    value
                )
            }

            /**
             * 5.特征被读取。当回复响应成功后，客户端会读取然后触发本方法
             */
            override fun onDescriptorReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "onDescriptorReadRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(TAG, String.format("onDescriptorReadRequest：requestId = %s", requestId))
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    null
                )
            }

            override fun onNotificationSent(device: BluetoothDevice, status: Int) {
                super.onNotificationSent(device, status)
                Log.e(
                    TAG,
                    String.format(
                        "5.onNotificationSent：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(TAG, String.format("5.onNotificationSent：status = %s", status))
            }

            override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
                super.onMtuChanged(device, mtu)
                Log.e(TAG, String.format("onMtuChanged：mtu = %s", mtu))
            }

            override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
                super.onExecuteWrite(device, requestId, execute)
                Log.e(TAG, String.format("onExecuteWrite：requestId = %s", requestId))
            }
        }

    /**
     * 4.处理响应内容
     *
     * @param reqeustBytes
     * @param device
     * @param requestId
     * @param characteristic
     */
    private fun onResponseToClient(
        reqeustBytes: ByteArray,
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.e(
            TAG,
            String.format(
                "4.onResponseToClient：device name = %s, address = %s",
                device.name,
                device.address
            )
        )
        Log.e(TAG, String.format("4.onResponseToClient：requestId = %s", requestId))
        Log.e(TAG, "4.收到：")
        val str = String(reqeustBytes) + " hello>"
//        characteristicRead.setValue(str.toByteArray())
//        gattServer?.notifyCharacteristicChanged(device, characteristicRead, false)
//        Log.i(TAG, "4.响应：$str")
//        MainActivity.handler.obtainMessage(MainActivity.DEVICE, String(reqeustBytes)).sendToTarget()
    }


    fun startBroadcast(context: Context) {
        //广播设置(必须)
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //发射功率级别: 极低,低,中,高
            .setTimeout(0)
            .setConnectable(true) //能否连接,广播分为可连接广播和不可连接广播
            .build()
        //广播数据(必须，广播启动就会发送)
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true) //包含蓝牙名称
            .setIncludeTxPowerLevel(true) //包含发射功率级别
            .addManufacturerData(1, byteArrayOf(23, 33)) //设备厂商数据，自定义
            .build()
        //扫描响应数据(可选，当客户端扫描时才发送)
        val scanResponse = AdvertiseData.Builder()
            .addManufacturerData(2, byteArrayOf(66, 66)) //设备厂商数据，自定义
            .addServiceUuid(ParcelUuid(UUID_SERVICE)) //服务UUID
            //                .addServiceData(new ParcelUuid(UUID_SERVICE), new byte[]{2}) //服务数据，自定义
            .build()

        bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        //BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // BLE广播Callback
        val mAdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d(TAG, "BLE广播开启成功")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.d(TAG, "BLE广播开启失败,错误码:$errorCode")
            }
        }

        // ============启动BLE蓝牙广播(广告) ===============
        val mBluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        mBluetoothLeAdvertiser.startAdvertising(
            settings,
            advertiseData,
            scanResponse,
            mAdvertiseCallback
        )

    }

//    fun startService(context: Context) {
//        // 注意：必须要开启可连接的BLE广播，其它设备才能发现并连接BLE服务端!
//        // =============启动BLE蓝牙服务端======================================
//        val service = BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY)
//
//        //添加可读+通知characteristic
//        val characteristicRead = BluetoothGattCharacteristic(
//            UUID_CHAR_READ_NOTIFY,
//            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
//            BluetoothGattCharacteristic.PERMISSION_READ
//        )
//        characteristicRead.addDescriptor(
//            BluetoothGattDescriptor(
//                UUID_DESC_NOTITY,
//                BluetoothGattCharacteristic.PERMISSION_WRITE
//            )
//        )
//        service.addCharacteristic(characteristicRead)
//
//        //添加可写characteristic
//        val characteristicWrite = BluetoothGattCharacteristic(
//            UUID_CHAR_WRITE,
//            BluetoothGattCharacteristic.PROPERTY_WRITE,
//            BluetoothGattCharacteristic.PERMISSION_WRITE
//        )
//        service.addCharacteristic(characteristicWrite)
//
//        if (bluetoothManager != null) {
//            gattServer =
//                bluetoothManager?.openGattServer(context, mBluetoothGattServerCallback)
//        }
//
//        gattServer.addService(service)
//
//
//    }

}
