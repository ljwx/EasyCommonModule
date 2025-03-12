package com.ljwx.basemodulev2.activity

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.ljwx.basebase.activity.IBaseActivity
import com.ljwx.basebase.util.NavigationBarUtils


open class BaseActivity(@LayoutRes private val layoutResID: Int) : AppCompatActivity(), IBaseActivity {

    private val windowInsetsController by lazy {
        WindowCompat.getInsetsController(
            window,
            window.decorView
        )
    }

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBeforeSetContentView()
        onSetContentView()
        onViewCreated(getRootView())
        enableUsualSteps()
    }

    override fun getLayoutRes(): Int {
        return layoutResID
    }

    override fun onBeforeSetContentView() {

    }

    override fun onSetContentView() {
        setContentView(getLayoutRes())
    }

    override fun onViewCreated(rootView: View) {

    }

    override fun getRootView(): View {
        return window.decorView.findViewById(android.R.id.content)
    }

    override fun getActivityOrientation(): Int {
        return getResources().configuration.orientation
    }

    override fun setActivityOrientation(orientation: Int) {
        requestedOrientation = orientation
    }

    override fun isActivityLandscape(): Boolean {
        return getActivityOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun setActivityLandscape() {
        setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    }

    override fun setActivityPortrait() {
        setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    override fun hideStatusBar() {
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    override fun setStatusBarLight() {

    }

    override fun setStatusBarDark() {
        // 设置状态栏文字为浅色（白色）
        windowInsetsController.isAppearanceLightStatusBars = false
    }

    override fun setStatusBarBackgroundColor(color: Int) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    override fun setStatusBarFontDark(dark: Boolean) {
        // 状态栏是否亮色
        windowInsetsController.isAppearanceLightStatusBars = dark
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 关闭浅色模式（默认白色文字）
//            );
//        }
    }

    override fun setStatusBarTransparent() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun initToolbar(title: CharSequence?) {
        val view = getRootView().findViewById<View>(com.ljwx.basebase.R.id.base_module_toolbar_id)
        if (view is Toolbar) {
            toolbar = view
        }
        toolbar?.setTitle(title)
        toolbar?.let {
            setSupportActionBar(it)
            toolbar?.setNavigationOnClickListener {
                if (!isFinishing && !isDestroyed) {
                    finish()
                }
            }
//            BaseModuleLog.dToolbar("初始化toolbar并返回", className)
        }
    }

    override fun getToolbar(): Toolbar? {
        return toolbar
    }

    override fun setToolbarTitle(title: CharSequence) {
        toolbar?.setTitle(title)
    }

    override fun startActivitySimple(
        clazz: Class<*>,
        from: Int?,
        resultCallback: ((result: ActivityResult) -> Unit)?
    ) {

    }

    override fun hideNavigationBar() {
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    override fun getNavigationBarHeight(rootView: View): Int {
        return NavigationBarUtils.getHeight(rootView)
    }

    override fun enableUsualSteps() {
        onUsualStep1InitData()
        onUsualStep2InitView()
        onUsualStep3ObserveData()
        onUsualStep4SetViewListener()
        onUsualStep5FetchData()
    }

    override fun onUsualStep1InitData() {

    }

    override fun onUsualStep2InitView() {

    }

    override fun onUsualStep3ObserveData() {

    }

    override fun onUsualStep4SetViewListener() {

    }

    override fun onUsualStep5FetchData(refresh: Boolean) {

    }

}