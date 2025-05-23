package com.ljwx.basebase.datarepository

interface IBaseResponseData<Data> {

    fun isCodeSuccess(): Boolean

    fun isCodeSuccessAndDataNotNull(): Boolean

    fun getCode(): Int?

    fun getMessage(): String?

    fun getData(): Data

    fun isRefreshData(): Boolean

}