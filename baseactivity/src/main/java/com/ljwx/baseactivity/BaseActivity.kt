package com.ljwx.baseactivity

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ljwx.baseactivity.statusbar.BaseStatusBar
import com.ljwx.baseapp.R
import com.ljwx.baseapp.constant.BaseConstBundleKey
import com.ljwx.baseapp.constant.BaseLogTag
import com.ljwx.baseapp.extensions.showToast
import com.ljwx.baseapp.keyboard.KeyboardHeightProvider
import com.ljwx.baseapp.page.IPageActivity
import com.ljwx.baseapp.page.IPageDialogTips
import com.ljwx.baseapp.page.IPageKeyboardHeight
import com.ljwx.baseapp.page.IPageLocalEvent
import com.ljwx.baseapp.page.IPagePermissions
import com.ljwx.baseapp.page.IPageProcessStep
import com.ljwx.baseapp.page.IPageStartPage
import com.ljwx.baseapp.page.IPageStatusBar
import com.ljwx.baseapp.page.IPageToolbar
import com.ljwx.baseapp.router.IPostcard
import com.ljwx.baseapp.util.BaseModuleLog
import com.ljwx.baseapp.util.LocalEventUtils
import com.ljwx.baseapp.view.IViewStatusBar
import com.ljwx.basedialog.common.BaseDialogBuilder
import com.ljwx.router.RouterPostcard

