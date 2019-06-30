package de.xikolo.testing.instrumented.ui.helper

import de.xikolo.managers.UserManager
import de.xikolo.network.jobs.CreateAccessTokenJob
import de.xikolo.network.jobs.base.NetworkStateLiveData

class AuthorizationHelper {

    companion object {

        const val EMAIL = "credentials@beingmocked.com"
        const val PASSWORD = "12345678"

        fun login() {
            if (!UserManager.isAuthorized) {
                CreateAccessTokenJob(EMAIL, PASSWORD, NetworkStateLiveData(), true).run()

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
