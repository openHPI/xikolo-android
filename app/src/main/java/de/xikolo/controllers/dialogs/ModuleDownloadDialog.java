package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.xikolo.R;

public class ModuleDownloadDialog extends DialogFragment {

    public static final String TAG = ModuleDownloadDialog.class.getSimpleName();

    private ModuleDownloadDialogListener listener;

    private boolean hdVideo;

    private boolean sdVideo;

    private boolean slides;

    private String moduleTitle;

    public static final String KEY_MODULE_TITLE = "module_title";

    public static final String KEY_HD_VIDEO = "hd_video";
    public static final String KEY_SD_VIDEO = "sd_video";
    public static final String KEY_SLIDES = "slides";

    public static ModuleDownloadDialog getInstance(String moduleTitle) {
        ModuleDownloadDialog fragment = new ModuleDownloadDialog();
        Bundle args = new Bundle();
        args.putString(KEY_MODULE_TITLE, moduleTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public ModuleDownloadDialog() {

    }

    public void setModuleDownloadDialogListener(ModuleDownloadDialogListener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            moduleTitle = getArguments().getString(KEY_MODULE_TITLE);
        }
        if (savedInstanceState != null) {
            hdVideo = savedInstanceState.getBoolean(KEY_HD_VIDEO);
            sdVideo = savedInstanceState.getBoolean(KEY_SD_VIDEO);
            slides = savedInstanceState.getBoolean(KEY_SLIDES);
        } else {
            hdVideo = false;
            sdVideo = false;
            slides = false;
        }

        String title = String.format(getActivity().getString(R.string.dialog_module_downloads_title), moduleTitle);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title)
                .setMultiChoiceItems(R.array.dialog_module_downloads_array, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
                            case 0:
                                hdVideo = isChecked;
                                break;
                            case 1:
                                sdVideo = isChecked;
                                break;
                            case 2:
                                slides = isChecked;
                                break;
                        }
                    }
                })
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDialogPositiveClick(ModuleDownloadDialog.this, hdVideo, sdVideo, slides);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ModuleDownloadDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HD_VIDEO, hdVideo);
        outState.putBoolean(KEY_SD_VIDEO, sdVideo);
        outState.putBoolean(KEY_SLIDES, slides);
    }

    public interface ModuleDownloadDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, boolean hdVideo, boolean sdVideo, boolean slides);
    }

}
