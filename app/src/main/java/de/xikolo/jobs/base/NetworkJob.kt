package de.xikolo.jobs.base

import de.xikolo.view_models.base.NetworkCode
import de.xikolo.view_models.base.NetworkStateLiveData
import de.xikolo.managers.UserManager
import de.xikolo.utils.NetworkUtil
import kotlinx.coroutines.experimental.launch

abstract class NetworkJob(private val networkState: NetworkStateLiveData, private val userRequest: Boolean, private vararg val preconditions: Precondition) {

    fun run() {
        if (preconditions.contains(Precondition.AUTH) && !UserManager.isAuthorized) {
            networkState.state(NetworkCode.NO_AUTH, userRequest)
            return
        }

        if (!NetworkUtil.isOnline()) {
            networkState.state(NetworkCode.NO_NETWORK, userRequest)
            return
        }

        launch {
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
