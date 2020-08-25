package com.mfpolat.starter.base

import android.util.Log
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

interface BaseListener {

    fun listener(event: Any) {

    }

    fun disposableListener(log: String): Disposable {
        return EventBus.observe<Any>()
            .subscribe(Consumer {
                Log.d(log, it.toString())
                listener(it)
            })
    }
}