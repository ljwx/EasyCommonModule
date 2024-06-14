package com.ljwx.baseapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View


object ImageBlurUtils {

    fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        return bitmap
    }

    fun blur(context: Context, inputBitmap: Bitmap, radius: Float): Bitmap {
        // 创建一个空的输出图片
        val outputBitmap = Bitmap.createBitmap(
            inputBitmap.width, inputBitmap.height, Bitmap.Config.ARGB_8888
        )

        // 创建 RenderScript 对象
        val rs = RenderScript.create(context)

        // 创建一个 Allocation 对象，用于输入图片
        val input = Allocation.createFromBitmap(
            rs,
            inputBitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )

        // 创建一个 Allocation 对象，用于输出图片
        val output = Allocation.createTyped(rs, input.type)

        // 创建一个模糊效果的 RenderScript 的内置脚本对象
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setInput(input)

        // 设置模糊半径
        script.setRadius(radius)

        // 运行脚本
        script.forEach(output)

        // 将输出保存到 Bitmap
        output.copyTo(outputBitmap)

        // 释放资源
        rs.destroy()
        input.destroy()
        output.destroy()
        script.destroy()
        return outputBitmap
    }

}