package com.ljwx.basemodulev2.activity

import android.view.View
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.ljwx.basebase.page.binding.IPageViewBinding

abstract class BaseVBindingActivity<VBinding : ViewBinding>(@LayoutRes private val layoutResID: Int) :
    BaseActivity(layoutResID), IPageViewBinding<VBinding> {

    protected lateinit var binding: VBinding

    override fun onSetContentView() {
        binding = getViewBinding()
    }

    override fun onViewCreated(rootView: View) {
        super.onViewCreated(rootView)

    }
}