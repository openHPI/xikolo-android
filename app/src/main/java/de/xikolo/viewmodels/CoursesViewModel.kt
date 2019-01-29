package de.xikolo.viewmodels

import android.arch.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.dao.CoursesDao
import de.xikolo.network.jobs.GetCourseJob
import de.xikolo.network.jobs.ListCoursesJob
import de.xikolo.viewmodels.base.BaseViewModel

open class CoursesViewModel(val courseId: String?) : BaseViewModel() {

    private val coursesDao = CoursesDao(realm)

    val course: LiveData<Course> by lazy {
        if (courseId != null) {
            coursesDao.course(courseId)
        } else {
            throw IllegalArgumentException("courseId must not be null to access this field")
        }
    }

    val enrolledCourses: LiveData<List<Course>> by lazy {
        coursesDao.enrolledCourses()
    }

    val coursesWithCertificates
        get() = coursesDao.coursesWithCertificates()

    val enrollmentCount
        get() = coursesDao.enrollmentCount()

    // ToDo is this okay without lazy as they are not LiveData
    val futureCourses
        get() = coursesDao.futureCourses()

    val currentAndPastCourses
        get() = coursesDao.currentAndPastCourses()

    val currentAndFutureCourses
        get() = coursesDao.currentAndFutureCourses()

    val pastCourses
        get() = coursesDao.pastCourses()

    val currentAndPastCoursesWithEnrollment
        get() = coursesDao.currentAndPastCoursesWithEnrollment()

    val futureCoursesWithEnrollment
        get() = coursesDao.futureCoursesWithEnrollment()

    val courseList: LiveData<List<Course>> by lazy {
        coursesDao.courses()
    }

    fun searchCourses(query: String, withEnrollment: Boolean): LiveData<List<Course>> =
        coursesDao.searchCourses(query, withEnrollment)


    override fun onFirstCreate() {
        requestCourse(false)
        requestCourseList(false)
    }

    override fun onRefresh() {
        requestCourse(true)
        requestCourseList(true)
    }

    private fun requestCourse(userRequest: Boolean) {
        if (courseId != null) {
            GetCourseJob(courseId, networkState, userRequest).run()
        }
    }

    private fun requestCourseList(userRequest: Boolean) {
        ListCoursesJob(networkState, userRequest).run()
    }
}

