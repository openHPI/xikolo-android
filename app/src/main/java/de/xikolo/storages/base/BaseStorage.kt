package de.xikolo.storages.base

import android.content.SharedPreferences

import de.xikolo.App

abstract class BaseStorage(name: String, mode: Int) {

    private val preferences: SharedPreferences = App.getInstance().getSharedPreferences(name, mode)

    fun getString(key: String, defValue: String? = null) : String? = preferences.getString(key, defValue)

    fun putString(key: String, value: String?) {
        preferences
            .edit()
            .putString(key, value)
            .commit()
    }

    fun delete() {
        preferences
            .edit()
            .clear()
            .apply()
    }

}
