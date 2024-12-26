package com.ljwx.baseapp.util

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.Log

object KeyboardUtils {

    fun setKeyboardShowListener(context: Context?, listener: (show: Boolean) -> Unit) {
        ContextUtils.getActivityFromContext(context)?.let { setKeyboardShowListener(it, listener) }
    }

    fun setKeyboardShowListener(activity: Activity?, listener: (show: Boolean) -> Unit) {
        activity?.apply {
            val rootView = this.window.decorView
            val r = Rect()
            var lastHeight = 0
            rootView.viewTreeObserver.addOnGlobalLayoutListener {
                rootView.getWindowVisibleDisplayFrame(r)
                val height = r.height()
                if (lastHeight == 0) {
                    lastHeight = height
                } else {
                    val diff = lastHeight - height
                    if (diff > 200) {
                        Log.d("键盘", "键盘弹起")
                        lastHeight = height
                        listener(true)
                    } else if (diff < -200) {
                        lastHeight = height
                        Log.d("键盘", "键盘收起")
                        listener(false)
                    }
                }
            }
        }
    }

}