package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import de.xikolo.models.Item

class OpenExternalContentDialog : BaseDialogFragment() {

    companion object {
        val TAG: String = OpenExternalContentDialog::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var type: String = Item.TYPE_LTI

    var listener: ExternalContentDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = if (type == Item.TYPE_LTI) {
            getString(
                R.string.dialog_external_content_message_lti,
                getString(R.string.app_name)
            )
        } else {
            getString(R.string.dialog_external_content_message_peer)
        }
        val yes = if (type == Item.TYPE_LTI) R.string.dialog_external_content_yes_lti else R.string.dialog_external_content_yes_peer
        val yesAlways = if (type == Item.TYPE_LTI) R.string.dialog_external_content_yes_always_lti else R.string.dialog_external_content_yes_always_peer

        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setMessage(message)
            .setTitle(R.string.dialog_external_content_title)
            .setPositiveButton(yes) { _, _ ->
                listener?.onOpen(this)
            }
            .setNegativeButton(R.string.dialog_negative) { _, _ ->
                dialog?.cancel()
            }
            .setNeutralButton(yesAlways) { _, _ ->
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
