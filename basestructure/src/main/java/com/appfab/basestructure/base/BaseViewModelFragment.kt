package com.appfab.basestructure.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.appfab.basestructure.utils.CacheManager
import io.reactivex.rxjava3.disposables.Disposable
import org.koin.android.ext.android.inject

abstract class BaseViewModelFragment<T : ViewDataBinding, N : BaseNavigator, V : BaseViewModel<N>> :
    Fragment(),
    LifecycleOwner,
    BaseListener {

    abstract val vm: V
    lateinit var binding: T
    abstract fun getLayoutResId(): Int

    val cacheManager: CacheManager by inject()
    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            getLayoutResId(),
            container,
            false
        )

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    private lateinit var disposeObservable: Disposable

    override fun onAttach(context: Context) {
        super.onAttach(context)
        disposeObservable = disposableListener("EVENT_IN_FRAGMENT")

        vm.start(this as N)

    }

    override fun onDetach() {
        super.onDetach()
        disposeObservable.dispose()
    }
}