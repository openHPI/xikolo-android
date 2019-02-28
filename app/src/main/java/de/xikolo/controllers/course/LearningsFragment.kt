package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.helper.SectionDownloadHelper
import de.xikolo.controllers.section.CourseItemsActivityAutoBundle
import de.xikolo.models.Course
import de.xikolo.models.Section
import de.xikolo.viewmodels.base.observe
import de.xikolo.viewmodels.course.LearningsViewModel
import de.xikolo.views.SpaceItemDecoration

class LearningsFragment : NetworkStateFragment<LearningsViewModel>(), SectionListAdapter.OnSectionClickListener, ItemListAdapter.OnItemClickListener {

    companion object {
        val TAG: String = LearningsFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: RecyclerView

    private lateinit var adapter: SectionListAdapter

    override val layoutResource: Int = R.layout.content_learnings

    override fun createViewModel(): LearningsViewModel {
        return LearningsViewModel(courseId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SectionListAdapter(activity!!, this, this)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)
        recyclerView.addItemDecoration(SpaceItemDecoration(
            0,
            activity!!.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return false
                }

                override fun getSpanCount(): Int {
                    return 1
                }

                override fun getItemCount(): Int {
                    return adapter.itemCount
                }
            }
        ))

        viewModel.course
            .observe(this) {
                activity?.title = it.title
            }

        viewModel.accessibleItems
            .observe(this) {
                setupSections(Section.listForCourse(courseId))
                showContent()
            }
    }

    override fun onSectionClicked(sectionId: String) {
        startCourseItemsActivity(courseId, sectionId, 0)
    }

    override fun onSectionDownloadClicked(sectionId: String) {
        val sectionDownloadHelper = SectionDownloadHelper(activity)
        sectionDownloadHelper.initSectionDownloads(Course.get(courseId), Section.get(sectionId))
    }

    override fun onItemClicked(sectionId: String, position: Int) {
        startCourseItemsActivity(courseId, sectionId, position)
    }

    private fun startCourseItemsActivity(courseId: String, sectionId: String, position: Int) {
        activity?.let {
            val intent = CourseItemsActivityAutoBundle.builder(courseId, sectionId, position).build(it)
            it.startActivity(intent)
        }
    }

    private fun setupSections(sectionList: List<Section>) {
        adapter.updateSections(sectionList)
    }

}
