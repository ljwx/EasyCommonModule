package com.ljwx.basemodulev2.test

import android.view.View
import android.widget.Toast
import com.ljwx.basemodulev2.R
import com.ljwx.basemodulev2.activity.BaseVBindingActivity
import com.ljwx.basemodulev2.databinding.ActivityViewBindingTestBinding

class BaseModuleTestActivity :
    BaseVBindingActivity<ActivityViewBindingTestBinding>(R.layout.activity_view_binding_test) {

    override fun getViewBinding(): ActivityViewBindingTestBinding {
        return ActivityViewBindingTestBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(rootView: View) {
        super.onViewCreated(rootView)
        setToolbarTitle("change")
    }

    override fun onUsualStep4SetViewListener() {
        super.onUsualStep4SetViewListener()
        binding.testButton1.setOnClickListener {
            setStatusBarDark()
        }
        binding.testButton2.setOnClickListener {
            setStatusBarLight()
        }
    }

}