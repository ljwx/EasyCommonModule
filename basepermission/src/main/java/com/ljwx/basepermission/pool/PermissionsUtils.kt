package com.ljwx.basepermission.pool

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ljwx.baseapp.util.ContextUtils

object PermissionsUtils {

    private val TAG = "权限池"
    private var blocking = false

    private val permissionsPool = LinkedHashMap<Int, Array<String>>()

    fun request(
        context: Context,
        requestCode: Int,
        permissions: List<String>,
        clear: Boolean = false
    ) {
        addPermissions(context, requestCode, permissions.toTypedArray(), clear)
    }

    fun request(
        context: Context,
        requestCode: Int,
        permissions: Array<String>,
        clear: Boolean = false
    ) {
        addPermissions(context, requestCode, permissions, clear)
    }

    fun request(
        context: Context,
        requestCode: Int,
        permissions: String,
        clear: Boolean = false
    ) {
        addPermissions(context, requestCode, arrayOf(permissions), clear)
    }

    private fun addPermissions(
        context: Context,
        requestCode: Int,
        permissions: Array<String>,
        clear: Boolean
    ) {
        if (clear) {
            permissionsPool.clear()
        }
        Log.d(TAG, "添加的权限:" + permissions.contentToString())
        permissionsPool[requestCode] = permissions.asList().toTypedArray()
        conditionExecuteRequest(context)
    }


    private fun getPriorityPermissions(removeFromPool: Boolean = true): Map.Entry<Int, Array<String>>? {
        val iterator = permissionsPool.iterator()
        while (iterator.hasNext()) {
            val permissions = iterator.next()
            if (removeFromPool) {
                iterator.remove()
            }
            return permissions
        }
        return null
    }

    private fun conditionExecuteRequest(context: Context) {
        val activity = ContextUtils.getActivityFromContext(context)
        if (!blocking && activity != null) {
            val entry = getPriorityPermissions()
            if (entry != null) {
                justRequest(activity, entry.value, entry.key)
                blocking = true
            } else {
                blocking = false
            }
        } else {
            Log.d(TAG, "条件不满足,不发起权限请求")
        }
    }

    private fun justRequest(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun hasResult(context: Context, requestCode: Int) {
        blocking = false
        conditionExecuteRequest(context)
    }

}