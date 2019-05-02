package de.xikolo.ui.helper

import de.xikolo.managers.UserManager
import de.xikolo.network.jobs.base.RequestJobCallback

class AuthorizationHelper {

    companion object {

        const val EMAIL = "credentials@beingmocked.com"
        const val PASSWORD = "12345678"

        fun login() {
            if (!UserManager.isAuthorized) {
                UserManager().login(EMAIL, PASSWORD, object : RequestJobCallback() {
                    override fun onSuccess() {}
                    override fun onError(code: ErrorCode) {}
                })

                Thread.sleep(NavigationHelper.WAIT_LOADING_SHORT)
            }
        }

        fun logout() {
            if (UserManager.isAuthorized) {
                UserManager.logout()
            }
        }
    }

}
