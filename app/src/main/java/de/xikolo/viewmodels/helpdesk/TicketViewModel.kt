package de.xikolo.viewmodels.helpdesk

import de.xikolo.models.TicketTopic
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.CreateTicketJob
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseListDelegate

class TicketViewModel : BaseViewModel() {

    private val courseListDelegate = CourseListDelegate(realm)

    val courses = courseListDelegate.courses

    fun send(title: String, report: String, topic: TicketTopic, mail: String? = null, courseId: String? = null) {
        CreateTicketJob(title, report, topic.apiTitle, mail, courseId, networkState, true).run()
    }

    override fun onRefresh() {
        courseListDelegate.requestCourseList(networkState, true)
    }

    override fun onFirstCreate() {
        courseListDelegate.requestCourseList(networkState, false)
    }
}
