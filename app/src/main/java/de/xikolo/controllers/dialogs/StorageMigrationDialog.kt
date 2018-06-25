package de.xikolo.controllers.dialogs

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog

import de.xikolo.R
import de.xikolo.utils.StorageUtil

class StorageMigrationDialog constructor(context: Context) : AlertDialog(context) {

    interface StorageMigrationDialogListener {
        fun onDialogPositiveClick()
    }

    companion object {

        val TAG = StorageMigrationDialog::class.java.simpleName

        fun getInstance(activity: Activity, from: StorageUtil.StorageType, listener: StorageMigrationDialogListener?): AlertDialog {
            val builder = AlertDialog.Builder(activity, R.style.AppTheme_Dialog)
                .setTitle(activity.getString(R.string.dialog_storage_migration_title))
                .setMessage(StorageUtil.buildMigrationMessage(activity, from))
                .setPositiveButton(R.string.dialog_storage_migration_confirm) { _, _ ->
                    listener?.onDialogPositiveClick()
                }
                .setNegativeButton(R.string.dialog_negative) { dialog, _ -> dialog.cancel() }
                .setCancelable(true)

            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(true)

            return dialog
        }
    }

}
