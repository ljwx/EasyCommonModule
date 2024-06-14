package com.ljwx.basemediaplayer

interface IMediaPlayer {

    fun startPlay()

    fun stopPlay()

    fun setMediaItem(uri: String)

    fun addMediaItem(uri: String)

    fun setRepeatMode(repeatMode: Int)

}