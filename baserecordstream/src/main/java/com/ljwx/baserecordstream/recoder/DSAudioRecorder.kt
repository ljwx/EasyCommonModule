package com.ljwx.baserecordstream.recoder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.codemao.toolssdk.constant.CMTStatus
import com.codemao.toolssdk.utils.ExtLog
import java.io.File
import java.lang.Math.log10
import java.lang.Math.sqrt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

class DSAudioRecorder {

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 16000 // 16kHz 采样率
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO // 单声道
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT // 16-bit PCM
        private const val BUFFER_FACTOR = 2 // 缓冲区倍数
        private const val ENABLE_AUDIO_ENHANCE = true // 启用音频增强
        private val enableCompress = true
        fun isEnableAac(): Boolean {
            return enableCompress
        }
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null

    private val bufferSize: Int by lazy {
        if (enableCompress) {
            val frameSize = 1024
            frameSize * 2
        } else {
            AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
                .takeIf { it != AudioRecord.ERROR && it != AudioRecord.ERROR_BAD_VALUE }
                ?: (SAMPLE_RATE * 2) // 默认缓冲区大小
        }
    }

    private var startRecordListener: ((success: Boolean, code: Int) -> Unit)? = null
    private var lastDBVolume: Double? = null
    private var dbCallback: ((Double) -> Unit)? = null
    private var streamCallback: ((ByteArray) -> Unit)? = null

    private var presentationTimeUs: Long = 0
    private var mediaCodec: MediaCodec? = null
    private fun initAacEncoder() {
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
                val format = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    SAMPLE_RATE,
                    1
                ).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, 16000)
                    setInteger(
                        MediaFormat.KEY_AAC_PROFILE,
                        MediaCodecInfo.CodecProfileLevel.AACObjectLC
                    )
                    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 2) // 明确设置输入缓冲区大小
                    setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
                }
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }
        } catch (e: Exception) {
            ExtLog.dStreamAAC("初始化aac异常：$e")
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeAudioRecorder() {
        val audioSource = if (ENABLE_AUDIO_ENHANCE) {
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MediaRecorder.AudioSource.UNPROCESSED
            } else {
                MediaRecorder.AudioSource.MIC
            }
        }
        if (enableCompress) {
            initAacEncoder()
        }
        audioRecord = AudioRecord(
            audioSource,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize * BUFFER_FACTOR
        ).takeIf { it.state == AudioRecord.STATE_INITIALIZED }
//        audioRecord ?: throw IllegalStateException("AudioRecord initialization failed")
    }

    fun startRecording(listener: (success: Boolean, code: Int) -> Unit) {
//        File(context.filesDir, "debug.aac").delete()
        this.startRecordListener = listener
        if (isRecording) {
            listener.invoke(false, CMTStatus.recorderTaskBusyError)
            return
        }
        if (audioRecord == null) {
            initializeAudioRecorder()
        }
        volumeTimes = 0
        isRecording = true
        try {
            audioRecord?.startRecording()
            listener?.invoke(true, CMTStatus.success)
        } catch (e: Exception) {
            listener.invoke(false, CMTStatus.recorderRecordLaunchError)
        }
        recordingThread = Thread(recordingRunnable, "AudioRecorder-Thread").apply {
            start()
        }
    }

    private var volumeTimes = 0
    private val recordingRunnable = Runnable {
        val buffer = if (enableCompress) ShortArray(1024) else ShortArray(bufferSize)

        while (isRecording) {
            val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: AudioRecord.ERROR

            if (readResult <= 0) {
                if (readResult == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Invalid audio recording operation")
                    break
                }
                continue
            }

            // 实时分贝计算
            calculateDbLevel(buffer).let { db ->
                val rate = 1
                if (volumeTimes > 0) {
                    if (lastDBVolume == null) {
                        dbCallback?.invoke(db * rate)
                    } else {
                        var diff = db.minus(lastDBVolume ?: 0.0) * 0.2
//                        ExtLog.dAIRecord("原值：" + db + "上次：" + lastDBVolume + ",削值：$diff")
                        var trans = db - diff
                        dbCallback?.invoke(trans * rate)
                    }
                    lastDBVolume = db
                }
                volumeTimes += 1
//                Handler(Looper.getMainLooper()).post { dbCallback(db) }
            }
            // 转换并推送音频数据
//            streamCallback?.invoke(convertToByteArray(buffer))
            val finalBuffer = if (enableCompress) encodeAac(buffer) else convertToByteArray(buffer)
//            ExtLog.dStreamAAC("实时数据：" + finalBuffer.contentToString())
            if (finalBuffer != null) {
                streamCallback?.invoke(finalBuffer)
            }
        }
    }

    private fun calculateDbLevel(buffer: ShortArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample.toDouble().pow(2)
        }
        val rms = sqrt(sum / buffer.size)
        return 20 * log10(rms / Short.MAX_VALUE)
    }

    private fun convertToByteArray(samples: ShortArray): ByteArray {
        return ByteBuffer.allocate(samples.size * 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                asShortBuffer().put(samples)
            }.array()
    }

    fun setAudioDataCallback(callback: ((ByteArray) -> Unit)?) {
        this.streamCallback = {
            // 保存到文件
//            File(context.filesDir, "debug.aac").appendBytes(it)
            callback?.invoke(it)
        }
    }

    fun setVolumeLevelChangeListener(listener: ((Double) -> Unit)?) {
        this.dbCallback = listener
    }

    fun stopRecording() {
        isRecording = false
        try {
            recordingThread?.interrupt()
            recordingThread?.join(500)
        } catch (e: Exception) {
        }
        runCatching {
            audioRecord?.apply {
                stop()
            }
            lastDBVolume = null
        }.onFailure {
            Log.e(TAG, "Stop recording failed", it)
        }
    }

    fun isRecord(): Boolean {
        return isRecording
    }

    private fun encodeAac(buffer: ShortArray): ByteArray? {
//        ExtLog.dStreamAAC("压缩前的数据：" + buffer.size)
        val inputBufferId = mediaCodec?.dequeueInputBuffer(10000) ?: return null
        val inputBuffer = mediaCodec?.getInputBuffer(inputBufferId) ?: return null

        val pcmBytes = convertToByteArray(buffer)

        if (inputBuffer.remaining() < pcmBytes.size) {
            ExtLog.dStreamAAC("输入缓冲区溢出: 需要${pcmBytes.size}, 剩余${inputBuffer.remaining()}")
            mediaCodec?.queueInputBuffer(inputBufferId, 0, 0, 0, 0) // 提交空数据避免阻塞
            return null
        }

        inputBuffer.put(pcmBytes)

        mediaCodec?.queueInputBuffer(
            inputBufferId,
            0,
            pcmBytes.size,
            presentationTimeUs,
            0
        )
        presentationTimeUs += (1_000_000L * 1024) / SAMPLE_RATE // 64000 μs/帧

        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferId = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000) ?: return null

        val encodedData = when {
            outputBufferId >= 0 -> {
                val outputBuffer = mediaCodec?.getOutputBuffer(outputBufferId)
                val data = ByteArray(bufferInfo.size)
                outputBuffer?.get(data)
                mediaCodec?.releaseOutputBuffer(outputBufferId, false)
                val dataWithHeader = addAdtsHeader(data, SAMPLE_RATE)
                dataWithHeader
            }

            outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                // 处理格式变化（首次调用时触发）
                val newFormat = mediaCodec?.outputFormat
                ExtLog.dStreamAAC("输出格式变化: $newFormat")
                // 重新获取输出 Buffer
                outputBufferId = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000) ?: return null
                null
            }

            else -> null
        }
