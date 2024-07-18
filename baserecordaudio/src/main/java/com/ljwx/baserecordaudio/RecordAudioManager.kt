package com.ljwx.baserecordaudio

class RecordAudioManager {
    companion object {

        private val manager by lazy { RecordAudioManager() }

        fun getInstance(): RecordAudioManager {
            return manager
        }
    }

    private var mediaRecorder: IRecordAudio? = null

    fun start(pathName: String, encoderType: Int) {
        mediaRecorder = mediaRecorder ?: RecordAudioPCM()
        mediaRecorder?.start(pathName, encoderType)
    }

    fun stop() {
        mediaRecorder?.stop()
    }

    fun release() {
        mediaRecorder?.release()
    }

    fun destroy() {
        mediaRecorder?.release()
        mediaRecorder = null
    }

    fun setVolumeDetection(listener: RecordAudioVolumeDetectionListener) {
        mediaRecorder?.setVolumeDetection(listener)
    }

    fun getPathName(): String? {
        return mediaRecorder?.getPathName()
    }

}