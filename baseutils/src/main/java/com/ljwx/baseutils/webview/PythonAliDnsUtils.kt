package com.ljwx.baseutils.webview

import android.content.Context
//import com.alibaba.pdns.DNSResolver
//import com.codemao.pythonmobile.utils.DebugLog

object PythonAliDnsUtils {

    private var id = "163542"; //设置您在控制台接入SDK的AccountID
    private var key = "163542_28885688097720320"; //设置您在控制台接入SDK的AccessKey ID
    private var secret = "8894effa6c9143f9831c59216073d90f"; //设置您在控制台接入SDK的AccessKey Secret

    private var preloadDomains = PythonWebViewUtils.getNeedReplaceHost()

    fun setKey(id: String, key: String, secret: String) {
        this.id = id
        this.key = key
        this.secret = secret
    }

    fun setPreLoadDomains(preloadDomains: Array<String>) {
        this.preloadDomains = preloadDomains
//        DNSResolver.getInstance().preLoadDomains(preloadDomains)
    }

    /**
     * sdk默认设置有预加载的wood和kn域名,以及初始化需要的key等,业务方也可以从过这个方法修改
     * @param context 阿里sdk初始化需要的上下文
     * @param preloadDomains 提前预加载指定域名,可空
     * @param id 阿里sdk初始化需要的id,可空
     * @param key 阿里sdk初始化需要的key,可空
     * @param secret 阿里sdk初始化需要的secret,可空
     */
    fun init(
        context: Context,
        preloadDomains: Array<String>? = null,
        id: String? = null,
        key: String? = null,
        secret: String? = null
    ) {
//        DNSResolver.Init(context, id ?: this.id, key ?: this.key, secret ?: this.secret)
//        DebugLog.dDns("执行ali初始化")
//        val preload = if (!preloadDomains.isNullOrEmpty()) preloadDomains else this.preloadDomains
//        if (preload.isNotEmpty()) {
//            //设置缓存保持配置的域名会在TTL * 75%时自动发起解析,实现配置域名解析时始终能命中缓存
//            DNSResolver.setKeepAliveDomains(preloadDomains)
//            //设置指定IPV4类型域名预解析,将预加载域名替换为您希望使用阿里DNS解析的域名
//            DNSResolver.getInstance().preLoadDomains(preloadDomains)
//        }
    }

    fun setEnableLogger(enable: Boolean) {
//        DNSResolver.setEnableLogger(enable)
    }

    fun setEnableCache(enable: Boolean) {
//        DNSResolver.setEnableCache(true); //默认开启使用缓存
    }

    fun setImmutableCacheEnable(enable: Boolean) {
//        DNSResolver.setImmutableCacheEnable(false); //默认不开启缓存永不过期
    }

    fun setIspEnable(enable: Boolean) {
//        DNSResolver.setIspEnable(enable);//是否开启依据ISP网络区分域名缓存
    }

    fun setMaxNegativeCache() {

    }

}