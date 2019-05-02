package de.xikolo.viewmodels.base

import androidx.lifecycle.ViewModel
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

abstract class BaseViewModel : ViewModel() {

    private var firstCreate = true

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    open val networkState: NetworkStateLiveData by lazy {
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
