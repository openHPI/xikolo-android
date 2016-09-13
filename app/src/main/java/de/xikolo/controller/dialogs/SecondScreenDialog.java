package de.xikolo.controller.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;
import de.xikolo.controller.SecondScreenActivity;

public class SecondScreenDialog extends DialogFragment {

    public static final String TAG = SecondScreenDialog.class.getSimpleName();

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(R.string.dialog_second_screen_title)
                .setMessage(R.string.dialog_second_screen_message)
                .setPositiveButton(R.string.dialog_second_screen_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getActivity(), SecondScreenActivity.class));
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SecondScreenDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

}
