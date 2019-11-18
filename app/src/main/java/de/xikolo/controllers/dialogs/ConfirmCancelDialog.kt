package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.xikolo.R

class ConfirmCancelDialog : DialogFragment() {

    companion object {
        @JvmField
        val TAG: String = ConfirmCancelDialog::class.java.simpleName
    }

    var listener: ConfirmCancelListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setNegativeButton(R.string.dialog_negative) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.dialog_discard_positive)) { dialog: DialogInterface, _: Int ->
                listener?.onConfirmCancel()
                dialog.dismiss()
            }
            .setMessage(getString(R.string.dialog_discard_inputs))
            .create()
    }

    interface ConfirmCancelListener {
        fun onConfirmCancel()
    }

}
