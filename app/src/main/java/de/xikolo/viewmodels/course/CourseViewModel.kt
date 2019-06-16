package de.xikolo.viewmodels.course

import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseViewModelDelegate
import de.xikolo.viewmodels.shared.DateListViewModelDelegate

class CourseViewModel(courseId: String) : BaseViewModel() {

    private val courseDelegate = CourseViewModelDelegate(realm, courseId)
    private val dateListDelegate = DateListViewModelDelegate(realm)

    val course = courseDelegate.course

    val dates = dateListDelegate.dates

    val dateCount: Int
        get() = dates.value?.size ?: 0

    override fun onFirstCreate() {
        courseDelegate.requestCourse(networkState, false)
        dateListDelegate.requestDateList(networkState, false)
    }

    override fun onRefresh() {
        courseDelegate.requestCourse(networkState, true)
        dateListDelegate.requestDateList(networkState, true)
    }
}
