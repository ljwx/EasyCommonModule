package com.ljwx.basefragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ljwx.baseapp.constant.BaseConstBundleKey
import com.ljwx.baseapp.constant.BaseLogTag
import com.ljwx.baseapp.page.IPageLocalEvent
import com.ljwx.baseapp.page.IPageProcessStep
import com.ljwx.baseapp.page.IPageDialogTips
import com.ljwx.baseapp.page.IPageKeyboardHeight
import com.ljwx.baseapp.page.IPagePermissions
import com.ljwx.baseapp.page.IPageStartPage
import com.ljwx.baseapp.router.IPostcard
import com.ljwx.baseapp.util.BaseModuleLog
import com.ljwx.baseapp.util.LocalEventUtils
import com.ljwx.basedialog.common.BaseDialogBuilder
import com.ljwx.router.RouterPostcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

open class BaseFragment(@LayoutRes private val layoutResID: Int = com.ljwx.baseapp.R.layout.baseapp_state_layout_empty) :
    BaseToolsFragment(),
    IPageLocalEvent,
    IPageDialogTips, IPageProcessStep, IPageStartPage, IPageKeyboardHeight, IPagePermissions {

    protected val className = this.javaClass.simpleName

    protected var mActivity: AppCompatActivity? = null

    private var isLazyInitialized = false

    private var isPermissionRequestInProgress = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            onPermissionsResult(result)
        }
    private val permissionsListenerMap =
        LinkedHashMap<Array<String>, (result: Map<String, @JvmSuppressWildcards Boolean>) -> Unit>()

    /**
     * 广播事件
     */
    private var broadcastReceivers: HashMap<String, BroadcastReceiver>? = null

    protected val argumentsFromType by lazy {
        arguments?.getInt(BaseConstBundleKey.FROM_TYPE, -10) ?: -10
    }

    open val argumentIsConditionType by lazy { "" }

    protected val argumentsDataId by lazy { arguments?.getString(BaseConstBundleKey.DATA_ID) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        BaseModuleLog.dFragment("生命周期onAttach", className)
        mActivity = context as AppCompatActivity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        BaseModuleLog.dFragment("生命周期onActivityCreated", className)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseModuleLog.dFragment("生命周期onCreate", className)
    }

    open fun getLayoutRes(): Int {
        return layoutResID
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        BaseModuleLog.dFragment("生命周期onCreateView", className)
        return LayoutInflater.from(requireContext()).inflate(getLayoutRes(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BaseModuleLog.dFragment("生命周期onViewCreated", className)
        if (enableKeyboardHeightListener()) {
            setKeyboardHeightListener(view, null)
        }
    }

    override fun onResume() {
        super.onResume()
        BaseModuleLog.dFragment("生命周期onResume", className)
        if (!isLazyInitialized && !isHidden) {
            onLazyInit()
            isLazyInitialized = true
        }
    }

    /**
     * 路由快速跳转
     */
    override fun startActivity(clazz: Class<*>, from: String?, requestCode: Int?) {
        if (context == null) {
            return
        }
        val intent = Intent(context, clazz)
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

    override fun showDialogTips(
        title: String?,
        content: String?,
        positiveText: String?
    ): Dialog? {
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
        if (!isAdded) return null
//        if (tag.notNullOrBlank()) {
//            val cache = childFragmentManager.findFragmentByTag(tag)
//            if (cache != null && cache is BaseDialogFragment) {
//                //报java.lang.IllegalStateException: Fragment already added
//                //有时间再看 TODO
////                cache.show(childFragmentManager, tag)
//                Log.d(TAG, "$tag,dialog有缓存")
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
            BaseModuleLog.dFragment("${(tag ?: content) ?: "tag为空"},dialog新创建", className)
            return showDialog(requireContext())
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    override fun registerLocalEventIntent(
        action: String,
        observer: (intent: Intent) -> Unit
    ) {
        if (!isAdded) return
        val intentFilter = IntentFilter(action)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.action?.let {
                    BaseModuleLog.dEvent("接收到事件广播:$it", className)
                    if (intentFilter.matchAction(it)) {
                        observer(intent)
                    }
                }
            }
        }
        broadcastReceivers = broadcastReceivers ?: HashMap()
        broadcastReceivers?.put(action, receiver)
        BaseModuleLog.dEvent("注册事件广播:$action", className)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
    }

    override fun registerLocalEvent(
        action: String,
        observer: (simpleData: String?) -> Unit
    ) {
        if (!isAdded) return
        val intentFilter = IntentFilter(action)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.action?.let {
                    BaseModuleLog.dEvent("接收到事件广播:$it", className)
                    if (intentFilter.matchAction(it)) {
                        observer(
                            intent.getStringExtra(BaseConstBundleKey.LOCAL_EVENT_COMMON_SIMPLE_DATA)
                        )
                    }
                }
            }
        }
        broadcastReceivers = broadcastReceivers ?: HashMap()
        broadcastReceivers?.put(action, receiver)
        BaseModuleLog.dEvent("注册事件广播:$action", className)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
    }

    /**
     * 事件广播使用
     */
