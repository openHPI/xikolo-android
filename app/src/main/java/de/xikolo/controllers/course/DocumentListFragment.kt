package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.extensions.observe
import de.xikolo.viewmodels.course.DocumentListViewModel

class DocumentListFragment : ViewModelFragment<DocumentListViewModel>() {

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: RecyclerView

    private lateinit var documentListAdapter: DocumentListAdapter

    override fun createViewModel(): DocumentListViewModel {
        return DocumentListViewModel(courseId)
    }

    override val layoutResource = R.layout.content_documents_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            layoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        activity?.let { a ->
            documentListAdapter = DocumentListAdapter(a)
            recyclerView.adapter = documentListAdapter
        }

        viewModel.documentsForCourse
            .observe(viewLifecycleOwner) { showDocuments() }

        viewModel.localizations
            .observe(viewLifecycleOwner) { showDocuments() }
    }

    private fun showDocuments() {
        viewModel.documentsForCourse.value?.let { documents ->
            if (documents.isEmpty()) {
                showEmptyMessage(R.string.empty_message_documents_title)
            } else {
                showContent()
                documentListAdapter.documentList = documents.toMutableList()
            }
        }
    }

}
