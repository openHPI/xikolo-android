package de.xikolo.extensions

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmResults

class RealmObjectLiveData<T : RealmObject>(private val realmObject: T) : LiveData<T>() {

    private val listener = RealmChangeListener<T> { result -> value = result }

    override fun onActive() {
        realmObject.addChangeListener(listener)
    }

    override fun onInactive() {
        realmObject.removeChangeListener(listener)
    }

}

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
fun <T : RealmModel> RealmResults<T>.asCopy(): List<T> = realm.copyFromRealm(this)


fun <T : RealmObject> T.asLiveData() = RealmObjectLiveData(this)
fun <T : RealmObject> T.asCopy(): T? = realm.copyFromRealm(this)
