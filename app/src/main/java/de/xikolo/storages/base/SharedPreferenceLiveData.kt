package de.xikolo.storages.base

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

// taken from https://stackoverflow.com/a/57074217
abstract class SharedPreferenceLiveData<T>(var sharedPrefs: SharedPreferences, var key: String, var defValue: Any) : LiveData<T>() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == this.key) {
            value = valueFromPreferences(key, defValue)
        }
    }

    abstract fun valueFromPreferences(key: String, defValue: Any): T?

    override fun onActive() {
        super.onActive()
        value = valueFromPreferences(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    class SharedPreferenceStringLiveData(sharedPrefs: SharedPreferences, key: String, defValue: String) : SharedPreferenceLiveData<String>(sharedPrefs, key, defValue) {
        override fun valueFromPreferences(key: String, defValue: Any): String? = sharedPrefs.getString(key, defValue as String?)
    }

    fun SharedPreferences.stringLiveData(key: String, defValue: String): SharedPreferenceLiveData<String> {
        return SharedPreferenceStringLiveData(this, key, defValue)
    }
}
