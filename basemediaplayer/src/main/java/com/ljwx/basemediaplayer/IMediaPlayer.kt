package com.ljwx.basemediaplayer

interface IMediaPlayer {

    fun setMediaItem(uri: String, id: String? = null)
    fun getMediaId(): String
    fun prepare()
    fun start()
    fun stop()
    fun release()

}