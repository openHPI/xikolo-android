package de.xikolo.viewmodels.shared

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.ListCoursesJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class CourseListDelegate(realm: Realm) {

    private val courseDao = CourseDao(realm)

    val courses: LiveData<List<Course>> by lazy {
        courseDao.all()
    }

    fun requestCourseList(networkState: NetworkStateLiveData, userRequest: Boolean) {
        ListCoursesJob(networkState, userRequest).run()
    }

}
