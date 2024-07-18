package com.ljwx.basemodule

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.ljwx.baseactivity.fast.QuickMainActivity
import com.ljwx.baseapp.extensions.singleClick
import com.ljwx.baseapp.infochange.IBaseUserInfo
import com.ljwx.basemediaplayer.LjwxMediaPlayer
import com.ljwx.basemodule.config.ConfigLaunchFunctionFragment
import com.ljwx.basemodule.constance.ConstRouter
import com.ljwx.basemodule.databinding.ActivityMainBinding
import com.ljwx.basemodule.fragments.*
import com.ljwx.basemodule.service.TestForegroundService
import com.ljwx.basemodule.vm.TestViewModel
import com.ljwx.baserecordaudio.RecordAudioManager
import com.ljwx.baserecordaudio.RecordAudioUtils
import com.ljwx.provideclipboardauto.ClipboardFragment

@Route(path = ConstRouter.FUNCTION_DEBUG_MAIN)
class MainActivity :
    QuickMainActivity<ActivityMainBinding, TestViewModel>(R.layout.activity_main) {

    override var enableUserInfoChangeListener = true
    private val dialog by lazy {
        TestDialog()
    }

    private val player by lazy { LjwxMediaPlayer(this) }

    override fun getTabLayout() = mBinding.tabLayout

    override fun getViewPager2() = mBinding.viewPager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        addTabFragment("ble", BleTestFragment())
//        addTabFragment("staterefresh", StateRefreshFragment())
//        addTabFragment("basefragment", BaseFragmentTest())
        addTabFragment("mediaPlayer", MediaPlayerTestFragment())
//        addTabFragment("mvvmFragment", LoginFragment())
//        addTabFragment("toolbar", BaseToolbarFragment())
        addTabFragment("loadmore", LoadMoreFragment())
//        addTabFragment("vmFragment", ViewModelFragment())
        addTabFragment("javaTest", ClipboardFragment())
        addTabFragment("config", ConfigLaunchFunctionFragment())

        unregisterLocalEvent("test4")
        var test = false
        RecordAudioUtils.registerPermission(this, object :ActivityResultCallback<Boolean> {
            override fun onActivityResult(result: Boolean?) {
                RecordAudioUtils.startTest(this@MainActivity)
                test = true
            }
        })
        mBinding.button.singleClick {
//            routerTo(ConstRouter.SECOND_ACTIVITY).start()
            if (!test) {
                RecordAudioUtils.startTest(this@MainActivity)
                test = true
            } else {
                RecordAudioUtils.stopTest()
                test = false
                RecordAudioManager.getInstance().getPathName()?.let {
                    player.setMediaItem(it)
                    player.prepare()
                    player.start()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, TestForegroundService::class.java))
        }

        registerLocalEvent("refresh") { action, type, value, intent ->
            Log.d("事件", "接收到刷新事件")
        }
        sendLocalEvent("refresh")
    }

    override fun getScreenOrientation() = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun userInfoChange(data: IBaseUserInfo?, type: Int) {
        super.userInfoChange(data, type)
        if (type == 3) {
            Log.d(TAG, "asdf1")
        }
    }

}