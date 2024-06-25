package com.ljwx.basemodule.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ljwx.baseapp.extensions.showToast
import com.ljwx.baseapp.extensions.singleClick
import com.ljwx.baseble.BaseBleConst
import com.ljwx.baseble.BaseBleManager
import com.ljwx.baseble.BaseBlePermissionUtils
import com.ljwx.basefragment.BaseBindingFragment
import com.ljwx.basemodule.R
import com.ljwx.basemodule.databinding.FragmentBleTestBinding

class BleTestFragment :
    BaseBindingFragment<FragmentBleTestBinding>(R.layout.fragment_ble_test) {

    private var bleDevice: BluetoothDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BaseBleManager.getInstance().init(requireContext())
        mBinding.playLink

        mBinding.check.singleClick {
            val state = BaseBleManager.getInstance().conditionCheck(requireContext())
            when (state) {
                BaseBleConst.CONDITION_PERMISSION -> {
                    showToast("权限不够")
                    BaseBlePermissionUtils.requestMultiple(requireActivity() as AppCompatActivity) {
                        showToast(it.key + (if (it.value) "通过" else "未通过"))
                    }
                }

                BaseBleConst.CONDITION_BLE_ENABLE -> {
                    showToast("蓝牙没开")
                }

                BaseBleConst.CONDITION_LOCATION_ENABLE -> {
                    showToast("定位没开")
                }

                BaseBleConst.CONDITION_LOCATION_ENABLE -> {
                    showToast("全部通过")
                }
            }
        }

        mBinding.scan.singleClick {
            BaseBleManager.getInstance().startScan()
        }

        mBinding.connect.singleClick {
            BaseBleManager.getInstance().connect(requireContext(), bleDevice, false)
        }

        mBinding.stopScan.singleClick {
            BaseBleManager.getInstance().stopScan()
        }

        BaseBleManager.getInstance().addStateListener(object : BaseBleManager.BleStateListener {
            override fun stateChange(
                code: Int,
                message: String,
                device: BluetoothDevice?,
                data: Any?
            ) {
                Log.d("蓝牙", "$message,$code")
                when (code) {
                    BaseBleConst.STATE_SCAN_RESULT -> {
                        if (message.equals("Redmi Buds 3")) {
                            BaseBleManager.getInstance().stopScan()
                            bleDevice = device
                        }
                    }
                }
            }

        })

    }

}