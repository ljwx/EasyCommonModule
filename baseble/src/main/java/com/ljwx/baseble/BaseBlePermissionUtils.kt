package com.ljwx.baseble

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object BaseBlePermissionUtils {

    private val REQUEST_PERMISSION_CODE = 2
    private val mPermissionList = ArrayList<String>()

    private fun getPermissions(): Array<String> {
        val permissionList = ArrayList<String>()
        // Android 版本大于等于 12 时，申请新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            //根据实际需要申请定位权限
//            mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//            mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            //Android 6.0开始 需要定位权限
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        return mPermissionList.toTypedArray()
    }

    fun permissionsGranted(context: Context): Boolean {
        var hasPermission = false
        getPermissions().forEach {
            val permissionCheck = ContextCompat.checkSelfPermission(context, it)
            hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED
        }
        return hasPermission
    }

    fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, getPermissions(), REQUEST_PERMISSION_CODE)
    }


}