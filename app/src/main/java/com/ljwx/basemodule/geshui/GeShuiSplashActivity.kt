package com.ljwx.basemodule.geshui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.ljwx.baseactivity.BaseBindingActivity
import com.ljwx.baseapp.extensions.delayRun
import com.ljwx.baseapp.extensions.singleClick
import com.ljwx.basemodule.R
import com.ljwx.basemodule.constance.ConstRouter
import com.ljwx.basemodule.databinding.ActivityGeShuiBinding

@Route(path = ConstRouter.FUNCTION_GESHUI_SPLASH)
class GeShuiSplashActivity : BaseBindingActivity<ActivityGeShuiBinding>(R.layout.activity_ge_shui) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarLight(false)
        mBinding.image.setImageResource(R.mipmap.splash)
        enableCommonSteps()
        delayRun(1500){
            routerTo(ConstGeShui.ROUTER_HOME).withFromType(1).start()
            finish()
        }
    }

    override fun commonStep4SetViewListener() {
        super.commonStep4SetViewListener()
    }

}