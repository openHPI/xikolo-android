package de.xikolo.view_models.base

import android.arch.lifecycle.ViewModel
import io.realm.Realm

abstract class BaseViewModel : ViewModel() {

    private var firstCreate = true

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    val networkState: NetworkStateLiveData by lazy {
        NetworkStateLiveData()
    }

    open fun onCreate() {
        if (firstCreate) {
            firstCreate = false
            onFirstCreate()
        }
    }

    open fun onFirstCreate() {}

    abstract fun onRefresh()

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

}
