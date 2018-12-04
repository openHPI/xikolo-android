package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.ProgressBar
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ProgressDialogIndeterminate : BaseDialogFragment() {

    companion object {
        @JvmField
        val TAG: String = ProgressDialogIndeterminate::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var title: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater: LayoutInflater = LayoutInflater.from(activity!!)
        val progressBar: ProgressBar = inflater.inflate(R.layout.dialog_progress_indeterminate, null) as ProgressBar

        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setView(progressBar)
            .setTitle(title)
            .setCancelable(false)

        val dialog = builder.create()

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
