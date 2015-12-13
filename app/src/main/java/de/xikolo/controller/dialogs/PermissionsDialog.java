package de.xikolo.controller.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import de.xikolo.R;

public class PermissionsDialog extends DialogFragment {

    public static final String TAG = PermissionsDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.settings_permissions)
                .setTitle(R.string.settings_title_permissions)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

}
