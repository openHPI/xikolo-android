package de.xikolo.controller.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import de.xikolo.R;
import de.xikolo.model.PermissionsModel;

public class PermissionsDialog extends DialogFragment {

    public static final String TAG = PermissionsDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_permissions)
                .setTitle(R.string.dialog_title_permissions)
                .setPositiveButton(R.string.title_section_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsModel.startAppInfo(getActivity());
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
