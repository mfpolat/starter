package com.mfpolat.starter.base

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


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