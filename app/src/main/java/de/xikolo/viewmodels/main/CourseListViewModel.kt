package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.dao.CourseDao
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.network.jobs.ListCoursesJob
import de.xikolo.utils.SectionList
import de.xikolo.viewmodels.base.BaseViewModel

class CourseListViewModel(private val filter: CourseListFilter) : BaseViewModel() {

    private val coursesDao = CourseDao(realm)

    val enrollmentCount
        get() = EnrollmentDao.Unmanaged.count()

    val enrolledCourses: LiveData<List<Course>> by lazy {
        coursesDao.allEnrolled()
    }

    val courses: LiveData<List<Course>> by lazy {
        coursesDao.all()
    }

    val sectionedCourseList: SectionList<String, List<Course>>
        get() {
            return if (filter == CourseListFilter.ALL) {
                courseListFilterAll
            } else {
                courseListFilterMy
            }
        }

    private val courseListFilterAll: SectionList<String, List<Course>>
        get() {
            val courseList = SectionList<String, List<Course>>()
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

    private val courseListFilterMy: SectionList<String, List<Course>>
        get() {
            val courseList = SectionList<String, List<Course>>()
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
    }

    override fun onRefresh() {
        requestCourseList(true)
    }

    fun requestCourseList(userRequest: Boolean) {
        ListCoursesJob(networkState, userRequest).run()
    }
}
