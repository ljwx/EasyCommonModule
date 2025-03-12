package com.ljwx.basebase.activity

interface IBaseActivityScreenOrientation {

    fun getActivityOrientation(): Int

    fun setActivityOrientation(orientation: Int)

    fun isActivityLandscape(): Boolean

    fun setActivityLandscape()

    fun setActivityPortrait()

}