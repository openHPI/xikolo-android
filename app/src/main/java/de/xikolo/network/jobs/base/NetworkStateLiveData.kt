package de.xikolo.network.jobs.base

import androidx.lifecycle.LiveData
import de.xikolo.events.NetworkStateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

open class NetworkStateLiveData : LiveData<NetworkState>() {

    fun success(userRequest: Boolean) {
        state(NetworkCode.SUCCESS, userRequest)
    }

    fun error(userRequest: Boolean) {
        state(NetworkCode.ERROR, userRequest)
    }

    fun state(code: NetworkCode, userRequest: Boolean) {
        when (code) {
            NetworkCode.SUCCESS    -> EventBus.getDefault().postSticky(NetworkStateEvent(true))
            NetworkCode.NO_NETWORK -> EventBus.getDefault().postSticky(NetworkStateEvent(false))
            else                   -> Unit
        }

        GlobalScope.launch(Dispatchers.Main) {
            value = NetworkState(code, userRequest)
        }
    }

}

open class NetworkState(val code: NetworkCode, val userRequest: Boolean = false)
