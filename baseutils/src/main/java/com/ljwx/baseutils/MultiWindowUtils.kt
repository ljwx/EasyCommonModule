package com.ljwx.recordcheck

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

object MultiWindowUtils {

    @RequiresApi(Build.VERSION_CODES.N)
    fun startMultiWindowActivity(activity: Activity, target: Class<*>) {
        val intent = Intent(activity, target)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        val options = getActivityOptions(1)
        options.launchBounds = Rect(0, 0, 300, 300)
        val bundle = options.toBundle()
        activity.startActivity(intent, bundle)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getActivityOptions(type: Int): ActivityOptions {
        val options = ActivityOptions.makeBasic()
        try {
            val setDockCreateMode =
                ActivityOptions::class.java.getMethod("setDockCreateMode", Integer.TYPE)
            setDockCreateMode?.invoke(options, 1)
            val setLaunchStackId =
                ActivityOptions::class.java.getMethod("setLaunchStackId", Integer.TYPE)
            setLaunchStackId.invoke(options, 3)
        } catch (e: Exception) {
            Log.e("MultiWindowError", e.toString())
        }
        return options
    }

    fun launchAppInSplitScreen(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 检查设备是否支持分屏模式
            if (activity.packageManager.hasSystemFeature("android.software.leanback")) {
                Log.d("分屏", "支持分屏")
                val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
                if (intent != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            activity.startActivity(intent,
//                                ActivityOptions.makeBasic().setLaunchBounds(getSplitScreenBounds())
//                                    .toBundle()
//                            )
                        } else {
                            activity.startActivity(intent)
                        }
                    }
                }
            } else {
                Log.d("分屏", "不支持分屏")
                // 如果设备不支持分屏，提示用户或进行其他处理
                activity.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
            }
        }
    }

    private fun getSplitScreenBounds(activity: Activity): ActivityOptions {
        return ActivityOptions.makeBasic().setLaunchBounds(
            Rect(
                0, 0,
                (activity.getResources().getDisplayMetrics().widthPixels / 2) as Int,
                activity.getResources().getDisplayMetrics().heightPixels
            )
        )
    }


}