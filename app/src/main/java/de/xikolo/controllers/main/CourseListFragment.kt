package de.xikolo.controllers.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.base.BaseCourseListAdapter
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.controllers.helper.LoginHelper
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.extensions.observeOnce
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.DateOverview
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.MetaSectionList
import de.xikolo.utils.extensions.openUrl
import de.xikolo.viewmodels.main.CourseListViewModel
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.CourseFilterView
import de.xikolo.views.SpaceItemDecoration

class CourseListFragment : MainFragment<CourseListViewModel>() {

    companion object {
        val TAG: String = CourseListFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var filter: CourseListFilter

    @BindView(R.id.recyclerView)
    lateinit var recyclerView: AutofitRecyclerView

    @BindView(R.id.filter_container)
    lateinit var filterView: CourseFilterView

    private lateinit var courseListAdapter: CourseListAdapter

    private var courseList: MetaSectionList<String, DateOverview, List<Course>> = MetaSectionList()

    override val layoutResource = R.layout.fragment_course_list

    override fun createViewModel(): CourseListViewModel {
        return CourseListViewModel(filter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
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
            requireActivity().resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
            requireActivity().resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
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
        view?.also {
            viewModel.courses
                .observe(viewLifecycleOwner) {
                    courseList = viewModel.sectionedCourseList
                    showCourseList()
                    activity?.invalidateOptionsMenu()
                }

            viewModel.dates
                .observe(viewLifecycleOwner) {
                    courseList = viewModel.sectionedCourseList
                    showCourseList()
                    activity?.invalidateOptionsMenu()
                }
        }
    }

    private fun unregisterObservers() {
        if (view != null) {
            viewModel.courses.removeObservers(viewLifecycleOwner)
            viewModel.dates.removeObservers(viewLifecycleOwner)
        }
    }

    override fun onStart() {
        super.onStart()

        if (filter === CourseListFilter.ALL) {
            activityCallback?.onFragmentAttached(R.id.navigation_all_courses)
        } else {
            activityCallback?.onFragmentAttached(R.id.navigation_my_courses)
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (networkStateHelper.anyProgressVisible) {
            return
        }

        inflater.inflate(R.menu.search, menu)

        val castItem = menu.findItem(R.id.media_route_menu_item)
        val filterItem = menu.findItem(R.id.search_filter)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                (activity as? BaseActivity)?.setScrollingBehavior(false) // lock action bar in place
                networkStateHelper.enableSwipeRefresh(false)

                castItem?.isVisible = false

                filterView.update()
                filterView.onFilterChangeListener = {
                    onFilter(searchView.query.toString(), it)
                }

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                (activity as? BaseActivity)?.setScrollingBehavior(true) // make action bar auto-hide again
                networkStateHelper.enableSwipeRefresh(true)

                castItem?.isVisible = true

                filterView.visibility = View.GONE
                filterView.clear()
                onFilter("", mapOf()) // reset the filter

                return true
            }
        })
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                onFilter(query, filterView.currentFilter)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                onFilter(newText, filterView.currentFilter)
                return false
            }
        })

        filterItem.setOnMenuItemClickListener {
            if (filterView.visibility == View.GONE) {
                filterView.visibility = View.VISIBLE
                if (!searchItem.isActionViewExpanded) {
                    searchItem.expandActionView()
                    searchItem.actionView?.clearFocus()
                }
            } else {
                filterView.visibility = View.GONE
            }
            true
        }
    }

    fun onFilter(searchQuery: String, filter: Map<String, String>) {
        unregisterObservers()
        if (searchQuery.isNotEmpty() || filterView.hasActiveFilter) {
            val results = viewModel.filterCourses(searchQuery, filter)
            courseList.clear()
            if (results.isEmpty()) {
                courseList.add(getString(R.string.notification_empty_search), results)
            } else {
                courseList.add(getString(R.string.title_search_results), results)
            }
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
            val intent =
                CourseActivityAutoBundle.builder().courseId(courseId).build(requireActivity())
            startActivity(intent)
        }
    }

    private fun enterCourseDetails(courseId: String) {
        val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(requireActivity())
        startActivity(intent)
    }

    private fun enterExternalCourse(course: Course) {
        activity?.openUrl(course.externalUrl)
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
        val intent = LoginHelper.loginIntent(requireActivity())
        startActivity(intent)
    }

    private fun showNoEnrollmentsMessage() {
        showMessage(R.string.notification_no_enrollments, R.string.notification_no_enrollments_summary) {
            activityCallback?.selectDrawerSection(R.id.navigation_all_courses)
        }
    }

}
