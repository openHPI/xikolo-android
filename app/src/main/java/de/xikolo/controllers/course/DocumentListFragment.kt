package de.xikolo.controllers.course

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.view_models.DocumentsViewModel
import de.xikolo.view_models.base.observe

class DocumentListFragment : NetworkStateFragment<DocumentsViewModel>() {

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    internal lateinit var recyclerView: RecyclerView

    private lateinit var documentListAdapter: DocumentListAdapter

    override fun createViewModel(): DocumentsViewModel {
        return DocumentsViewModel(courseId)
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
            .observe(this) { showDocuments() }

        viewModel.localizations
            .observe(this) { showDocuments() }
    }

    private fun showDocuments() {
        viewModel.documentsForCourse.value?.let {documents ->
            if (documents.isEmpty()) {
                showEmptyMessage(R.string.empty_message_documents_title)
            } else {
                showContent()
                documentListAdapter.documentList = documents.toMutableList()
            }
        }
    }

}
