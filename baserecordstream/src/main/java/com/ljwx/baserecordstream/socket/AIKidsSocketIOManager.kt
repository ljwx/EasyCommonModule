package com.ljwx.baserecordstream.socket

import android.content.Context
import android.net.Uri
import com.codemao.toolssdk.aikids.steamrecord.DSAudioRecorder
import com.codemao.toolssdk.constant.CMTStatus
import com.codemao.toolssdk.manager.CMToolsManager
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.AudioByteEnd
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.AudioByteStart
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.SocketIOEventCommonResponse
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.SocketIOEventConnectACK
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.SocketIOEventJoinACK
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.SocketIOPromptResponse
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.SocketIOUUIDData
import com.codemao.toolssdk.model.dsbridge.compat.audiostream.SocketIOUUIDResponse
import com.codemao.toolssdk.utils.CTEnvUrlUtils
import com.codemao.toolssdk.utils.ExtLog
import com.codemao.toolssdk.utils.NetWorkHelper
import io.socket.client.Socket
import org.json.JSONObject
import java.util.UUID

class AIKidsSocketIOManager : SocketIOBase() {

    private val host = "cr-aichat.codemao.cn/"
    private val testHost = "http://test-cr-aichat.codemao.cn/"
    private val hostTest = "http://192.168.112.66:9096/"
    private val path = "/aichat"
    private val query =
        "stag=8&model=DOUBAO-DEEPSEEK-V3&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJDb2RlbWFvIEF1dGgiLCJ1c2VyX3R5cGUiOiJzdHVkZW50IiwiZGV2aWNlX2lkIjowLCJ1c2VyX2lkIjoxMDAwMDE2MDIxLCJpc3MiOiJBdXRoIFNlcnZpY2UiLCJwaWQiOiJPcU1WWHZYcCIsImV4cCI6MTc0NjE4NzA3NSwiaWF0IjoxNzQyMjk5MDc1LCJqdGkiOiI0NDdlNzIyNC1jZjMwLTQ2YTUtYTRmMS1mYmZhMWZlNmRmM2IifQ.0rMuJLn9j9XySqLhK12GipbZrUmCaR7E_Ol06VVlyUw&invoke_mode_type=EDIT&course_id=4&platform=web&EIO=3&transport=websocket"
    private val queryTest = "stag=8&EIO=4"

    private var token: String? = null
    private var courseId: String? = null
    private var isWebCallDisconnect = false

    private var context: Context? = null

    companion object {
        private val SERVER_EVENT_CONNECT_ACK = "on_connect_ack"

        private val CLIENT_EVENT_JOIN = "join"
        val CLIENT_EVENT_AUDIO_START = "push_audio_byte_start"
        private val CLIENT_EVENT_AUDIO_DATA = "push_audio_byte_data"
        val CLIENT_EVENT_AUDIO_END = "push_audio_byte_end"
        private val CLIENT_EVENT_CLEAR_AUDIO_BUFFER = "clear_audio_buffer"
        private val CLIENT_EVENT_OPEN_NEW_CONVERSATION = "open_new_conversation"
        private val CLENT_EVENT_BEGIN_SYSTEM_ASK = "push_begin_system_ask"
        private val CLENT_EVENT_COMMON_EVENT = "push_client_common_event"

        val SERVER_EVENT_JOIN_ACK = "join_ack"

        val SERVER_EVENT_ACK_AUDIO_START = "push_audio_byte_start_ack"
        private val SERVER_EVENT_ACK_AUDIO_DATA = "push_audio_byte_data_ack"
        val SERVER_EVENT_ACK_AUDIO_END = "push_audio_byte_end_ack"
        private val SERVER_EVENT_ACK_CLEAR_AUDIO_BUFFER = "clear_audio_buffer_ack"
        private val SERVER_EVENT_ACK_OPEN_NEW_CONVERSATION = "open_new_conversation_ack"
        private val CLENT_EVENT_ACK_BEGIN_SYSTEM_ASK = "push_begin_system_ask_ack"
        private val CLENT_EVENT_ACK_COMMON_EVENT = "push_client_common_event_ack"

        val SERVER_EVENT_TEXT_START = "server_push_text_start"
        val SERVER_EVENT_TEXT_DATA = "server_push_text_data"
        val SERVER_EVENT_TEXT_END = "server_push_text_end"
        val SERVER_EVENT_AUDIO_START = "server_push_audio_start"
        private val SERVER_EVENT_AUDIO_DATA_TEST = "server_push_media_data"
        private val SERVER_EVENT_AUDIO_DATA = "server_push_audio_data"
        val SERVER_EVENT_AUDIO_END = "server_push_audio_end"
        val SERVER_EVENT_TEXT_IMG_PROMPT = "server_push_text2img_prompt"
        val SERVER_EVENT_DISCONNECT = "server_push_to_disconnect"
        val SERVER_EVENT_COMMON_EVENT = "server_push_common_event"
    }

