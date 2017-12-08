package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.yatatsu.autobundle.AutoBundleField;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.base.BaseDialogFragment;

public class ApiVersionDeprecatedDialog extends BaseDialogFragment {

    public static final String TAG = ApiVersionDeprecatedDialog.class.getSimpleName();

    private ApiVersionDeprecatedDialogListener listener;

    @AutoBundleField Date deprecationDate;

    public void setDialogListener(ApiVersionDeprecatedDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.dialog_api_deprecation_title, getString(R.string.app_name)))
                .setMessage(getString(R.string.dialog_api_deprecation_message, getString(R.string.app_name), df.format(deprecationDate)))
                .setPositiveButton(getString(R.string.dialog_api_deprecation_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onOpenPlayStoreClicked();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_api_deprecation_no), new DialogInterface.OnClickListener() {
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

    public interface ApiVersionDeprecatedDialogListener {
        void onOpenPlayStoreClicked();
        void onDismissed();
    }

}
