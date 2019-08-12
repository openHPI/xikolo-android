package de.xikolo.events.base

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ConnectivityStateLiveData : LiveData<Boolean>() {

    fun online() {
        state(true)
    }

    fun offline() {
        state(false)
    }

    private fun state(online: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            value = online
        }
    }

}
