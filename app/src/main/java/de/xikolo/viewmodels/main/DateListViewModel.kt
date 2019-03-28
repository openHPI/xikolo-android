package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.models.DateOverview
import de.xikolo.models.dao.DateDao
import de.xikolo.network.jobs.ListDatesJob
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.base.BaseViewModel

open class DateListViewModel : BaseViewModel() {

    private val courseListViewModel = CourseListViewModel(CourseListFilter.MY)
    private val dateDao = DateDao(realm)

    val dates: LiveData<List<CourseDate>> by lazy {
        dateDao.all()
    }

    val courses: LiveData<List<Course>> = courseListViewModel.courses

    val sectionedDateList: MetaSectionList<String, DateOverview, List<CourseDate>>
        get() {
            val dateList = MetaSectionList<String, DateOverview, List<CourseDate>>(
                DateOverview(
                    null,
                    DateDao.Unmanaged.countToday(),
                    DateDao.Unmanaged.countNextSevenDays(),
                    DateDao.Unmanaged.countFuture()
                )
            )
            var subList: List<CourseDate> = DateDao.Unmanaged.allToday()
            if (subList.isNotEmpty()) {
                dateList.add(
                    App.getInstance().getString(R.string.course_dates_today),
                    subList
                )
            }
            subList = DateDao.Unmanaged.allNextSevenDaysWithoutToday()
            if (subList.isNotEmpty()) {
                dateList.add(
                    App.getInstance().getString(R.string.course_dates_week),
                    subList
                )
            }
            subList = DateDao.Unmanaged.allFutureWithoutNextSevenDays()
            if (subList.isNotEmpty()) {
                dateList.add(
                    if (dateList.size > 0) {
                        App.getInstance().getString(R.string.course_dates_later)
                    } else {
                        App.getInstance().getString(R.string.course_dates_all)
                    },
                    subList
                )
            }

            return dateList
        }

    override fun onFirstCreate() {
        courseListViewModel.requestCourseList(false)
        requestDateList(false)
    }

    override fun onRefresh() {
        courseListViewModel.requestCourseList(true)
        requestDateList(true)
    }

    fun requestDateList(userRequest: Boolean) {
        ListDatesJob(networkState, userRequest).run()
    }
}
