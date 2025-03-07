package com.ljwx.basebase.dialog

import android.app.Dialog

interface IBaseDialog {

    fun createCommonDialog(
        title: String? = null,
        content: String? = null,
        positiveText: String? = null,
    ): Dialog?

}