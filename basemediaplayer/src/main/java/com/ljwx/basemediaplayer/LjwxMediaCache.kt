package com.ljwx.basemediaplayer

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import java.io.File


@SuppressLint("UnsafeOptInUsageError")
class LjwxMediaCache {

    class Builder(private val context: Context) {

        private var cachePath = "/"
        private var cacheSize = 1024 * 1024 * 500L

        fun setCachePath(path: String): Builder {
            this.cachePath = path
            return this
        }

        fun setCacheSize(cacheSize: Long?): Builder {
            this.cacheSize = cacheSize ?: this.cacheSize
            return this
        }

        fun build(): MediaSource.Factory {
            return getMediaSourceFactory()
        }

        private fun getCacheFile(): File {
            return File(cachePath)
        }

        private fun getCacheSize(): LeastRecentlyUsedCacheEvictor {
            return LeastRecentlyUsedCacheEvictor(cacheSize)
        }

        fun getCache(): SimpleCache {
            // 设置缓存目录和缓存机制，如果不需要清除缓存可以使用NoOpCacheEvictor
            return SimpleCache(
                getCacheFile(),
                getCacheSize(),
                StandaloneDatabaseProvider(context)
            )
        }

        private fun getCacheDataSourceFactory(): CacheDataSource.Factory {
            val factory = CacheDataSource.Factory().setCache(getCache())
                // 设置上游数据源，缓存未命中时通过此获取数据
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                )
            factory.createDataSource()
            return factory
        }

        private fun getMediaSourceFactory(): MediaSource.Factory {
            return ProgressiveMediaSource.Factory(getCacheDataSourceFactory())
        }

    }

}