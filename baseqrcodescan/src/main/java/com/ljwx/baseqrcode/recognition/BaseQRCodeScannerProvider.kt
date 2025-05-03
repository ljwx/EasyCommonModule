package com.ljwx.baseqrcode.recognition

import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BaseQRCodeScannerProvider {

    private var scanner: BarcodeScanner? = null

    init {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .enableAllPotentialBarcodes()
//            .setZoomSuggestionOptions(
//                ZoomSuggestionOptions.Builder(object : ZoomSuggestionOptions.ZoomCallback {
//                    override fun setZoom(p0: Float): Boolean {
//
//                    }
//
//                }).setMaxSupportedZoomRatio()
//                    .build()
//            ) // Optional
            .build()
        scanner = BarcodeScanning.getClient(options)
    }

    fun startRecognition(image: InputImage, listener:ScannerRecognitionListener) {
        scanner?.process(image)
            ?.addOnSuccessListener { barcodeList ->
                val barcode = barcodeList.getOrNull(0)
                // `rawValue` is the decoded value of the barcode
                barcode?.rawValue?.let { value ->
                    qrLog("识别图片结果:$value")
                    listener.onSuccess(value)
                }
            }?.addOnFailureListener { e ->
                qrLog("识别图片失败")
                listener.onFailure()
            }?.addOnCompleteListener {
                qrLog("识别图片完成")
                listener.onComplete()
            }

    }

    fun qrLog(content: String) {
        Log.d("qrCodeRecognition", content)
    }

    fun onDestroy() {
        scanner = null
    }

    interface ScannerRecognitionListener {
        fun onSuccess(value: String)
        fun onFailure()
        fun onComplete()
    }

}