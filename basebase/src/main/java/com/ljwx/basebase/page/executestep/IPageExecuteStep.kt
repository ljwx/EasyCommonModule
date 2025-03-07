package com.ljwx.basebase.page.executestep

interface IPageExecuteStep {

    fun enableUsualSteps()

    fun onUsualStep1InitData()

    fun onUsualStep2InitView()

    fun onUsualStep3ObserveData()

    fun onUsualStep4SetViewListener()

    fun onUsualStep5FetchData(refresh: Boolean = true)

}