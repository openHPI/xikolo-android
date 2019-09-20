package de.xikolo.controllers.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.BaseCourseListAdapter
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.extensions.observe
import de.xikolo.extensions.observeOnce
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.DateOverview
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.main.CourseListViewModel
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.SpaceItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CourseListFragment : ViewModelMainFragment<CourseListViewModel>() {

    companion object {
        val TAG: String = CourseListFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var filter: CourseListFilter

    @BindView(R.id.content_view)
    lateinit var recyclerView: AutofitRecyclerView

    private lateinit var courseListAdapter: CourseListAdapter

    private var courseList: MetaSectionList<String, DateOverview, List<Course>> = MetaSectionList()

    override val layoutResource = R.layout.content_course_list

    override fun createViewModel(): CourseListViewModel {
        return CourseListViewModel(filter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseListAdapter = CourseListAdapter(
            this,
            filter,
            object : BaseCourseListAdapter.OnCourseButtonClickListener {
                override fun onEnrollButtonClicked(courseId: String) {
                    enroll(courseId)
                }

                override fun onContinueButtonClicked(courseId: String) {
                    enterCourse(courseId)
                }

                override fun onDetailButtonClicked(courseId: String) {
                    enterCourseDetails(courseId)
                }

                override fun onExternalButtonClicked(course: Course) {
                    enterExternalCourse(course)
                }
            },
            object : CourseListAdapter.OnDateOverviewClickListener {
                override fun onDateOverviewClicked() {
                    activityCallback?.selectDrawerSection(R.id.navigation_dates)
                }
            }
        )

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

                override val spanCount: Int
                    get() = recyclerView.spanCount

                override val itemCount: Int
                    get() = courseListAdapter.itemCount
            }
        ))

        registerObservers()
    }

    private fun registerObservers() {
        viewModel.courses
            .observe(viewLifecycleOwner) {
                courseList = viewModel.sectionedCourseList
                showCourseList()
            }

        viewModel.dates
            .observe(viewLifecycleOwner) {
                courseList = viewModel.sectionedCourseList
                showCourseList()
            }
    }

    private fun unregisterObservers() {
        viewModel.courses.removeObservers(this)
        viewModel.dates.removeObservers(this)
    }

    override fun onStart() {
        super.onStart()

        if (filter === CourseListFilter.ALL) {
            activityCallback?.onFragmentAttached(R.id.navigation_all_courses)
        } else {
            activityCallback?.onFragmentAttached(R.id.navigation_my_courses)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun showCourseList() {
        if (filter == CourseListFilter.MY) {
            if (!UserManager.isAuthorized) {
                hideContent()
                showLoginRequired()
                return
            } else if (!viewModel.hasEnrollments) {
                hideContent()
                showNoEnrollmentsMessage()
                return
            }
        }
        updateCourseList()
        showContent()
    }

    private fun updateCourseList() {
        courseListAdapter.update(courseList)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.search, menu)

        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                onSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                onSearch(newText)
                return false
            }
        })
    }

    fun onSearch(query: String?) {
        unregisterObservers()
        if (query != null && query.isNotEmpty()) {
            val results = viewModel.searchCourses(query)
            courseList.clear()
            courseList.add(null, results)
            updateCourseList()
        } else {
            registerObservers()
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

    private fun enterExternalCourse(course: Course) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.externalUrl))
        startActivity(intent)
    }

    fun enroll(courseId: String) {
        showBlockingProgress()

        val enrollmentCreationNetworkState = NetworkStateLiveData()
        enrollmentCreationNetworkState
            .observeOnce(viewLifecycleOwner) {
                when (it.code) {
                    NetworkCode.SUCCESS    -> {
                        hideAnyProgress()
                        val course = CourseDao.Unmanaged.find(courseId)
                        if (course?.accessible == true) {
                            enterCourse(courseId)
                        }
                        true
                    }
                    NetworkCode.NO_NETWORK -> {
                        hideAnyProgress()
                        showNetworkRequired()
                        true
                    }
                    NetworkCode.NO_AUTH    -> {
                        hideAnyProgress()
                        showLoginRequired()
                        openLogin()
                        true
                    }
                    else                   -> false
                }
            }

        viewModel.enroll(courseId, enrollmentCreationNetworkState)
    }

    private fun openLogin() {
        val intent = LoginActivityAutoBundle.builder().build(activity!!)
        startActivity(intent)
    }

    private fun showNoEnrollmentsMessage() {
        showMessage(R.string.notification_no_enrollments, R.string.notification_no_enrollments_summary) {
            activityCallback?.selectDrawerSection(R.id.navigation_all_courses)
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        onRefresh()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        onRefresh()
    }

}
