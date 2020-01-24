package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.helper.SectionDownloadHelper
import de.xikolo.controllers.section.CourseItemsActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.models.Section
import de.xikolo.models.dao.CourseDao
import de.xikolo.models.dao.SectionDao
import de.xikolo.viewmodels.course.LearningsViewModel
import de.xikolo.views.SpaceItemDecoration

class LearningsFragment : ViewModelFragment<LearningsViewModel>(), SectionListAdapter.OnSectionClickListener, ItemListAdapter.OnItemClickListener {

    companion object {
        val TAG: String = LearningsFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: RecyclerView

    private lateinit var adapter: SectionListAdapter

    override val layoutResource: Int = R.layout.fragment_course_learnings

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

                override val spanCount: Int
                    get() = 1

                override val itemCount: Int
                    get() = adapter.itemCount
            }
        ))

        viewModel.accessibleItems
            .observe(viewLifecycleOwner) {
                setupSections(SectionDao.Unmanaged.allForCourse(courseId))
                showContent()
            }
    }

    override fun onSectionClicked(sectionId: String) {
        startCourseItemsActivity(courseId, sectionId, 0)
    }

    override fun onSectionDownloadClicked(sectionId: String) {
        val sectionDownloadHelper = SectionDownloadHelper(activity!!)
        sectionDownloadHelper.initSectionDownloads(CourseDao.Unmanaged.find(courseId)!!, SectionDao.Unmanaged.find(sectionId)!!)
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
