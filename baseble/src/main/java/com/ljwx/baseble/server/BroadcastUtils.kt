package com.ljwx.baseble.server

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import com.ljwx.baseble.ConstUUID
import java.util.UUID


object BroadcastUtils {

    private val UUID_SERVICE = UUID.fromString(ConstUUID.READ_DESCRIPTOR_UUID)

    fun init() {
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


    }

}