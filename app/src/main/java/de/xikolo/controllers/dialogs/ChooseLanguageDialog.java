//package de.xikolo.controllers.dialogs;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.DialogFragment;
//import android.support.v7.app.AlertDialog;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.xikolo.R;
//import de.xikolo.models.SubtitleTrack;
//import de.xikolo.utils.LanguageUtil;
//
//public class ChooseLanguageDialog extends DialogFragment {
//
//    public static final String TAG = ChooseLanguageDialog.class.getSimpleName();
//
//    private ChooseLanguageDialogListener listener;
//
//    private List<SubtitleTrack> subtitleList;
//
//    public static final String ARG_SUBTITLES = "arg_subtitles";
//
//    public void setMobileDownloadDialogListener(ChooseLanguageDialogListener listener) {
//        this.listener = listener;
//    }
//
//    public ChooseLanguageDialog() {
//    }
//
//    public static ChooseLanguageDialog getInstance(List<SubtitleTrack> subtitleList) {
//        ChooseLanguageDialog fragment = new ChooseLanguageDialog();
//        Bundle args = new Bundle();
//        args.putParcelableArrayList(ARG_SUBTITLES, (ArrayList<SubtitleTrack>) subtitleList);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        CharSequence[] items = null;
//
//        if (getArguments() != null) {
//            subtitleList = getArguments().getParcelableArrayList(ARG_SUBTITLES);
//        }
//
//        if (subtitleList != null) {
//            items = new CharSequence[subtitleList.size()];
//            for (int i = 0; i < subtitleList.size(); i++) {
//                items[i] = LanguageUtil.languageForCode(getContext(), subtitleList.get(i).language());
//            }
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
//        builder.setMessageTitle(R.string.action_language)
//                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        listener.onDialogItemClick(which);
//                        ChooseLanguageDialog.this.getDialog().cancel();
//                    }
//                })
//                .setCancelable(true);
//
//        AlertDialog dialog = builder.create();
//        dialog.setCanceledOnTouchOutside(true);
//
//        return dialog;
//    }
//
//    public interface ChooseLanguageDialogListener {
//        void onDialogItemClick(int position);
//    }
//
//}
