package de.xikolo.network.jobs.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        super.notifyAboutConnectivityChange(code)

        GlobalScope.launch(Dispatchers.Main) {
            value = HealthCheckNetworkState(code, userRequest, deprecationDate)
        }
    }

}

class HealthCheckNetworkState(code: NetworkCode, userRequest: Boolean = false, val deprecationDate: Date? = null) : NetworkState(code, userRequest)
