package de.xikolo.jobs.base

import de.xikolo.lifecycle.base.NetworkCode
import de.xikolo.lifecycle.base.NetworkStateLiveData
import de.xikolo.managers.UserManager
import de.xikolo.utils.NetworkUtil
import kotlinx.coroutines.experimental.launch

abstract class NetworkJob(protected val networkState: NetworkStateLiveData, private vararg val preconditions: Precondition) {

    fun run() {
        if (preconditions.contains(Precondition.AUTH) && !UserManager.isAuthorized) {
            networkState.state(NetworkCode.NO_AUTH)
            return
        }

        if (!NetworkUtil.isOnline()) {
            networkState.state(NetworkCode.NO_NETWORK)
            return
        }

        launch {
            try {
                onRun()
            } catch (e: Throwable) {
                networkState.state(NetworkCode.ERROR)
            }
        }
    }

    protected abstract suspend fun onRun()

    enum class Precondition {
        AUTH
    }

}
