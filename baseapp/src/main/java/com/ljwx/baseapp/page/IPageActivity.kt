package com.ljwx.baseapp.page

import android.view.View

interface IPageActivity {

    fun getScreenOrientation(): Int?

    fun onViewCreated(rootView: View)

}