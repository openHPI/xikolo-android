package de.xikolo.viewmodels.course

import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseDelegate
import de.xikolo.viewmodels.shared.DateListDelegate
import de.xikolo.viewmodels.shared.EnrollmentDelegate

class CourseViewModel(private val courseId: String) : BaseViewModel() {

    private val courseDelegate = CourseDelegate(realm, courseId)
    private val dateListDelegate = DateListDelegate(realm)
    private val enrollmentDelegate = EnrollmentDelegate(realm)

    val course = courseDelegate.course

    val dates = dateListDelegate.dates

    val dateCount: Int
        get() = dates.value?.size ?: 0

    fun enroll(networkState: NetworkStateLiveData) {
        enrollmentDelegate.createEnrollment(courseId, networkState, true)
    }

    fun unenroll(enrollmentId: String, networkState: NetworkStateLiveData) {
        enrollmentDelegate.deleteEnrollment(enrollmentId, networkState, true)
    }

    override fun onFirstCreate() {
        courseDelegate.requestCourse(networkState, false)
        dateListDelegate.requestDateList(networkState, false)
    }

    override fun onRefresh() {
        courseDelegate.requestCourse(networkState, true)
        dateListDelegate.requestDateList(networkState, true)
    }
}
