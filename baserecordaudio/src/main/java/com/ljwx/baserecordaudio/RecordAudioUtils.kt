package com.ljwx.baserecordaudio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.core.content.ContextCompat
import java.io.File


object RecordAudioUtils {

    fun startTest(activity: AppCompatActivity) {
        val pathName =
            getCacheDir(activity, "record_audio") + "/" + getFileName(getFileSuffix(ConstAudioType.FILE_TYPE_AAC))
        if (checkPermissions(activity)) {
            RecordAudioManager.getInstance().start(pathName, ConstAudioType.FILE_TYPE_AAC)
            RecordAudioManager.getInstance()
                .setVolumeDetection(object : RecordAudioVolumeDetectionListener {
                    override fun maxAmplitude(value: Int) {
                        Log.d("录音", "音量大小:$value")
                    }

                })
        } else {
            requestPermission()
        }
    }

    fun stopTest() {
        RecordAudioManager.getInstance().release()
    }

    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var permissionCallback: ActivityResultCallback<Boolean>? = null
    private val minAngle = Math.PI.toFloat() * 4 / 11

    fun getCacheDir(context: Context, targetDir: String): String {
        val dirPath = context.cacheDir.path + "/" + targetDir
        val dirFile = File(dirPath)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        return dirPath
    }

    fun getFileSuffix(type: Int): String {
        when (type) {
            ConstAudioType.FILE_TYPE_AAC -> {
                return "aac"
            }

            ConstAudioType.FILE_TYPE_AMR -> {
                return "amr"
            }

            ConstAudioType.FILE_TYPE_PCM -> {
                return "pcm"
            }
        }
        return "pcm"
    }

    fun getFileName(suffix: String, fileName: String? = null): String {
        if (fileName.isNullOrEmpty()) {
            return System.currentTimeMillis().toString() + "." + suffix
        } else {
            return "$fileName.$suffix"
        }
    }

    fun checkPermissions(context: Context): Boolean {
        val result = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionAndRegister(
        activity: AppCompatActivity,
        callback: ActivityResultCallback<Boolean>
    ) {
        if (!checkPermissions(activity)) {
            permissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission(), callback
            )
            this.permissionCallback = callback
        }
    }

    fun registerPermission(activity: AppCompatActivity, callback: ActivityResultCallback<Boolean>) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(), callback
        )
        this.permissionCallback = callback
    }

    fun requestPermission() {
        permissionLauncher?.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun countVolume(maxAmplitude: Int): Float {
        var angle = 0f
        angle = 100 * minAngle * (maxAmplitude) / 32768
        if (angle > 100) {
            angle = 100F
        }
        return angle
    }

}