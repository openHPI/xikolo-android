package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ServerMaintenanceDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ServerMaintenanceDialog::class.java.simpleName
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setTitle(getString(R.string.dialog_server_maintenance_title))
            .setMessage(getString(R.string.dialog_server_maintenance_message))
            .setNegativeButton(getString(R.string.dialog_server_maintenance_no)) { _, _ -> listener?.onDismissed() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        listener?.onDismissed()
    }

    interface Listener {
        fun onDismissed()
    }

}
