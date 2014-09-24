package de.xikolo.controller.course.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import de.xikolo.R;

public class UnenrollDialog extends DialogFragment {

    public static final String TAG = UnenrollDialog.class.getSimpleName();

    private UnenrollDialogListener mListener;

    public void setUnenrollDialogListener(UnenrollDialogListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_unenroll_message)
                .setTitle(R.string.dialog_unenroll_title)
                .setPositiveButton(R.string.dialog_unenroll_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onDialogPositiveClick(UnenrollDialog.this);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_unenroll_no, new DialogInterface.OnClickListener() {
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
        public void onDialogPositiveClick(DialogFragment dialog);
    }

}
