package com.ljwx.baseble

object BaseBleConst {

    //权限结果
    const val PERMISSION_GRANTED = 0
    const val PERMISSION_DENIED = -1
    const val PERMISSION_EXPLAIN = 1

    //蓝牙可用条件检测
    const val CONDITION_PASS = 20000
    const val CONDITION_PERMISSION = 20001
    const val CONDITION_BLE_ENABLE = 20002
    const val CONDITION_LOCATION_ENABLE = 20003

    const val STATE_SCAN_RESULT = 30001
    const val STATE_CONNECT_SUCCESS = 30002
    const val STATE_CONNECT_TIMEOUT = 30003
    const val STATE_DISCONNECT = 30004
    const val STATE_SERVICES_DISCOVERED = 30005
}