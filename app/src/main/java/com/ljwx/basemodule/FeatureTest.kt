package com.ljwx.basemodule

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ljwx.baseapp.extensions.showToast
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
//        checkPermission(context)
        hmsSR = HmsSpeechRecognizer(context)
        hmsSR?.setDefaultListener()
    }

    fun start() {
        hmsSR?.startRecognizing()
    }

    fun testClick(activity: MainActivity) {
        val permission = Manifest.permission.CAMERA
        activity.handlePermission(permission) { granted, denied ->
            if (granted) {
                activity.showToast("权限通过了")
            } else if (!denied) {
                activity.showToast("拒绝过,但没有不再提示")
                activity.showPermissionRationale(permission) {
                    if (it) {
                        activity.requestPermission(permission)
                    }
                }
            } else {
                activity.showToast("权限拒绝了")
                activity.openAppDetailsSettings()
            }
        }
    }

}