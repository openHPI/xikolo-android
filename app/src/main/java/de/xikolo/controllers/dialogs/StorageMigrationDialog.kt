package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import de.xikolo.utils.StorageUtil

class StorageMigrationDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = StorageMigrationDialog::class.java.simpleName
    }

    @AutoBundleField
    lateinit var from: StorageUtil.StorageType

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
            .setTitle(activity!!.getString(R.string.dialog_storage_migration_title))
            .setMessage(StorageUtil.buildMigrationMessage(activity!!, from))
            .setPositiveButton(R.string.dialog_storage_migration_confirm) { _, _ -> listener?.onDialogPositiveClick() }
            .setNegativeButton(R.string.dialog_negative) { dialog, _ -> dialog.cancel() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onDialogPositiveClick()
    }

}
