package com.ljwx.basebase.activity

import android.view.View

interface IBaseActivityCreateStep {

    fun onBeforeSetContentView()

    fun onSetContentView()

    fun onViewCreated(rootView: View)

}