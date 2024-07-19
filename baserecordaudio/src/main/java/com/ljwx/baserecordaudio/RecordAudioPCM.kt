package com.ljwx.baserecordaudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class RecordAudioPCM : IRecordAudio {

    private val SAMPLE_RATE = 44100 // 44.1 kHz
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    private var isRecording = false


    private var pathName: String? = null
    private var audioRecord: AudioRecord? = null
    private var volumeDetectionListener: RecordAudioVolumeDetectionListener? = null
    private var audioHandler: Handler? = null
    private var volumeDetectionRunnable: Runnable? = null
    private var volumeDetectionHandler: Handler? = null

    override fun start(pathName: String, audioType: Int) {
        this.pathName = pathName
        audioRecord = audioRecord ?: AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE
        );
        Log.d("录音", "输出的录音文件:$pathName")
        try {
            audioRecord?.startRecording()
            isRecording = true
            startRun()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("录音", "录音异常:$e")
        }
    }

    private fun startRun() {
        val handlerThread = HandlerThread("AudioHandlerThread")
        handlerThread.start();
        audioHandler = Handler(handlerThread.getLooper());
        audioHandler?.post { writeAudioDataToFile() }
    }

    private fun writeAudioDataToFile() {
        val filePath = pathName
        val buffer = ByteArray(BUFFER_SIZE)
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(filePath)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    var maxAmplitude = 0
                    for (s in buffer) {
                        if (Math.abs(s.toInt()) > maxAmplitude) {
                            maxAmplitude = Math.abs(s.toInt())
                        }
                    }
                    volumeDetectionListener?.maxAmplitude(maxAmplitude)
                    os.write(buffer, 0, read)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun stop() {
        audioRecord?.stop()
        if (audioRecord != null) {
            isRecording = false
            audioRecord?.stop()
        }
        if (audioHandler != null) {
            audioHandler?.looper?.quit();
        }
    }

    override fun release() {
        stop()
        audioRecord?.release()
        Log.d("录音", "释放录音")
        PcmToWavUtils.pcmToWav(
            pathName, File(pathName).parent + "/" + System.currentTimeMillis() + ".wav"
        )
    }

    override fun destroy() {
        release()
        audioRecord = null
        audioHandler = null
        volumeDetectionRunnable = null
        volumeDetectionListener = null
    }

    override fun setVolumeDetection(listener: RecordAudioVolumeDetectionListener) {
        this.volumeDetectionListener = listener
//        volumeDetectionHandler = volumeDetectionHandler ?: Handler()
//        volumeDetectionRunnable = volumeDetectionRunnable ?: object : Runnable {
//            override fun run() {
//                if (isRecording) {
//                    val buffer = ShortArray(BUFFER_SIZE)
//                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
//                    if (read > 0) {
//                        var maxAmplitude = 0
//                        for (s in buffer) {
//                            if (Math.abs(s.toInt()) > maxAmplitude) {
//                                maxAmplitude = Math.abs(s.toInt())
//                            }
//                        }
//                        volumeDetectionListener?.volumeValue((maxAmplitude).toFloat())
//                    }
//                }
//                volumeDetectionHandler?.postDelayed(this, 100)
//            }
//        }
//        volumeDetectionHandler?.post(volumeDetectionRunnable!!)
    }

    override fun getPathName(): String? {
        Log.d("录音", "当前录音文件:$pathName")
        return pathName
    }

}