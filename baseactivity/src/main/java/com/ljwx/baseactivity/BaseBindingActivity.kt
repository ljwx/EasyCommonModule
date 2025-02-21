package com.ljwx.baseactivity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

open class BaseBindingActivity<Binding : ViewDataBinding>(@LayoutRes private val layoutResID: Int) :
    BaseStateRefreshActivity() {

    protected lateinit var mBinding: Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSetContentView() {
        mBinding = DataBindingUtil.setContentView(this, layoutResID)
    }

    override fun onDestroy() {
        mBinding.unbind()
        super.onDestroy()
    }

}