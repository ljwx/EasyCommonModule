package com.ljwx.basebase.globalconfig

import android.view.View

interface IBaseGlobalConfig {

    fun setCommonDialogLayout(view: View)

    fun setCommonMultiStateLoading(view: View)

    fun setCommonMultiStateError(view: View)

    fun setCommonMultiStateEmpty(view: View)

    fun setCommonMultiStateNoNetWork(view: View)

    fun setCommonMultiStateNoLogin(view: View)

    fun setCommonRefreshHeader(view: View)

}