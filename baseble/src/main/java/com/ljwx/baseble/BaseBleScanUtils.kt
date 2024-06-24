package com.ljwx.baseble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

object BaseBleScanUtils {

    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    fun setAdapter(adapter: BluetoothAdapter) {
        this.adapter = adapter
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        scanner = scanner ?: adapter?.bluetoothLeScanner
        // 下面使用Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，
        // 而新API帮我们解析成ScanRecord类
        if (scanCallback != null) {
            stopScan(scanCallback!!)
            scanCallback = null
        }
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                // result.getScanRecord() 获取BLE广播数据
                val device = result.device//获取BLE设备信息
                val deviceName = device.name;
                if (!deviceName.isNullOrEmpty()) {
                    //添加设备到列表

                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }
        }
        scanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan(scanCallback: ScanCallback) {
        scanner?.stopScan(scanCallback)
    }

}