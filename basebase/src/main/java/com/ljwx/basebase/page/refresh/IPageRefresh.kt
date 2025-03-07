package com.ljwx.basebase.page.refresh

interface IPageRefresh {

    fun initPullRefreshView()

    fun enablePullRefreshView(enable: Boolean = true)

    fun onPullRefresh()

    fun onPullRefreshFinished()

}