package com.ljwx.baserecordstream.manager

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.ljwx.baserecordstream.player.RealtimePcmPlayerWithAudioTrack
import com.ljwx.baserecordstream.recoder.DSAudioRecorder
import com.ljwx.baserecordstream.socket.AIKidsSocketIOManager
import org.json.JSONObject

abstract class AIKidsToolsManagerBiz  {

    private var socketManager: AIKidsSocketIOManager? = null
    private var recordStreamManager: DSAudioRecorder? = null
    private var playerStreamManager: RealtimePcmPlayerWithAudioTrack? = null

    private var isLifecycleStop = false
    private val lastAudioStream = ArrayList<ByteArray>()

    private var isRecordTimeout: Boolean = false
    private var recordStartListener: ((code: Int) -> Unit)? = null
    private var recordUuid: String? = null

    private var courseCode: String? = null

    private fun initAIKidsConfig() {
        if (isWorkAIKids()) {
            ExtLog.dSocketIO("执行AIKidsManager初始化")
            initAIKidsBiz()
            playerStreamManager = playerStreamManager ?: RealtimePcmPlayerWithAudioTrack()
        }
    }

    private val streamRecordTimeoutRunnable = Runnable {
        ExtLog.dAIKids("录音启动超时了")
        isRecordTimeout = true
        recordStartListener?.invoke(CMTStatus.recorderArgInvalidError)
    }

