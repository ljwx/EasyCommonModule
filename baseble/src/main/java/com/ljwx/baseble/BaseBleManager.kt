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
        BaseBleScanUtils.startScan(stateListener)
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

    interface BleStateListener {

        fun stateChange(
            code: Int,
            message: String,
            device: BluetoothDevice? = null,
            data: Any? = null
        )

    }

}