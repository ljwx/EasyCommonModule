package com.ljwx.baseqrcodegenerator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class BaseQRCodeGeneratorTestActivity : AppCompatActivity() {

    companion object {
        fun startActivity(activity: Activity, content: String, size: Int = 600) {
            val intent = Intent(activity, BaseQRCodeGeneratorTestActivity::class.java)
            intent.putExtra("qrCodeData", content)
            intent.putExtra("qrCodeSize", size)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.baseqrcodegenerator_test_activity)

        val data = intent.getStringExtra("qrCodeData") ?: "empty"
        val size = intent.getIntExtra("qrCodeSize", 300)

        findViewById<ImageView>(R.id.qr_code_image).apply {
            val layoutParams = FrameLayout.LayoutParams(size, size)
            layoutParams.gravity = Gravity.CENTER
            setLayoutParams(layoutParams)
            setImageBitmap(BaseQRCodeGenerator.generateQRCode(data, size))
        }

    }

}