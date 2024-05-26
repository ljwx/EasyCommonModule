package com.ljwx.baseapp.constant

import androidx.annotation.IntDef

object BaseLayoutStatus {

    const val CONTENT = 0
    const val LOADING = 1
    const val EMPTY = 2
    const val ERROR = 3
    const val OFFLINE = 4
    const val EXTEND = 5
    const val EXTEND2 = 6
    const val EXTEND3 = 7

    @IntDef(
        CONTENT,
        LOADING,
        EMPTY,
        ERROR,
        OFFLINE,
        EXTEND,
        EXTEND2,
        EXTEND3,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class LayoutStatus

}