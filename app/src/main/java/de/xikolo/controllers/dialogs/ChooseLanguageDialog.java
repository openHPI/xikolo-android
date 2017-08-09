package de.xikolo.controllers.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.base.BaseDialogFragment;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.utils.LanguageUtil;

public class ChooseLanguageDialog extends BaseDialogFragment {

    public static final String TAG = ChooseLanguageDialog.class.getSimpleName();

    @AutoBundleField String videoId;

    private ChooseLanguageDialogListener listener;

    public void setMobileDownloadDialogListener(ChooseLanguageDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = null;

        List<SubtitleTrack> subtitles = SubtitleTrack.listForVideoId(videoId);

        if (subtitles.size() > 0) {
            items = new CharSequence[subtitles.size()];
            for (int i = 0; i < subtitles.size(); i++) {
                items[i] = LanguageUtil.languageForCode(getContext(), subtitles.get(i).language);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(R.string.action_language)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogItemClick(which);
                        ChooseLanguageDialog.this.getDialog().cancel();
                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    public interface ChooseLanguageDialogListener {
        void onDialogItemClick(int position);
    }

}
