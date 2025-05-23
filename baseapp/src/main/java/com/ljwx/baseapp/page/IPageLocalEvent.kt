package com.ljwx.baseapp.page

import android.content.Intent
import com.ljwx.baseapp.event.ISendLocalEvent

interface IPageLocalEvent : ISendLocalEvent {

    fun registerLocalEvent(action: String, observer: (simpleData: String?) -> Unit)

    fun registerLocalEventIntent(action: String, observer: (intent: Intent) -> Unit)

//    @Deprecated(message = "too long")
//    fun registerLocalEvent(
//        action: String?,
//        observer: (action: String, type: Long?, value: String?, intent: Intent) -> Unit
//    )

    fun unregisterLocalEvent(action: String?)

}