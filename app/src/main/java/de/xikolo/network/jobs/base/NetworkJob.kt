package de.xikolo.network.jobs.base

import de.xikolo.App
import de.xikolo.managers.UserManager
import de.xikolo.utils.extensions.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class NetworkJob(private val networkState: NetworkStateLiveData, private val userRequest: Boolean, private vararg val preconditions: Precondition) {

    fun run() {
        if (preconditions.contains(Precondition.AUTH) && !UserManager.isAuthorized) {
            networkState.state(NetworkCode.NO_AUTH, userRequest)
            return
        }

        if (!App.instance.isOnline) {
            networkState.state(NetworkCode.NO_NETWORK, userRequest)
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                networkState.state(NetworkCode.STARTED, userRequest)
                onRun()
            } catch (e: Throwable) {
                networkState.state(NetworkCode.ERROR, userRequest)
            }
        }
    }

    fun success() = networkState.success(userRequest)

    fun error() = networkState.error(userRequest)

    protected abstract suspend fun onRun()

    enum class Precondition {
        AUTH
    }

}
