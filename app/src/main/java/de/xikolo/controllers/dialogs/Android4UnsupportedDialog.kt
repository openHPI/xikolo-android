package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class Android4UnsupportedDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = Android4UnsupportedDialog::class.java.simpleName
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setTitle(getString(R.string.dialog_android_4_unsupported_title))
            .setMessage(getString(R.string.dialog_android_4_unsupported_message))
            .setNegativeButton(getString(R.string.dialog_android_4_unsupported_no)) { _, _ -> listener?.onConfirmed() }
            .create()
    }

    interface Listener {
        fun onConfirmed()
    }

}
