package de.xikolo.models.base

import io.realm.RealmModel

interface RealmAdapter<T : RealmModel> {

    fun convertToRealmObject(): T

}
