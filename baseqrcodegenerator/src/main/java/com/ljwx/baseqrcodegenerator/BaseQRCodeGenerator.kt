package com.ljwx.baseqrcodegenerator

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

object BaseQRCodeGenerator {

    fun generateQRCode(content: String, size: Int, charSet: String = "UTF-8"): Bitmap {
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] =
            com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H // 高容错
        hints[EncodeHintType.MARGIN] = 1 // 边距
        hints[EncodeHintType.CHARACTER_SET] = charSet //要支持中文就得UTF8

        val bitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] =
                    if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }

    fun generateQRCodeWithLogo(content: String, size: Int, logo: Bitmap): Bitmap {
        val qrBitmap = generateQRCode(content, size)
        val combined = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)

        // 计算 Logo 位置（居中）
        val logoSize = (size * 0.2f).toInt()
        val scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)
        val left = (size - logoSize) / 2f
        val top = (size - logoSize) / 2f
        canvas.drawBitmap(scaledLogo, left, top, null)

        return combined
    }

}