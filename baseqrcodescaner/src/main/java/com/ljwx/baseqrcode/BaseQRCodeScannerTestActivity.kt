package com.ljwx.baseqrcode

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.common.InputImage
import com.ljwx.baseqrcode.preview.BaseCameraPreviewProvider
import com.ljwx.baseqrcode.recognition.BaseQRCodeScannerProvider

open class BaseQRCodeScannerTestActivity : AppCompatActivity() {

    companion object {
        fun qrLog(content: String) {
            Log.d("qrCode", content)
        }
    }

    private val previewView by lazy { findViewById<PreviewView>(R.id.camera_preview) }
    private val preview by lazy { BaseCameraPreviewProvider(this, this, previewView) }
    private val scanner by lazy { BaseQRCodeScannerProvider() }
    private var resultContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.baseqrcode_activity_scan)

        if (checkAndRequestPermission()) {
            startWork()
        }

    }

    open fun checkAndRequestPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            val permission = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, permission, 1111)
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1111) {
            val result = grantResults.firstOrNull()
            if (result == PackageManager.PERMISSION_GRANTED) {
                startWork()
            } else {
                Toast.makeText(this, "没有权限无法扫描", Toast.LENGTH_SHORT).show()
            }
        }
    }

    open fun startWork() {
        preview.startCamera(object : BaseCameraPreviewProvider.CameraPreviewListener {
            override fun onFailure(code: Int, message: String) {
                qrLog("预览失败：$message")
            }

            override fun onSuccess(image: InputImage?) {
                if (image != null && resultContent.isNullOrEmpty()) {
                    startRecognition(image)
                }
            }
        })
    }

    open fun startRecognition(image: InputImage) {
        scanner.startRecognition(
            image,
            object : BaseQRCodeScannerProvider.ScannerRecognitionListener {
                override fun onSuccess(value: String) {
                    if (value.isNotEmpty()) {
                        if (resultContent.isNullOrEmpty()) {
                            onCodeResult(value)
                        }
                        resultContent = value
                    } else {
                        preview.previewAgain()
                    }
                }

                override fun onFailure() {
                    preview.previewAgain()
                }

                override fun onComplete() {
                    if (resultContent.isNullOrEmpty()) {
                        preview.previewAgain()
                    }
                }

            })
    }

    open fun onCodeResult(codeResult: String) {
        val intent = Intent()
        intent.putExtra("codeResult", codeResult)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        preview.onDestroy()
        scanner.onDestroy()
    }

}
