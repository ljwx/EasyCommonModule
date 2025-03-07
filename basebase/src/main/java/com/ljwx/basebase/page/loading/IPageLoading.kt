package com.ljwx.basebase.page.loading

interface IPageLoading {

    fun showLoadingDialog(show: Boolean, cancelable: Boolean = true)

    fun showLoadingPopup(show: Boolean, cancelable: Boolean = true)

    fun dismissLoadingDialog()

    fun dismissLoadingPopup()

}