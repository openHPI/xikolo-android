package de.xikolo.controller.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import de.xikolo.R;

public class ContributorsDialog extends DialogFragment {

    public static final String TAG = DialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.settings_contributors_content)
                .setTitle(R.string.settings_title_contributors)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

}
