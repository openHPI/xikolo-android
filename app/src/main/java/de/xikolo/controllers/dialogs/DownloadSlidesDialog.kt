package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class DownloadSlidesDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = DownloadSlidesDialog::class.java.simpleName
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setTitle(R.string.dialog_download_slides_title)
            .setMessage(R.string.dialog_download_slides_message)
            .setPositiveButton(R.string.dialog_download_slides_yes) { _, _ -> listener?.onDialogPositiveClick() }
            .setNegativeButton(R.string.dialog_negative) { _, _ ->
                listener?.onDialogNegativeClick()
                dialog.cancel()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    interface Listener {
        fun onDialogPositiveClick()
        fun onDialogNegativeClick()
    }

}
