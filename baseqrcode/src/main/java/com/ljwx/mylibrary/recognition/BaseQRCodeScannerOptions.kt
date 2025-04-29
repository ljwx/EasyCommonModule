package com.ljwx.mylibrary.recognition

import com.google.mlkit.vision.barcode.common.Barcode.BarcodeFormat

open class BaseQRCodeScannerOptions(
    private val format: Int,
    private val moreFormats: IntArray,
    private val enableAllPotentialBarcodes: Boolean
) {

    class Builder {
        private var format: Int = 0
        private var moreFormats = intArrayOf()

        private var enableAllPotentialBarcodes = false

        fun setBarcodeFormats(
            @BarcodeFormat format: Int,
            @BarcodeFormat vararg moreFormats: Int
        ): Builder {
            this.format = format
            this.moreFormats = moreFormats
            return this
        }

        /**
         * 检测潜在不完整条码
         */
        fun enableAllPotentialBarcodes() {
            enableAllPotentialBarcodes = true
        }

        fun build(): BaseQRCodeScannerOptions {
            return BaseQRCodeScannerOptions(format, moreFormats, enableAllPotentialBarcodes)
        }

    }

}