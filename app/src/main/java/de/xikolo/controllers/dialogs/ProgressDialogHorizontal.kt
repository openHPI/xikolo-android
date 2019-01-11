package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ProgressDialogHorizontal : BaseDialogFragment() {

    companion object {
        @JvmField
        val TAG: String = ProgressDialogHorizontal::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var title: String? = null

    @AutoBundleField(required = false)
    var message: String? = null

    private var progressBar: ProgressBar? = null

    var progress: Int = 0
        set(value) {
            progressBar?.progress = value
            field = value
        }

    var max: Int = 1
        set(value) {
            progressBar?.max = value
            field = value
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater: LayoutInflater = LayoutInflater.from(activity!!)
        progressBar = inflater.inflate(R.layout.dialog_progress_horizontal, null) as ProgressBar
        progressBar?.max = max
        progressBar?.progress = progress


        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setView(progressBar)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)

        val dialog = builder.create()

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
