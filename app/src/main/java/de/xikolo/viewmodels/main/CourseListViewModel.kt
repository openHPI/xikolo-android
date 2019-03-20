package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.dao.CoursesDao
import de.xikolo.network.jobs.ListCoursesJob
import de.xikolo.utils.SectionList
import de.xikolo.viewmodels.base.BaseViewModel

class CourseListViewModel(private val filter: CourseListFilter) : BaseViewModel() {

    private val coursesDao = CoursesDao(realm)

    val enrollmentCount
        get() = coursesDao.enrollmentCount()

    val enrolledCourses: LiveData<List<Course>> by lazy {
        coursesDao.enrolledCourses()
    }

    val courses: LiveData<List<Course>> by lazy {
        coursesDao.courses()
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
                subList = coursesDao.futureCourses()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.getInstance().getString(R.string.header_future_courses),
                        subList
                    )
                }
                subList = coursesDao.currentAndPastCourses()
                if (subList.isNotEmpty()) {
                    courseList.add(App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                    )
                }
            } else {
                subList = coursesDao.currentAndFutureCourses()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                        subList
                    )
                }
                subList = coursesDao.pastCourses()
                if (subList.isNotEmpty()) {
                    courseList.add(
                        App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                    )
                }
            }
            return courseList
        }

    private val courseListFilterMy: SectionList<String, List<Course>>
        get() {
            val courseList = SectionList<String, List<Course>>()
            var subList = coursesDao.currentAndPastCoursesWithEnrollment()
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_my_current_courses),
                    subList
                )
            }
            subList = coursesDao.futureCoursesWithEnrollment()
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_my_future_courses),
                    subList
                )
            }
            return courseList
        }

    fun searchCourses(query: String): List<Course> =
        coursesDao.searchCourses(query, filter == CourseListFilter.MY)


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

