package com.ljwx.baseapp.page

import com.ljwx.baseapp.router.IPostcard

interface IPageStartPage {

    fun startActivity(clazz: Class<*>, from: String? = null, requestCode: Int? = null)

    fun routerTo(path: String): IPostcard

}