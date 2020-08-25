package com.mfpolat.starter.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.mfpolat.starter.utils.CacheManager
import com.mfpolat.starter.utils.DialogListener
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity(), LifecycleOwner,
    BaseListener, DialogListener {

    val cacheManager: CacheManager by inject()
    lateinit var lifecycleRegistry: LifecycleRegistry
    lateinit var binding: T
    private lateinit var disposeObservable: Disposable
    private var lastFragmentTag = ""
    abstract fun getLayoutResId(): Int

    protected open fun getTransformFunction(): (T) -> T {
        return { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
        binding = DataBindingUtil.inflate(layoutInflater, getLayoutResId(), null, false)
        setContentView(getTransformFunction().invoke(binding).root)
        disposeObservable = disposableListener("EVENT_IN_ACTIVITY")

    }


    override fun getLifecycle(): Lifecycle {
        if (!::lifecycleRegistry.isInitialized || lifecycleRegistry == null) {
            lifecycleRegistry = LifecycleRegistry(this)
        }
        return lifecycleRegistry
    }

    open fun needToChangeView(reason: String? = null, result: String? = null) {

    }

    override fun onDestroy() {
        super.onDestroy()
        disposeObservable.dispose()

    }


    fun addOrReplaceFragment(
        fragment: Class<*>,
        bindingFragmentContainerId: Int,
        args: Bundle? = null,
        dontBack: Boolean? = null,
        visibleToUserFunc: (fragment: Fragment) -> Unit = {}
    ) {
        if (supportFragmentManager.findFragmentByTag(fragment.name) == null) {
            try {
                val fragment1 = fragment.newInstance() as BaseFragment
                fragment1.arguments = args
                initFragment(fragment1, bindingFragmentContainerId, dontBack, visibleToUserFunc)
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        } else {
            if (lastFragmentTag != fragment.name)
                replaceFragment(fragment.name, bindingFragmentContainerId, visibleToUserFunc)
        }
    }

    fun removeFragment(fragment: Class<*>) {

        Log.e("XXXXX","removing fragment  = $fragment.name")
        val removingFragment = supportFragmentManager.findFragmentByTag(fragment.name)
        removingFragment?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }

    fun initFragment(
        fragment: BaseFragment,
        bindingFragmentContainerId: Int,
        dontBack: Boolean? = null,
        visibleToUserFunc: (fragment: Fragment) -> Unit = {}
    ) {
        val tag = fragment.javaClass.name
        val transaction = supportFragmentManager.beginTransaction()
            .replace(bindingFragmentContainerId, fragment, tag)
//        if (dontBack != true) {
//            transaction.addToBackStack(tag)
//        }
        transaction.commitAllowingStateLoss()
        lastFragmentTag = tag
        visibleToUserFunc(fragment)
        Log.e("XXXXX","adding fragment  = $tag")

    }

    fun replaceFragment(
        tag: String,
        bindingFragmentContainerId: Int,
        visibleToUserFunc: (fragment: Fragment) -> Unit
    ) {


        val fragment = supportFragmentManager.findFragmentByTag(tag)
        val lastFragment = supportFragmentManager.findFragmentByTag(lastFragmentTag)
        lastFragment?.let {
            lastFragmentTag = tag
            supportFragmentManager.beginTransaction()
                .show(fragment!!)
                .hide(it)
                .commitAllowingStateLoss()
            visibleToUserFunc(fragment)
            Log.e("XXXXX","showing fragment  = $tag")
            Log.e("XXXXX","hiding fragment  = ${it.javaClass.name}")

        } ?: kotlin.run {
            supportFragmentManager.beginTransaction()
                .show(fragment!!)
                .commitAllowingStateLoss()
            visibleToUserFunc(fragment)
        }

    }

}