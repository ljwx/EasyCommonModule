package com.ljwx.baseapp.event

import android.content.Intent

interface ISendLocalEvent {

    fun sendLocalEvent(action: String, simpleData: String?)

    fun sendLocalEvent(action: String, dataIntent: Intent)

//    fun sendLocalEvent(action: String?, type: Long? = null, value: String? = null)

}