package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.ViewModelDialogFragment
import de.xikolo.controllers.main.DateListAdapter
import de.xikolo.extensions.observe
import de.xikolo.viewmodels.main.DateListViewModel

class CourseDateListDialog : ViewModelDialogFragment<DateListViewModel>() {

    companion object {
        @JvmField
        val TAG: String = CourseDateListDialog::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    override val layoutResource = R.layout.fragment_date_list

    private var adapter: DateListAdapter = DateListAdapter(null)

    override fun createViewModel(): DateListViewModel {
        return DateListViewModel(courseId)
    }

    init {
        adapter.showCourse = false
    }

    override fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onDialogViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        viewModel.dates.observe(this) {
            adapter.update(viewModel.sectionedDateList)
            showContent()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setNegativeButton(R.string.dialog_course_dates_no) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }
            .setCancelable(true)
            .setView(dialogView)
            .create()
    }

}
