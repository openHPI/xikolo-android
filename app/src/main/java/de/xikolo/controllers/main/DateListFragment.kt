package de.xikolo.controllers.main

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.R
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.models.CourseDate
import de.xikolo.viewmodels.main.DateListViewModel

class DateListFragment : MainFragment<DateListViewModel>() {

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
                showDateList(it)
            }
    }

    private fun showDateList(list: List<CourseDate>) {
        if (!UserManager.isAuthorized) {
            hideContent()
            showLoginRequired()
        } else if (list.isEmpty()) {
            hideContent()
            showEmptyMessage(R.string.empty_message_dates)
        } else {
            adapter.update(viewModel.sectionedDateList)
            showContent()
        }
    }

    override fun onLoginStateChange(isLoggedIn: Boolean) {
        super.onLoginStateChange(isLoggedIn)

        // refreshing itself does not trigger a change of the dates, so it is done manually
        showDateList(listOf())
    }

}
