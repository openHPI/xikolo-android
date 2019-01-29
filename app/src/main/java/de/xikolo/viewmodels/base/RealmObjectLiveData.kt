package de.xikolo.viewmodels.base

import android.arch.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmObject

class RealmObjectLiveData<T : RealmObject>(private val realmObject: T) : LiveData<T>() {

    private val listener = RealmChangeListener<T> { result -> value = result }

    override fun onActive() {
        realmObject.addChangeListener(listener)
    }

    override fun onInactive() {
        realmObject.removeChangeListener(listener)
    }

}

fun <T : RealmObject> T.asLiveData() = RealmObjectLiveData(this)

// ToDo is this allowed?
