package com.ljwx.provide.speechrecognizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.huawei.hms.mlsdk.asr.MLAsrConstants
import com.huawei.hms.mlsdk.asr.MLAsrListener
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer

class HmsSpeechRecognizer(context: Context) {

    private var asrSpeechRecognizer: MLAsrRecognizer? = MLAsrRecognizer.createAsrRecognizer(context)

    fun setListener(listener: MLAsrListener) {
        asrSpeechRecognizer?.setAsrListener(listener)
    }

    fun setDefaultListener() {
        asrSpeechRecognizer?.setAsrListener(SpeechRecognitionListener())
    }

    fun startRecognizing() {
        // 新建Intent，用于配置语音识别参数。
        val mSpeechRecognizerIntent = Intent(MLAsrConstants.ACTION_HMS_ASR_SPEECH)
        // 通过Intent进行语音识别参数设置。
        mSpeechRecognizerIntent
            // 设置识别语言为英语，若不设置，则默认识别英语。
            // 支持设置："zh-CN":中文；"en-US":英语；"fr-FR":法语；"es-ES":西班牙语；"de-DE":德语；
            // "it-IT":意大利语；"ar":阿拉伯语；"th=TH"：泰语；"ms-MY"：马来语；
            // "fil-PH"：菲律宾语；"tr-TR"：土耳其语。
            .putExtra(MLAsrConstants.LANGUAGE, "zh-CN") // 设置识别文本返回模式为边识别边出字，若不设置，默认为边识别边出字。支持设置：
            // MLAsrConstants.FEATURE_WORDFLUX：通过onRecognizingResults接口，识别同时返回文字；
            // MLAsrConstants.FEATURE_ALLINONE：识别完成后通过onResults接口返回文字。
            .putExtra(
                MLAsrConstants.FEATURE,
                MLAsrConstants.FEATURE_WORDFLUX
            ) // 设置使用场景，MLAsrConstants.SCENES_SHOPPING：表示购物，仅支持中文，该场景对华为商品名识别进行了优化。
            .putExtra(MLAsrConstants.SCENES, MLAsrConstants.SCENES_SHOPPING)
            // 静音检测时长（发音前，可设置3000到60000毫秒）（毫秒）
            .putExtra(MLAsrConstants.VAD_START_MUTE_DURATION, 6000)
            // 静音检测时长（发音后）（毫秒）
            .putExtra(MLAsrConstants.VAD_END_MUTE_DURATION, 700)
            // 是否设置标点
            .putExtra(MLAsrConstants.PUNCTUATION_ENABLE, true)
        // 启动语音识别。
        asrSpeechRecognizer?.startRecognizing(mSpeechRecognizerIntent)
    }

    fun destroy() {
        asrSpeechRecognizer?.destroy()
    }

    // 回调实现MLAsrListener接口，实现接口中的方法。
    internal inner class SpeechRecognitionListener : MLAsrListener {
        override fun onStartListening() {
            // 录音器开始接收声音。
            Log.d("hms语音识别", "开始接收声音")
        }

        override fun onStartingOfSpeech() {
            // 用户开始讲话，即语音识别器检测到用户开始讲话。
            Log.d("hms语音识别", "语音识别器检测到用户开始讲话")
        }

        override fun onVoiceDataReceived(data: ByteArray, energy: Float, bundle: Bundle) {
            // 返回给用户原始的PCM音频流和音频能量，该接口并非运行在主线程中，返回结果需要在子线程中处理。
//            Log.d("hms语音识别", "非主线程,原始的PCM音频流和音频能量:$energy")
        }

        override fun onRecognizingResults(partialResults: Bundle) {
            // 从MLAsrRecognizer接收到持续语音识别的文本，该接口并非运行在主线程中，返回结果需要在子线程中处理。
            val partialResult = partialResults.getString(MLAsrRecognizer.RESULTS_RECOGNIZING)
            if (!partialResult.isNullOrEmpty()) {
                Log.d("hms语音识别", "非主线程,持续接收的文本:$partialResult")
            }
        }

        override fun onResults(results: Bundle) {
            // 语音识别的文本数据，该接口并非运行在主线程中，返回结果需要在子线程中处理。
            val result = results.getString(MLAsrRecognizer.RESULTS_RECOGNIZED)
            if (result != null) {
                Log.d("hms语音识别", "识别结果: $result")
            }
        }

        override fun onError(error: Int, errorMessage: String) {
            // 识别发生错误后调用该接口，该接口并非运行在主线程中，返回结果需要在子线程中处理。
            Log.d("hms语音识别", "出现异常: $error::$errorMessage")
        }

        override fun onState(state: Int, params: Bundle) {
            // 通知应用状态发生改变，该接口并非运行在主线程中，返回结果需要在子线程中处理。
            Log.d("hms语音识别", "状态改变: $state")
            when (state) {
                MLAsrConstants.STATE_LISTENING -> Log.d("hms语音识别", "正在听")
                MLAsrConstants.STATE_NO_SOUND -> Log.d("hms语音识别", "没声音")
                MLAsrConstants.STATE_NO_SOUND_TIMES_EXCEED -> Log.d("hms语音识别", "没声音超时")
                MLAsrConstants.STATE_NO_UNDERSTAND -> Log.d("hms语音识别", "没理解")
                MLAsrConstants.STATE_NO_NETWORK -> Log.d("hms语音识别", "没有网")
                MLAsrConstants.STATE_WAITING -> Log.d("hms语音识别", "正在等")
            }
        }
    }

}