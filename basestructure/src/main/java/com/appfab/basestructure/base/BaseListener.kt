package com.appfab.basestructure.base

import android.util.Log
import io.reactivex.rxjava3.disposables.Disposable

interface BaseListener {

    fun listener(event: Any) {

    }
    fun disposableListener(log: String): Disposable {
        return EventBus.observe<Any>()
            .subscribe({
                Log.d(log, it.toString())
                listener(it)
            }, {
                Log.e("${log}_ERROR", it.localizedMessage, it)
                listener(it)
            }, {

            })
    }
}