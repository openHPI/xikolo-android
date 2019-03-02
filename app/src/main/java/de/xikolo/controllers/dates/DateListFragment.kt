package de.xikolo.controllers.dates

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.events.LogoutEvent
import de.xikolo.viewmodels.CourseListViewModel
import de.xikolo.viewmodels.base.observe
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DateListFragment : NetworkStateFragment<CourseListViewModel>() {

    companion object {
        val TAG: String = DateListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    private lateinit var adapter: DateListAdapter

    override val layoutResource: Int = R.layout.content_date_list

    override fun createViewModel(): CourseListViewModel {
        return CourseListViewModel(CourseListFilter.MY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        EventBus.getDefault().register(this)
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

        viewModel.courses
            .observe(this) {
                // request the date list here as it is not included with the courses and needs to be refreshed upon courses change
                viewModel.requestDateList(false)
            }

        viewModel.dates
            .observe(this) {
                adapter.update(
                    viewModel.sectionedDateList,
                    viewModel.todaysDateCount,
                    viewModel.nextSevenDaysDateCount,
                    viewModel.futureDateCount
                )
                showContent()
            }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        activity?.finish()
    }

}
