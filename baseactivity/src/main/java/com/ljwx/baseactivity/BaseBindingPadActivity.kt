package com.ljwx.baseactivity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.ljwx.baseapp.util.OtherUtils

abstract class BaseBindingPadActivity<Binding : ViewDataBinding, BindingPad : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    @LayoutRes private val layoutResPad: Int
) : BaseStateRefreshActivity() {

    /**
     * DataBinding
     */
    protected lateinit var mBinding: Binding
    protected lateinit var mBindingPad: BindingPad

    protected var isPad = OtherUtils.isDevicePad()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSetContentView() {
        if (isPad) {
            mBindingPad = DataBindingUtil.setContentView(this, getLayoutRes())
        } else {
            mBinding = DataBindingUtil.setContentView(this, getLayoutRes())
        }
    }

    override fun getLayoutRes(): Int {
        return if (isPad) layoutResPad else layoutRes
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPad) {
            mBindingPad.unbind()
        } else {
            mBinding.unbind()
        }
    }

}