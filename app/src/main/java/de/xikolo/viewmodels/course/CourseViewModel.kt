package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.dao.CoursesDao
import de.xikolo.network.jobs.GetCourseJob
import de.xikolo.viewmodels.base.BaseViewModel

class CourseViewModel(val courseId: String) : BaseViewModel() {

    private val coursesDao = CoursesDao(realm)

    val course: LiveData<Course> by lazy {
        coursesDao.course(courseId)
    }

    override fun onFirstCreate() {
        requestCourse(false)
    }

    override fun onRefresh() {
        requestCourse(true)
    }

    fun requestCourse(userRequest: Boolean) {
        GetCourseJob(courseId, networkState, userRequest).run()
    }
}
