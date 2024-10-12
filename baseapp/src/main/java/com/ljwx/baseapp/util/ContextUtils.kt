package com.ljwx.baseapp.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

object ContextUtils {

    fun getActivityFromContext(context: Context?): Activity? {
        if (context == null) {
            return null
        }
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return getActivityFromContext(context.baseContext)
        }
        return null
    }

}