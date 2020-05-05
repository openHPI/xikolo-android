package de.xikolo.controllers.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class DateInfoDialog(val title: String, private val localDateText: String, private val utcDateText: String) : BaseDialogFragment() {

    companion object {
        @JvmField
        val TAG: String = DateInfoDialog::class.java.simpleName
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_date_info, null)
        val localDate: TextView = view.findViewById(R.id.localDate)
        val utcDate: TextView = view.findViewById(R.id.utcDate)

        localDate.text = localDateText
        utcDate.text = utcDateText

        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setView(view)
            .setNegativeButton(R.string.dialog_date_info_no) { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

}
