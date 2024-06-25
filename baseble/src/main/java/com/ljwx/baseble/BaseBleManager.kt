package com.ljwx.baseble

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class BaseBleManager private constructor() {

    private var context: Context? = null

    companion object {

        private var instance = BaseBleManager()

        fun getInstance(): BaseBleManager {
            return instance
        }
    }

    fun init(context: Context) {
        this.context = context
        BaseBleHomeUtils.init(context)
        BaseBleScanUtils.init(BaseBleHomeUtils.getAdapter())
    }

    fun checkPermission(): Boolean {
        return BaseBlePermissionUtils.permissionsGranted(context!!)
    }

    fun requestPermission(
        activity: AppCompatActivity,
        permission: String,
        result: (isGranted: Boolean) -> Unit
    ) {
        BaseBlePermissionUtils.requestSingle(activity, permission, result)
    }

    fun conditionCheck(context: Context): Int {
        if (!checkPermission()) {
            return BaseBleConst.CONDITION_PERMISSION
        }
        if (!BaseBleHomeUtils.isBleEnable()) {
            return BaseBleConst.CONDITION_BLE_ENABLE
        }
        if (!BaseBleHomeUtils.isLocationEnable(context)) {
            return BaseBleConst.CONDITION_LOCATION_ENABLE
        }
        return BaseBleConst.CONDITION_PASS
    }

    fun startScan() {
        BaseBleScanUtils.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                // result.getScanRecord() 获取BLE广播数据
                val device = result.device//获取BLE设备信息
                val deviceName = device.name;
                if (!deviceName.isNullOrEmpty()) {
                    //添加设备到列表
                    Log.d("蓝牙扫描结果", "设备名：$deviceName")
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d("蓝牙扫描结果失败", "失败码：$errorCode")
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }
        })
    }

    fun stopScan() {
        BaseBleScanUtils.stopScan()
    }

    fun connect(id: String) {
        BaseBleConnectUtils
    }

}