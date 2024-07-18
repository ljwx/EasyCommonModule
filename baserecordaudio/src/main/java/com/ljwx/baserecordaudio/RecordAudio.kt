package com.ljwx.baserecordaudio

import android.media.MediaRecorder
import android.os.Handler
import android.util.Log
import java.io.IOException

class RecordAudio : IRecordAudio {

    private var pathName: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var volumeDetectionListener: RecordAudioVolumeDetectionListener? = null
    private var volumeDetectionHandler: Handler? = null
    private var volumeDetectionRunnable: Runnable? = null
    override fun start(pathName: String, audioType: Int) {
        this.pathName = pathName
        mediaRecorder = mediaRecorder ?: MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setOutputFile(pathName)
        Log.d("录音", "输出的录音文件:$pathName")
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("录音", "录音异常:$e")
        }
    }

    override fun stop() {
        mediaRecorder?.stop()
    }

    override fun release() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        volumeDetectionHandler = null
        volumeDetectionRunnable = null
        volumeDetectionListener = null
        Log.d("录音", "释放录音")
    }

    override fun setVolumeDetection(listener: RecordAudioVolumeDetectionListener) {
        this.volumeDetectionListener = listener
        volumeDetectionHandler = volumeDetectionHandler ?: Handler()
        volumeDetectionRunnable = volumeDetectionRunnable ?: object : Runnable {
            override fun run() {
                volumeDetectionListener?.volumeValue((mediaRecorder?.maxAmplitude ?: 0).toFloat())
                volumeDetectionHandler?.postDelayed(this, 100)
            }
        }
        volumeDetectionHandler?.post(volumeDetectionRunnable!!)
    }

    override fun getPathName(): String? {
        Log.d("录音", "当前录音文件:$pathName")
        return pathName
    }

}