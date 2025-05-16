package com.ljwx.baserecordstream.socket

import com.codemao.toolssdk.utils.ExtLog
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import java.net.URISyntaxException

open class SocketIOBase {

    protected var mSocket: Socket? = null
    private val options by lazy { IO.Options() }

    protected val gson by lazy { Gson() }

    protected fun setUrlConfig(host: String, path: String?, query: String?) {
        try {
            path?.let { options.path = it }
            options.transports = arrayOf(WebSocket.NAME)
            query?.let { options.query = it }
            options.forceNew = true
            options.rememberUpgrade = false
//            options.secure = false
            mSocket = null
            mSocket = IO.socket(host, options)
            ExtLog.eSocketIO("执行socket连接配置:$mSocket:$host")
        } catch (e: URISyntaxException) {
            ExtLog.eSocketIO("初始化异常:$e")
        }
    }

    protected fun setReconnectionConfig() {
        options.reconnection = false // 自动重连
        options.reconnectionDelay = 1000 // 重连延迟
        options.reconnectionDelayMax = 2000 // 最大重连延迟
        options.reconnectionAttempts = 2 // 重连尝试次数
        options.timeout = 5000 // 连接超时时间
        ExtLog.eSocketIO("设置重连")
    }

    protected fun registerSystemEvent() {
        // 连接成功
        mSocket?.off(Socket.EVENT_CONNECT)
        mSocket?.on(Socket.EVENT_CONNECT) { args ->
            ExtLog.dSocketIO("系统事件:已连接到服务器:" + args.contentToString())
        }
        mSocket?.off("error")
        mSocket?.on("error") { args ->
            ExtLog.eSocketIO("系统事件:error: " + args.contentToString())
        }
        mSocket?.off("message")
        mSocket?.on("message") {
            ExtLog.dSocketIO("系统事件:message:" + it.contentToString())
        }
    }

    fun executeConnect() {
        mSocket?.connect()
        ExtLog.eSocketIO("发起连接:$mSocket")
    }

    open fun executeDisconnect(webCall: Boolean) {
        mSocket?.disconnect()
    }

    protected fun commonEmitEvent(name: String, data: Any) {
//        val jsonData = gson.toJson(data)
        val jsonData = data
        if (name == "push_audio_byte_data") {
//            ExtLog.dSocketIO2("发送事件:$name,data:$jsonData")
        } else {
            ExtLog.dSocketIO("发送事件:$name,data:$jsonData")
        }
        mSocket?.emit(name, jsonData)
    }

//    open fun startConnect(connectListener: ((success: Boolean, code: Int?, response: Any?) -> Unit)?) {
//
//    }

    protected fun <T> parseData(arrayData: Array<Any>, event: String, clazz: Class<T>): T? {
        try {
            val json = arrayData[0]
            ExtLog.dSocketIO("原始数据:$json")
            if (json is JSONObject) {
                val bean = gson.fromJson(json.toString(), clazz)
                ExtLog.dSocketIO("$event,后端数据:$bean")
                return bean
            } else {
                ExtLog.eSocketIO("$event,数据结构异常")
                return null
            }
        } catch (e: Exception) {
            ExtLog.eSocketIO("$event,json异常:$e")
            return null
        }
    }

}