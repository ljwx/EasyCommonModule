package com.ljwx.baserecordaudio

import android.media.MediaRecorder

object ConstAudioType {

    const val FILE_TYPE_PCM = "pcm"
    const val FILE_TYPE_AAC = MediaRecorder.AudioEncoder.AAC
    const val FILE_TYPE_AMR = MediaRecorder.AudioEncoder.AMR_NB
    const val ENCODER_TYPE_3GGP = MediaRecorder.OutputFormat.THREE_GPP
    const val ENCODER_TYPE_AMR_NB = MediaRecorder.OutputFormat.AMR_NB

}