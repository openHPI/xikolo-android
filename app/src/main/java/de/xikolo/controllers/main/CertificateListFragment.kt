package de.xikolo.controllers.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.viewmodels.CourseListViewModel
import de.xikolo.viewmodels.base.observe
import de.xikolo.views.SpaceItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CertificateListFragment : ViewModelMainFragment<CourseListViewModel>() {

    companion object {
        val TAG: String = CertificateListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    private lateinit var certificateListAdapter: CertificateListAdapter

    override val layoutResource = R.layout.content_certificate_list

    override fun createViewModel(): CourseListViewModel {
        return CourseListViewModel(CourseListFilter.ALL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        certificateListAdapter = CertificateListAdapter(this, object : CertificateListAdapter.OnCertificateCardClickListener {
            override fun onCourseClicked(courseId: String) {
                val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(App.getInstance())
                startActivity(intent)
            }
        })

        activity?.let { activity ->
            recyclerView.layoutManager = LinearLayoutManager(App.getInstance())
            recyclerView.addItemDecoration(SpaceItemDecoration(
                activity.resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
                activity.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
                false,
                object : SpaceItemDecoration.RecyclerViewInfo {
                    override fun isHeader(position: Int): Boolean {
                        return false
                    }

                    override fun getSpanCount(): Int {
                        return 1
                    }

                    override fun getItemCount(): Int {
                        return certificateListAdapter.itemCount
                    }
                }))
        }
        recyclerView.adapter = certificateListAdapter

        viewModel.courses
            .observe(this) {
                showCertificateList(
                    viewModel.coursesWithCertificates
                )
            }
    }

    private fun showCertificateList(courses: List<Course>) {
        if (!UserManager.isAuthorized) {
            hideContent()
            showLoginRequired()
        } else if (courses.isEmpty()) {
            hideContent()
            showNoCertificatesMessage()
        } else {
            certificateListAdapter.update(courses)
            showContent()
        }
    }

    private fun showLoginRequired() {
        super.showLoginRequired {
            activityCallback?.selectDrawerSection(NavigationAdapter.NAV_PROFILE.position)
        }
    }

    private fun showNoCertificatesMessage() {
        showMessage(R.string.notification_no_certificates, R.string.notification_no_certificates_summary) {
            activityCallback?.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.position)
        }
    }

    override fun onStart() {
        super.onStart()

        activityCallback?.onFragmentAttached(NavigationAdapter.NAV_CERTIFICATES.position, getString(R.string.title_section_certificates))
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (activityCallback?.isDrawerOpen == false) {
            inflater?.inflate(R.menu.refresh, menu)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        onRefresh()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        onRefresh()
    }
}
