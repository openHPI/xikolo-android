package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.lifecycle.DocumentsViewModel
import de.xikolo.lifecycle.base.nonNull
import de.xikolo.lifecycle.base.observe

class DocumentsFragment : NetworkStateFragment<DocumentsViewModel>() {

    @AutoBundleField
    lateinit var courseId: String

    override fun createViewModel(): DocumentsViewModel {
        return DocumentsViewModel(courseId)
    }

    override fun getLayoutResource(): Int = R.layout.content_richtext

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.documentsForCourse
            .nonNull()
            .observe(this) { showDocuments() }

        viewModel.localizations
            .nonNull()
            .observe(this) { showDocuments() }
    }

    private fun showDocuments() {
        viewModel.documentsForCourse.value?.let {documents ->
            // display list view
        }
    }

}
