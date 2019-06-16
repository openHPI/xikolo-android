package de.xikolo.viewmodels.main

import de.xikolo.models.dao.CourseDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseListViewModelDelegate

class CertificateListViewModel : BaseViewModel() {

    private val courseListDelegate = CourseListViewModelDelegate(realm)

    val courses = courseListDelegate.courses

    val coursesWithCertificates
        get() = CourseDao.Unmanaged.allWithCertificates()

    override fun onFirstCreate() {
        courseListDelegate.requestCourseList(networkState, false)
    }

    override fun onRefresh() {
        courseListDelegate.requestCourseList(networkState, true)
    }

}
