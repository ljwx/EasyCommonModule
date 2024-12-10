package com.ljwx.baselogcheck

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.ljwx.baselogcheck.data.LogCheckPool
import java.text.SimpleDateFormat
import java.util.Date

object LogExtCheck {

    private val dataformat by lazy { SimpleDateFormat("HH:mm:ss") }
    private val leveD = 1
    private val leveW = 2
    private val leveE = 3

    @JvmStatic
    fun d(category: String?, tag: String?, content: String?) {
        addLog(category, tag, content, leveD)
    }

    @JvmStatic
    fun w(category: String?, tag: String?, content: String?) {
        addLog(category, tag, content, leveW)
    }

    @JvmStatic
    fun e(category: String?, tag: String?, content: String?) {
        addLog(category, tag, content, leveE)
    }

    private fun addLog(category: String?, tag: String?, content: String?, level: Int) {
        if (tag != null && content != null) {
            when (level) {
                leveD -> Log.d(tag, content)
                leveW -> Log.w(tag, content)
                leveE -> Log.e(tag, content)
            }

            if (category != null) {
                val spanStr = getSpanContent(level, "$tag:$content")
                LogCheckPool.getLogPool(category).add(spanStr)
            }
        }
    }

    private fun getSpanContent(level: Int, content: String): SpannableString {
        val time = dataformat.format(Date(System.currentTimeMillis()))
        val final = "$time: $content"
        val spanStr = SpannableString(final)
        val color = when (level) {
            leveW -> Color.BLUE
            leveE -> Color.RED
            else -> Color.BLACK
        }
        spanStr.setSpan(
            ForegroundColorSpan(color),
            0,
            final.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spanStr
    }

}