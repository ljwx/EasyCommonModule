package com.ljwx.basebase.activity

import android.view.View
import com.ljwx.basebase.navigationbar.IBaseNavigationBar
import com.ljwx.basebase.page.usualstep.IPageUsualStep

interface IBaseActivity : IBaseActivityCreateStep, IBaseActivityScreenOrientation,
    IBaseActivityStatusBar, IBaseActivityToolbar, IBaseStartActivity, IBaseNavigationBar ,IPageUsualStep{

    fun getLayoutRes(): Int

    fun getRootView(): View

}