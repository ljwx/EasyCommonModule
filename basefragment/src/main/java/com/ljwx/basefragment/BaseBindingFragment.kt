package com.ljwx.basefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.ljwx.baseapp.util.BaseModuleLog

abstract class BaseBindingFragment<Binding : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    BaseStateRefreshFragment(layoutRes) {

    /**
     * DataBinding
     */
    protected lateinit var mBinding: Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        return mBinding.root
    }

    override fun onDestroy() {
        mBinding.unbind()
        super.onDestroy()
    }

}