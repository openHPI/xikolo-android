package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import java.text.DateFormat
import java.util.*

class ApiVersionDeprecatedDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ApiVersionDeprecatedDialog::class.java.simpleName
    }

    var listener: Listener? = null

    @AutoBundleField
    lateinit var deprecationDate: Date

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setTitle(
                getString(
                    R.string.dialog_api_deprecation_title,
                    getString(R.string.app_name)
                )
            )
            .setMessage(
                getString(
                    R.string.dialog_api_deprecation_message,
                    getString(R.string.app_name),
                    df.format(deprecationDate)
                )
            )
            .setPositiveButton(getString(R.string.dialog_api_deprecation_yes)) { _, _ ->
                listener?.onOpenPlayStoreClicked()
            }
            .setNegativeButton(getString(R.string.dialog_api_deprecation_no)) { _, _ -> listener?.onDismissed() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCancel(dialog: DialogInterface?) {
        listener?.onDismissed()
    }

    interface Listener {
        fun onOpenPlayStoreClicked()
        fun onDismissed()
    }

}
