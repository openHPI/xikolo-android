package de.xikolo.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import io.realm.RealmObject
import io.realm.RealmResults

class NonNullMediatorLiveData<T> : MediatorLiveData<T>()

fun <T> LiveData<T>.nonNull(): NonNullMediatorLiveData<T> {
    val mediator: NonNullMediatorLiveData<T> = NonNullMediatorLiveData()
    mediator.addSource(this) { it?.let { mediator.value = it } }
    return mediator
}

fun <T> NonNullMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit): (t: T) -> Unit {
    this.observe(owner, Observer<T> {
        it?.let(observer)
    })
    return observer
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit): (t: T) -> Unit {
    this.observe(owner, Observer<T> {
        if ((it is RealmObject && !it.isValid) || (it is RealmResults<*> && !it.isValid)) {
            return@Observer
        }

        it?.let(observer)
    })
    return observer
}

// observing stops when the observer function returns true
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (t: T) -> Boolean): (t: T) -> Boolean {
    this.observe(owner, object : Observer<T> {
        override fun onChanged(t: T) {
            if ((t is RealmObject && !t.isValid) || (t is RealmResults<*> && !t.isValid)) {
                return
            }

            if (t?.let(observer) == true) {
                removeObserver(this)
            }
        }
    })
    return observer
}

fun <T : RealmObject> LiveData<T>.observeUnsafeOnce(owner: LifecycleOwner, observer: (t: T) -> Boolean): (t: T) -> Boolean {
    this.observe(owner, object : Observer<T> {
        override fun onChanged(t: T) {
            if (t.let(observer)) {
                removeObserver(this)
            }
        }
    })
    return observer
}
