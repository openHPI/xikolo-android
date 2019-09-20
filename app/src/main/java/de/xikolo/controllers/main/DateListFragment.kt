package de.xikolo.controllers.main

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.R
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.events.LogoutEvent
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.models.CourseDate
import de.xikolo.models.DateOverview
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.main.DateListViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DateListFragment : ViewModelMainFragment<DateListViewModel>() {

    companion object {
        val TAG: String = DateListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    private lateinit var adapter: DateListAdapter

    override val layoutResource: Int = R.layout.content_date_list

    override fun createViewModel(): DateListViewModel {
        return DateListViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
    }

    override fun onStart() {
        super.onStart()
        activityCallback?.onFragmentAttached(R.id.navigation_dates)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DateListAdapter(object : DateListAdapter.OnDateClickListener {
            override fun onCourseClicked(courseId: String?) {
                startActivity(
                    CourseActivityAutoBundle.builder().courseId(courseId).build(activity!!)
                )
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        viewModel.dates
            .observe(viewLifecycleOwner) {
                if (UserManager.isAuthorized) {
                    if (it.isNotEmpty()) {
                        showDateList(viewModel.sectionedDateList)
                    } else {
                        showEmptyMessage(R.string.empty_message_dates)
                    }
                } else {
                    showLoginRequired()
                }
            }
    }

    private fun showDateList(dateList: MetaSectionList<String, DateOverview, List<CourseDate>>) {
        adapter.update(dateList)
        showContent()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        activity?.finish()
    }

}
