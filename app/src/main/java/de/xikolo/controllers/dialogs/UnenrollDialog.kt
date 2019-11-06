package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class UnenrollDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = UnenrollDialog::class.java.simpleName
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setMessage(R.string.dialog_unenroll_message)
            .setTitle(R.string.dialog_unenroll_title)
            .setPositiveButton(R.string.dialog_unenroll_yes) { _, _ -> listener?.onDialogPositiveClick(this) }
            .setNegativeButton(R.string.dialog_negative) { _, _ -> dialog?.cancel() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

}