    private var connectListener: ((success: Boolean, code: Int?, response: Any?) -> Unit)? =
        null
    private var systemDisconnectListener: ((data: Any?) -> Unit)? = null
    private var eventListener: ((event: String, success: Boolean, code: Int?, response: Any?) -> Unit)? =
        null
    private var audioStreamListener: ((data: ByteArray) -> Unit)? = null

    fun initConfig(token: String?, courseId: String?) {
        this.token = token
        this.courseId = courseId
        exeInitConfig()
    }

    private fun exeInitConfig() {
        val query = appendQueryParameter(token, courseId).replace("?", "")
        ExtLog.dSocketIO("query参数:$query")
        val env = CTEnvUrlUtils.getCommonPrefix(CMToolsManager.getEnvMode())
        val uri = CTEnvUrlUtils.getScheme() + env + host
        clearAllEvent()
        setUrlConfig(uri, path, query)
        registerSystemEvent()
        registerConnectEvent()
        registerFeatureEvent()
        setReconnectionConfig()
        onServerPushEvent()
        openNewConversationResponse()
    }

    private fun appendQueryParameter(token: String?, courseId: String?): String {
        val uri = Uri.Builder()
        uri.appendQueryParameter("stag", "8")
        uri.appendQueryParameter("model", "DOUBAO-DEEPSEEK-V3")
        uri.appendQueryParameter("invoke_mode_type", "EDIT")
        uri.appendQueryParameter("course_id", courseId)
        uri.appendQueryParameter("platform", "android")
        uri.appendQueryParameter("EIO", "3")
        uri.appendQueryParameter("transport", "websocket")
        uri.appendQueryParameter("token", token)
        return uri.toString()
    }

    private fun registerFeatureEvent() {
        registerRecordResponse()
    }

    /**
     * 连接
     */
    fun startConnect(connectListener: ((success: Boolean, code: Int?, response: Any?) -> Unit)?) {
        this.connectListener = connectListener
        executeConnect()
    }

    /**
     * 系统断联
     */
    fun setSystemDisconnectListener(context: Context?, listener: ((data: Any?) -> Unit)?) {
        this.context = context
        this.systemDisconnectListener = listener
    }

