package com.ljwx.basebase.activity

import android.view.View

interface IBaseActivity : IBaseActivityStatusBar {

    fun getScreenOrientation(): Int?

    fun onViewCreated(rootView: View)

}