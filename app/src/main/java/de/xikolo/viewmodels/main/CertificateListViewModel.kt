package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.dao.CourseDao
import de.xikolo.viewmodels.base.BaseViewModel

class CertificateListViewModel : BaseViewModel() {

    private val courseListViewModel = CourseListViewModel(CourseListFilter.ALL)

    val courses: LiveData<List<Course>> = courseListViewModel.courses

    val coursesWithCertificates
        get() = CourseDao.Unmanaged.allWithCertificates()

    override fun onFirstCreate() {
        courseListViewModel.requestCourseList(false, networkState)
    }

    override fun onRefresh() {
        courseListViewModel.requestCourseList(true, networkState)
    }
}
