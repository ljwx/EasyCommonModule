package com.ljwx.basemodule

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils
import com.huawei.hms.mlsdk.common.MLApplication
import com.ljwx.baseapp.util.BaseAppUtils
import com.ljwx.baserefresh.BaseRefreshHeader
import com.ljwx.baserefresh.BaseRefreshLayout

class AppApplication : Application() ,ViewModelStoreOwner{

    override fun onCreate() {
        super.onCreate()

        BaseRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            BaseRefreshHeader(context, null, 0).getHeader()
        }

        Utils.init(this)
        BaseAppUtils.init(this)
        //初始化阿里路由框架
        if (BuildConfig.DEBUG) {
            ARouter.openLog() //
            ARouter.openDebug() // 开启调试模式
        }
        ARouter.init(this)
        MLApplication.initialize(applicationContext)
        MLApplication.getInstance().setApiKey("DQEDAPY/+37HeIsqBfU2UVZ00kN9fkujIGfsccHMR3bs8Er3OAA1KVd4bKKduEv6gNWSYEcxxwc6JyRNuFVErviEpJ9oe7IyrsfblA==");
    }

    override fun getViewModelStore(): ViewModelStore {
        return ViewModelStore()
    }

}