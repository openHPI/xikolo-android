package de.xikolo.network.jobs.base

import de.xikolo.events.NetworkStateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

class HealthCheckNetworkStateLiveData : NetworkStateLiveData() {

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
            value = HealthCheckNetworkState(code, userRequest, deprecationDate)
        }
    }

}

class HealthCheckNetworkState(code: NetworkCode, userRequest: Boolean = false, val deprecationDate: Date? = null) : NetworkState(code, userRequest)
