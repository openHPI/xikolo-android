package de.xikolo.lifecycle.base

import android.arch.lifecycle.ViewModel
import io.realm.Realm

abstract class BaseViewModel : ViewModel() {

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val networkState: NetworkStateLiveData by lazy {
        NetworkStateLiveData()
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    abstract fun onCreate()

    abstract fun onRefresh()

}
