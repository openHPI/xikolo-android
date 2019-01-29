package de.xikolo.controllers.main

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.base.BaseCourseListAdapter
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.managers.CourseManager
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.utils.SectionList
import de.xikolo.viewmodels.CoursesViewModel
import de.xikolo.viewmodels.base.observe
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.SpaceItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CourseListFragment : ViewModelMainFragment<CoursesViewModel>() {

    companion object {
        val TAG: String = CourseListFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var filter: CourseListFilter

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: AutofitRecyclerView

    private lateinit var courseListAdapter: CourseListAdapter

    private val courseManager = CourseManager()

    private val courseList: SectionList<String, List<Course>> = SectionList()

    override val layoutResource = R.layout.content_course_list

    override fun createViewModel(): CoursesViewModel {
        return CoursesViewModel(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseListAdapter = CourseListAdapter(this, object : BaseCourseListAdapter.OnCourseButtonClickListener {
            override fun onEnrollButtonClicked(courseId: String) {
                enroll(courseId)
            }

            override fun onContinueButtonClicked(courseId: String) {
                enterCourse(courseId)
            }

            override fun onDetailButtonClicked(courseId: String) {
                enterCourseDetails(courseId)
            }
        }, filter)

        recyclerView.adapter = courseListAdapter

        recyclerView.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (courseListAdapter.isHeader(position)) recyclerView.spanCount else 1
            }
        }

        recyclerView.addItemDecoration(SpaceItemDecoration(
            activity!!.resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
            activity!!.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return courseListAdapter.isHeader(position)
                }

                override fun getSpanCount(): Int {
                    return recyclerView.spanCount
                }

                override fun getItemCount(): Int {
                    return courseListAdapter.itemCount
                }
            }))

        registerObservers()
    }

    private fun registerObservers() {
        if (filter == CourseListFilter.ALL) {
            viewModel.courseList
                .observe(this) {
                    // as new course list is loaded into the database it is accessed via the non-async queries
                    buildAndShowCourseList()
                }
        }
        viewModel.enrolledCourses
            .observe(this) {
                buildAndShowCourseList()
            }
    }

    private fun unregisterObservers() {
        viewModel.courseList.removeObservers(this)
        viewModel.enrolledCourses.removeObservers(this)
    }

    override fun onStart() {
        super.onStart()

        if (filter === CourseListFilter.ALL) {
            activityCallback?.onFragmentAttached(NavigationAdapter.NAV_ALL_COURSES.position, getString(R.string.title_section_all_courses))
        } else {
            activityCallback?.onFragmentAttached(NavigationAdapter.NAV_MY_COURSES.position, getString(R.string.title_section_my_courses))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun buildAndShowCourseList() {
        if (filter == CourseListFilter.ALL) {
            buildCourseListFilterAll()
        } else {
            if (!UserManager.isAuthorized) {
                hideContent()
                showLoginRequired()
                return
            } else if (viewModel.enrollmentCount == 0L) {
                hideContent()
                showNoEnrollmentsMessage()
                return
            } else {
                buildCourseListFilterMy()
            }
        }
        showCourseList()
        showContent()
    }

    private fun showCourseList() {
        courseListAdapter.update(courseList)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (activityCallback?.isDrawerOpen == false) {
            inflater?.inflate(R.menu.refresh, menu)
            inflater?.inflate(R.menu.search, menu)

            val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
            searchView.setIconifiedByDefault(false)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    onSearch(query, filter === CourseListFilter.MY)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    onSearch(newText, filter === CourseListFilter.MY)
                    return false
                }
            })
        }
    }

    fun onSearch(query: String?, withEnrollment: Boolean) {
        unregisterObservers()
        var searchLiveDataObject: LiveData<List<Course>>? = null
        if (query != null && query.isNotEmpty()) {
            searchLiveDataObject = viewModel.searchCourses(query, withEnrollment)
            searchLiveDataObject.observe(this) { results ->
                searchLiveDataObject.removeObservers(this)
                courseList.clear()
                courseList.add(null, results)
                showCourseList()
            }
        } else {
            searchLiveDataObject?.removeObservers(this)
            registerObservers()
        }
    }

    private fun buildCourseListFilterMy() {
        courseList.clear()
        var subList = viewModel.currentAndPastCoursesWithEnrollment
        if (subList.isNotEmpty()) {
            courseList.add(
                App.getInstance().getString(R.string.header_my_current_courses),
                subList
            )
        }
        subList = viewModel.futureCoursesWithEnrollment
        if (subList.isNotEmpty()) {
            courseList.add(
                App.getInstance().getString(R.string.header_my_future_courses),
                subList
            )
        }
    }

    private fun buildCourseListFilterAll() {
        courseList.clear()
        var subList: List<Course>
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = viewModel.futureCourses
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_future_courses),
                    subList
                )
            }
            subList = viewModel.currentAndPastCourses
            if (subList.isNotEmpty()) {
                courseList.add(App.getInstance().getString(R.string.header_self_paced_courses),
                    subList
                )
            }
        } else {
            subList = viewModel.currentAndFutureCourses
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                    subList
                )
            }
            subList = viewModel.pastCourses
            if (subList.isNotEmpty()) {
                courseList.add(
                    App.getInstance().getString(R.string.header_self_paced_courses),
                    subList
                )
            }
        }
    }

    private fun enterCourse(courseId: String) {
        if (!UserManager.isAuthorized) {
            showLoginRequired()
            openLogin()
        } else {
            val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(activity!!)
            startActivity(intent)
        }
    }

    private fun enterCourseDetails(courseId: String) {
        val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(activity!!)
        startActivity(intent)
    }

    fun enroll(courseId: String) { //ToDo refactor out, has duplicate code in BaseCourseListPresenter used by Channels
        showBlockingProgress()
        courseManager.createEnrollment(courseId, object : RequestJobCallback() {
            public override fun onSuccess() {
                if (view != null) {
                    hideAnyProgress()
                    val course = Course.get(courseId)
                    if (course.accessible) {
                        enterCourse(courseId)
                    }
                }
            }

            public override fun onError(code: RequestJobCallback.ErrorCode) {
                if (view != null) {
                    hideAnyProgress()
                    if (code === ErrorCode.NO_NETWORK) {
                        showNetworkRequired()
                    } else if (code === RequestJobCallback.ErrorCode.NO_AUTH) {
                        showLoginRequired()
                        openLogin()
                    }
                }
            }
        })
    }

    private fun openLogin() {
        val intent = LoginActivityAutoBundle.builder().build(activity!!)
        startActivity(intent)
    }

    private fun showLoginRequired() {
        super.showLoginRequired {
            activityCallback?.selectDrawerSection(NavigationAdapter.NAV_PROFILE.position)
        }
    }

    private fun showNoEnrollmentsMessage() {
        super.showMessage(R.string.notification_no_enrollments, R.string.notification_no_enrollments_summary) {
            activityCallback?.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        onRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        onRefresh()
    }

    // ToDo kann das raus?

}
