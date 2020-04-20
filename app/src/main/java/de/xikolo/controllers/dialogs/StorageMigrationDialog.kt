package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import de.xikolo.models.Storage
import de.xikolo.utils.extensions.buildMigrationMessage

class StorageMigrationDialog : BaseDialogFragment() {

    companion object {
        val TAG: String = StorageMigrationDialog::class.java.simpleName
    }

    @AutoBundleField
    lateinit var from: Storage.Type

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(activity!!.getString(R.string.dialog_storage_migration_title))
            .setMessage(activity?.buildMigrationMessage(from))
            .setPositiveButton(R.string.dialog_storage_migration_confirm) { _, _ -> listener?.onDialogPositiveClick() }
            .setNegativeButton(R.string.dialog_no) { dialog, _ -> dialog.cancel() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onDialogPositiveClick()
    }

}
