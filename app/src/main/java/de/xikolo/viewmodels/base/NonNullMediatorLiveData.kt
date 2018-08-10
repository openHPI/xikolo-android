package de.xikolo.viewmodels.base

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

class NonNullMediatorLiveData<T> : MediatorLiveData<T>()

fun <T> LiveData<T>.nonNull(): NonNullMediatorLiveData<T> {
    val mediator: NonNullMediatorLiveData<T> = NonNullMediatorLiveData()
    mediator.addSource(this) { it?.let { mediator.value = it } }
    return mediator
}

fun <T> NonNullMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit): (t: T) -> Unit {
    this.observe(owner, android.arch.lifecycle.Observer {
        it?.let(observer)
    })
    return observer
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit): (t: T) -> Unit {
    this.observe(owner, android.arch.lifecycle.Observer {
        it?.let(observer)
    })
    return observer
}
