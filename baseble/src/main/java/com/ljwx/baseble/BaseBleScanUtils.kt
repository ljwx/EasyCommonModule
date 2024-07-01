package com.ljwx.baseble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

object BaseBleScanUtils {

    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    fun init(adapter: BluetoothAdapter?) {
        this.adapter = adapter
    }

    @SuppressLint("MissingPermission")
    fun startScan(stateListener: BaseBleManager.BleStateListener?) {
        scanner = scanner ?: adapter?.bluetoothLeScanner
        // 下面使用Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，
        // 而新API帮我们解析成ScanRecord类
        if (this.scanCallback != null) {
            stopScan()
            this.scanCallback = null
        }
        this.scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                // result.getScanRecord() 获取BLE广播数据
                val device = result.device//获取BLE设备信息
                val deviceName = device.name ?: "空的"
                stateListener?.stateChange(
                    BaseBleConst.STATE_SCAN_SUCCESS_RESULT,
                    "设备名：$deviceName", device
                )
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d("蓝牙扫描结果失败", "失败码：$errorCode")
                stateListener?.stateChange(BaseBleConst.STATE_SCAN_FAIL, "错误码：$errorCode")
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }
        }
        scanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        Log.d("蓝牙扫描", "停止扫描")
        scanner?.stopScan(scanCallback)
    }

}