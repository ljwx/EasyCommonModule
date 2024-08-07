package com.ljwx.basemediaplayer

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors


class LjwxMediaPlayer(
    private val context: Context,
    private val cacheFileName: String? = null,
    private val cachePath: String = context.cacheDir.path + "/media_cache/" + cacheFileName,
    private val cacheSize: Long = 1024 * 1024 * 40L
) : IMediaPlayer {

    companion object {
        private val audioPool by lazy { CopyOnWriteArrayList<IMediaPlayer>() }
        private val audioThread by lazy { Executors.newSingleThreadExecutor() }

        fun execute(runnable: Runnable) {
            audioThread.execute(runnable)
        }

        fun addAndStartPlay(player: IMediaPlayer, data: IMediaData) {
            audioThread.execute {
                audioPool.add(player)
                player.setMediaItem(data.getMediaUri(), data.getMediaId())
                player.prepare()
                player.start()
            }
        }

        fun getProgress() {
            audioThread.execute {

            }
        }

        fun stop(id: String) {
            audioThread.execute {
                audioPool.forEach {
                    if (it.getMediaId() == id) {
                        it.stop()
                        it.release()
                    }
                }
            }
        }

    }

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
        return !cacheFileName.isNullOrEmpty()
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

    override fun prepare() {
        player?.prepare()
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying == true
    }

    override fun start() {
        player?.play()
    }

    fun resume() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    override fun stop() {
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

    override fun setMediaItem(uri: String, id: String?) {
        val mediaItem = MediaItem.Builder().setUri(uri).apply {
            id?.apply { setMediaId(id) }
        }.build()
        Log.d("音频", "添加的音频uri:$uri")
        player?.setMediaItem(mediaItem)
    }

    override fun getMediaId(): String {
        return player?.currentMediaItem?.mediaId ?: ""
    }

    fun setMediaItem(list: List<String>) {
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

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0L
    }

    fun getProgress(): Float {
        if (getDuration() < 1) {
            return 0F
        }
        return getCurrentPosition() * 100.0F / getDuration()
    }

    override fun release() {
        player?.release()
    }

}