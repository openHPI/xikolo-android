package de.xikolo.viewmodels.shared

import androidx.lifecycle.LiveData
import de.xikolo.managers.CourseManager
import de.xikolo.models.Enrollment
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.network.jobs.CreateEnrollmentJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.jobs.base.RequestJobCallback
import io.realm.Realm

class EnrollmentDelegate(realm: Realm) {

    private val enrollmentDao = EnrollmentDao(realm)

    private val courseManager = CourseManager()

    val enrollments: LiveData<List<Enrollment>> by lazy {
        enrollmentDao.all()
    }

    val enrollmentCount
        get() = EnrollmentDao.Unmanaged.count()

    fun createEnrollment(courseId: String, networkState: NetworkStateLiveData, userRequest: Boolean) {
        CreateEnrollmentJob(courseId, networkState, userRequest).run()
    }

    fun requestEnrollmentList(networkState: NetworkStateLiveData, userRequest: Boolean) {
        courseManager.requestEnrollmentList(object : RequestJobCallback() {
            override fun onSuccess() {}

            override fun onError(code: ErrorCode) {}
        })
    }

}
