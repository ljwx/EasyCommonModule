package com.ljwx.basemodule

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ljwx.provide.speechrecognizer.HmsSpeechRecognizer

object FeatureTest {

    private var hmsSR: HmsSpeechRecognizer? = null

    fun checkPermission(context: Activity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                111

            )
        }
    }

    fun init(context: Activity) {
        checkPermission(context)
        hmsSR = HmsSpeechRecognizer(context)
        hmsSR?.setDefaultListener()
    }

    fun start() {
        hmsSR?.startRecognizing()
    }

}