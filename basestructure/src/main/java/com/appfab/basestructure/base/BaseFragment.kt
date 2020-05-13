package com.appfab.basestructure.base

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.appfab.basestructure.utils.CacheManager
import io.reactivex.rxjava3.disposables.Disposable
import org.koin.android.ext.android.inject

abstract class BaseFragment: Fragment() ,LifecycleOwner,BaseListener {


    val cacheManager: CacheManager by inject()
    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
    private lateinit var disposeObservable: Disposable

    override fun onAttach(context: Context) {
        super.onAttach(context)
        disposeObservable = disposableListener("EVENT_IN_FRAGMENT")
    }

    override fun onDetach() {
        super.onDetach()
        disposeObservable.dispose()
    }
}