    private fun initAIKidsBiz() {
        socketManager = socketManager ?: AIKidsSocketIOManager()
        socketManager?.initConfig(getUserToken(), courseCode)
        socketManager?.setStreamListener {
            playerStreamManager?.enqueuePcmData(it)
        }
        socketManager?.setSystemDisconnectListener(getWRContext()) {
            sendSystemDisconnect(it)
        }
        socketManager?.setEventListener { event, success, code, data ->
            val eventData = JSONObject()
            eventData.put("name", event)
            eventData.put("payload", data)
            getWRCppView()?.socketPostAsyncEvent(event, data, {

            }, { code, message ->

            })
            when (event) {
                AIKidsSocketIOManager.SERVER_EVENT_ACK_AUDIO_START -> {
                    if (data is JSONObject && data.has("data")) {
                        val d = data.get("data")
                        var uuidKey = "p_uuid"
                        if (d is JSONObject && d.has(uuidKey)) {
                            recordUuid = d.get(uuidKey) as? String
                        }
//                        recordUuid = data.data?.p_uuid
                    }
                    getWRContext()?.let {
                        first = recordStreamManager == null
                        recordStreamManager = recordStreamManager ?: DSAudioRecorder()
                        if (first) {
                            recordStreamManager?.setAudioDataCallback {
                                socketManager?.pushRecordData(it)
                            }
                            recordStreamManager?.setVolumeLevelChangeListener {
                                sendAudioStreamVolume(it.toFloat())
                            }
                            first = false
                        }
                    }
                    if (!isRecordTimeout) {
                        getWRContext()?.let {
                            if (ContextCompat.checkSelfPermission(
                                    it,
                                    Manifest.permission.RECORD_AUDIO
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ExtLog.dAIKids("想录音,但没有权限")
                                recordStartListener?.invoke(CMTStatus.recorderPermissionDeniedError)
                            } else {
                                ExtLog.dAIKids("没有超时,正常录音")
                                recordStreamManager?.startRecording { success, code ->
                                    recordStartListener?.invoke(code)
                                    getMainHandler()?.removeCallbacks(streamRecordTimeoutRunnable)
                                }
                            }
                        }
                    }
                }

                AIKidsSocketIOManager.SERVER_EVENT_ACK_AUDIO_END -> {

                }

                AIKidsSocketIOManager.SERVER_EVENT_AUDIO_START -> {
                    startPlayerStream(false)
                }

                AIKidsSocketIOManager.SERVER_EVENT_AUDIO_END -> {
                    ExtLog.dAIKids("收到音频流结束")
                    playerStreamManager?.enqueuePcmData(RealtimePcmPlayerWithAudioTrack.END_OF_STREAM)
                }

                AIKidsSocketIOManager.SERVER_EVENT_TEXT_START -> {

                }

                AIKidsSocketIOManager.SERVER_EVENT_TEXT_DATA -> {
                }

                AIKidsSocketIOManager.SERVER_EVENT_TEXT_END -> {

                }

            }
        }
    }

    /**
     * js调原生
     */

    private fun startConnect(connectListener: ((success: Boolean, code: Int?, response: Any?) -> Unit)?) {
        getCourseCode()
        ExtLog.dSocketIO("执行AIKidsManager的socket连接:$socketManager")
        socketManager?.startConnect(connectListener)
        getMainHandler()?.post {
            getWRCppView()?.getCurrentView()?.keepScreenOn = true
        }
    }

    private var first = true
    override fun startRecordStream(timeOut: Long?, result: (code: Int) -> Unit) {
        isRecordTimeout = false
        this.recordStartListener = result
        socketManager?.cleanRecordBuffer(recordUuid)
        socketManager?.pushRecordStart()
        ExtLog.dAIKids("录音超时倒计时:" + getMainHandler())
        getMainHandler()?.postDelayed(streamRecordTimeoutRunnable, timeOut ?: 60000)
    }

    override fun stopRecordStream(normal: Boolean) {
        if (recordStreamManager?.isRecord() == true) {
            if (normal) {
                socketManager?.pushRecordEnd(recordUuid)
            }
        }
        recordStreamManager?.stopRecording()
    }

    override fun startPlayerStream(replay: Boolean) {
        playerStreamManager?.startPlaying {
            stopPlayerStream()
        }
        sendAudioStreamStart()
    }

    override fun stopPlayerStream() {
        playerStreamManager?.stopPlaying()
        sendAudioStreamEnd()
    }

    override fun startSocketConnect(listener: ((success: Boolean, code: Int?, response: Any?) -> Unit)?) {
        startConnect(listener)
    }

    override fun stopSocketConnect(webCall: Boolean) {
        socketManager?.executeDisconnect(webCall)
    }

    override fun downloadImageToAlbum() {

    }

    override fun startAISystemAsk() {
        socketManager?.sendBeginSystemAsk()
    }

    override fun sendAICommonEvent(data: Any?) {
        socketManager?.sendClientCommonEvent(data)
    }

    override fun callAIKidsClientReady(
        success: ((iResult: String?) -> Unit),
        fail: ((errorCode: Int, message: String?) -> Unit)
    ) {
        ExtLog.dAIKids("通知web client ready")
        getWRCppView()?.socketPostAsyncEvent("client_ready", success, fail)
    }

    private fun sendAudioStreamVolume(value: Float) {
        val payload = JsonObject()
        val safeVolume = when {
            value.isInfinite() -> -80.0 // 替换为默认值
            value.isNaN() -> -80.0
            else -> value
        }
        payload.addProperty("sound", safeVolume)
        val toolsEvent = MessageOptions("receiveSound", payload)
        val data = JSONObject(Gson().toJson(toolsEvent))
        getWRCppView()?.socketPostAsyncEvent(data, {

        }, { code, message ->

        })
    }

    private fun sendAudioStreamStart() {
        getWRCppView()?.socketPostAsyncEvent("client_audio_play_start", {

        }, { code, message ->

        })
    }

    private fun sendAudioStreamEnd() {
        getWRCppView()?.socketPostAsyncEvent("client_audio_play_end", {

        }, { code, message ->

        })
    }

    private fun sendSystemDisconnect(it: Any?) {
        getWRCppView()?.socketPostAsyncEvent("client_disconnect", it, {

        }, { code, message ->

        })
    }

    fun sendBeginSystemAsk() {
        socketManager
    }

    private fun getCourseCode(): String? {
        val lastUrl = getLastLoadUrl()
        var courseCode: String? = null
        try {
            courseCode = Uri.parse(lastUrl).getQueryParameter("courseCode") ?: "empty"
        } catch (e: Exception) {
            ExtLog.dAIKids("courseCode解析异常")
        }
        if (this.courseCode == null) {
            this.courseCode = courseCode
        }
        initAIKidsConfig()
        return courseCode
    }

    override fun onStop() {
        super.onStop()
        if (isWorkAIKids()) {
            WoodAudioPlayerManager.getPlayer().resetAllAudioBeanState()
            WoodAudioPlayerManager.getPlayer().addStopAction(null)
        }
        stopRecordStream(false)
        playerStreamManager?.stopPlaying()
    }

    override fun destroy() {
        super.destroy()
        ExtLog.dAIKids("aiKids触发destroy")
        socketManager?.destroy()
        socketManager = null
        recordStreamManager?.destroy()
        recordStreamManager = null
        playerStreamManager?.destroy()
        playerStreamManager = null
        recordStartListener = null
    }

}