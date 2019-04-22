package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.base.BaseViewModel

class DescriptionViewModel(val courseId: String) : BaseViewModel() {

    private val courseViewModel = CourseViewModel(courseId)

    val course: LiveData<Course> = courseViewModel.course

    override fun onFirstCreate() {
        courseViewModel.requestCourse(false)
    }

    override fun onRefresh() {
        courseViewModel.requestCourse(true)
    }

    override val networkState: NetworkStateLiveData
        get() = courseViewModel.networkState

}
