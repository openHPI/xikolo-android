package de.xikolo.controllers.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.R
import de.xikolo.controllers.announcement.AnnouncementActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.viewmodels.AnnouncementsViewModel
import de.xikolo.viewmodels.base.observe
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NewsListFragment : ViewModelMainFragment<AnnouncementsViewModel>() {

    companion object {
        val TAG: String = NewsListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: RecyclerView

    private lateinit var announcementListAdapter: AnnouncementListAdapter

    override val layoutResource = R.layout.content_news_list

    override fun createViewModel(): AnnouncementsViewModel {
        return AnnouncementsViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        announcementListAdapter = AnnouncementListAdapter({ announcementId -> openAnnouncement(announcementId) })

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = announcementListAdapter

        viewModel.globalAnnouncements
            .observe(this) { showAnnouncementList() }
    }

    override fun onStart() {
        super.onStart()

        activityCallback?.onFragmentAttached(NavigationAdapter.NAV_NEWS.position, getString(R.string.title_section_news))
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun showAnnouncementList() {
        viewModel.globalAnnouncements.value?.let { announcements ->
            if (announcements.isEmpty()) {
                showEmptyMessage(R.string.empty_message_global_announcements_title)
            } else {
                showContent()
                announcementListAdapter.announcementList = announcements.toMutableList()
            }
        }
    }

    private fun openAnnouncement(announcementId: String) {
        val intent = AnnouncementActivityAutoBundle.builder(announcementId, true).build(activity!!)
        startActivity(intent)
        LanalyticsUtil.trackVisitedAnnouncementDetail(announcementId)
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
