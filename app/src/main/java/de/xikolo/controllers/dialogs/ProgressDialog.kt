package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.ProgressBar
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ProgressDialog : BaseDialogFragment() {

    companion object {
        @JvmField
        val TAG: String = ProgressDialog::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var title: String? = null

    @AutoBundleField(required = false)
    var message: String? = null

    var max = 0

    var progressStyle = android.app.ProgressDialog.STYLE_HORIZONTAL

    var progress = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = android.app.ProgressDialog(activity)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        isCancelable = false

        val p: ProgressBar = layoutInflater.inflate(R.layout.container_progress_bar, null) as ProgressBar

        val b = AlertDialog.Builder(App.getInstance())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setView(p)

        return dialog
    }

}
