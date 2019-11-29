package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.ViewModelDialogFragment
import de.xikolo.managers.UserManager
import de.xikolo.models.TicketTopic
import de.xikolo.models.dao.CourseDao
import de.xikolo.utils.extensions.isOnline
import de.xikolo.utils.extensions.showToast
import de.xikolo.viewmodels.helpdesk.TicketViewModel

class CreateTicketDialog : ViewModelDialogFragment<TicketViewModel>(), HelpdeskTopicDialog.HelpdeskTopicListener, ConfirmCancelDialog.ConfirmCancelListener {

    companion object {
        @JvmField
        val TAG: String = CreateTicketDialog::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var courseId: String? = null

    @BindView(R.id.ticketContent)
    lateinit var messageEditText: EditText

    @BindView(R.id.ticketEmail)
    lateinit var emailEditText: EditText

    @BindView(R.id.ticketTitle)
    lateinit var titleEditText: EditText

    @BindView(R.id.ticketTopic)
    lateinit var ticketTopicEditText: EditText

    @BindView(R.id.ticketInfoText)
    lateinit var ticketInfoText: TextView

    private var topic: TicketTopic = TicketTopic.NONE

    override val layoutResource = R.layout.dialog_helpdesk_create_ticket

    override fun createViewModel(): TicketViewModel {
        return TicketViewModel()
    }

    override fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onDialogViewCreated(view, savedInstanceState)

        if (!context.isOnline) {
            showToast(R.string.toast_no_network)
            dialog?.cancel()
            return
        }

        networkStateHelper.enableSwipeRefresh(false)

        if (courseId == null) {
            changeTopic(false)
        } else {
            ticketTopicEditText.setText(CourseDao.Unmanaged.find(courseId)?.title)
            topic = TicketTopic.COURSE
        }

        if (UserManager.isAuthorized) {
            emailEditText.visibility = View.GONE
            ticketInfoText.text = getString(R.string.helpdesk_info_text_logged_in)
        }

        //listeners for wrong entries in EditTexts after focusing another view
        titleEditText.setOnFocusChangeListener { _, b ->
            if (titleEditText.text.isEmpty() && !b) {
                titleEditText.error = getString(R.string.helpdesk_title_error)
            }
            setPositiveButton()
        }

        messageEditText.setOnFocusChangeListener { _, b ->
            if (messageEditText.text.isEmpty() && !b) {
                messageEditText.error = getString(R.string.helpdesk_message_error)
            }
            setPositiveButton()
        }

        emailEditText.setOnFocusChangeListener { _, b ->
            if ((!Patterns.EMAIL_ADDRESS.matcher(emailEditText.text).matches() || emailEditText.text.isEmpty()) && emailEditText.visibility != View.GONE && !b) {
                emailEditText.error = getString(R.string.helpdesk_email_error)
            }
        }

        //listeners for positive button to change while typing
        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                setPositiveButton()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        }

        messageEditText.addTextChangedListener(textWatcher)
        emailEditText.addTextChangedListener(textWatcher)
        titleEditText.addTextChangedListener(textWatcher)
        ticketTopicEditText.addTextChangedListener(textWatcher)

        ticketTopicEditText.setOnClickListener {
            changeTopic(true)
        }
        ticketTopicEditText.inputType = InputType.TYPE_NULL

        showContent()
    }

    override fun onResume() {
        super.onResume()
        (dialog as? AlertDialog)?.apply {
            getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                cancel(this)
            }
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (context.isOnline) {
                    viewModel.send(titleEditText.text.toString(), messageEditText.text.toString(), topic, emailEditText.text.toString(), courseId)
                    dialog?.dismiss()
                } else {
                    showToast(R.string.toast_no_network)
                }
            }
        }
        //needed for initial disabling of positive button
        setPositiveButton()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setNegativeButton(R.string.dialog_negative) { _: DialogInterface, _: Int ->
                //see onResume
            }
            .setPositiveButton(R.string.dialog_send) { _: DialogInterface, _: Int ->
                //see onResume
            }
            .setView(dialogView)
            .setTitle(R.string.helpdesk_dialog_header)
            .setOnCancelListener { dialog: DialogInterface ->
                cancel(dialog)
            }
            .setOnKeyListener { dialog, keyCode, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    cancel(dialog)
                    return@setOnKeyListener true
                }
                false
            }
            .create()
    }

    private fun cancel(dialog: DialogInterface) {
        if (atLeastOneFieldHasInput) {
            confirmCancel()
        } else {
            dialog.dismiss()
        }
    }

    private fun changeTopic(isChange: Boolean) {
        val dialog = HelpdeskTopicDialogAutoBundle.builder().fromTicketDialog(isChange).build()
        dialog.listener = this
        dialog.show(fragmentManager!!, HelpdeskTopicDialog.TAG)
    }

    override fun onTopicChosen(title: String?, topic: TicketTopic, courseId: String?) {
        this.courseId = courseId
        this.topic = topic
        ticketTopicEditText.setText(title)
    }

    fun setPositiveButton() {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = allFieldsHaveInput
    }

    override fun closeTicketDialog() {
        dialog?.dismiss()
    }

    override fun onConfirmCancel() {
        closeTicketDialog()
    }

    private val atLeastOneFieldHasInput: Boolean
        get() =
            (titleEditText.text.isNotEmpty() ||
                messageEditText.text.isNotEmpty() ||
                emailEditText.text.isNotEmpty())

    private val allFieldsHaveInput: Boolean
        get() = titleEditText.text.isNotEmpty() && messageEditText.text.isNotEmpty() && ticketTopicEditText.text.isNotEmpty() &&
            ((emailEditText.text.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailEditText.text).matches()) || emailEditText.visibility == View.GONE) &&
            topic !== TicketTopic.NONE

    private fun confirmCancel() {
        val dialog = ConfirmCancelDialog()
        dialog.listener = this
        dialog.show(fragmentManager!!, ConfirmCancelDialog.TAG)
    }

}
