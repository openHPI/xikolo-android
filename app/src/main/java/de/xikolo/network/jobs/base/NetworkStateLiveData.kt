package de.xikolo.network.jobs.base

import androidx.lifecycle.LiveData
import de.xikolo.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class NetworkStateLiveData : LiveData<NetworkState>() {

    fun success(userRequest: Boolean) {
        state(NetworkCode.SUCCESS, userRequest)
    }

    fun error(userRequest: Boolean) {
        state(NetworkCode.ERROR, userRequest)
    }

    fun state(code: NetworkCode, userRequest: Boolean) {
        when (code) {
            NetworkCode.SUCCESS    -> App.instance.state.connectivity.online()
            NetworkCode.NO_NETWORK -> App.instance.state.connectivity.offline()
            else -> Unit
        }

        GlobalScope.launch(Dispatchers.Main) {
            value = NetworkState(code, userRequest)
        }
    }

}

open class NetworkState(val code: NetworkCode, val userRequest: Boolean = false)
