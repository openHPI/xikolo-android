package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.CourseDate
import de.xikolo.models.DateOverview
import de.xikolo.models.dao.DateDao
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.base.BaseViewModel

open class DateListViewModel(val courseId: String? = null) : BaseViewModel() {

    private val courseListViewModel = CourseListViewModel(CourseListFilter.MY)
    private val dateDao = DateDao(realm)

    val dates: LiveData<List<CourseDate>> by lazy {
        if (courseId != null) {
            dateDao.allForCourse(courseId)
        } else {
            dateDao.all()
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
                    de.xikolo.App.instance.getString(de.xikolo.R.string.course_dates_today),
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
                    de.xikolo.App.instance.getString(de.xikolo.R.string.course_dates_week),
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
                        de.xikolo.App.instance.getString(de.xikolo.R.string.course_dates_later)
                    } else {
                        de.xikolo.App.instance.getString(de.xikolo.R.string.course_dates_all)
                    },
                    subList
                )
            }

            return dateList
        }

    override fun onFirstCreate() {
        // includes requesting of date list
        courseListViewModel.onFirstCreate()
    }

    override fun onRefresh() {
        // includes requesting of date list
        courseListViewModel.onRefresh()
    }

}
