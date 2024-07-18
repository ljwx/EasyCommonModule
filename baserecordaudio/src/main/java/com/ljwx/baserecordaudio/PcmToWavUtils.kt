package com.ljwx.baserecordaudio

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


object PcmToWavUtils {
    private const val SAMPLE_RATE = 44100
    private const val CHANNELS = 1
    private const val BITS_PER_SAMPLE = 16

    @Throws(IOException::class)
    fun pcmToWav(pcmPath: String?, wavPath: String?) {
        var pcmInputStream: FileInputStream? = null
        var wavOutputStream: FileOutputStream? = null
        try {
            pcmInputStream = FileInputStream(pcmPath)
            wavOutputStream = FileOutputStream(wavPath)
            val pcmData = ByteArray(pcmInputStream.available())
            pcmInputStream.read(pcmData)
            writeWavHeader(wavOutputStream, pcmData.size)
            wavOutputStream.write(pcmData)
        } finally {
            pcmInputStream?.close()
            wavOutputStream?.close()
        }
    }

    @Throws(IOException::class)
    private fun writeWavHeader(out: FileOutputStream, pcmDataLength: Int) {
        val totalDataLen = pcmDataLength + 36
        val byteRate = SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = CHANNELS.toByte()
        header[23] = 0
        header[24] = (SAMPLE_RATE and 0xff).toByte()
        header[25] = (SAMPLE_RATE shr 8 and 0xff).toByte()
        header[26] = (SAMPLE_RATE shr 16 and 0xff).toByte()
        header[27] = (SAMPLE_RATE shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (CHANNELS * BITS_PER_SAMPLE / 8).toByte() // block align
        header[33] = 0
        header[34] = BITS_PER_SAMPLE.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmDataLength and 0xff).toByte()
        header[41] = (pcmDataLength shr 8 and 0xff).toByte()
        header[42] = (pcmDataLength shr 16 and 0xff).toByte()
        header[43] = (pcmDataLength shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }

}