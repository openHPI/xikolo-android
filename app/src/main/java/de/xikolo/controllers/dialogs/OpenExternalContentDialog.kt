package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class OpenExternalContentDialog : BaseDialogFragment() {

    companion object {
        val TAG: String = OpenExternalContentDialog::class.java.simpleName
    }

    var listener: ExternalContentDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setMessage(R.string.dialog_external_content_message)
            .setTitle(R.string.dialog_external_content_title)
            .setPositiveButton(R.string.dialog_external_content_yes) { _, _ ->
                listener?.onOpen(this)
            }
            .setNegativeButton(R.string.dialog_negative) { _, _ ->
                dialog?.cancel()
            }
            .setNeutralButton(R.string.dialog_external_content_yes_always) { _, _ ->
                listener?.onOpenAlways(this)
            }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface ExternalContentDialogListener {
        fun onOpen(dialog: DialogFragment)

        fun onOpenAlways(dialog: DialogFragment)
    }

}
