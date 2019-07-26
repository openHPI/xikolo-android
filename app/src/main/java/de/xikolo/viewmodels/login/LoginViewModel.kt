package de.xikolo.viewmodels.login

import de.xikolo.network.jobs.CreateAccessTokenJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.UserDelegate

class LoginViewModel : BaseViewModel() {

    private val userDelegate = UserDelegate(realm)

    val loginNetworkState: NetworkStateLiveData = networkState
    val profileNetworkState: NetworkStateLiveData by lazy {
        NetworkStateLiveData()
    }

    override fun onFirstCreate() {
    }

    override fun onRefresh() {
    }

    fun login(email: String, password: String) {
        CreateAccessTokenJob(email, password, loginNetworkState, true).run()
    }

    fun requestUserWithProfile() {
        userDelegate.requestUserWithProfile(profileNetworkState, false)
    }

}