//        ExtLog.dStreamAAC("压缩后的数据：" + encodedData?.size)
        return encodedData
    }

    // 添加 ADTS 头函数
    private fun addAdtsHeader(aacData: ByteArray, sampleRate: Int): ByteArray {
        val adtsHeader = ByteArray(7)
        val frameLength = aacData.size + 7  // ADTS头长度7字节

        // ADTS头字段解析（参考ISO/IEC 13818-7标准）
        adtsHeader[0] = 0xFF.toByte()      // Syncword高8位
        adtsHeader[1] = 0xF9.toByte()      // Syncword低4位 + 协议版本0 + 层0 + 保护缺失标志1

        // 第2字节：配置类型 + 采样率索引 + 私有位 + 声道配置高位
        val sampleRateIndex = sampleRateIndex(sampleRate)
        adtsHeader[2] = (
                (0x01 and 0x03 shl 6) or      // 配置类型 (AAC LC)
                        (sampleRateIndex and 0x0F shl 2) or  // 采样率索引
                        (0x01 and 0x01 shl 1)          // 私有位
                ).toByte()

        // 第3字节：声道配置低位 + 帧长度高位
        adtsHeader[3] = (
                (0x01 and 0x01 shl 7) or       // 声道配置低位 (单声道)
                        ((frameLength shr 11) and 0x03)
                ).toByte()

        // 第4-5字节：帧长度中低位
        adtsHeader[4] = (frameLength shr 3).toByte()
        adtsHeader[5] = (
                ((frameLength and 0x07) shl 5) or 0x1F  // 帧长度末3位 + 缓冲区完整性
                ).toByte()

        // 第7字节：CRC校验（此处未使用）
        adtsHeader[6] = 0xFC.toByte()

        return adtsHeader + aacData
    }

    // 采样率索引映射表
    private fun sampleRateIndex(sampleRate: Int): Int {
        return when (sampleRate) {
            96000 -> 0
            88200 -> 1
            64000 -> 2
            48000 -> 3
            44100 -> 4
            32000 -> 5
            24000 -> 6
            22050 -> 7
            16000 -> 8  // 确保匹配SAMPLE_RATE=16000
            12000 -> 9
            11025 -> 10
            8000 -> 11
            else -> 8   // 默认16kHz
        }
    }

    fun destroy() {
        isRecording = false
        recordingThread?.join(500)
        recordingThread = null
        runCatching {
            audioRecord?.apply {
                stop()
                release()
            }
        }.onFailure {
            Log.e(TAG, "Stop recording failed", it)
        }
        audioRecord = null
        mediaCodec?.stop()
        mediaCodec = null
    }

}