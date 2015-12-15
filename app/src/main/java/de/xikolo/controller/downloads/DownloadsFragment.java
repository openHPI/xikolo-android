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

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.dialogs.ConfirmDeleteDialog;
import de.xikolo.controller.downloads.adapter.DownloadsAdapter;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.data.preferences.AppPreferences;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.PermissionsModel;
import de.xikolo.model.events.PermissionDeniedEvent;
import de.xikolo.model.events.PermissionGrantedEvent;
import de.xikolo.util.FileUtil;
import de.xikolo.util.ToastUtil;

public class DownloadsFragment extends Fragment implements DownloadsAdapter.OnDeleteButtonClickedListener {

    public static final String TAG = DownloadsFragment.class.getSimpleName();

    private ListView listView;
    private DownloadsAdapter adapter;

    private DownloadModel downloadModel;

    private PermissionsModel permissionsModel;

    private NotificationController mNotificationController;

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

        downloadModel = new DownloadModel(GlobalApplication.getInstance().getJobManager(), getActivity());
        permissionsModel = new PermissionsModel(GlobalApplication.getInstance().getJobManager(), getActivity());
        adapter = new DownloadsAdapter(getActivity(), this);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_downloads, container, false);

        listView = (ListView) layout.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        mNotificationController = new NotificationController(layout);
        mNotificationController.setInvisible();

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchItems();
    }

    public void onEvent(PermissionGrantedEvent permissionGrantedEvent) {
        if (permissionGrantedEvent.getRequestCode() == PermissionsModel.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            fetchItems();
        }
    }

    public void onEvent(PermissionDeniedEvent permissionDeniedEvent) {
        if (permissionDeniedEvent.getRequestCode() == PermissionsModel.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            fetchItems();
        }
    }

    private void fetchItems() {
        List<DownloadsAdapter.Item> items = new ArrayList<DownloadsAdapter.Item>();
        if (permissionsModel.requestPermission(PermissionsModel.WRITE_EXTERNAL_STORAGE) == 1) {
            mNotificationController.setInvisible();

            items.add(new DownloadsAdapter.SectionItem(getString(R.string.overall)));
            DownloadsAdapter.FolderItem total = new DownloadsAdapter.FolderItem(downloadModel.getAppFolder().substring(downloadModel.getAppFolder().lastIndexOf(File.separator) + 1),
                    downloadModel.getAppFolder());
            items.add(total);

            List<String> folders = downloadModel.getFoldersWithDownloads();
            if (folders.size() > 0) {
                items.add(new DownloadsAdapter.SectionItem(getString(R.string.courses)));
                for (String folder : folders) {
                    DownloadsAdapter.FolderItem item = new DownloadsAdapter.FolderItem(folder.substring(folder.lastIndexOf(File.separator) + 1, folder.lastIndexOf("_")),
                            folder);
                    items.add(item);
                }
            }
        } else {
            mNotificationController.setTitle(R.string.dialog_title_permissions);
            mNotificationController.setSummary(R.string.dialog_permissions);
            mNotificationController.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionsModel.startAppInfo(getActivity());
                }
            });
            mNotificationController.setNotificationVisible(true);
        }
        adapter.updateItems(items);
    }

    @Override
    public void onDeleteButtonClicked(final DownloadsAdapter.FolderItem item) {
        final AppPreferences appPreferences = GlobalApplication.getInstance().getPreferencesFactory().getAppPreferences();

        if (appPreferences.confirmBeforeDeleting()) {
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.getInstance(true);
            dialog.setConfirmDeleteDialogListener(new ConfirmDeleteDialog.ConfirmDeleteDialogListener() {
                @Override
                public void onDialogPositiveClick(DialogFragment dialog) {
                    deleteFolder(item);
                }

                @Override
                public void onDialogPositiveAndAlwaysClick(DialogFragment dialog) {
                    appPreferences.setConfirmBeforeDeleting(false);
                    deleteFolder(item);
                }
            });
            dialog.show(getActivity().getSupportFragmentManager(), ConfirmDeleteDialog.TAG);
        } else {
            deleteFolder(item);
        }
    }

    private void deleteFolder(DownloadsAdapter.FolderItem item) {
        File dir = new File(item.getPath());

        if (dir.exists()) {
            FileUtil.delete(dir);
        } else {
            ToastUtil.show(getActivity(), R.string.error);
        }

        fetchItems();
    }

}
