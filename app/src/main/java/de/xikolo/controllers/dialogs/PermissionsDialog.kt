package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import de.xikolo.managers.PermissionManager

class PermissionsDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = PermissionsDialog::class.java.simpleName
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setMessage(R.string.dialog_permissions)
            .setTitle(R.string.dialog_title_permissions)
            .setPositiveButton(R.string.title_section_settings) { _, _ ->
                PermissionManager.startAppInfo(activity!!)
            }
            .setNegativeButton(R.string.dialog_negative) { _, _ -> dialog.cancel() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

}
