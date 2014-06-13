package de.xikolo.controller.fragments.dialog;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import de.xikolo.R;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public class UnenrollDialog {

    public static void show(Context context, FragmentManager fragmentManager, Fragment targetFragment) {
        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(context, fragmentManager);
        builder.setTitle(R.string.dialog_unenroll_title);
        builder.setMessage(R.string.dialog_unenroll_message);
        builder.setPositiveButtonText(R.string.dialog_unenroll_yes);
        builder.setNegativeButtonText(R.string.dialog_unenroll_no);
        builder.setCancelable(true);
        builder.setCancelableOnTouchOutside(true);
        builder.setTargetFragment(targetFragment, 0);

        builder.show();
    }

}
