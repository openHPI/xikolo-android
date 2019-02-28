package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.SectionProgress
import de.xikolo.models.dao.course.ProgressDao
import de.xikolo.network.jobs.GetCourseProgressWithSectionsJob
import de.xikolo.viewmodels.base.BaseViewModel

open class ProgressViewModel(val courseId: String) : BaseViewModel() {

    private val progressDao = ProgressDao(realm)

    val sectionProgresses: LiveData<List<SectionProgress>> by lazy {
        progressDao.sectionProgressesForCourse(courseId)
    }

    override fun onFirstCreate() {
        requestCourseProgressWithSections(false)
    }

    override fun onRefresh() {
        requestCourseProgressWithSections(true)
    }

    private fun requestCourseProgressWithSections(userRequest: Boolean) {
        GetCourseProgressWithSectionsJob(courseId, networkState, userRequest).run()
    }

}
