package de.xikolo.controller.downloads;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.dialogs.ConfirmDeleteDialog;
import de.xikolo.controller.downloads.adapter.DownlodsAdapter;
import de.xikolo.data.preferences.AppPreferences;
import de.xikolo.model.DownloadModel;
import de.xikolo.util.FileUtil;
import de.xikolo.util.ToastUtil;

public class DownloadsFragment extends Fragment implements DownlodsAdapter.OnDeleteButtonClickedListener {

    public static final String TAG = DownloadsFragment.class.getSimpleName();

    private ListView listView;
    private DownlodsAdapter adapter;

    private DownloadModel downloadModel;

    public DownloadsFragment() {
        // Required empty public constructor
    }

    public static DownloadsFragment newInstance() {
        DownloadsFragment fragment = new DownloadsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadModel = new DownloadModel(getActivity(), GlobalApplication.getInstance().getJobManager());
        adapter = new DownlodsAdapter(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_downloads, container, false);

        listView = (ListView) layout.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchItems();
    }

    private void fetchItems() {
        List<DownlodsAdapter.Item> items = new ArrayList<DownlodsAdapter.Item>();

        items.add(new DownlodsAdapter.SectionItem(getString(R.string.overall)));
        DownlodsAdapter.FolderItem total = new DownlodsAdapter.FolderItem(downloadModel.getAppFolder().substring(downloadModel.getAppFolder().lastIndexOf(File.separator) + 1),
                downloadModel.getAppFolder());
        items.add(total);

        List<String> folders = downloadModel.getFoldersWithDownloads();
        if (folders.size() > 0) {
            items.add(new DownlodsAdapter.SectionItem(getString(R.string.courses)));
            for (String folder : folders) {
                DownlodsAdapter.FolderItem item = new DownlodsAdapter.FolderItem(folder.substring(folder.lastIndexOf(File.separator) + 1, folder.lastIndexOf("_")),
                        folder);
                items.add(item);
            }
        }

        adapter.updateItems(items);
    }

    @Override
    public void onDeleteButtonClicked(final DownlodsAdapter.FolderItem item) {
        if (AppPreferences.confirmBeforeDeleting(GlobalApplication.getInstance())) {
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.getInstance(true);
            dialog.setConfirmDeleteDialogListener(new ConfirmDeleteDialog.ConfirmDeleteDialogListener() {
                @Override
                public void onDialogPositiveClick(DialogFragment dialog) {
                    deleteFolder(item);
                }

                @Override
                public void onDialogPositiveAndAlwaysClick(DialogFragment dialog) {
                    AppPreferences.setConfirmBeforeDeleting(getActivity(), false);
                    deleteFolder(item);
                }
            });
            dialog.show(getActivity().getSupportFragmentManager(), ConfirmDeleteDialog.TAG);
        } else {
            deleteFolder(item);
        }
    }

    private void deleteFolder(DownlodsAdapter.FolderItem item) {
        File dir = new File(item.getPath());

        if (dir.exists()) {
            FileUtil.delete(dir);
        } else {
            ToastUtil.show(getActivity(), R.string.error);
        }

        fetchItems();
    }

}
