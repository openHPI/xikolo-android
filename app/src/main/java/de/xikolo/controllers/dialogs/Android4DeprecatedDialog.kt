package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class Android4DeprecatedDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = Android4DeprecatedDialog::class.java.simpleName
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setTitle(getString(R.string.dialog_android_4_deprecation_title))
            .setMessage(getString(R.string.dialog_android_4_deprecation_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> listener?.onConfirmed() }
            .create()
    }

    interface Listener {
        fun onConfirmed()
    }

}
