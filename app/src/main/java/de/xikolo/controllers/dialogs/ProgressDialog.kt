package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ProgressDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ProgressDialog::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var title: String? = null

    @AutoBundleField(required = false)
    var message: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = android.app.ProgressDialog(activity)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        isCancelable = false

        return dialog
    }

}
