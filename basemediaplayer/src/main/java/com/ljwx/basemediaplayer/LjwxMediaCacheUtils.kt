package com.ljwx.basemediaplayer

import android.annotation.SuppressLint
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache


@SuppressLint("UnsafeOptInUsageError")
object LjwxMediaCacheUtils {

    private var cachePath = ""

    var simpleCache: SimpleCache? = null
    fun initCache() {
        val leastRecentlyUsedCacheEvictor =
                LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024);
//        if (simpleCache == null)
//        {
//            simpleCache = SimpleCache(
//                cachePath, leastRecentlyUsedCacheEvictor,
//            ExoDatabaseProvider(this)
//            );
//        }
    }

}