package de.xikolo.viewmodels.shared

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.GetCourseJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class CourseDelegate(realm: Realm, private val courseId: String) {

    private val courseDao = CourseDao(realm)

    val course: LiveData<Course> by lazy {
        courseDao.find(courseId)
    }

    fun requestCourse(networkState: NetworkStateLiveData, userRequest: Boolean) {
        GetCourseJob(courseId, networkState, userRequest).run()
    }

}
