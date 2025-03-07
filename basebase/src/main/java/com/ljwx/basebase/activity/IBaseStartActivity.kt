package com.ljwx.basebase.activity

import androidx.activity.result.ActivityResult

interface IBaseStartActivity {

    fun startActivitySimple(
        clazz: Class<*>,
        from: Int? = null,
        resultCallback: ((result: ActivityResult) -> Unit)? = null
    )

}