package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.base.BaseDialogFragment;

public class ServerErrorDialog extends BaseDialogFragment {

    public static final String TAG = ServerErrorDialog.class.getSimpleName();

    private ServerErrorDialogListener listener;

    public void setDialogListener(ServerErrorDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.dialog_server_error_title))
                .setMessage(getString(R.string.dialog_server_error_message))
                .setNegativeButton(getString(R.string.dialog_server_error_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDismissed();
                        }
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (listener != null) {
            listener.onDismissed();
        }
    }

    public interface ServerErrorDialogListener {
        void onDismissed();
    }

}
