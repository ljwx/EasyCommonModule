package com.ljwx.basebase.page.binding

import androidx.viewbinding.ViewBinding

interface IPageViewBinding<VBinding : ViewBinding> {

    fun getViewBinding(): VBinding

}