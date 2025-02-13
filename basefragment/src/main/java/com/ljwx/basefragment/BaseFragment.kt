package com.ljwx.basefragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ljwx.baseapp.constant.BaseConstBundleKey
import com.ljwx.baseapp.constant.BaseLogTag
import com.ljwx.baseapp.keyboard.KeyboardHeightProvider
import com.ljwx.baseapp.page.IPageLocalEvent
import com.ljwx.baseapp.page.IPageProcessStep
import com.ljwx.baseapp.page.IPageDialogTips
import com.ljwx.baseapp.page.IPageKeyboardHeight
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

open class BaseFragment(@LayoutRes private val layoutResID: Int = com.ljwx.baseapp.R.layout.baseapp_state_layout_empty) :
    BaseToolsFragment(),
    IPageLocalEvent,
    IPageDialogTips, IPageProcessStep, IPageStartPage, IPageKeyboardHeight {

    protected val className = this.javaClass.simpleName

    protected var mActivity: AppCompatActivity? = null

    private var isLazyInitialized = false

    /**
     * 键盘
     */
    protected var mScreenHeight = -1//辅助计算键盘高度

    protected var keyboardHighProvider: KeyboardHeightProvider? = null

    private var hidePopBottom = 0

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
            createKeyboardHeightProvider()
            keyboardHeightRootView()?.post { keyboardHighProvider?.start() }
        }
    }

    override fun onResume() {
        super.onResume()
        BaseModuleLog.dFragment("生命周期onResume", className)
        if (!isLazyInitialized && !isHidden) {
            onLazyInit()
            isLazyInitialized = true
        }
        if (enableKeyboardHeightListener()) {
            setKeyboardHeightListener()
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
        if (!isAdded) {
            return null
        }
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
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(receiver, intentFilter)
        }
    }

    override fun sendLocalEvent(action: String?, type: Long?, value: String?) {
        LocalEventUtils.sendAction(action, type)
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

    /*---------------------------------------------------------------------------------------*/


    override fun enableKeyboardHeightListener(): Boolean = false

    override fun createKeyboardHeightProvider() {
        keyboardHighProvider = keyboardHighProvider ?: KeyboardHeightProvider(requireActivity())
    }

    override fun keyboardHeightRootView(): View? = view

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
        keyboardHighProvider?.setKeyboardHeightListener(null)
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
        keyboardHighProvider?.close()
    }

}