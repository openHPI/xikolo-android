package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.models.User
import de.xikolo.models.dao.UserDao
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.EnrollmentDelegate
import de.xikolo.viewmodels.shared.UserDelegate

class ProfileViewModel : BaseViewModel() {

    private val userDelegate = UserDelegate(realm)
    private val enrollmentDelegate = EnrollmentDelegate(realm)

    private val userDao = UserDao(realm)

    val user: LiveData<User> by lazy {
        userDao.current()
    }

    val enrollments = enrollmentDelegate.enrollments

    val enrollmentCount
        get() = enrollmentDelegate.enrollmentCount

    override fun onFirstCreate() {
        userDelegate.requestUserWithProfile(networkState, false)
        enrollmentDelegate.requestEnrollmentList(NetworkStateLiveData(), false)
    }

    override fun onRefresh() {
        userDelegate.requestUserWithProfile(networkState, true)
        enrollmentDelegate.requestEnrollmentList(NetworkStateLiveData(), true)
    }

}
