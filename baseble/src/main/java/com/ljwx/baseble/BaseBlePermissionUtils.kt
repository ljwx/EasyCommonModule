package com.ljwx.baseble

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object BaseBlePermissionUtils {

    private val mPermissionList = ArrayList<String>()

    private fun getPermissions(): Array<String> {
        val permissionList = ArrayList<String>()
        // Android 版本大于等于 12 时，申请新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            //根据实际需要申请定位权限
            mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
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

    fun checkPermission(activity: Activity, permission: String): Int {
        // 检查权限状态
        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return BaseBleConst.PERMISSION_GRANTED
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // 需要向用户解释为什么需要这个权限
            return BaseBleConst.PERMISSION_EXPLAIN
        } else {
            // 请求权限
            return BaseBleConst.PERMISSION_DENIED
        }
    }

    fun requestOld(activity: Activity) {
        ActivityCompat.requestPermissions(activity, getPermissions(), 1)
    }

    fun requestSingle(
        activity: AppCompatActivity,
        permission: String,
        result: (isGranted: Boolean) -> Unit
    ) {
        val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            result.invoke(isGranted)
            if (isGranted) {
                // 权限已授予，可以继续操作
            } else {
                // 权限被拒绝，解释为什么需要这个权限
            }
        }
        requestPermissionLauncher.launch(permission)
    }

    fun requestMultiple(
        activity: AppCompatActivity,
        permissionResult: (item: Map.Entry<String, Boolean>) -> Unit
    ) {
        val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.entries.forEach {
                permissionResult.invoke(it)
                val permission = it.key
                val isGranted = it.value
                if (isGranted) {
                    // 权限已授予
                } else {
                    // 权限被拒绝
                }
            }
        }
        requestPermissionLauncher.launch(getPermissions())
    }

}