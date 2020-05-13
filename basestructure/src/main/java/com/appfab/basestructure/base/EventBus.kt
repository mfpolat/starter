package com.appfab.basestructure.base

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

object EventBus {

    var publisher = PublishSubject.create<Any>()
        private set

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    inline fun <reified T : Any> observe(): Observable<T> {
        return publisher.ofType(T::class.java)
    }
}