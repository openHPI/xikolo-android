package de.xikolo.controllers.main

import android.os.Bundle
import android.view.View
import butterknife.BindView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.channels.ChannelDetailsActivityAutoBundle
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.extensions.observe
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.viewmodels.main.ChannelListViewModel
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.SpaceItemDecoration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChannelListFragment : ViewModelMainFragment<ChannelListViewModel>() {

    companion object {
        val TAG: String = ChannelListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: AutofitRecyclerView

    private lateinit var channelListAdapter: ChannelListAdapter

    override val layoutResource = R.layout.content_channel_list

    override fun createViewModel(): ChannelListViewModel {
        return ChannelListViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelListAdapter = ChannelListAdapter(object : ChannelListAdapter.OnChannelCardClickListener {
            override fun onChannelClicked(channelId: String) {
                val intent = ChannelDetailsActivityAutoBundle
                    .builder(channelId)
                    .build(App.instance)
                startActivity(intent)
            }

            override fun onCourseClicked(course: Course) {
                val intent = CourseActivityAutoBundle
                    .builder()
                    .courseId(course.id)
                    .build(App.instance)
                startActivity(intent)
            }

            override fun onMoreCoursesClicked(channelId: String, scrollPosition: Int) {
                val intent = ChannelDetailsActivityAutoBundle
                    .builder(channelId)
                    .scrollToCoursePosition(scrollPosition)
                    .build(App.instance)
                startActivity(intent)
            }
        })

        recyclerView.adapter = channelListAdapter

        recyclerView.addItemDecoration(SpaceItemDecoration(
            activity!!.resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
            activity!!.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return false
                }

                override val spanCount: Int
                    get() = recyclerView.spanCount

                override val itemCount: Int
                    get() = channelListAdapter.itemCount
            }
        ))

        viewModel.channels
            .observe(viewLifecycleOwner) {
                showChannelList(
                    it,
                    viewModel.buildCourseLists(it))
            }
    }

    private fun showChannelList(channelList: List<Channel>, courseLists: List<List<Course>>) {
        channelListAdapter.update(channelList, courseLists)
        showContent()
    }

    override fun onStart() {
        super.onStart()
        activityCallback?.onFragmentAttached(R.id.navigation_channels)
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
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
