package com.ljwx.baseble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class BaseBleManager private constructor() {

    private var context: Context? = null
    private var gatt: BluetoothGatt? = null
    private var stateListener: BleStateListener? = null
    private var stateListener2: BleStateListener? = null
    private var stateListener3: BleStateListener? = null

    companion object {

        private var instance = BaseBleManager()

        fun getInstance(): BaseBleManager {
            return instance
        }
    }

    fun init(context: Context) {
        this.context = context
        BaseBleScanUtils.init(BaseBleHomeUtils.getAdapter(context))
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
        })
    }

    fun stopScan() {
        BaseBleScanUtils.stopScan()
    }

    fun connect(context: Context, device: BluetoothDevice?, autoConnect: Boolean): BluetoothGatt? {
        gatt = BaseBleConnectUtils.connectGATT(context, device, autoConnect, stateListener)
        BaseBleCommunicationUtils.init(gatt)
        return gatt
    }

    fun addStateListener(listener: BleStateListener) {
        stateListener = listener
    }

    fun read(): Boolean {
        return BaseBleCommunicationUtils.read()
    }

    fun write(): Boolean {
        return BaseBleCommunicationUtils.write("0A")
    }

    fun disConnect() {
        BaseBleConnectUtils.disconnect()
    }

    fun addStateListener2(listener: BleStateListener) {
        stateListener2 = listener
    }

    fun addStateListener3(listener: BleStateListener) {
        stateListener3 = listener
    }

    interface BleStateListener {

        fun stateChange(
            code: Int,
            message: String,
            device: BluetoothDevice? = null,
            data: Any? = null
        )

    }

}