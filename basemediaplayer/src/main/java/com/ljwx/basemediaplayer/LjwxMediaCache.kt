package com.ljwx.basemediaplayer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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

    private val TAG = "mediaCache"

    class Builder(private val context: Context) {

        private val TAG = "mediaCache"

        private var cachePath = "/"
        private var cacheSize = 1024 * 1024 * 500L
        private var cache: SimpleCache? = null

        fun setCachePath(path: String): Builder {
            this.cachePath = path
            return this
        }

        fun setCacheSize(cacheSize: Long?): Builder {
            this.cacheSize = cacheSize ?: this.cacheSize
            return this
        }

        fun build(): MediaSource.Factory? {
            return getMediaSourceFactory()
        }

        private fun getCacheFile(): File {
            return File(cachePath)
        }

        private fun getCacheSize(): LeastRecentlyUsedCacheEvictor {
            return LeastRecentlyUsedCacheEvictor(cacheSize)
        }

        private fun getCache(): SimpleCache? {
            try {
                cache = cache ?: SimpleCache(
                    File(cachePath),
                    LeastRecentlyUsedCacheEvictor(cacheSize),
                    StandaloneDatabaseProvider(context)
                )
                return cache
            } catch (e: Exception) {
                Log.d(TAG, "创建音频缓存异常：" + e.message)
                return null
            }
        }

        private fun getCacheDataSourceFactory(): CacheDataSource.Factory? {
            getCache()?.let {
                val factory = CacheDataSource.Factory().setCache(it)
                    // 设置上游数据源，缓存未命中时通过此获取数据
                    .setUpstreamDataSourceFactory(
                        DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                    )
                factory.createDataSource()
                return factory
            }
            return null
        }


        private fun getMediaSourceFactory(): MediaSource.Factory? {
            getCacheDataSourceFactory()?.let {
                return ProgressiveMediaSource.Factory(it)
            }
            return null
        }

    }

}