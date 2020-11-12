package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class UnsupportedIntentDialog : BaseDialogFragment() {

    companion object {
        @JvmField
        val TAG: String = UnsupportedIntentDialog::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var fileMimeType: String? = null

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = getString(R.string.dialog_unsupported_intent_message) +
            (
                fileMimeType?.let {
                    getString(R.string.dialog_unsupported_intent_message_file, it)
                } ?: ""
                )

        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.dialog_unsupported_intent_title)
            .setMessage(message)
            .setNeutralButton(R.string.dialog_unsupported_intent_message_open_anyway) { _, _ ->
                listener?.onOpenAnywayClicked()
            }
            .setNegativeButton(R.string.dialog_server_error_no) { _, _ ->
                dismiss()
            }
            .apply {
                if (fileMimeType != null) {
                    setPositiveButton(
                        R.string.dialog_unsupported_intent_message_open_path
                    ) { _, _ ->
                        listener?.onOpenPathClicked()
                    }
                }
            }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onOpenPathClicked()
        fun onOpenAnywayClicked()
    }
}
