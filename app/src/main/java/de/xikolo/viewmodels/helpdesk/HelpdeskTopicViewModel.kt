package de.xikolo.viewmodels.helpdesk

import de.xikolo.models.dao.CourseDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseListDelegate

class HelpdeskTopicViewModel : BaseViewModel() {

    private val courseDao = CourseDao(realm)
    private val courseListDelegate = CourseListDelegate(realm)

    val courses = courseDao.allSortedByTitle()

    override fun onRefresh() {
        courseListDelegate.requestCourseList(networkState, true)
    }

    override fun onFirstCreate() {
        courseListDelegate.requestCourseList(networkState, false)
    }
}
