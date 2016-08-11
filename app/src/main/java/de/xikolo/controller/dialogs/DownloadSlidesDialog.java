package de.xikolo.controller.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;

public class DownloadSlidesDialog extends DialogFragment {

    public static final String TAG = DownloadSlidesDialog.class.getSimpleName();

    private DownloadSlidesDialogListener listener;

    public void setListener(DownloadSlidesDialogListener listener) {
        this.listener = listener;
    }

    public DownloadSlidesDialog() {
    }

    public static DownloadSlidesDialog getInstance() {
        return new DownloadSlidesDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(R.string.dialog_download_slides_title)
                .setMessage(R.string.dialog_download_slides_message)
                .setPositiveButton(R.string.dialog_download_slides_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDialogPositiveClick();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick();
                        DownloadSlidesDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    public interface DownloadSlidesDialogListener {
        void onDialogPositiveClick();

        void onDialogNegativeClick();
    }

}
