package com.ljwx.baseactivity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.ljwx.baseapp.util.BaseModuleLog

open class BaseBindingActivity<Binding : ViewDataBinding>(@LayoutRes private val layoutResID: Int) :
    BaseStateRefreshActivity() {

    protected lateinit var mBinding: Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, layoutResID)
        onViewCreated()
    }

    override fun onDestroy() {
        mBinding.unbind()
        super.onDestroy()
    }

}