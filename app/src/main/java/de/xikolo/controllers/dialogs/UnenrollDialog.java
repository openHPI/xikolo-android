package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.base.BaseDialogFragment;

public class UnenrollDialog extends BaseDialogFragment {

    public static final String TAG = UnenrollDialog.class.getSimpleName();

    private UnenrollDialogListener listener;

    public void setUnenrollDialogListener(UnenrollDialogListener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setMessage(R.string.dialog_unenroll_message)
                .setTitle(R.string.dialog_unenroll_title)
                .setPositiveButton(R.string.dialog_unenroll_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDialogPositiveClick(UnenrollDialog.this);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UnenrollDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    public interface UnenrollDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

}
