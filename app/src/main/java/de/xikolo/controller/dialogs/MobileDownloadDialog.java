package de.xikolo.controller.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import de.xikolo.R;
import de.xikolo.util.DisplayUtil;

public class MobileDownloadDialog extends DialogFragment {

    public static final String TAG = MobileDownloadDialog.class.getSimpleName();

    private MobileDownloadDialogListener mListener;

    public void setMobileDownloadDialogListener(MobileDownloadDialogListener listener) {
        mListener = listener;
    }

    public MobileDownloadDialog() {
    }

    public static MobileDownloadDialog getInstance() {
        MobileDownloadDialog fragment = new MobileDownloadDialog();
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_mobile_download_message)
                .setTitle(R.string.dialog_mobile_download_title)
                .setPositiveButton(R.string.dialog_mobile_download_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onDialogPositiveClick(MobileDownloadDialog.this);
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
        public void onDialogPositiveClick(DialogFragment dialog);
    }

}
