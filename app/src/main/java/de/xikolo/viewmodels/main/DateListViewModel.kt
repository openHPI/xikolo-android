package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.R
import de.xikolo.models.CourseDate
import de.xikolo.models.DateOverview
import de.xikolo.models.dao.DateDao
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseListDelegate
import de.xikolo.viewmodels.shared.DateListDelegate

open class DateListViewModel(private val courseId: String? = null) : BaseViewModel() {

    private val courseListDelegate = CourseListDelegate(realm)
    private val dateListDelegate = DateListDelegate(realm)

    val dates: LiveData<List<CourseDate>> by lazy {
        if (courseId != null) {
            dateListDelegate.datesForCourse(courseId)
        } else {
            dateListDelegate.dates
        }
    }

    val sectionedDateList: MetaSectionList<String, DateOverview, List<CourseDate>>
        get() {
            val dateOverview =
                if (courseId != null) {
                    DateOverview(
                        null,
                        DateDao.Unmanaged.countTodayForCourse(courseId),
                        DateDao.Unmanaged.countNextSevenDaysForCourse(courseId),
                        DateDao.Unmanaged.countFutureForCourse(courseId)
                    )
                } else {
                    DateOverview(
                        null,
                        DateDao.Unmanaged.countToday(),
                        DateDao.Unmanaged.countNextSevenDays(),
                        DateDao.Unmanaged.countFuture()
                    )
                }

            val dateList = MetaSectionList<String, DateOverview, List<CourseDate>>(dateOverview)

            var subList =
                if (courseId != null) {
                    DateDao.Unmanaged.allTodayForCourse(courseId)
                } else {
                    DateDao.Unmanaged.allToday()
                }
            if (subList.isNotEmpty()) {
                dateList.add(
                    App.instance.getString(R.string.course_dates_today),
                    subList
                )
            }

            subList =
                if (courseId != null) {
                    DateDao.Unmanaged.allNextSevenDaysWithoutTodayForCourse(courseId)
                } else {
                    DateDao.Unmanaged.allNextSevenDaysWithoutToday()
                }
            if (subList.isNotEmpty()) {
                dateList.add(
                    App.instance.getString(R.string.course_dates_week),
                    subList
                )
            }

            subList =
                if (courseId != null) {
                    DateDao.Unmanaged.allFutureWithoutNextSevenDaysForCourse(courseId)
                } else {
                    DateDao.Unmanaged.allFutureWithoutNextSevenDays()
                }
            if (subList.isNotEmpty()) {
                dateList.add(
                    if (dateList.size > 0) {
                        App.instance.getString(R.string.course_dates_later)
                    } else {
                        App.instance.getString(R.string.course_dates_all)
                    },
                    subList
                )
            }

            return dateList
        }

    override fun onFirstCreate() {
        courseListDelegate.requestCourseList(networkState, false)
        dateListDelegate.requestDateList(networkState, false)
    }

    override fun onRefresh() {
        courseListDelegate.requestCourseList(networkState, true)
        dateListDelegate.requestDateList(networkState, true)
    }
}
