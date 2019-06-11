package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.GetCourseJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.main.DateListViewModel

class CourseViewModel(val courseId: String) : BaseViewModel() {

    private val courseDao = CourseDao(realm)
    private val dateListViewModel = DateListViewModel(courseId)

    val course: LiveData<Course> by lazy {
        courseDao.find(courseId)
    }

    val dates: LiveData<List<CourseDate>> = dateListViewModel.dates

    val dateCount: Int
        get() = dates.value?.size ?: 0

    override fun onFirstCreate() {
        requestCourse(false)
        dateListViewModel.onFirstCreate()
    }

    override fun onRefresh() {
        requestCourse(true)
        dateListViewModel.onRefresh()
    }

    private fun requestCourse(userRequest: Boolean) {
        GetCourseJob(courseId, networkState, userRequest).run()
    }

    override val networkState: NetworkStateLiveData
        get() = dateListViewModel.networkState
}
