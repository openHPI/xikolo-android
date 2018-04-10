package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.base.BaseDialogFragment;

public class BirthdayVoucherDialog extends BaseDialogFragment {

    public static final String TAG = BirthdayVoucherDialog.class.getSimpleName();

    private Listener listener;

    public void setDialogListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.dialog_birthday_voucher_title))
                .setMessage(getString(R.string.dialog_birthday_voucher_message))
                .setPositiveButton(getString(R.string.dialog_birthday_voucher_ok), (dialog, which) -> {
                    if (listener != null) {
                        listener.onAccepted();
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
            listener.onAccepted();
        }
    }

    public interface Listener {
        void onAccepted();
    }

}
