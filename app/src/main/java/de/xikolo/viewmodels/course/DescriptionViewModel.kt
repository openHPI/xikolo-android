package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.viewmodels.base.BaseViewModel

class DescriptionViewModel(val courseId: String) : BaseViewModel() {

    private val courseViewModel = CourseViewModel(courseId)

    val course: LiveData<Course> = courseViewModel.course

    override fun onFirstCreate() {
        courseViewModel.requestCourse(false, networkState)
    }

    override fun onRefresh() {
        courseViewModel.requestCourse(true, networkState)
    }
}
