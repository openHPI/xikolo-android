package de.xikolo.network.jobs.base

import androidx.lifecycle.LiveData
import de.xikolo.events.NetworkStateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

class NetworkStateLiveData : LiveData<NetworkState>() {

    fun success(userRequest: Boolean) {
        state(NetworkCode.SUCCESS, userRequest)
    }

    fun error(userRequest: Boolean) {
        state(NetworkCode.ERROR, userRequest)
    }

    fun deprecated(deprecationDate: Date, userRequest: Boolean) {
        state(NetworkCode.API_VERSION_DEPRECATED, userRequest, deprecationDate)
    }

    fun apiVersionExpired(userRequest: Boolean) {
        state(NetworkCode.API_VERSION_EXPIRED, userRequest)
    }

    fun maintenance(userRequest: Boolean) {
        state(NetworkCode.MAINTENANCE, userRequest)
    }

    fun state(code: NetworkCode, userRequest: Boolean, deprecationDate: Date? = null) {
        when (code) {
            NetworkCode.SUCCESS    -> EventBus.getDefault().postSticky(NetworkStateEvent(true))
            NetworkCode.NO_NETWORK -> EventBus.getDefault().postSticky(NetworkStateEvent(false))
            else                   -> Unit
        }

        GlobalScope.launch(Dispatchers.Main) {
            value = NetworkState(code, userRequest, deprecationDate)
        }
    }

}

data class NetworkState(val code: NetworkCode, val userRequest: Boolean = false, val deprecationDate: Date? = null)

enum class NetworkCode {
    STARTED, SUCCESS, ERROR, CANCEL, NO_NETWORK, NO_AUTH, MAINTENANCE, API_VERSION_EXPIRED, API_VERSION_DEPRECATED
}
