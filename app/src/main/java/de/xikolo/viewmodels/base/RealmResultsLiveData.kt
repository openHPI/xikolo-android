package de.xikolo.viewmodels.base

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults

class RealmResultsLiveData<T : RealmModel>(private val realmResults: RealmResults<T>) : LiveData<List<T>>() {

    private val listener = RealmChangeListener<RealmResults<T>> { results -> value = results }

    override fun onActive() {
        realmResults.addChangeListener(listener)
    }

    override fun onInactive() {
        realmResults.removeChangeListener(listener)
    }

}

fun <T : RealmModel> RealmResults<T>.asLiveData() = RealmResultsLiveData(this)
