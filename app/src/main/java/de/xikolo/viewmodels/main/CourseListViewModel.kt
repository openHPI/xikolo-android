package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.models.dao.CourseDao
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.network.jobs.ListCoursesJob
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.network.jobs.ListDatesJob
import de.xikolo.utils.MetaSectionList

class CourseListViewModel(private val filter: CourseListFilter) : BaseViewModel() {

    private val coursesDao = CourseDao(realm)
    private val dateDao = DateDao(realm)

    val enrollmentCount
        get() = EnrollmentDao.Unmanaged.count()

    val courses: LiveData<List<Course>> by lazy {
        coursesDao.all()
    }

    val dates: LiveData<List<CourseDate>> by lazy {
        dateDao.all()
    }

    val sectionedCourseList: MetaSectionList<String, DateOverview, List<Course>>
        get() {
            return if (filter == CourseListFilter.ALL) {
                courseListFilterAll
            } else {
                courseListFilterMyWithDateOverview
            }
        }

    private val courseListFilterAll: MetaSectionList<String, DateOverview, List<Course>>
        get() {
            val courseList = MetaSectionList<String, DateOverview, List<Course>>()
            var subList: List<Course>
            if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                subList = CourseDao.Unmanaged.allFuture()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.instance.getString(R.string.header_future_courses),
                        subList
                    )
                }
                subList = CourseDao.Unmanaged.allCurrentAndPast()
                if (subList.isNotEmpty()) {
                    courseList.add(App.instance.getString(R.string.header_self_paced_courses),
                        subList
                    )
                }
            } else {
                subList = CourseDao.Unmanaged.allCurrentAndFuture()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.instance.getString(R.string.header_current_and_upcoming_courses),
                        subList
                    )
                }
                subList = CourseDao.Unmanaged.allPast()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.instance.getString(R.string.header_self_paced_courses),
                        subList
                    )
                }
            }
            return courseList
        }

    private val courseListFilterMyWithDateOverview: MetaSectionList<String, DateOverview, List<Course>>
        get() {
            val courseList = MetaSectionList<String, DateOverview, List<Course>>(
                DateOverview(
                    DateDao.Unmanaged.findNext(),
                    DateDao.Unmanaged.countToday(),
                    DateDao.Unmanaged.countNextSevenDays(),
                    DateDao.Unmanaged.countFuture()
                ),
                App.instance.getString(R.string.course_list_my_dates_title)
            )
            var subList = CourseDao.Unmanaged.allCurrentAndPastWithEnrollment()
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.instance.getString(R.string.header_my_current_courses),
                    subList
                )
            }
            subList = CourseDao.Unmanaged.allFutureWithEnrollment()
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.instance.getString(R.string.header_my_future_courses),
                    subList
                )
            }
            return courseList
        }

    fun searchCourses(query: String): List<Course> =
        CourseDao.Unmanaged.search(query, filter == CourseListFilter.MY)

    override fun onFirstCreate() {
        requestCourseList(false)

        if (filter == CourseListFilter.MY) {
            requestDateList(false)
        }
    }

    override fun onRefresh() {
        requestCourseList(true)

        if (filter == CourseListFilter.MY) {
            requestDateList(true)
        }
    }

    fun requestCourseList(userRequest: Boolean) {
        ListCoursesJob(networkState, userRequest).run()
    }
}