    fun registerConnectEvent() {
        // 断开连接
        mSocket?.on(Socket.EVENT_DISCONNECT) { args ->
            val exception = args.firstOrNull()
//            if (exception != null && exception == "transport error") {
//                return@on
//            }
            ExtLog.dSocketIO("系统事件" + mSocket + ":断开连接:" + args.contentToString())
            callWebNetworkNotAvailable(args)
        }
        // 错误处理
        mSocket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            ExtLog.eSocketIO("系统事件:连接错误:$args")
            connectListener?.invoke(false, CMTStatus.socketConnectError, args)
            callWebNetworkNotAvailable(args)
        }
        mSocket?.on(SERVER_EVENT_CONNECT_ACK) {
            val data = parseData(it, SERVER_EVENT_CONNECT_ACK, SocketIOEventConnectACK::class.java)
            data?.apply {
                if (isSuccess()) {
                    /**
                     * 连接成功,发送join事件
                     */
                    commonEmitEvent(CLIENT_EVENT_JOIN, JSONObject())
                    eventListener?.invoke(SERVER_EVENT_CONNECT_ACK, true, 0, it.firstOrNull())
                } else if (timesLimit()) {
                    ExtLog.eSocketIO("达到次数上限")
                    connectListener?.invoke(false, CMTStatus.socketConnectError, data)
                    eventListener?.invoke(SERVER_EVENT_CONNECT_ACK, false, 0, it.firstOrNull())
                } else {
                    ExtLog.eSocketIO("连接异常")
                    connectListener?.invoke(false, CMTStatus.socketConnectError, data)
                    eventListener?.invoke(SERVER_EVENT_CONNECT_ACK, false, 0, it.firstOrNull())
                }
            }
        }
        mSocket?.on(SERVER_EVENT_JOIN_ACK) {
            val data = parseData(it, SERVER_EVENT_JOIN_ACK, SocketIOEventJoinACK::class.java)
            data?.apply {
                if (isSuccess()) {
                    ExtLog.dSocketIO("join成功")
                    connectListener?.invoke(true, 0, data)
                    eventListener?.invoke(SERVER_EVENT_JOIN_ACK, true, 0, it.firstOrNull())
                } else if (serverClose()) {
                    ExtLog.dSocketIO("join失败,服务端关闭了服务")
                    connectListener?.invoke(false, CMTStatus.socketJoinError, data)
                    eventListener?.invoke(SERVER_EVENT_JOIN_ACK, false, 0, it.firstOrNull())
                } else {
                    ExtLog.dSocketIO("join异常,其他异常")
                    connectListener?.invoke(false, CMTStatus.socketJoinError, data)
                    eventListener?.invoke(SERVER_EVENT_JOIN_ACK, false, 0, it.firstOrNull())
                }
            }
        }
    }

    private fun callWebNetworkNotAvailable(args: Array<Any>) {
        context?.let {
            Thread.sleep(100)
            if (NetWorkHelper.getDeviceJsApiNetworkStatus(it) == 3) {
                ExtLog.dSocketIO("发现当前无网络")
                if (!isWebCallDisconnect) {
                    systemDisconnectListener?.invoke(args)
                }
                isWebCallDisconnect = false
            }
        }
    }

    /*==============================================================*/
    /**
     * 录音流
     */
    fun pushRecordStart() {
        ExtLog.dSocketIO("推送开始录音给后端")
        val json = JSONObject()
        json.put("lan", "zh")
        json.put("format", if (DSAudioRecorder.isEnableAac()) "aac" else "pcm")
        commonEmitEvent(CLIENT_EVENT_AUDIO_START, json)
    }

    fun pushRecordData(bytes: ByteArray) {
        commonEmitEvent(CLIENT_EVENT_AUDIO_DATA, bytes)
    }

    fun pushRecordEnd(uuid: String?) {
        ExtLog.dSocketIO("推送停止录音给后端")
        val json = JSONObject()
        json.put("p_uuid", uuid)
        commonEmitEvent(CLIENT_EVENT_AUDIO_END, json)
    }

    fun registerRecordResponse() {
        //1.录音开始
        mSocket?.on(SERVER_EVENT_ACK_AUDIO_START) {
            val data =
                parseData(it, SERVER_EVENT_ACK_AUDIO_START, SocketIOEventCommonResponse::class.java)
            data?.apply {
                ExtLog.dSocketIO("开始录音事件响应结果:" + isSuccess() + ",code:" + code)
            }
            eventListener?.invoke(SERVER_EVENT_ACK_AUDIO_START, true, 1, it.firstOrNull())
        }
        //2.推送数据结果(失败才有)
        mSocket?.on(SERVER_EVENT_ACK_AUDIO_DATA) {
            val data = parseData(it, SERVER_EVENT_ACK_AUDIO_DATA, SocketIOUUIDResponse::class.java)
            ExtLog.dSocketIO("推送的音频流失败:" + data?.data?.content)
            eventListener?.invoke(SERVER_EVENT_ACK_AUDIO_DATA, false, 1, it.firstOrNull())
        }
        //录音结束结果
        mSocket?.on(SERVER_EVENT_ACK_AUDIO_END) {
            val data =
                parseData(it, SERVER_EVENT_ACK_AUDIO_END, SocketIOEventCommonResponse::class.java)
            data?.apply {
                ExtLog.dSocketIO("结束录音事件响应结果:" + isSuccess() + ",code:" + code)
            }
            eventListener?.invoke(SERVER_EVENT_ACK_AUDIO_END, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_ACK_CLEAR_AUDIO_BUFFER) {
            val data =
                parseData(it, SERVER_EVENT_ACK_CLEAR_AUDIO_BUFFER, SocketIOUUIDResponse::class.java)
            data?.apply {
                if (isSuccess()) {
                    ExtLog.dSocketIO("录音数据清除成功")
                }
            }
            eventListener?.invoke(SERVER_EVENT_ACK_CLEAR_AUDIO_BUFFER, true, 1, it.firstOrNull())
        }
        mSocket?.on(CLENT_EVENT_ACK_BEGIN_SYSTEM_ASK) {
            val data =
                parseData(it, CLENT_EVENT_ACK_BEGIN_SYSTEM_ASK, SocketIOUUIDResponse::class.java)
            data?.apply {
                if (isSuccess()) {
                    ExtLog.dSocketIO("系统首次提问成功")
                }
            }
            eventListener?.invoke(CLENT_EVENT_ACK_BEGIN_SYSTEM_ASK, true, 1, it.firstOrNull())
        }
        mSocket?.on(CLENT_EVENT_ACK_COMMON_EVENT) {
            val data =
                parseData(it, CLENT_EVENT_ACK_COMMON_EVENT, SocketIOUUIDResponse::class.java)
            data?.apply {
                if (isSuccess()) {
                    ExtLog.dSocketIO("通用事件响应成功")
                }
            }
            eventListener?.invoke(CLENT_EVENT_ACK_COMMON_EVENT, true, 1, it.firstOrNull())
        }
    }

    /*==============================================*/
    /**
     * 清除录音数据
     */
    fun cleanRecordBuffer(uuid: String?) {
        val json = JSONObject()
        json.put("p_uuid", uuid)
        commonEmitEvent(CLIENT_EVENT_CLEAR_AUDIO_BUFFER, json)
    }

    /**
     * 开启新会话
     */
    fun openNewConversation() {
        commonEmitEvent(CLIENT_EVENT_OPEN_NEW_CONVERSATION, JSONObject())
    }

    fun openNewConversationResponse() {
        mSocket?.on(SERVER_EVENT_ACK_OPEN_NEW_CONVERSATION) {
            val data = parseData(
                it,
                SERVER_EVENT_ACK_OPEN_NEW_CONVERSATION,
                SocketIOUUIDResponse::class.java
            )
            data?.apply {
                if (isSuccess()) {
                    ExtLog.dSocketIO("开启新会话成功")
                }
            }
        }
    }

    /*============================*/
    fun sendBeginSystemAsk() {
        commonEmitEvent(CLENT_EVENT_BEGIN_SYSTEM_ASK, JSONObject())
    }

    fun sendClientCommonEvent(data: Any?) {
        commonEmitEvent(CLENT_EVENT_COMMON_EVENT, data ?: "")
    }

    /*============================================================================*/
    /**
     * 服务端主动推送的事件
     */

    fun onServerPushEvent() {
        mSocket?.on(SERVER_EVENT_TEXT_START) {
            ExtLog.dSocketIO("后端要开始返回text流:" + it.contentToString())
            eventListener?.invoke(SERVER_EVENT_TEXT_START, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_TEXT_DATA) {
            ExtLog.dSocketIO("服务端给客户端文本流数据:" + mSocket + eventListener + ":" + it.contentToString())
            eventListener?.invoke(SERVER_EVENT_TEXT_DATA, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_TEXT_END) {
            ExtLog.dSocketIO("服务端给客户端媒体流结束:" + it.contentToString())
            eventListener?.invoke(SERVER_EVENT_TEXT_END, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_AUDIO_START) {
            ExtLog.dSocketIO("服务端推给客户端媒体流开始:" + it.contentToString())
            eventListener?.invoke(SERVER_EVENT_AUDIO_START, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_AUDIO_DATA) { array ->
//            ExtLog.dSocketIO2("服务端给客户端媒体流数据:" + array.contentToString())
            array.forEach { item ->
                try {
                    val bytes = item as ByteArray
                    audioStreamListener?.invoke(bytes)
                } catch (e: Exception) {
                    ExtLog.eSocketIO("音频流数据异常:$e")
                }
            }
        }
        mSocket?.on(SERVER_EVENT_AUDIO_END) {
            ExtLog.dSocketIO("服务端给客户端媒体流结束:" + it.contentToString())
            eventListener?.invoke(SERVER_EVENT_AUDIO_END, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_TEXT_IMG_PROMPT) {
            ExtLog.dSocketIO("服务端给客户端已经生成好的文生图prompt:" + it.contentToString())
            val data =
                parseData(it, SERVER_EVENT_TEXT_IMG_PROMPT, SocketIOPromptResponse::class.java)
            ExtLog.dSocketIO("文生图promptSessionId:" + data?.data?.audio_session_id)
            eventListener?.invoke(SERVER_EVENT_TEXT_IMG_PROMPT, true, 1, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_DISCONNECT) {
            //服务器发现有半小时无chat事件调用，则推送该事件，并且推送完该事件后1~2s钟服务器自动断开该连接
            ExtLog.dSocketIO("服务端主动断开连接:" + it.contentToString())
            val data = parseData(it, SERVER_EVENT_DISCONNECT, SocketIOUUIDResponse::class.java)
            data?.apply {
                if (isIDLE()) {
                    ExtLog.dSocketIO("长时间未操作而断开连接")
                } else if (isServerBusy()) {
                    ExtLog.dSocketIO("人数太多而断开连接")
                }
            }
            eventListener?.invoke(SERVER_EVENT_DISCONNECT, true, 0, it.firstOrNull())
        }
        mSocket?.on(SERVER_EVENT_COMMON_EVENT) {
            ExtLog.dSocketIO("服务器推送给客户端通用事件:" + it.contentToString())
            eventListener?.invoke(SERVER_EVENT_COMMON_EVENT, true, 1, it.firstOrNull())
        }
    }

    private fun clearAllEvent() {
        mSocket?.off(Socket.EVENT_DISCONNECT)
        mSocket?.off(Socket.EVENT_CONNECT_ERROR)
        mSocket?.off(SERVER_EVENT_CONNECT_ACK)
        mSocket?.off(SERVER_EVENT_JOIN_ACK)
        mSocket?.off(SERVER_EVENT_ACK_AUDIO_START)
        mSocket?.off(SERVER_EVENT_ACK_AUDIO_DATA)
        mSocket?.off(SERVER_EVENT_ACK_AUDIO_END)
        mSocket?.off(SERVER_EVENT_ACK_CLEAR_AUDIO_BUFFER)
        mSocket?.off(CLENT_EVENT_ACK_BEGIN_SYSTEM_ASK)
        mSocket?.off(CLENT_EVENT_ACK_COMMON_EVENT)
        mSocket?.off(SERVER_EVENT_TEXT_START)
        mSocket?.off(SERVER_EVENT_TEXT_DATA)
        mSocket?.off(SERVER_EVENT_TEXT_END)
        mSocket?.off(SERVER_EVENT_AUDIO_START)
        mSocket?.off(SERVER_EVENT_AUDIO_DATA)
        mSocket?.off(SERVER_EVENT_AUDIO_END)
        mSocket?.off(SERVER_EVENT_TEXT_IMG_PROMPT)
        mSocket?.off(SERVER_EVENT_DISCONNECT)
        mSocket?.off(SERVER_EVENT_COMMON_EVENT)
    }

    fun setEventListener(listener: ((event: String, success: Boolean, code: Int?, data: Any?) -> Unit)?) {
        this.eventListener = listener
    }

    fun setStreamListener(listener: ((data: ByteArray) -> Unit)? = null) {
        this.audioStreamListener = listener
    }

    override fun executeDisconnect(webCall: Boolean) {
        this.isWebCallDisconnect = webCall
        super.executeDisconnect(webCall)
    }


    @Deprecated(message = "测试用")
    fun receiveTestData() {
        ExtLog.dSocketIO("模拟接收数据")
        mSocket?.emit("clear_chatting_msg", gson.toJson(AudioByteEnd()))
    }

    fun destroy() {
        executeDisconnect(false)
        mSocket = null
        context = null
        connectListener = null
        eventListener = null
        systemDisconnectListener = null
        audioStreamListener = null
    }

}