package com.ljwx.basebase.eventbus

import android.content.Intent

interface IBaseLocalBroadcast {

    fun registerLocalEvent(action: String, observer: (simpleData: String?) -> Unit)

    fun registerLocalEventIntent(action: String, observer: (intent: Intent) -> Unit)

    fun unregisterLocalEvent(action: String?)

}