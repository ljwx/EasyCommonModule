package com.ljwx.baseactivity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.ljwx.baseapp.BasePopupLoading
import com.ljwx.baseapp.R
import com.ljwx.baseapp.view.IViewRefreshLayout
import com.ljwx.baseapp.view.IViewStateLayout
import com.ljwx.baseapp.constant.BaseLayoutStatus
import com.ljwx.baseapp.extensions.isMainThread
import com.ljwx.baseapp.page.IPagePopLoading
import com.ljwx.baseapp.page.IPageRefreshLayout
import com.ljwx.baseapp.page.IPageStateLayout
import com.ljwx.baseapp.util.BaseModuleLog

open class BaseStateRefreshActivity(@LayoutRes layoutResID: Int = R.layout.baseapp_state_layout_empty) :
    BaseActivity(layoutResID), IPagePopLoading, IPageStateLayout,
    IPageRefreshLayout {

    private val mPopupLoading by lazy {
        BasePopupLoading(this)
    }

    /**
     * 多状态控件
     */
    private var mStateLayout: IViewStateLayout? = null

    /**
     * 下拉刷新控件
     */
    private var mRefreshLayout: IViewRefreshLayout? = null

    /**
     * 多状态的数据是否成功获取过 成功过
     */
    protected var stateLoadingDataSucceeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onViewCreated() {
        super.onViewCreated()
        enableAutoInitStateLayout()
        enableAutoInitRefreshLayout()
    }

    override fun showPopLoading(
        show: Boolean,
        message: CharSequence?,
        cancelable: Boolean,
        transparent: Boolean,
        level: Int
    ) {
        if (!show || (isPopupLoadingShowing()) || isFinishing) {
            return
        }
        runOnUiThread {
            BaseModuleLog.dDialog("自动主线程显示loading", className)
//            mPopupLoading.setCancelable(cancelable)
//            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
            mPopupLoading.showPopup(show, cancelable, message, backgroundTransparent = transparent)
        }
    }

    override fun dismissPopLoading(dismiss: Boolean) {
        runOnUiThread {
            BaseModuleLog.dDialog("自动主线程取消loading", className)
            mPopupLoading.dismiss()
        }
    }

    override fun isPopupLoadingShowing(): Boolean = mPopupLoading.isShowing()

    override fun setPopupLoadingLayout(@LayoutRes layout: Int) {
        mPopupLoading.setLayout(layout)
    }

    /*================================================================*/

    /**
     * 快速使用状态布局
     */
    override fun enableAutoInitStateLayout() {
        val stateLayout = findViewById<View>(R.id.base_app_quick_state_layout)
        if (stateLayout != null && stateLayout is IViewStateLayout) {
            initStateLayout(stateLayout)
        }
    }

    /**
     * 快速使用刷新布局
     */
    override fun enableAutoInitRefreshLayout() {
        var refreshLayout = findViewById<View>(R.id.base_app_quick_refresh_layout)
        if (refreshLayout == null) {
            refreshLayout = findViewById(R.id.base_app_page_refresh_layout)
        }
        if (refreshLayout != null && refreshLayout is IViewRefreshLayout) {
            initRefreshLayout(refreshLayout)
        }
    }

    /*================================================================*/

    /**
     * 初始化多状态布局
     *
     * @param stateLayout 多状态布局容器
     */
    override fun initStateLayout(stateLayout: IViewStateLayout?) {
        BaseModuleLog.dStateRefresh("初始化多状态布局", className)
        this.mStateLayout = stateLayout
    }

    override fun addStateLayoutClick(
        @BaseLayoutStatus.LayoutStatus state: Int,
        id: Int,
        listener: View.OnClickListener,
    ) {
        BaseModuleLog.dStateRefresh("添加多状态下的点击事件id:$id", className)
        mStateLayout?.addClickListener(state, id, listener)
    }

    override fun showLoadingStateInit() {
        if (!stateLoadingDataSucceeded) {
            BaseModuleLog.dStateRefresh("首次加载数据,显示stateLoading", className)
            showStateLoading()
        }
    }

    override fun loadingStateInitComplete() {
        BaseModuleLog.dStateRefresh("首次数据加载成功", className)
        stateLoadingDataSucceeded = true
    }

    override fun showErrorStateInit(): Boolean {
        if (!stateLoadingDataSucceeded) {
            BaseModuleLog.dStateRefresh("首次加载数据失败,显示stateError", className)
            showStateError()
            return true
        }
        return false
    }

    /**
     * 显示布局状态
     *
     * @param state 哪种状态
     * @param show 是否需要显示
     * @param tag 携带数据
     */
    override fun showStateLayout(state: Int, show: Boolean, view: View?, tag: Any?) {
        BaseModuleLog.dStateRefresh("自动主线程修改当前多状态:$show--$state", className)
        if (!show || isFinishing) {
            return
        }
        runOnUiThread {
            mStateLayout?.showStateView(state, view, tag)
        }
    }


    open fun showStateContent() = showStateLayout(BaseLayoutStatus.CONTENT)
    open fun showStateEmpty() = showStateLayout(BaseLayoutStatus.EMPTY)
    open fun showStateLoading() = showStateLayout(BaseLayoutStatus.LOADING)
    open fun showStateError() = showStateLayout(BaseLayoutStatus.ERROR)
    open fun showStateOffline() = showStateLayout(BaseLayoutStatus.OFFLINE)
    open fun showStateExtend() = showStateLayout(BaseLayoutStatus.EXTEND)
    open fun showStateExtend2() = showStateLayout(BaseLayoutStatus.EXTEND2)
    open fun showStateExtend3() = showStateLayout(BaseLayoutStatus.EXTEND3)

    /*================================================================*/

    override fun enableRefresh(): Boolean = true

    override fun initRefreshLayout(refreshLayout: IViewRefreshLayout?) {
        if (enableRefresh()) {
            BaseModuleLog.dStateRefresh("启用快捷下拉刷新,refreshView", className)
            refreshLayout?.enableRefresh(true)
            this.mRefreshLayout = refreshLayout
            refreshLayout?.setRefreshPage(this)
        } else {
            refreshLayout?.enableRefresh(false)
        }
    }

    override fun initRefreshLayout(refreshId: Int) {
        this.mRefreshLayout = findViewById<View>(refreshId) as? IViewRefreshLayout
        if (enableRefresh()) {
            BaseModuleLog.dStateRefresh("启用快捷下拉刷新,refresh id")
            this.mRefreshLayout?.enableRefresh(true)
            this.mRefreshLayout?.setRefreshPage(this)
        } else {
            this.mRefreshLayout?.enableRefresh(false)
        }
    }

    /**
     * 下拉刷新逻辑
     */
    override fun refreshViewOnRefresh() {
        BaseModuleLog.dStateRefresh("下拉刷新控件,触发刷新", className)

    }

    override fun onLoadData(refresh: Boolean, params: String?) {
        BaseModuleLog.dDialog("触发onLoadData,是否刷新:$refresh", className)
    }

//    override fun onLoadData(refresh: Boolean, type: Long?) {
//
//    }

    /**
     * 刷新结束
     */
    override fun pullRefreshFinish() {
        BaseModuleLog.dStateRefresh("下拉刷新控件刷新完成", className)
        runOnUiThread {
            mRefreshLayout?.refreshFinish()
        }
    }

    override fun onDestroy() {
        mPopupLoading.dismiss()
        mStateLayout = null
        mRefreshLayout = null
        super.onDestroy()
    }

}