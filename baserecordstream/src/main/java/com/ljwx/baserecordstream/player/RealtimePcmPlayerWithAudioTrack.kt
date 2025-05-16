package com.ljwx.baserecordstream.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.codemao.toolssdk.utils.ExtLog
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class RealtimePcmPlayerWithAudioTrack(
    private val sampleRateInHz: Int = 16000, // PCM 数据的采样率
    private val channelConfig: Int = AudioFormat.CHANNEL_OUT_MONO, // PCM 数据的声道配置
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT // PCM 数据的音频编码
) {

    private val TAG = "audio_stream"
    private var audioTrack: AudioTrack? = null
    private val pcmDataQueue: BlockingQueue<ByteArray> =
        ArrayBlockingQueue(1024) // 用于接收实时 PCM 数据的队列
    private var isPlaying = AtomicBoolean(false)
    private var playThread: Thread? = null
    private var bufferSizeInBytes: Int = 0

    companion object {
        val END_OF_STREAM = ByteArray(0)
    }

    init {
        initializeAudioTrack()
    }

    private fun initializeAudioTrack() {
        if (audioTrack == null) {
            try {
                // 计算最小缓冲区大小
                bufferSizeInBytes = AudioTrack.getMinBufferSize(
                    sampleRateInHz,
                    channelConfig,
                    audioFormat
                )
                if (bufferSizeInBytes <= 0) {
                    Log.e(TAG, "无法获取有效的最小缓冲区大小。")
                    return
                }
                Log.d(TAG, "最小缓冲区大小: $bufferSizeInBytes 字节")

                // 创建 AudioTrack 实例
                // 根据 Android 版本选择不同的 AudioTrack 初始化方式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d(TAG, "使用 AudioTrack.Builder 初始化")
                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setSampleRate(sampleRateInHz)
                                .setEncoding(audioFormat)
                                .setChannelMask(channelConfig)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSizeInBytes * 2)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    Log.d(TAG, "使用旧的 AudioTrack 构造函数初始化")
                    audioTrack = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRateInHz,
                        channelConfig,
                        audioFormat,
                        bufferSizeInBytes * 2,
                        AudioTrack.MODE_STREAM
                    )
                }

                if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                    Log.d(TAG, "AudioTrack 初始化完成。")
                } else {
                    Log.e(TAG, "AudioTrack 初始化失败。状态: ${audioTrack?.state}")
                    audioTrack = null
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "创建 AudioTrack 实例失败: ${e.message}")
                audioTrack = null
            }
        }
    }

    /**
     * 将实时 PCM 数据添加到播放队列。
     *
     * @param data PCM 音频数据的字节数组。
     */
    fun enqueuePcmData(data: ByteArray) {
        try {
            pcmDataQueue.put(data)
            Log.v(
                "队列_$TAG",
                "添加 PCM 数据到播放队列，大小: ${data.size} 字节，队列大小: ${pcmDataQueue.size}"
            )
        } catch (e: InterruptedException) {
            Log.w(TAG, "向 PCM 播放队列添加数据时被中断: ${e.message}")
        }
    }

    /**
     * 开始播放实时 PCM 数据。
     */
    fun startPlaying(isOver: ((finish: Boolean) -> Unit)? = null) {
        Log.d(TAG, "开始播放实时 PCM 数据。")
        if (audioTrack == null) {
            initializeAudioTrack()
        }

        if (audioTrack?.state == AudioTrack.STATE_INITIALIZED && isPlaying.compareAndSet(
                false,
                true
            )
        ) {
            audioTrack?.play()
            Log.d(TAG, "AudioTrack 开始播放。")

            // 启动播放线程
            playThread = Thread {
                try {
                    while (isPlaying.get()) {
                        val data = pcmDataQueue.take() // 阻塞等待数据
                        if (data === END_OF_STREAM) {
                            ExtLog.dAIKids("接收到流结束标记。")
                            isPlaying.set(false)
                            audioTrack?.stop()
                            isOver?.invoke(true)
                            break
                        }
                        if (isPlaying.get()) {
                            audioTrack?.write(data, 0, data.size)
                            Log.v("队列_$TAG", "写入数据到 AudioTrack，大小: ${data.size} 字节")
                        }
                    }
                } catch (e: InterruptedException) {
                    Log.w(TAG, "播放线程被中断: ${e.message}")
                } finally {
                    Log.d(TAG, "播放线程已结束。")
                }
            }
            playThread?.start()
        } else if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack 未初始化，无法开始播放。")
        } else {
            Log.w(TAG, "AudioTrack 播放已在进行中。")
        }
    }

    /**
     * 停止播放实时 PCM 数据。
     */
    fun stopPlaying() {
        Log.d(TAG, "停止播放实时 PCM 数据。")
        if (isPlaying.compareAndSet(true, false)) {
            playThread?.interrupt() // 中断播放线程
            audioTrack?.stop()
            Log.d(TAG, "AudioTrack 已停止。")
        } else {
            Log.w(TAG, "AudioTrack 播放尚未开始或已停止。")
        }
    }

    /**
     * 释放 AudioTrack 资源。
     */
    fun releasePlayer() {
        Log.d(TAG, "释放 AudioTrack 资源。")
        stopPlaying()
        audioTrack?.release()
        audioTrack = null
        pcmDataQueue.clear()
        Log.d(TAG, "AudioTrack 已释放，队列已清空。")
    }

    fun destroy() {
        releasePlayer()
    }

}