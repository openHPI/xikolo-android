package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.extensions.observe
import de.xikolo.models.dao.CourseProgressDao
import de.xikolo.viewmodels.course.ProgressViewModel
import de.xikolo.views.SpaceItemDecoration

class ProgressFragment : NetworkStateFragment<ProgressViewModel>() {

    companion object {
        val TAG: String = ProgressFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    private lateinit var adapter: ProgressListAdapter

    override val layoutResource: Int = R.layout.content_progress

    override fun createViewModel(): ProgressViewModel {
        return ProgressViewModel(courseId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.content_view)

        adapter = ProgressListAdapter(activity!!)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(SpaceItemDecoration(
            0,
            activity!!.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return false
                }

                override val spanCount: Int
                    get() = 1

                override val itemCount: Int
                    get() = adapter.itemCount
            }
        ))

        viewModel.sectionProgresses
            .observe(viewLifecycleOwner) { sp ->
                val cp = CourseProgressDao.Unmanaged.find(courseId)
                if (sp.isNotEmpty() && cp != null) {
                    adapter.update(cp, sp)
                    showContent()
                }
            }
    }

}
