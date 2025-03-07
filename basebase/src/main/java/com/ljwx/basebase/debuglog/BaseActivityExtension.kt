package com.ljwx.basebase.debuglog

import android.util.Log
import com.ljwx.basebase.constant.BaseConstLogTag

inline fun IBaseLogClassName.runActivity(content: String) {
    Log.d(BaseConstLogTag.ACTIVITY_RUN, content + "-" + getClassNameTag())
}

inline fun IBaseLogClassName.runFragment(content: String) {
    Log.d(BaseConstLogTag.FRAGMENT_RUN, content + "-" + getClassNameTag())
}