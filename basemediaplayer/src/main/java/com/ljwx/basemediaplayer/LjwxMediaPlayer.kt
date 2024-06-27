package com.ljwx.basemediaplayer

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class LjwxMediaPlayer(
    private val context: Context,
    private val fileName: String? = null,
    private val cachePath: String = context.cacheDir.path + "/media_cache/" + fileName,
    private val cacheSize: Long = 1024 * 1024 * 40L
) {

//    companion object {
//
//        private lateinit var context: Context
//        private var cachePath: String? = null
//        private var cacheSize: Long? = null
//        private val instance by lazy { LjwxMediaPlayer(context, cachePath, cacheSize) }
//
//        fun setInstanceInit(
//            context: Context,
//            cachePath: String? = null,
//            cacheSize: Long? = null
//        ): Companion {
//            this.context = context
//            this.cachePath = cachePath
//            this.cacheSize = cacheSize
//            return this
//        }
//
//        fun getInstance(): LjwxMediaPlayer {
//            return instance
//        }
//
//    }

    private val TAG = "basemediaplayer"

    private var player: Player? = null

    private var exoListener: Player.Listener? = null

    init {
        initPlayer()
    }

    private fun enableCache(): Boolean {
        return !fileName.isNullOrEmpty()
    }

    private fun initPlayer(): Player {
        if (player == null) {
            val builder = ExoPlayer.Builder(context)
            builder.apply {
                if (enableCache()) {
                    LjwxMediaCache.Builder(context).setCachePath(cachePath)
                        .setCacheSize(cacheSize).build()?.let {
                            setMediaSourceFactory(it)
                        }
                }
            }
            val build = builder.build()
            build.apply {
                // 设置重复模式
                // Player.REPEAT_MODE_ALL 无限重复
                // Player.REPEAT_MODE_ONE 重复一次
                // Player.REPEAT_MODE_OFF 不重复
                repeatMode = Player.REPEAT_MODE_OFF
                // 设置当缓冲完毕后直接播放视频
                playWhenReady = false
            }
            player = build
        }
        return player!!
    }

    fun getPlayer(): Player {
        return initPlayer()
    }

    fun addListener(listener: LjwxMediaPlayerListener?) {
        exoListener?.let {
            player?.removeListener(it)
        }
        if (listener == null) {
            return
        }
        exoListener?.let {
            player?.removeListener(it)
        }
        exoListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    listener?.onStateChange(PlayerStates.STATE_IS_PLAYING, "播放中")
                } else {
                    listener?.onStateChange(PlayerStates.STATE_NOT_PLAYING, "非播放中")
                }
                Log.d(TAG, "是否播放中:$isPlaying")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        //播放器停止时的状态
                        listener.onStateChange(PlayerStates.STATE_IDLE, "已停止")
                        Log.d(TAG, "播放停止")
                    }

                    Player.STATE_BUFFERING -> {
                        // 正在缓冲数据
                        listener.onStateChange(PlayerStates.STATE_BUFFERING, "正在缓冲数据")
                        Log.d(TAG, "正在缓冲数据")
                    }

                    Player.STATE_READY -> {
                        // 可以开始播放
                        if (player?.playWhenReady == true) {
                            listener.onStateChange(PlayerStates.STATE_PLAY_START, "开始播放")
                        } else {
                            listener.onStateChange(PlayerStates.STATE_READY, "可以开始播放")
                        }
                        Log.d(TAG, "可以开始播放")
                    }

                    Player.STATE_ENDED -> {
                        // 播放结束
                        listener.onStateChange(PlayerStates.STATE_ENDED, "播放结束")
                        Log.d(TAG, "播放结束")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                // 获取播放错误信息
                listener.onStateChange(
                    PlayerStates.STATE_PLAY_ERROR,
                    "播放出错,code:" + error.errorCode + "," + error.message
                )
                Log.d(TAG, "播放出错:" + error.message)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                //进度改变
            }

        }
        player?.addListener(exoListener!!)
    }

    fun prepare() {
        player?.prepare()
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying == true
    }

    fun startPlay(path: String? = null, prepare: Boolean = true) {
        path?.let {
            resetMediaItem(path)
        }
        if (prepare) {
            player?.prepare()
        }
        player?.play()
    }

    fun resumePlay() {
        player?.play()
    }

    fun pausePlay() {
        player?.pause()
    }

    fun stopPlay() {
        player?.stop()
    }

    fun seekToMs(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun playPrevious() {
        player?.seekToPrevious()
    }

    fun playNext() {
        player?.seekToNext()
    }

    fun resetMediaItem(uri: String) {
        player?.setMediaItem(MediaItem.fromUri(uri))
    }

    fun resetMediaItem(list: List<String>) {
        player?.setMediaItems(list.map { MediaItem.fromUri(it) })
    }

    fun addMediaItem(uri: String, index: Int? = null) {
        if (index == null) {
            player?.addMediaItem(MediaItem.fromUri(uri))
        } else {
            player?.addMediaItem(index, MediaItem.fromUri(uri))
        }
    }

    fun addMediaItem(list: List<String>, index: Int? = null) {
        if (index == null) {
            player?.addMediaItems(list.map { MediaItem.fromUri(it) })
        } else {
            player?.addMediaItems(index, list.map { MediaItem.fromUri(it) })
        }
    }

    fun release() {
        player?.release()
    }

}