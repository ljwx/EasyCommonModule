package com.ljwx.baserecordaudio

interface IRecordAudio {

    fun start(pathName: String, audioType: Int)

    fun stop()

    fun release()

    fun destroy()

    fun setVolumeDetection(listener: RecordAudioVolumeDetectionListener)

    fun getPathName(): String?

}