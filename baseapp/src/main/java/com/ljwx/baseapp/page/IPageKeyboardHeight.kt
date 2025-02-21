package com.ljwx.baseapp.page

import android.view.View

interface IPageKeyboardHeight {

    fun enableKeyboardHeightListener(): Boolean

    fun setKeyboardHeightListener(
        rootView: View,
        callback: ((height: Int, visible: Boolean) -> Unit)?
    )

    fun getScreenRealHeight(): Int

    fun getNavigationBarHeight(rootView: View): Int

    fun onKeyboardHeightChanged(height: Int, visible: Boolean)

}