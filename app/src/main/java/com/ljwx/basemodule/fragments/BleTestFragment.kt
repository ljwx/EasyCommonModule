package com.ljwx.basemodule.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import com.ljwx.baseapp.extensions.singleClick
import com.ljwx.baseble.BaseBleManager
import com.ljwx.basefragment.BaseBindingFragment
import com.ljwx.basemediaplayer.LjwxMediaPlayer
import com.ljwx.basemediaplayer.LjwxMediaPlayerListener
import com.ljwx.basemodule.R
import com.ljwx.basemodule.databinding.FragmentBleTestBinding

class BleTestFragment :
    BaseBindingFragment<FragmentBleTestBinding>(R.layout.fragment_ble_test) {

    private val player by lazy { LjwxMediaPlayer(requireContext(), requireContext().cacheDir.path) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BaseBleManager.getInstance().init(requireContext())
        mBinding.playLink

        mBinding.check.singleClick {
            BaseBleManager.getInstance().checkPermission()
        }

        mBinding.scan.singleClick {
            BaseBleManager.getInstance().startScan()
        }

        mBinding.connect.singleClick {
            BaseBleManager.getInstance().connect("")
        }

        mBinding.stopScan.singleClick {
            BaseBleManager.getInstance().stopScan()
        }

    }

}