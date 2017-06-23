package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import de.xikolo.App;
import de.xikolo.R;

public class ProgressDialog extends DialogFragment {

    public static final String TAG = ProgressDialog.class.getSimpleName();

    public static final String ARG_TITLE = "multiple_title";
    public static final String ARG_MESSAGE = "multiple_message";

    private String title;

    private String message;

    public ProgressDialog() {
    }

    public static ProgressDialog getInstance() {
        return getInstance(App.getInstance().getString(R.string.dialog_progress_title),
                App.getInstance().getString(R.string.dialog_progress_message));
    }

    public static ProgressDialog getInstance(String title, String message) {
        ProgressDialog fragment = new ProgressDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            message = getArguments().getString(ARG_MESSAGE);
        }

        android.app.ProgressDialog dialog = new android.app.ProgressDialog(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        setCancelable(false);

        return dialog;
    }

}
