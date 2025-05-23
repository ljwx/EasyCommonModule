package com.ljwx.baseapp.vm

import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.StringUtils
import com.ljwx.baseapp.constant.BaseLogTag
import com.ljwx.baseapp.event.ISendLocalEvent
import com.ljwx.baseapp.response.DataResult
import com.ljwx.baseapp.util.BaseModuleLog
import com.ljwx.baseapp.util.LocalEventUtils
import com.ljwx.baseapp.vm.model.BaseDataRepository

abstract class BaseViewModel<M : BaseDataRepository<*>> : ViewModel(), IBaseViewModel<M>,
    DefaultLifecycleObserver, IRxAutoCleared, ISendLocalEvent {

    protected val currentClassName = this.javaClass.simpleName
    open val TAG = this.javaClass.simpleName + BaseLogTag.VIEW_MODEL

    protected var mRepository: M

    private var mCompositeDisposable2: io.reactivex.disposables.CompositeDisposable? = null
    private var mCompositeDisposable3: io.reactivex.rxjava3.disposables.CompositeDisposable? = null

    //是否显示,code,message
    private val mShowPopLoading = MutableLiveData<Triple<Boolean, Int, String>>()
    private val mDismissPopLoading = MutableLiveData<Triple<Boolean, Int, String>>()
    private val mFinishActivity = MutableLiveData<Boolean>()
    val popLoadingShow: LiveData<Triple<Boolean, Int, String>> = mShowPopLoading
    val popLoadingDismiss: LiveData<Triple<Boolean, Int, String>> = mDismissPopLoading
    val finishActivity: LiveData<Boolean> = mFinishActivity


    init {
        mRepository = createRepository()
    }


    override fun autoClear(disposable: io.reactivex.disposables.Disposable) {
        BaseModuleLog.dViewmodel("添加Rx2自动取消", currentClassName)
        if (mCompositeDisposable2 == null) {
            mCompositeDisposable2 = io.reactivex.disposables.CompositeDisposable()
        }
        mCompositeDisposable2?.add(disposable)
    }

    override fun autoClear(disposable: io.reactivex.rxjava3.disposables.Disposable) {
        BaseModuleLog.dViewmodel("添加Rx3自动取消", currentClassName)
        if (mCompositeDisposable3 == null) {
            mCompositeDisposable3 = io.reactivex.rxjava3.disposables.CompositeDisposable()
        }
        mCompositeDisposable3?.add(disposable)
    }

    override fun onRxCleared() {
        BaseModuleLog.dViewmodel("执行Rx自动取消", currentClassName)
        mCompositeDisposable2?.clear()
        mCompositeDisposable3?.clear()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        BaseModuleLog.dViewmodel("生命周期onStop", currentClassName)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        BaseModuleLog.dViewmodel("生命周期onDestroy", currentClassName)
    }

    override fun onCleared() {
        super.onCleared()
        mRepository?.onRxCleared()
        onRxCleared()
        BaseModuleLog.dViewmodel("生命周期onCleared", currentClassName)
    }

    open fun showPopLoading(show: Boolean = true, code: Int? = 0, message: String? = "") {
        BaseModuleLog.dViewmodel("显示Loading弹窗", currentClassName)
        mShowPopLoading.postValue(Triple(show, code ?: 0, message ?: ""))
    }

    open fun dismissPopLoading(dismiss: Boolean = true, code: Int? = 0, message: String? = "") {
        BaseModuleLog.dViewmodel("取消Loading弹窗", currentClassName)
        mDismissPopLoading.postValue(Triple(dismiss, code ?: 0, message ?: ""))
    }

    override fun commonResponseNotSuccess(result: DataResult<*>) {

    }

//    override fun sendLocalEvent(action: String?, type: Long?, value: String?) {
//        LocalEventUtils.sendAction(action, type)
//    }

    override fun sendLocalEvent(action: String, simpleData: String?) {
        LocalEventUtils.sendAction(action, simpleData)
    }

    override fun sendLocalEvent(action: String, dataIntent: Intent) {
        LocalEventUtils.sendAction(action, dataIntent)
    }

    override fun getString(string: Int) {
        StringUtils.getString(string)
    }

    override fun finishActivity(finish: Boolean) {
        BaseModuleLog.dViewmodel("发送消息结束activity:$finish", currentClassName)
        mFinishActivity.postValue(finish)
    }

}