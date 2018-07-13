package de.xikolo.controllers.course

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.lifecycle.DocumentsViewModel
import de.xikolo.utils.ToastUtil

class DocumentsFragment : NetworkStateFragment<DocumentsViewModel>() {

    @AutoBundleField
    lateinit var courseId: String

    override fun createViewModel(): DocumentsViewModel {
        return DocumentsViewModel(courseId)
    }

    override fun getLayoutResource(): Int = R.layout.content_richtext

    override fun setTitle(title: CharSequence?) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.documents.observe(this, Observer {
            ToastUtil.show("${it?.size} documents received for course!")
        })
    }

}
