package com.ljwx.basebase.activity

import androidx.annotation.ColorInt

interface IBaseActivityStatusBar {

    fun setStatusBarColor(@ColorInt color: Int)

    fun setStatusBarFontDark(dark: Boolean)

}