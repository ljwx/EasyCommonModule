package com.ljwx.baseqrcode.preview

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class BaseCameraPreviewProvider(
    private var context: Context,
    private var lifecycleOwner: LifecycleOwner,
    private var previewView: PreviewView
) {

    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(context) }
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private var imageProxy: ImageProxy? = null

    fun startCamera(listener: CameraPreviewListener) {
        qrLog("启动预览")
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            startPreview(cameraProvider, lifecycleOwner, listener)
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 开启预览
     */
    private fun startPreview(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        listener: CameraPreviewListener
    ) {
        try {
            val cameraAnalysis = getCameraAnalysis(listener)
            val preview = getPreview(previewView)
            createCamera(cameraProvider, lifecycleOwner, preview, cameraAnalysis)
        } catch (exc: Exception) {
            exc.printStackTrace()
            qrLog("相机启动失败:$exc")
            listener.onFailure(400, "相机启动失败：$exc")
        }
    }

    private fun getPreview(previewView: PreviewView): Preview {
        val preview = Preview.Builder().setTargetAspectRatio((AspectRatio.RATIO_4_3)).build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        return preview
    }

    private fun createCamera(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        preview: Preview,
        imageAnalysis: ImageAnalysis
    ): Camera {
        cameraProvider.unbindAll()
        return cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun getCameraAnalysis(listener: CameraPreviewListener): ImageAnalysis {
        val cameraAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(720, 1280))
            // 仅将最新图像传送到分析仪，并在到达图像时将其丢弃。
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(1)
            .build()
        cameraAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            this.imageProxy = imageProxy
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                listener.onSuccess(image)
            } else {
                qrLog("mediaImage是空的")
                listener.onSuccess(null)
            }
        }
        return cameraAnalysis
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun previewAgain() {
        qrLog("刷新图片")
        imageProxy?.image?.close()
        imageProxy?.close()

    }

    fun qrLog(content: String) {
        Log.d("qrCodePreview", content)
    }

    interface CameraPreviewListener {
        fun onFailure(code: Int, message: String)
        fun onSuccess(mediaImage: InputImage?)
    }

}