package com.ljwx.basemodule.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ljwx.baseapp.extensions.showToast
import com.ljwx.baseapp.extensions.singleClick
import com.ljwx.baseble.BaseBleConst
import com.ljwx.baseble.BaseBleManager
import com.ljwx.baseble.BaseBlePermissionUtils
import com.ljwx.basefragment.BaseBindingFragment
import com.ljwx.basemodule.R
import com.ljwx.basemodule.databinding.FragmentBleTestBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.concurrent.ConcurrentHashMap

class BleTestFragment :
    BaseBindingFragment<FragmentBleTestBinding>(R.layout.fragment_ble_test) {

    private var bleDevice: BluetoothDevice? = null

    private val devices = ConcurrentHashMap<String, BluetoothDevice>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BaseBleManager.getInstance().init(requireContext())
        mBinding.playLink

        mBinding.check.singleClick {
            val state = BaseBleManager.getInstance().conditionCheck(requireContext())
            when (state) {
                BaseBleConst.CONDITION_PERMISSION -> {
                    showToast("权限不够")
                    BaseBlePermissionUtils.requestOld(requireActivity())
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
            lifecycleScope.launch(Dispatchers.IO) {
                while (true) {
                    delay(2000)
                    val names = StringBuilder()
                    devices.forEach {
                        names.append(it.value.name+",")
                    }
                    if (!names.isNullOrEmpty()) {
                        Log.d("蓝牙", names.toString())
                    }
                }
            }
        }

        mBinding.connect.singleClick {
            BaseBleManager.getInstance().stopScan()
            devices.forEach {
                if (it.value.name.contains("tudao", true)) {
                    bleDevice = it.value
                }
            }
            BaseBleManager.getInstance().connect(requireContext(), bleDevice, false)
        }

        mBinding.stopScan.singleClick {
            BaseBleManager.getInstance().stopScan()
            devices.clear()
        }

        BaseBleManager.getInstance().addStateListener(object : BaseBleManager.BleStateListener {
            override fun stateChange(
                code: Int,
                message: String,
                device: BluetoothDevice?,
                data: Any?
            ) {
                if (code == BaseBleConst.STATE_SCAN_SUCCESS_RESULT) {
                    device?.let {
                        if (it.address !in devices.keys && !it.name.isNullOrEmpty()) {
                            devices.put(it.address, it)
                        }
                    }
                } else {
                    Log.d("蓝牙", "$message,$code")
                }
                when (code) {
                    BaseBleConst.STATE_DISCONNECT -> {
                        BaseBleManager.getInstance().disConnect()
                    }
                    BaseBleConst.STATE_SCAN_SUCCESS_RESULT -> {
                        if (message.contains("catbit")) {
                            BaseBleManager.getInstance().stopScan()
                            bleDevice = device
                        }
                    }
                    BaseBleConst.STATE_CONNECT_SUCCESS -> {
                        devices.clear()
                    }
                    BaseBleConst.STATE_SERVICES_DISCOVERED -> {
//                        val read = BaseBleManager.getInstance().read()
//                        Log.d("蓝牙", "读是否成功：$read")
//                        val write = BaseBleManager.getInstance().write()
//                        Log.d("蓝牙", "写是否成功：$write")
                    }
                }
            }

        })

    }

}