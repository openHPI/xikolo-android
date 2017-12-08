package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.base.BaseDialogFragment;
import de.xikolo.managers.PermissionManager;

public class PermissionsDialog extends BaseDialogFragment {

    public static final String TAG = PermissionsDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setMessage(R.string.dialog_permissions)
                .setTitle(R.string.dialog_title_permissions)
                .setPositiveButton(R.string.title_section_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionManager.startAppInfo(getActivity());
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

}
