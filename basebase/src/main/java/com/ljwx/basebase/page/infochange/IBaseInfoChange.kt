package com.ljwx.basebase.page.infochange

import com.ljwx.basebase.business.IBaseAppConfig
import com.ljwx.basebase.business.IBaseUserConfig
import com.ljwx.basebase.business.IBaseUserInfo

interface IBaseInfoChange {

    fun onUserInfoChange(userInfo: IBaseUserInfo)

    fun onUserConfigChange(userConfig: IBaseUserConfig)

    fun onAppConfigChange(appConfig: IBaseAppConfig)

}