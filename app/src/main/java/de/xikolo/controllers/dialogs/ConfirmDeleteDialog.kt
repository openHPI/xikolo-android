package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import de.xikolo.utils.extensions.is7inchTablet

class ConfirmDeleteDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ConfirmDeleteDialog::class.java.simpleName
    }

    var listener: Listener? = null

    @AutoBundleField
    var multipleFiles: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setMessage(if (multipleFiles) R.string.dialog_confirm_delete_message_multi else R.string.dialog_confirm_delete_message)
            .setTitle(if (multipleFiles) R.string.dialog_confirm_delete_title_multi else R.string.dialog_confirm_delete_title)
            .setPositiveButton(R.string.dialog_confirm_delete_yes) { _, _ -> listener?.onDialogPositiveClick(this) }
            .setNegativeButton(R.string.dialog_negative) { _, _ -> dialog?.cancel() }
            .setCancelable(true)

        if (activity?.is7inchTablet == true) {
            builder.setNeutralButton(R.string.dialog_confirm_delete_yes_always) { _, _ -> listener?.onDialogPositiveAndAlwaysClick(this) }
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogPositiveAndAlwaysClick(dialog: DialogFragment)
    }

}
