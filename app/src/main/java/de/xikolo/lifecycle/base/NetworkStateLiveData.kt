package de.xikolo.lifecycle.base

import android.arch.lifecycle.LiveData
import de.xikolo.events.NetworkStateEvent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus

class NetworkStateLiveData : LiveData<NetworkState>() {

    fun success() {
        error(NetworkCode.SUCCESS)
    }

    fun error() {
        error(NetworkCode.ERROR)
    }

    fun state(code: NetworkCode, userRequest: Boolean = false) {
        when (code) {
            NetworkCode.SUCCESS    -> EventBus.getDefault().postSticky(NetworkStateEvent(true))
            NetworkCode.NO_NETWORK -> EventBus.getDefault().postSticky(NetworkStateEvent(false))
            else -> {}
        }

        launch(UI) {
            value = NetworkState(code, userRequest)
        }
    }

}

data class NetworkState(val code: NetworkCode, val userRequest: Boolean = false)

enum class NetworkCode {
    SUCCESS, ERROR, CANCEL, NO_NETWORK, NO_AUTH, MAINTENANCE, API_VERSION_EXPIRED
}
