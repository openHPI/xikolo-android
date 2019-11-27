package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.FeatureConfig
import de.xikolo.controllers.dialogs.base.ViewModelDialogFragment
import de.xikolo.models.Course
import de.xikolo.models.TicketTopic
import de.xikolo.viewmodels.helpdesk.HelpdeskTopicViewModel

class HelpdeskTopicDialog : ViewModelDialogFragment<HelpdeskTopicViewModel>() {

    companion object {
        @JvmField
        val TAG: String = HelpdeskTopicDialog::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var fromTicketDialog: Boolean = false

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    var listener: HelpdeskTopicListener? = null
    private lateinit var helpdeskTopicAdapter: HelpdeskTopicAdapter

    override val layoutResource = R.layout.dialog_helpdesk_topic_selection

    override fun createViewModel(): HelpdeskTopicViewModel {
        return HelpdeskTopicViewModel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return androidx.appcompat.app.AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setNegativeButton(R.string.dialog_negative) { _: DialogInterface, _: Int ->
                closeDialog()
            }
            .setOnKeyListener { _, keyCode, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    closeDialog()
                }
                false
            }
            .setView(dialogView)
            .setTitle(R.string.helpdesk_topic_dialog_header)
            .create()

    }

    override fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onDialogViewCreated(view, savedInstanceState)

        helpdeskTopicAdapter = HelpdeskTopicAdapter(object : HelpdeskTopicAdapter.OnTopicClickedListener {
            override fun onTopicClicked(title: String, topic: TicketTopic, courseId: String?) {
                dialog?.dismiss()
                listener?.onTopicChosen(title, topic, courseId)
            }

        })
        recyclerView.adapter = helpdeskTopicAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        viewModel.courses.observe(this, Observer<List<Course>> { courses ->
            helpdeskTopicAdapter.update(createHelpdeskTopicList(courses))
            showContent()
        })

        showContent()
    }

    private fun createHelpdeskTopicList(courseList: List<Course>): HelpdeskTopicList {
        val helpdeskTopicList = HelpdeskTopicList()
        val generalList = mutableListOf<HelpdeskTopic>()
        val technicalTopic = HelpdeskTopic(TicketTopic.TECHNICAL, null)

        generalList.add(technicalTopic)

        if (FeatureConfig.HELPDESK_COURSE_REACTIVATION) {
            val reactivationTopic = HelpdeskTopic(TicketTopic.REACTIVATION, null)
            generalList.add(reactivationTopic)
        }

        helpdeskTopicList.add(getString(R.string.helpdesk_topic_list_general), generalList)

        val courseTopicList = courseList.map { course ->
            HelpdeskTopic(TicketTopic.COURSE, course.id)
        }

        helpdeskTopicList.add(getString(R.string.helpdesk_topic_list_course), courseTopicList)

        return helpdeskTopicList
    }

    private fun closeDialog() {
        if (fromTicketDialog) {
            dialog?.dismiss()
        } else {
            listener?.closeTicketDialog()
            dialog?.dismiss()
        }
    }

    interface HelpdeskTopicListener {
        fun onTopicChosen(title: String, topic: TicketTopic, courseId: String?)

        fun closeTicketDialog()
    }

}