abstract class BaseActivity(@LayoutRes private val layoutResID: Int = com.ljwx.baseapp.R.layout.baseapp_state_layout_empty) :
    BaseToolsActivity(), IPageStatusBar, IPageToolbar, IPageLocalEvent,
    IPageDialogTips, IPageProcessStep, IPageActivity, IPageStartPage, IPageKeyboardHeight,
    IPagePermissions {

    protected val className = this.javaClass.simpleName
    open val TAG = className + BaseLogTag.ACTIVITY

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            onPermissionsResult(result)
        }
    private val permissionsListeners by lazy { ArrayList<(result: Map<String, @JvmSuppressWildcards Boolean>) -> Unit>() }

    /**
     * 键盘
     */
    protected var mScreenHeight = -1//辅助计算键盘高度

    protected var keyboardHighProvider: KeyboardHeightProvider? = null

    private var hidePopBottom = 0

    private val mStatusBar by lazy { BaseStatusBar(this) }

    private var mStateSaved = false

    private var broadcastReceivers: HashMap<String, BroadcastReceiver>? = null

    private var onBackPressInterceptors: (ArrayList<() -> Boolean>)? = null

    protected val argumentsFromType by lazy {
        intent.getIntExtra(BaseConstBundleKey.FROM_TYPE, -10)
    }

    protected val argumentsDataId by lazy { intent.getStringExtra(BaseConstBundleKey.DATA_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseModuleLog.dActivity("生命周期onCreate", className)
        setStatusBarLight(true)
        getScreenOrientation()?.let {
            requestedOrientation = it
            BaseModuleLog.dActivity("设置屏幕方向:$it", className)
        }
        if (enableKeyboardHeightListener()) {
            createKeyboardHeightProvider()
            keyboardHeightRootView()?.post { keyboardHighProvider?.start() }
            BaseModuleLog.dKeyboard("启用键盘高度监听", className)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        BaseModuleLog.dActivity("生命周期onWindowFocusChanged,hasFocus:$hasFocus")
    }

    override fun onViewCreated() {
        initToolbar(R.id.base_app_toolbar)
    }

    open fun getLayoutRes(): Int {
        return layoutResID
    }

    /**
     * 路由快速跳转
     */
    override fun startActivity(clazz: Class<*>, from: String?, requestCode: Int?) {
        val intent = Intent(this, clazz)
        if (!from.isNullOrEmpty()) {
            intent.putExtra(BaseConstBundleKey.FROM_TYPE, from)
        }
        if (requestCode == null) {
            startActivity(intent)
        } else {
            startActivityForResult(intent, requestCode)
        }
        BaseModuleLog.dActivityStart("打开其他activity:" + clazz.simpleName, className)
    }

    override fun routerTo(path: String): IPostcard {
        BaseModuleLog.dActivityStart("路由跳转到:$path", className)
        return RouterPostcard(path)
    }

    override fun getScreenOrientation(): Int? {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun getStatusBar(): IViewStatusBar {
        return mStatusBar;
    }

    override fun setStatusBar(backgroundColor: Int, fontDark: Boolean): IViewStatusBar {
        return mStatusBar.setCustomStatusBar(backgroundColor, fontDark)
    }

    override fun setStatusBarLight(light: Boolean) {
        if (light) {
            mStatusBar.setCustomStatusBar(com.ljwx.baseapp.R.color.white, true)
        } else {
            mStatusBar.setCustomStatusBar(
                com.ljwx.baseapp.R.color.base_app_textColorSecondary,
                false
            )
        }
        BaseModuleLog.dActivity("设置状态栏亮色:$light", className)
    }

    override fun setStatusBarTransparent(transparent: Boolean) {
        if (transparent) {
            mStatusBar.transparent(transparent)
        }
    }

    override fun initToolbar(toolbarId: Int): Toolbar? {
        val toolbar = findViewById(toolbarId) as? Toolbar
        return setToolbar(toolbar)
    }

    override fun initToolbar(toolbar: Toolbar?): Toolbar? {
        return setToolbar(toolbar)
    }

    private fun setToolbar(toolbar: Toolbar?): Toolbar? {
        toolbar?.let {
            setSupportActionBar(toolbar)
            toolbar?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            BaseModuleLog.dToolbar("初始化toolbar并返回", className)
        }
        return toolbar
    }

    override fun setToolbarTitle(title: CharSequence) {
        supportActionBar?.title = title
        BaseModuleLog.dToolbar("设置toolbar的标题", className)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        BaseModuleLog.dActivity("生命周期onSaveInstanceState", className)
        mStateSaved = true
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        BaseModuleLog.dActivity("生命周期onRestoreInstanceState", className)
    }

    override fun onResume() {
        super.onResume()
        BaseModuleLog.dActivity("生命周期onResume", className)
        mStateSaved = false
        if (enableKeyboardHeightListener()) {
            setKeyboardHeightListener()
        }
    }

    override fun onStop() {
        super.onStop()
        BaseModuleLog.dActivity("生命周期onStop", className)
        mStateSaved = true
    }

    override fun onStart() {
        super.onStart()
        BaseModuleLog.dActivity("生命周期onStart", className)
        mStateSaved = false
    }

    override fun showDialogTips(
        title: String?,
        content: String?,
        positiveText: String?
    ): Dialog? {
        BaseModuleLog.dDialog("快速显示弹窗", className)
        return showDialogTips(title, content, positiveText, null, null, null, false, null, null)
    }


    /**
     * 快速显示dialog提示
     *
     * @param title 标题,为空不显示标题控件
     * @param content 内容
     * @param positiveText 积极的文案
     * @param positiveListener 积极的点击, 当文案和点击都为空,则不显示积极控件
     * @param negativeText 消极的文案,为空,则不显示消极控件
     */
    override fun showDialogTips(
        title: String?,
        content: String?,
        positiveText: String?,
        negativeText: String?,
        showClose: Boolean?,
        tag: String?,
        reversalButtons: Boolean,
        negativeListener: View.OnClickListener?,
        positiveListener: View.OnClickListener?
    ): Dialog? {
        BaseModuleLog.dDialog("快速显示弹窗", className)
//        if (tag.notNullOrBlank()) {
//            val cache = supportFragmentManager.findFragmentByTag(tag)
//            if (cache != null && cache is BaseDialogFragment) {
//                //报java.lang.IllegalStateException: Fragment already added
////                cache.show(supportFragmentManager, tag)
//                Log2.d(TAG, "$tag,dialog有缓存")
//                return cache.getBuilder()
//            }
//        }
        val builder = BaseDialogBuilder()
        builder.apply {
            showCloseIcon(showClose)
            if (title != null) {
                setTitle(title)
            }
            setContent(content)
            //是否显示确定等
            if (positiveText != null || positiveListener != null) {
                setPositiveButton(positiveText, positiveListener)
            }
            //是否显示取消等
            if (negativeText != null || negativeListener != null) {
                setNegativeButton(negativeText, negativeListener)
            }
            //是否反转按钮
            buttonsReversal(reversalButtons)
            return this.showDialog(this@BaseActivity)
        }
    }

    /**
     * 事件广播使用
     */
    override fun registerLocalEvent(
        action: String?,
        observer: (action: String, type: Long?, value: String?, intent: Intent) -> Unit
    ) {
        if (action == null) {
            return
        }
        val intentFilter = IntentFilter(action)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.action?.let {
                    BaseModuleLog.dEvent("接收到事件广播:$it", className)
                    if (intentFilter.matchAction(it)) {
                        val type =
                            intent.getLongExtra(BaseConstBundleKey.LOCAL_EVENT_COMMON_TYPE, -1)
                        val value =
                            intent.getStringExtra(BaseConstBundleKey.LOCAL_EVENT_COMMON_VALUE)
                        observer(action, type, value, intent)
                    }
                }
            }
        }
        broadcastReceivers = broadcastReceivers ?: HashMap()
        broadcastReceivers?.put(action, receiver)
        BaseModuleLog.dEvent("注册事件广播:$action", className)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    override fun sendLocalEvent(action: String?, type: Long?, value: String?) {
        LocalEventUtils.sendAction(action, type)
    }

    override fun unregisterLocalEvent(action: String?) {
        action?.let {
            broadcastReceivers?.get(it)?.let {
                BaseModuleLog.dEvent("注销事件广播:$action", className)
                LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
            }
            broadcastReceivers?.remove(it)
        }
    }

    fun addBackPressedInterceptor(block: () -> Boolean) {
        onBackPressInterceptors = onBackPressInterceptors ?: ArrayList()
        onBackPressInterceptors?.add(block)
    }

    override fun onBackPressed() {
        BaseModuleLog.dActivity("触发onBackPress", className)
        onBackPressInterceptors?.forEach {
            if (it.invoke()) {
                BaseModuleLog.dActivity("返回被拦截", className)
                return
            }
        }
        super.onBackPressed()
    }


    override fun enableCommonSteps() {
        commonStep1InitData()
        commonStep2InitView()
        commonStep3ObserveData()
        commonStep4SetViewListener()
        commonStep5RequestData()
    }

    override fun commonStep1InitData() {

    }

    override fun commonStep2InitView() {

    }

    override fun commonStep3ObserveData() {


    }

    override fun commonStep4SetViewListener() {

    }

    override fun commonStep5RequestData(refresh: Boolean) {

    }

    /*--------------------------------------------------------------------------------------*/

    override fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isPermissionsGranted(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (!isPermissionGranted(it)) {
                return false
            }
        }
        return true
    }

    override fun isPermissionShouldShowRational(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

    override fun isPermissionNotRequest(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_DENIED
    }

    override fun isPermissionDenied(permission: String): Boolean {
        return !isPermissionShouldShowRational(permission) && ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_DENIED
    }

    override fun showPermissionRationale(
        permission: String,
        listener: (positive: Boolean) -> Unit
    ) {
        BaseModuleLog.dPermission("显示权限提示弹窗:$permission")
        showDialogTips("权限提示", "我为什么需要这个权限", negativeListener = {
            BaseModuleLog.dPermission("显示权限理由后,依然选择拒绝")
            listener(false)
        }, positiveListener = {
            BaseModuleLog.dPermission("显示权限理由后,点了同意")
            listener(true)
        })
    }

    override fun addPermissionsListener(listener: (Map<String, @JvmSuppressWildcards Boolean>) -> Unit) {
        permissionsListeners.add(listener)
    }

    override fun onPermissionsResult(result: Map<String, @JvmSuppressWildcards Boolean>) {
        permissionsListeners.forEach {
            it.invoke(result)
        }
    }

    override fun openAppDetailsSettings() {
        BaseModuleLog.dPermission("跳转系统设置界面")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

//    override fun checkAndRequestPermission(permission: String) {
//
//    }
//
//    override fun checkAndRequestPermissions(permissions: Array<String>) {
//
//    }

    override fun handlePermission(
        permission: String,
        callback: (granted: Boolean, denied: Boolean) -> Unit
    ) {
        if (isPermissionGranted(permission)) {
            BaseModuleLog.dPermission("权限通过:$permission")
            callback(true, false)
        } else if (isPermissionShouldShowRational(permission)) {
            BaseModuleLog.dPermission("权限未通过,但没有不再提示")
            callback(false, false)
        } else if (isPermissionNotRequest(permission)) {
            BaseModuleLog.dPermission("权限未请求过,发起请求")
            var listener: ((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)? = null
            listener = {
                BaseModuleLog.dPermission("添加权限监听结果回调")
                if (it.keys.contains(permission)) {
                    val result = it[permission] ?: false
                    if (result) {
                        BaseModuleLog.dPermission("发起请求后,权限通过:$permission")
                        callback(true, false)
                    } else {
                        //用户拒绝但未勾选“不再询问”，可以再次请求
                        if (isPermissionShouldShowRational(permission)) {
                            BaseModuleLog.dPermission("发起请求后,权限未通过,但没有不再提示:$permission")
//                            showPermissionRationale(permission)
                            callback(false, false)
                        } else {
                            BaseModuleLog.dPermission("发起请求后,权限未通过,且不再提示:$permission")
//                            openAppDetailsSettings()
                            callback(false, true)
                        }
                    }
                }
                permissionsListeners.remove(listener)
            }
            addPermissionsListener(listener)
            requestPermission(permission)
        } else if (isPermissionDenied(permission)) {
            BaseModuleLog.dPermission("权限已拒绝,且不在提示:$permission")
            callback(false, true)
        }
    }

    override fun requestPermission(permission: String) {
        BaseModuleLog.dPermission("启动权限请求:$permission")
        requestPermissionLauncher.launch(arrayOf(permission))
    }

    override fun requestPermissions(permission: Array<String>) {
        requestPermissionLauncher.launch(permission)
    }

    /*---------------------------------------------------------------------------------------*/


    override fun enableKeyboardHeightListener(): Boolean = false

    override fun createKeyboardHeightProvider() {
        keyboardHighProvider = keyboardHighProvider ?: KeyboardHeightProvider(this)
    }

    override fun keyboardHeightRootView(): View? = rootLayout

    override fun setKeyboardHeightListener() {
        keyboardHighProvider?.setKeyboardHeightListener { height, orientation ->
            BaseModuleLog.dKeyboard("keyboardHeightListener:$height", className)
            var keyBoardHeight = 0
            if (mScreenHeight <= 0) {
                hidePopBottom = height
            } else {
                if (keyBoardHeight <= 0 && height > hidePopBottom && height - hidePopBottom > mScreenHeight / 4) {
                    keyBoardHeight = height - hidePopBottom
                }
                if (keyBoardHeight > mScreenHeight * 3 / 5) {
                    keyBoardHeight = height - hidePopBottom
                }
            }
            if (mScreenHeight <= 0) {
                mScreenHeight = keyboardHeightRootView()?.getHeight() ?: 2000
            }
            if (isKeyboardShow(height, hidePopBottom)) { //软键盘弹出
                onKeyboardHeightChange(true, keyBoardHeight)
            } else {
                onKeyboardHeightChange(false, 0)
            }
        }
    }

    override fun isKeyboardShow(height: Int, buffHeight: Int): Boolean {
        return height - buffHeight > mScreenHeight / 4
    }

    override fun onKeyboardHeightChange(show: Boolean, height: Int) {
        BaseModuleLog.dKeyboard("触发键盘高度变化:$height", className)
    }

    override fun onPause() {
        super.onPause()
        BaseModuleLog.dActivity("生命周期onPause", className)
        keyboardHighProvider?.setKeyboardHeightListener(null)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        BaseModuleLog.dActivity("生命周期onConfigurationChanged", className)
    }

    override fun onRestart() {
        super.onRestart()
        BaseModuleLog.dActivity("生命周期onRestart", className)
    }

    inline fun <reified F : Fragment> fragmentInstance(fromType: Int): F? {
        return fragmentInstanceEx(fromType)
    }

    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
        super.overridePendingTransition(enterAnim, exitAnim)
//        overridePendingTransition(0, R.anim.bottom_out)
//        BaseModuleLog.dActivity("进出动画", className)
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseModuleLog.dActivity("生命周期onDestroy", className)
        broadcastReceivers?.keys?.toList()?.forEach {
            unregisterLocalEvent(it)
        }
        keyboardHighProvider?.close()
    }

}