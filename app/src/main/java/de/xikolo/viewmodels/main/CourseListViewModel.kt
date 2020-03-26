package de.xikolo.viewmodels.main

import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.DateOverview
import de.xikolo.models.dao.CourseDao
import de.xikolo.models.dao.DateDao
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.ChannelListDelegate
import de.xikolo.viewmodels.shared.CourseListDelegate
import de.xikolo.viewmodels.shared.DateListDelegate
import de.xikolo.viewmodels.shared.EnrollmentDelegate

class CourseListViewModel(private val filter: CourseListFilter) : BaseViewModel() {

    private val courseListDelegate = CourseListDelegate(realm)
    private val dateListDelegate = DateListDelegate(realm)
    private val enrollmentDelegate = EnrollmentDelegate(realm)
    private val channelListDelegate = ChannelListDelegate(realm)

    val hasEnrollments
        get() = enrollmentDelegate.enrollmentCount > 0

    val courses = courseListDelegate.courses

    val dates = dateListDelegate.dates

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

    fun filterCourses(query: String, filterMap: Map<String, String>): List<Course> =
        CourseDao.Unmanaged.filter(query, filterMap, filter == CourseListFilter.MY)

    fun enroll(courseId: String, networkState: NetworkStateLiveData) {
        enrollmentDelegate.createEnrollment(courseId, networkState, true)
    }

    override fun onFirstCreate() {
        channelListDelegate.requestChannels(NetworkStateLiveData(), false) // request channels to be available for course filtering by channel
        courseListDelegate.requestCourseList(networkState, false)

        if (filter == CourseListFilter.MY) {
            dateListDelegate.requestDateList(networkState, false)
        }
    }

    override fun onRefresh() {
        courseListDelegate.requestCourseList(networkState, true)

        if (filter == CourseListFilter.MY) {
            dateListDelegate.requestDateList(networkState, true)
        }
    }
}
