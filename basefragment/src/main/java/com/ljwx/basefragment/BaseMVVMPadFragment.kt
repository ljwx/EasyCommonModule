package com.ljwx.basefragment

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ljwx.baseapp.vm.BaseViewModel
import com.ljwx.baseapp.vm.ViewModelScope
import java.lang.reflect.ParameterizedType

abstract class BaseMVVMPadFragment<Binding : ViewDataBinding, BindingPad : ViewDataBinding, ViewModel : BaseViewModel<*>>(
    @LayoutRes layoutRes: Int,
    @LayoutRes private val layoutResPad: Int
) : BaseBindingPadFragment<Binding, BindingPad>(layoutRes, layoutResPad) {

    protected val mViewModelScope by lazy {
        ViewModelScope()
    }

    /**
     * ViewModel
     */
    protected lateinit var mViewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = createViewModel()
        lifecycle.addObserver(mViewModel)
        initPopLoadingObserver()
    }

    open fun initPopLoadingObserver() {
        mViewModel.popLoadingShow.observe(this) {
            showPopLoading(it.first)
        }
        mViewModel.popLoadingDismiss.observe(this) {
            dismissPopLoading(it.first)
        }
    }

    open fun createViewModel(): ViewModel {
        val type = javaClass.genericSuperclass as ParameterizedType
        val modelClass = type.actualTypeArguments.getOrNull(2) as Class<ViewModel>
        return if (viewModelProviderFromFragment() != null)
            mViewModelScope.getFragmentScopeViewModel(
                viewModelProviderFromFragment()!!,
                modelClass
            )
        else if (viewModelProviderFromActivity()) mViewModelScope.getActivityScopeViewModel(
            requireActivity(),
            modelClass
        ) else ViewModelProvider(this)[modelClass]
    }

    open fun viewModelProviderFromActivity() = false

    open fun viewModelProviderFromFragment(): Fragment? {
        return null
    }

    protected fun <L : LiveData<T>, T> L.observe(observer: Observer<T>) {
        observe(viewLifecycleOwner, observer)
    }

    override fun commonStep3ObserveData() {
        mViewModel.finishActivity.observe(this) {
            if (it) {
                activity?.finish()
            }
        }
        mViewModel.scope()
    }

    open fun ViewModel.scope() {

    }

}