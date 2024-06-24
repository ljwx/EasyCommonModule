package com.ljwx.baseble

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings

object BaseBleHomeUtils {

    private val REQUEST_LOCATION_PERMISSION = 1
    private var adapter: BluetoothAdapter? = null

    fun hasBle(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun getAdapter(context: Context): BluetoothAdapter {
        return adapter
            ?: (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    fun isBleEnable(): Boolean {
        return adapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun enableBle(activity: Activity) {
        if (isBleEnable()) {
            //不建议强制打开蓝牙，官方建议通过Intent让用户选择打开蓝牙
            adapter?.enable();
//        val enableBleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        activity.startActivityForResult(enableBleIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    fun isLocationEnable(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //gps定位
        val isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        val isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider || isNetWorkProvider;
    }

    fun enableLocation(activity: Activity) {
        //开启位置服务，支持获取ble蓝牙扫描结果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationEnable(activity)) {
            val enableLocate = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(enableLocate, REQUEST_LOCATION_PERMISSION);
        }
    }

}