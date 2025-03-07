package com.ljwx.basebase.business

interface IBaseUserInfo {

    fun isLogin(): Boolean

    fun getUserId(): String?

    fun getUserName(): String?

    fun getUserInfoChangeType(): String

}