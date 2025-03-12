package com.ljwx.basebase.util

import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

object NavigationBarUtils {

    fun getHeight(rootView: View): Int {
        var navBarHeight = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //在 Android 11（API 30）及以上 版本中，通过 WindowInsets.Type.navigationBars() 获取导航栏高度。
            val insets = rootView.rootWindowInsets
            if (insets != null) {
                navBarHeight = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 到 Android 10，使用过时 API（需忽略警告）
            val insets = rootView.rootWindowInsets;
            if (insets != null) {
                navBarHeight = insets.stableInsetBottom;
            }
        }
        if (navBarHeight != -1) {
            //通过 AndroidX 的 WindowInsetsCompat 库，可以统一处理所有 Android 版本，无需手动判断 API 等级。
            val insets = ViewCompat.getRootWindowInsets(rootView)
            navBarHeight = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        }
//        BaseModuleLog.dKeyboard("获取的导航栏高度:$navBarHeight", className)
        return max(navBarHeight, 0)
    }

}