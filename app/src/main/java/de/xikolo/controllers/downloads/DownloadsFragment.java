package de.xikolo.controllers.downloads;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog;
import de.xikolo.controllers.helper.LoadingStateHelper;
import de.xikolo.events.PermissionDeniedEvent;
import de.xikolo.events.PermissionGrantedEvent;
import de.xikolo.managers.DownloadManager;
import de.xikolo.managers.PermissionManager;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.FileUtil;
import de.xikolo.utils.ToastUtil;

public class DownloadsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, DownloadsAdapter.OnDeleteButtonClickedListener {

    public static final String TAG = DownloadsFragment.class.getSimpleName();

    private DownloadsAdapter adapter;

    private DownloadManager downloadManager;

    private PermissionManager permissionManager;

    private LoadingStateHelper notificationController;

    public DownloadsFragment() {
        // Required empty public constructor
    }

    public static DownloadsFragment newInstance() {
        return new DownloadsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadManager = new DownloadManager(getActivity());
        permissionManager = new PermissionManager(getActivity());
        adapter = new DownloadsAdapter(this);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_downloads, container, false);

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.content_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        notificationController = new LoadingStateHelper(getActivity(), layout, this);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchItems();
    }

    @Override
    public void onRefresh() {
        fetchItems();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPermissionGrantedEvent(PermissionGrantedEvent permissionGrantedEvent) {
        if (permissionGrantedEvent.getRequestCode() == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            fetchItems();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPermissionDeniedEvent(PermissionDeniedEvent permissionDeniedEvent) {
        if (permissionDeniedEvent.getRequestCode() == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            fetchItems();
        }
    }

    private void fetchItems() {
        adapter.clear();
        if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
            notificationController.showContentView();

            List<DownloadsAdapter.FolderItem> list = new ArrayList<>();

            String appFolder = FileUtil.createPublicAppFolderPath();

            DownloadsAdapter.FolderItem total = new DownloadsAdapter.FolderItem(
                    appFolder.substring(appFolder.lastIndexOf(File.separator) + 1),
                    appFolder);
            list.add(total);

            adapter.addItem(getString(R.string.overall), list);

            List<String> folders = downloadManager.getFoldersWithDownloads();
            if (folders.size() > 0) {
                list = new ArrayList<>();
                for (String folder : folders) {
                    String name;
                    try {
                        name = folder.substring(folder.lastIndexOf(File.separator) + 1, folder.lastIndexOf("_"));
                    } catch (Exception e) {
                        name = folder;
                    }
                    DownloadsAdapter.FolderItem item = new DownloadsAdapter.FolderItem(name, folder);
                    list.add(item);
                }
                adapter.addItem(getString(R.string.courses), list);
            }
        } else {
            notificationController.setMessageTitle(R.string.dialog_title_permissions);
            notificationController.setMessageSummary(R.string.dialog_permissions);
            notificationController.setMessageOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionManager.startAppInfo(getActivity());
                }
            });
            notificationController.showMessage();
        }
    }

    @Override
    public void onDeleteButtonClicked(final DownloadsAdapter.FolderItem item) {
        final ApplicationPreferences appPreferences = new ApplicationPreferences();

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
            ToastUtil.show(R.string.error);
        }

        fetchItems();
    }

}
