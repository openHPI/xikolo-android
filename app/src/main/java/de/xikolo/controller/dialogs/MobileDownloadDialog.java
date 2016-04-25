package de.xikolo.controller.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;

public class MobileDownloadDialog extends DialogFragment {

    public static final String TAG = MobileDownloadDialog.class.getSimpleName();

    private MobileDownloadDialogListener listener;

    public void setMobileDownloadDialogListener(MobileDownloadDialogListener listener) {
        this.listener = listener;
    }

    public MobileDownloadDialog() {
    }

    public static MobileDownloadDialog getInstance() {
        return new MobileDownloadDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setMessage(R.string.dialog_mobile_download_message)
                .setTitle(R.string.dialog_mobile_download_title)
                .setPositiveButton(R.string.dialog_mobile_download_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDialogPositiveClick(MobileDownloadDialog.this);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MobileDownloadDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    public interface MobileDownloadDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

}
