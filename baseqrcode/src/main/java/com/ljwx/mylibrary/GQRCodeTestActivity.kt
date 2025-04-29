package com.ljwx.mylibrary

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.ljwx.mylibrary.preview.BaseCameraPreviewProvider
import com.ljwx.mylibrary.recognition.BaseQRCodeScannerProvider

class GQRCodeTestActivity : AppCompatActivity() {

    companion object {
        fun qrLog(content: String) {
            Log.d("qrCode", content)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.baseqrcode_activity_scan)

        val previewView = findViewById<PreviewView>(R.id.camera_preview)

        val preview = BaseCameraPreviewProvider(this, this, previewView)
        val scanner = BaseQRCodeScannerProvider()

        val button = findViewById<Button>(R.id.preview_start)
        var success = false
        button.setOnClickListener {
            preview.startCamera(object : BaseCameraPreviewProvider.CameraPreviewListener {
                override fun onFailure(code: Int, message: String) {
                    qrLog("预览失败：$message")
                }

                override fun onSuccess(image: InputImage?) {
                    if (image != null && !success) {
                        scanner.startRecognition(
                            image,
                            object : BaseQRCodeScannerProvider.ScannerRecognitionListener {
                                override fun onSuccess(value: String) {
                                    if (value.isNotEmpty()) {
                                        success = true
                                    } else {
                                        preview.previewAgain()
                                    }
                                }

                                override fun onFailure() {
                                    preview.previewAgain()
                                }

                                override fun onComplete() {
                                    if (!success) {
                                        preview.previewAgain()
                                    }
                                }

                            })
                    }
                }
            })
        }

    }

}
