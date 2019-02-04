package de.xikolo.controllers.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.models.Course
import de.xikolo.presenters.base.PresenterFactory
import de.xikolo.presenters.main.CertificateListPresenter
import de.xikolo.presenters.main.CertificateListPresenterFactory
import de.xikolo.presenters.main.CertificateListView
import de.xikolo.views.SpaceItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CertificateListFragment : PresenterMainFragment<CertificateListPresenter, CertificateListView>(), CertificateListView {

    companion object {
        val TAG: String = CertificateListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    private var certificateListAdapter: CertificateListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun getLayoutResource(): Int {
        return R.layout.content_certificate_list
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
                        return certificateListAdapter!!.itemCount
                    }
                }))
        }
        recyclerView.adapter = certificateListAdapter
    }

    override fun showCertificateList(courses: MutableList<Course>) {
        certificateListAdapter?.update(courses)
    }

    override fun showLoginRequiredMessage() {
        super.showLoginRequiredMessage()
        loadingStateHelper.setMessageOnClickListener { _ ->
            activityCallback?.selectDrawerSection(NavigationAdapter.NAV_PROFILE.position)
        }
    }

    override fun showNoCertificatesMessage() {
        loadingStateHelper.setMessageTitle(R.string.notification_no_certificates)
        loadingStateHelper.setMessageSummary(R.string.notification_no_certificates_summary)
        loadingStateHelper.setMessageOnClickListener { _ -> activityCallback?.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.position) }
        loadingStateHelper.showMessage()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        when (itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getPresenterFactory(): PresenterFactory<CertificateListPresenter> {
        return CertificateListPresenterFactory()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        presenter?.onRefresh()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        presenter?.onRefresh()
    }
}
