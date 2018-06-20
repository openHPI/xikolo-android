package de.xikolo.controllers.dialogs;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;
import de.xikolo.utils.StorageUtil;

public class MigrationDialog extends AlertDialog {

    public static final String TAG = MigrationDialog.class.getSimpleName();

    protected MigrationDialog(@NonNull Context context) {
        super(context);
    }

    public static AlertDialog getInstance(Activity activity, StorageUtil.StorageType from, MigrationDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog)
            .setTitle(activity.getString(R.string.dialog_storage_migration_title))
            .setMessage(StorageUtil.buildMigrationMessage(activity, from))
            .setPositiveButton(R.string.dialog_storage_migration_confirm, (dialog, which) -> {
                if (listener != null) {
                    listener.onDialogPositiveClick();
                }
            })
            .setNegativeButton(R.string.dialog_negative, (dialog, which) -> dialog.cancel())
            .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    public interface MigrationDialogListener {
        void onDialogPositiveClick();
    }

}
