package com.ljwx.baserecordaudio

class RecordAudioManager {
    companion object {

        private val manager by lazy { RecordAudioManager() }

        fun getInstance(): RecordAudioManager {
            return manager
        }
    }

    private var mediaRecorder: IRecordAudio? = null

    private fun getRecorder(encoderType: Int): IRecordAudio? {
        when (encoderType) {
            ConstAudioType.FILE_TYPE_AAC -> {
                mediaRecorder = RecordAudio()
            }

            ConstAudioType.FILE_TYPE_PCM -> {
                mediaRecorder = RecordAudioPCM()
            }
        }
        return mediaRecorder
    }

    fun start(pathName: String, encoderType: Int) {
        mediaRecorder = mediaRecorder ?: getRecorder(encoderType)
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