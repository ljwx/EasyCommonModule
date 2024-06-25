package com.ljwx.basepermission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    const val PERMISSION_GRANTED = 0
    const val PERMISSION_DENIED = -1
    const val PERMISSION_EXPLAIN = 1

    fun checkPermission(activity: Activity, permission: String): Int {
        // 检查权限状态
        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return PERMISSION_GRANTED
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // 需要向用户解释为什么需要这个权限
            return PERMISSION_EXPLAIN
        } else {
            // 请求权限
            return PERMISSION_DENIED
        }
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
        permission: Array<String>,
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
        requestPermissionLauncher.launch(permission)
    }

}