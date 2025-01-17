package com.ljwx.baseutils.webview

import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
//import com.alibaba.pdns.DNSResolver
//import com.codemao.pythonmobile.utils.DebugLog
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object PythonWebViewUtils {

    private var hostName =
        arrayOf("tools-entry.codemao.cn", "lunar-turtle.codemao.cn", "kn.codemao.cn")
    private var urlReplaceEnable = true

    fun setUrlReplaceEnable(enable: Boolean) {
        urlReplaceEnable = enable
    }

    fun setNeedReplaceHosts(hosts: Array<String>) {
        this.hostName = hosts
    }

    fun getNeedReplaceHost(): Array<String> {
        return hostName
    }

    @JvmStatic
    fun needUrlReplace(url: String?): Boolean {
        if (!urlReplaceEnable) {
            return false
        }
        val list = hostName.filter { url?.contains(it) == true }
        if (list.isNotEmpty()) {
            DebugLog.dDns("需要dns替换的url:" + url)
        }
        return list.isNotEmpty()
    }

    private fun getIpByHost(host: String): String? {
//        val ips = DNSResolver.getInstance().getIpsByHost(host)
        val ips = arrayOf("")
        DebugLog.dDns("需要查询的host:" + host + ",通过ali获取的ip:" + ips.contentToString())
        if (ips.isNullOrEmpty()) {
            return null
        } else {
            return ips[0]
        }
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: ""
        val schema = request?.url?.scheme?.trim()
        val method = request?.method
        //WebResourceRequest 不包含body信息 , 所以只拦截Get请求
        if (!HTTP_REQUEST_GET.equals(method, true)) {
            return null
        }
        schema?.let {
            //非http协议不拦截
            if (!SCHEME_HTTPS.startsWith(schema) && !SCHEME_HTTP.startsWith(schema)) {
                return null
            }
            val headers = request.requestHeaders
            try {
                //获取资源失败不拦截,还是走原来的请求
                val urlConnection = recursiveRequest(url, headers) ?: return null
                //实例化 WebResourceResponse 需要入参 mimeType
                val contentType = urlConnection.contentType
                DebugLog.dDns("contentType:$contentType")
                val mimeType = contentType?.split(";")?.get(0)
                //实例化 WebResourceResponse 需要入参 charset
                val charset = getCharset(contentType)
                val httpURLConnection = urlConnection as HttpURLConnection
                val statusCode = httpURLConnection.responseCode
                var response = httpURLConnection.responseMessage
                val headerFields = httpURLConnection.headerFields

                val resourceResponse = WebResourceResponse(
                    mimeType,
                    charset,
                    httpURLConnection.inputStream
                )
                if (TextUtils.isEmpty(response)) {
                    response = "OK"
                }
                //设置statusCode 和 response
                resourceResponse.setStatusCodeAndReasonPhrase(statusCode, response)
                //构造响应头
                val responseHeader: MutableMap<String?, String> = HashMap()
                for ((key) in headerFields) {
                    // HttpUrlConnection可能包含key为null的报头，指向该http请求状态码
                    responseHeader[key] = httpURLConnection.getHeaderField(key)
                }
                resourceResponse.responseHeaders = responseHeader
                DebugLog.dDns("正常替换ip流程走完")
                return resourceResponse
            } catch (e: Exception) {
                DebugLog.dDns("流程异常异常:$e")
            }
        }
        return null
    }

    private fun recursiveRequest(path: String, headers: Map<String, String>?): URLConnection? {
        try {
            val url = URL(path)
//            val currMill = System.currentTimeMillis()
//            val result =
//                viewModel.httpDnsService?.getHttpDnsResultForHostSync(url.host, RequestIpType.auto)
//                    ?.apply {
//                        log(this@WebViewCaseFragment, this.toString())
//                        if (url.host == viewModel.host.value) {
//                            //展示loadUrl的解析结果
//                            viewModel.showResolveResult(this, currMill)
//                        }
//                    }

//            val hostIP = viewModel.getIp(result)
            val hostIP = getIpByHost(url.host)
            if (TextUtils.isEmpty(hostIP)) {
                return null
            }

            //host 替换为 ip 之后的 url
            val newUrl = path.replaceFirst(url.host, hostIP!!)
            DebugLog.dDns("替换之后的url:$newUrl")
            val urlConnection: HttpURLConnection = URL(newUrl).openConnection() as HttpURLConnection
            if (headers != null) {
                for ((key, value) in headers) {
                    urlConnection.setRequestProperty(key, value)
                }
            }
            //用于证书校验
            urlConnection.setRequestProperty(HTTP_SCHEME_HEADER_HOST, url.host)
            urlConnection.connectTimeout = 30000
            urlConnection.readTimeout = 30000
            //禁止重定向,抛出异常,通过异常code , 处理重定向
            urlConnection.instanceFollowRedirects = false
            if (urlConnection is HttpsURLConnection) {
                //https场景 , 证书校验
                val sniFactory = SNISocketFactory(urlConnection)
                urlConnection.sslSocketFactory = sniFactory
                urlConnection.hostnameVerifier = HostnameVerifier { _, session ->
                    var host: String? = urlConnection.getRequestProperty(HTTP_SCHEME_HEADER_HOST)
                    if (null == host) {
                        host = urlConnection.getURL().host
                    }
                    HttpsURLConnection.getDefaultHostnameVerifier().verify(host, session)
                }
                DebugLog.dDns("https场景证书校验完成")
            }

            val responseCode = urlConnection.responseCode
            if (responseCode in 300..399) {
                //有缓存, 不发起请求, 没有解析必要 , 返回空
                if (containCookie(headers)) {
                    return null
                }
                //处理重定向逻辑
                var location: String? = urlConnection.getHeaderField(LOCATION_UP)
                if (location == null) {
                    location = urlConnection.getHeaderField(LOCATION)
                }

                return if (location != null) {
                    if (!(location.startsWith(SCHEME_HTTP) || location.startsWith(SCHEME_HTTPS))) {
                        //某些时候会省略host，只返回后面的path，所以需要补全url
                        val originalUrl = URL(path)
                        location =
                            (originalUrl.protocol + SCHEME_DIVIDE + originalUrl.host + location)
                    }
                    recursiveRequest(location, headers)
                } else {
                    null
                }
            } else {
                return urlConnection
            }
        } catch (e: MalformedURLException) {
            DebugLog.dDns("逻辑异常:$e")
        } catch (e: IOException) {
            DebugLog.dDns("逻辑异常2:$e")
        }
        return null
    }

    private fun containCookie(headers: Map<String, String>?): Boolean {
        if (headers == null) {
            return false
        }
        for ((key) in headers) {
            if (key.contains(HTTP_COOKIE)) {
                return true
            }
        }
        return false
    }

    private fun getCharset(contentType: String?): String? {
        if (contentType == null) {
            return null
        }
        val fields = contentType.split(";")
        if (fields.size <= 1) {
            return null
        }
        var charset = fields[1]
        if (!charset.contains("=")) {
            return null
        }
        charset = charset.substring(charset.indexOf("=") + 1)
        return charset
    }

    private fun trustAllCertificates() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {

                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                }
            )
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            val allHostsValid =
                HostnameVerifier { hostname, session -> true }
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
        } catch (e: Exception) {
            Log.d("dns", "信任所有证书,异常:$e")
        }
    }

}