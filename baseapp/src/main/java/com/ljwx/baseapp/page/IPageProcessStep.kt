package com.ljwx.baseapp.page

interface IPageProcessStep {

    fun enableCommonSteps()

    fun commonStep1InitData()

    fun commonStep2InitView()

    fun commonStep3ObserveData()

    fun commonStep4SetViewListener()

    fun commonStep5RequestData(refresh: Boolean = true)

}