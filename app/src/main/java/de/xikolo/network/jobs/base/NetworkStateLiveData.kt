package de.xikolo.network.jobs.base

import de.xikolo.App
import de.xikolo.states.base.LiveDataState

open class NetworkStateLiveData : LiveDataState<NetworkState>() {

    fun success(userRequest: Boolean) {
        state(NetworkCode.SUCCESS, userRequest)
    }

    fun error(userRequest: Boolean) {
        state(NetworkCode.ERROR, userRequest)
    }

    fun state(code: NetworkCode, userRequest: Boolean) {
        notifyAboutConnectivityChange(code)

        super.state(NetworkState(code, userRequest))
    }

    protected fun notifyAboutConnectivityChange(code: NetworkCode) {
        when (code) {
            NetworkCode.SUCCESS    -> App.instance.state.connectivity.online()
            NetworkCode.NO_NETWORK -> App.instance.state.connectivity.offline()
            else                   -> Unit
        }
    }
}

open class NetworkState(val code: NetworkCode, val userRequest: Boolean = false)
