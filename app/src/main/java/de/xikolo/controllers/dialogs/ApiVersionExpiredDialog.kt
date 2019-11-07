package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ApiVersionExpiredDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ApiVersionExpiredDialog::class.java.simpleName
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setTitle(getString(R.string.dialog_api_expiration_title, getString(R.string.app_name)))
            .setMessage(
                getString(
                    R.string.dialog_api_expiration_message,
                    getString(R.string.app_name)
                )
            )
            .setPositiveButton(getString(R.string.dialog_api_expiration_yes)) { _, _ ->
                listener?.onOpenPlayStoreClicked()
            }
            .setNegativeButton(getString(R.string.dialog_api_expiration_no)) { _, _ ->
                listener?.onDismissed()
            }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        listener?.onDismissed()
    }

    interface Listener {
        fun onOpenPlayStoreClicked()
        fun onDismissed()
    }

}