//    override fun registerLocalEvent(
//        action: String?,
//        observer: (action: String, type: Long?, value: String?, intent: Intent) -> Unit
//    ) {
//        if (action == null) {
//            return
//        }
//        val intentFilter = IntentFilter(action)
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                intent.action?.let {
//                    BaseModuleLog.dEvent("接收到事件广播:$it", className)
//                    if (intentFilter.matchAction(it)) {
//                        val type =
//                            intent.getLongExtra(BaseConstBundleKey.LOCAL_EVENT_COMMON_TYPE, -1)
//                        val value =
//                            intent.getStringExtra(BaseConstBundleKey.LOCAL_EVENT_COMMON_VALUE)
//                        observer(action, type, value, intent)
//                    }
//                }
//            }
//        }
//        broadcastReceivers = broadcastReceivers ?: HashMap()
//        broadcastReceivers?.put(action, receiver)
//        BaseModuleLog.dEvent("注册事件广播:$action", className)
//        context?.let {
//            LocalBroadcastManager.getInstance(it).registerReceiver(receiver, intentFilter)
//        }
//    }
//
//    override fun sendLocalEvent(action: String?, type: Long?, value: String?) {
//        LocalEventUtils.sendAction(action, type)
//    }

    override fun sendLocalEvent(action: String, simpleData: String?) {
        LocalEventUtils.sendAction(action, simpleData)
    }

    override fun sendLocalEvent(action: String, dataIntent: Intent) {
        LocalEventUtils.sendAction(action, dataIntent)
    }

    override fun unregisterLocalEvent(action: String?) {
        action?.let {
            broadcastReceivers?.get(action)?.let {
                context?.let { c ->
                    BaseModuleLog.dEvent("注销事件广播:$action", className)
                    LocalBroadcastManager.getInstance(c).unregisterReceiver(it)
                }
            }
            broadcastReceivers?.remove(it)
        }
    }

    open fun onLazyInit() {
        BaseModuleLog.dFragment("触发懒加载", className)
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

    inline fun <reified F : Fragment> fragmentInstance(fromType: Int): F? {
        return fragmentInstanceEx(fromType)
    }

    fun delayRunJava(time: Long = 2000, runnable: Runnable) {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(time)
            withContext(Dispatchers.Main) {
                runnable.run()
            }
        }
    }

    /*--------------------------------------------------------------------------------------*/

    override fun isPermissionGranted(permission: String): Boolean {
        if (!isAdded) return false
        return ContextCompat.checkSelfPermission(
            requireContext(),
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
        if (!isAdded) return false
        return ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
    }

    override fun isPermissionNotRequest(permission: String): Boolean {
        if (!isAdded) return false
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_DENIED
    }

    override fun isPermissionDenied(permission: String): Boolean {
        if (!isAdded) return false
        return !isPermissionShouldShowRational(permission) && ContextCompat.checkSelfPermission(
            requireContext(),
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

    override fun addPermissionsListener(
        permissions: Array<String>,
        listener: (Map<String, @JvmSuppressWildcards Boolean>) -> Unit
    ) {
        permissionsListenerMap[permissions] = listener
    }

    override fun onPermissionsResult(result: Map<String, @JvmSuppressWildcards Boolean>) {
        val iterator = permissionsListenerMap.iterator()
        if (iterator.hasNext()) {
            isPermissionRequestInProgress = false
            val entry = iterator.next()
            iterator.remove()
            entry.value.invoke(result)
        }
    }

    override fun openAppDetailsSettings() {
        if (!isAdded) return
        BaseModuleLog.dPermission("跳转系统设置界面")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
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
                    autoRequestPermissionsPool()
                }
            }
            addPermissionsListener(arrayOf(permission), listener)
            requestPermission(permission)
        } else if (isPermissionDenied(permission)) {
            BaseModuleLog.dPermission("权限已拒绝,且不在提示:$permission")
            callback(false, true)
        }
    }

    private fun autoRequestPermissionsPool() {
        permissionsListenerMap.iterator().let {
            if (it.hasNext()) {
                BaseModuleLog.dPermission("权限池不为空,继续自动请求")
                launchPermission(it.next().key)
            }
        }
    }

    override fun requestPermission(permission: String) {
        val perm = arrayOf(permission)
        launchPermission(perm)
    }

    override fun requestPermissions(permission: Array<String>) {
        launchPermission(permission)
    }

    private fun launchPermission(permission: Array<String>) {
        if (!isPermissionRequestInProgress) {
            BaseModuleLog.dPermission("启动权限请求:" + permission.contentToString())
            requestPermissionLauncher.launch(permission)
            isPermissionRequestInProgress = true
        }
    }

    /*---------------------------------------------------------------------------------------*/


    override fun enableKeyboardHeightListener(): Boolean = false

    override fun setKeyboardHeightListener(
        rootView: View,
        callback: ((height: Int, visible: Boolean) -> Unit)?
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val height = imeInsets.bottom
            val visible = insets.isVisible(WindowInsetsCompat.Type.ime())
            onKeyboardHeightChanged(height, visible)
            callback?.invoke(height, visible)
            insets
        }
    }

    override fun getScreenRealHeight(): Int {
        if (activity == null) return 0
        val screenRealHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 12+ 使用 WindowMetrics
            val windowMetrics = activity?.windowManager?.currentWindowMetrics
            val insets =
                windowMetrics?.windowInsets?.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            (windowMetrics?.bounds?.height() ?: 0) - (insets?.top ?: 0) - (insets?.bottom ?: 0)
        } else {
            // 旧版本兼容（已过时但可用）
            val size = Point()
            activity?.windowManager?.defaultDisplay?.getRealSize(size)
            size.y
        }
        BaseModuleLog.dKeyboard("获取到的屏幕真实高度:$screenRealHeight", className)
        return screenRealHeight
    }

    override fun getNavigationBarHeight(rootView: View): Int {
        var navBarHeight = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //在 Android 11（API 30）及以上 版本中，通过 WindowInsets.Type.navigationBars() 获取导航栏高度。
            val insets = rootView.rootWindowInsets
            if (insets != null) {
                navBarHeight = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 到 Android 10，使用过时 API（需忽略警告）
            val insets = rootView.rootWindowInsets;
            if (insets != null) {
                navBarHeight = insets.stableInsetBottom;
            }
        }
        if (navBarHeight != -1) {
            //通过 AndroidX 的 WindowInsetsCompat 库，可以统一处理所有 Android 版本，无需手动判断 API 等级。
            val insets = ViewCompat.getRootWindowInsets(rootView)
            navBarHeight = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        }
        BaseModuleLog.dKeyboard("获取的导航栏高度:$navBarHeight")
        return max(navBarHeight, 0)
    }

    override fun onKeyboardHeightChanged(height: Int, visible: Boolean) {
        BaseModuleLog.dKeyboard("触发键盘高度变化:$height-是否显示:$visible", className)
    }

    /*----------------------------------------------------------------------------------------*/

    open fun getConditionType(): Boolean =
        arguments?.getBoolean(BaseConstBundleKey.IS_CONDITION_TYPE, false) ?: false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        BaseModuleLog.dFragment("生命周期onSaveInstanceState", className)
    }

    override fun onPause() {
        super.onPause()
        BaseModuleLog.dFragment("生命周期onPause", className)
    }

    override fun onStart() {
        super.onStart()
        BaseModuleLog.dFragment("生命周期onStart", className)
    }

    override fun onStop() {
        super.onStop()
        BaseModuleLog.dFragment("生命周期onStop", className)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        BaseModuleLog.dFragment("生命周期onHiddenChanged,hidden:$hidden", className)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        BaseModuleLog.dFragment("生命周期onLowMemory", className)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BaseModuleLog.dFragment("执行onDestroyView", className)
        isLazyInitialized = false
    }

    override fun onDetach() {
        super.onDetach()
        BaseModuleLog.dFragment("执行onDetach", className)
        mActivity = null
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseModuleLog.dFragment("执行onDestroy", className)
        broadcastReceivers?.keys?.toList()?.forEach {
            unregisterLocalEvent(it)
        }
    }

}