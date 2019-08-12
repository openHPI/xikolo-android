package de.xikolo.viewmodels.shared

import androidx.lifecycle.LiveData
import de.xikolo.models.Enrollment
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.network.jobs.CreateEnrollmentJob
import de.xikolo.network.jobs.DeleteEnrollmentJob
import de.xikolo.network.jobs.ListEnrollmentsJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class EnrollmentDelegate(realm: Realm) {

    private val enrollmentDao = EnrollmentDao(realm)

    val enrollments: LiveData<List<Enrollment>> by lazy {
        enrollmentDao.all()
    }

    val enrollmentCount
        get() = EnrollmentDao.Unmanaged.count()

    fun createEnrollment(courseId: String, networkState: NetworkStateLiveData, userRequest: Boolean) {
        CreateEnrollmentJob(courseId, networkState, userRequest).run()
    }

    fun deleteEnrollment(enrollmentId: String, networkState: NetworkStateLiveData, userRequest: Boolean) {
        DeleteEnrollmentJob(enrollmentId, networkState, userRequest).run()
    }

    fun requestEnrollmentList(networkState: NetworkStateLiveData, userRequest: Boolean) {
        ListEnrollmentsJob(networkState, userRequest).run()
    }

}
