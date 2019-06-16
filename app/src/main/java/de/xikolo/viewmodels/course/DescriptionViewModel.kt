package de.xikolo.viewmodels.course

import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseViewModelDelegate

class DescriptionViewModel(courseId: String) : BaseViewModel() {

    private val courseDelegate = CourseViewModelDelegate(realm, courseId)

    val course = courseDelegate.course

    override fun onFirstCreate() {
        courseDelegate.requestCourse(networkState, false)
    }

    override fun onRefresh() {
        courseDelegate.requestCourse(networkState, true)
    }

}
