package de.xikolo.network.jobs.base

import de.xikolo.events.NetworkStateEvent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

abstract class RequestJobCallback {

    enum class ErrorCode {
        ERROR, CANCEL, NO_NETWORK, NO_AUTH, MAINTENANCE, API_VERSION_EXPIRED
    }

    protected abstract fun onSuccess()

    protected abstract fun onError(code: ErrorCode)

    protected open fun onDeprecated(deprecationDate: Date) {}

    fun success() {
        EventBus.getDefault().postSticky(NetworkStateEvent(true))

        launch(UI) { onSuccess() }
    }

    fun error(errorCode: ErrorCode) {
        if (errorCode == ErrorCode.NO_NETWORK) {
            EventBus.getDefault().postSticky(NetworkStateEvent(false))
        }
        launch(UI) { onError(errorCode) }
    }

    fun deprecated(deprecationDate: Date) {
        launch(UI) { onDeprecated(deprecationDate) }
    }

}
