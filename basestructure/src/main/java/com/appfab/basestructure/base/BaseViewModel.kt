package com.appfab.basestructure.base

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.appfab.basestructure.utils.CacheManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

abstract class BaseViewModel <T:BaseNavigator> : ViewModel() ,BaseListener, KoinComponent,Observable {

    private val registry: PropertyChangeRegistry =  PropertyChangeRegistry()
    var isLoading : ObservableBoolean = ObservableBoolean()
    val cacheManager: CacheManager by inject()
    /**
     * Coroutine scopes. All tasks on these scopes cancel when viewModel is destroyed.
     */
    private val viewModelJob = Job()
    val bgScope = CoroutineScope(viewModelJob)
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    /**
     * Propertylerin degismesi kayda aliniyor. Databinding ile verilerin otomatik guncellemesini
     * saglar
     *
     * @param callback
     */
    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) = registry.add(callback)


    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) = registry.remove(callback)
    /**
     * Bu metodu cagirarak view model icerisinde olusturdugunuz bindable metodlarini otomatik
     * olarak gunceller
     *
     */
    fun notifyChange() {
        registry.notifyCallbacks(this, 0, null)
    }
    /**
     * Spesifik olarak istediginiz bindable degiskeninizi guncellemenizi saglar
     *
     * @param fieldId guncellemek istedigin item idsini verebilirsiniz
     */
    fun notifyPropertyChanged(fieldId: Int) {
        registry.notifyCallbacks(this, fieldId, null)
    }
    /**
     * Interface between activities/fragments and viewmodels.
     */
    protected lateinit var navigator: T

    /**
     * @param nav Navigator interface for viewModel to use.
     */
    open fun start(nav: T) {
        if (!::navigator.isInitialized) this.navigator = nav
    }

    /**
     * Convenience function to run code on UI thread. Useful for switching back to UI thread inside
     * a background scope.
     * @param codeBlock Funtion to run.
     */
    protected fun launchOnUi(codeBlock: CodeBlock) {
        uiScope.launch { codeBlock() }
    }

    /**
     * Convenience function to run code on background thread.
     * @param codeBlock Funtion to run.
     */
    protected fun launchOnBg(
        codeBlock: CodeBlock
    ) {
        bgScope.launch {
            codeBlock()
        }
    }

    /**
     * Convenience function to run a list of functions in parallel.
     * Starts all functions at the same time, returns when all of them are completed.
     * @param codeBlocks List of functions to run.
     */
    protected fun launchParallel(codeBlocks: List<CodeBlock>) {
        bgScope.launch {
            val asyncList: MutableList<Deferred<Unit>> = mutableListOf()
            codeBlocks.forEach {
                asyncList.add(CoroutineScope(bgScope.coroutineContext).async {
                    it()
                })
            }

            asyncList.awaitAll()
        }
    }

    private val disposables: CompositeDisposable = CompositeDisposable()
    private var disposeObservable: Disposable = this.disposableListener("EVENT_IN_VM")

    init {
        addToDisposable(disposeObservable)
    }

    /**
     * This function provides you set add AresBus Listener then automatically clear on onCleared method
     *
     */
    fun addToDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    /**
     * This function provides you unset disposable listeners
     * Called when ViewModel is destroyed. Cancels both bg and ui scopes in this ViewModel.
     * If you need your tasks to survive ViewModels, define them in shared ViewModels or
     * another scope.
     */
    override fun onCleared() {
        disposables.clear()
        viewModelJob.cancel()
        super.onCleared()
    }





}
typealias CodeBlock = suspend () -> Unit
