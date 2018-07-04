package de.xikolo.controllers.course_items;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog;
import de.xikolo.controllers.dialogs.MobileDownloadDialog;
import de.xikolo.events.AllDownloadsCancelledEvent;
import de.xikolo.events.DownloadCompletedEvent;
import de.xikolo.events.DownloadDeletedEvent;
import de.xikolo.events.DownloadStartedEvent;
import de.xikolo.managers.DownloadManager;
import de.xikolo.models.Download;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.DownloadUtil;
import de.xikolo.utils.FileProviderUtil;
import de.xikolo.utils.FileUtil;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;
import de.xikolo.views.IconButton;

import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.SLIDES;
import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.TRANSCRIPT;
import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.VIDEO_HD;
import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.VIDEO_SD;
import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.VideoAssetType;

public class DownloadViewController {

    public static final String TAG = DownloadViewController.class.getSimpleName();
    private static final int MILLISECONDS = 250;

    private FragmentActivity activity;

    private DownloadManager downloadManager;

    private DownloadUtil.AssetDownload download;

    private View layout;
    private TextView textFileName;
    private TextView textFileSize;
    private View viewDownloadStart;
    private IconButton buttonDownloadStart;
    private View viewDownloadRunning;
    private TextView buttonDownloadCancel;
    private ProgressBar progressBarDownload;
    private View viewDownloadEnd;
    private Button buttonOpenDownload;
    private Button buttonDeleteDownload;

    private Runnable progressBarUpdater;
    private boolean progressBarUpdaterRunning = false;

    private String url;
    private int size;

    @SuppressWarnings("SetTextI18n")
    public DownloadViewController(FragmentActivity a, DownloadUtil.AssetDownload download) {
        activity = a;

        this.downloadManager = new DownloadManager(a);

        this.download = download;

        LayoutInflater inflater = LayoutInflater.from(App.getInstance());
        layout = inflater.inflate(R.layout.container_download, null);

        textFileSize = layout.findViewById(R.id.textFileSize);
        textFileName = layout.findViewById(R.id.textFileName);

        final ApplicationPreferences appPreferences = new ApplicationPreferences();

        viewDownloadStart = layout.findViewById(R.id.downloadStartContainer);
        buttonDownloadStart = layout.findViewById(R.id.buttonDownloadStart);
        buttonDownloadStart.setOnClickListener(v -> {
            if (NetworkUtil.isOnline()) {
                if (NetworkUtil.getConnectivityStatus() == NetworkUtil.TYPE_MOBILE && appPreferences.isDownloadNetworkLimitedOnMobile()) {
                    MobileDownloadDialog dialog = MobileDownloadDialog.getInstance();
                    dialog.setMobileDownloadDialogListener(dialog1 -> {
                        appPreferences.setDownloadNetworkLimitedOnMobile(false);
                        startDownload();
                    });
                    dialog.show(activity.getSupportFragmentManager(), MobileDownloadDialog.TAG);
                } else {
                    startDownload();
                }
            } else {
                NetworkUtil.showNoConnectionToast();
            }
        });

        viewDownloadRunning = layout.findViewById(R.id.downloadRunningContainer);
        progressBarDownload = layout.findViewById(R.id.progressDownload);
        buttonDownloadCancel = layout.findViewById(R.id.buttonDownloadCancel);
        buttonDownloadCancel.setOnClickListener(v -> {
            downloadManager.cancelItemAssetDownload(download);

            showStartState();
        });

        viewDownloadEnd = layout.findViewById(R.id.downloadEndContainer);
        buttonOpenDownload = layout.findViewById(R.id.buttonDownloadOpen);
        buttonDeleteDownload = layout.findViewById(R.id.buttonDownloadDelete);
        buttonDeleteDownload.setOnClickListener(v -> {
            if (appPreferences.getConfirmBeforeDeleting()) {
                ConfirmDeleteDialog dialog = ConfirmDeleteDialog.getInstance(false);
                dialog.setConfirmDeleteDialogListener(new ConfirmDeleteDialog.ConfirmDeleteDialogListener() {
                    @Override
                    public void onDialogPositiveClick(DialogFragment dialog) {
                        deleteFile();
                    }

                    @Override
                    public void onDialogPositiveAndAlwaysClick(DialogFragment dialog) {
                        appPreferences.setConfirmBeforeDeleting(false);
                        deleteFile();
                    }
                });
                dialog.show(activity.getSupportFragmentManager(), ConfirmDeleteDialog.TAG);
            } else {
                deleteFile();
            }
        });

        if(download.getAssetType() instanceof DownloadUtil.AssetType.CourseAssetType.ItemAssetType.VideoAssetType) {
            Video video = ((VideoAssetType) download.getAssetType()).getVideo();
            switch (download.getAssetType().getType()) {
                case SLIDES:
                    url = video.slidesUrl;
                    size = video.slidesSize;
                    textFileName.setText(App.getInstance().getText(R.string.slides_as_pdf));
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_pdf));
                    openFileAsPdf();
                    break;
                case TRANSCRIPT:
                    url = video.transcriptUrl;
                    size = video.transcriptSize;
                    textFileName.setText(App.getInstance().getText(R.string.transcript_as_pdf));
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_pdf));
                    openFileAsPdf();
                    break;
                case VIDEO_HD:
                    url = video.singleStream.hdUrl;
                    size = video.singleStream.hdSize;
                    textFileName.setText(App.getInstance().getText(R.string.video_hd_as_mp4));
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_video));
                    buttonOpenDownload.setVisibility(View.GONE);
                    break;
                case VIDEO_SD:
                    url = video.singleStream.sdUrl;
                    size = video.singleStream.sdSize;
                    textFileName.setText(App.getInstance().getText(R.string.video_sd_as_mp4));
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_video));
                    buttonOpenDownload.setVisibility(View.GONE);
                    break;
            }
        }

        if (url == null) {
            layout.setVisibility(View.GONE);
        }

        EventBus.getDefault().register(this);

        progressBarUpdater = new Runnable() {
            @Override
            public void run() {
                final Download dl = downloadManager.getDownload(download);

                if (dl != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (progressBarUpdaterRunning) {
                            progressBarDownload.setIndeterminate(false);
                            if (dl.totalBytes == 0) {
                                progressBarDownload.setProgress(0);
                            } else {
                                progressBarDownload.setProgress((int) (dl.bytesWritten * 100 / dl.totalBytes));
                            }
                            textFileSize.setText(FileUtil.getFormattedFileSize(dl.bytesWritten) + " / "
                                    + FileUtil.getFormattedFileSize(dl.totalBytes));
                        }
                    });
                }

                if (progressBarUpdaterRunning) {
                    progressBarDownload.postDelayed(this, MILLISECONDS);
                }
            }
        };

        if (downloadManager.downloadRunning(download)) {
            showRunningState();
        } else if (downloadManager.downloadExists(download)) {
            showEndState();
        } else {
            showStartState();
        }

    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    private void deleteFile() {
        if (downloadManager.deleteItemAssetDownload(download)) {
            showStartState();
        }
    }

    private void startDownload() {
        if (downloadManager.startAssetDownload(download)) {
            showRunningState();
        }
    }

    public View getLayout() {
        return layout;
    }

    private void showStartState() {
        if (viewDownloadStart != null) {
            viewDownloadStart.setVisibility(View.VISIBLE);
        }
        if (viewDownloadRunning != null) {
            viewDownloadRunning.setVisibility(View.INVISIBLE);
        }
        if (viewDownloadEnd != null) {
            viewDownloadEnd.setVisibility(View.INVISIBLE);
        }

        progressBarDownload.setProgress(0);
        progressBarDownload.setIndeterminate(true);
        progressBarUpdaterRunning = false;

        textFileSize.setText(FileUtil.getFormattedFileSize(size));
    }

    private void showRunningState() {
        if (viewDownloadStart != null) {
            viewDownloadStart.setVisibility(View.INVISIBLE);
        }
        if (viewDownloadRunning != null) {
            viewDownloadRunning.setVisibility(View.VISIBLE);
        }
        if (viewDownloadEnd != null) {
            viewDownloadEnd.setVisibility(View.INVISIBLE);
        }

        progressBarUpdaterRunning = true;
        new Thread(progressBarUpdater).start();
    }

    private void showEndState() {
        if (viewDownloadStart != null) {
            viewDownloadStart.setVisibility(View.INVISIBLE);
        }
        if (viewDownloadRunning != null) {
            viewDownloadRunning.setVisibility(View.INVISIBLE);
        }
        if (viewDownloadEnd != null) {
            viewDownloadEnd.setVisibility(View.VISIBLE);
        }

        textFileSize.setText(FileUtil.getFormattedFileSize(size));

        progressBarUpdaterRunning = false;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadCompletedEvent(DownloadCompletedEvent event) {
        if (event.url.equals(url)) {
            showEndState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadStartedEvent(DownloadStartedEvent event) {
        if (event.getDownload().equals(download) && !progressBarUpdaterRunning) {
            showRunningState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadDeletedEvent(DownloadDeletedEvent event) {
        if (event.getDownload().equals(download) && progressBarUpdaterRunning) {
            showStartState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAllDownloadCancelledEvent(AllDownloadsCancelledEvent event) {
        if (progressBarUpdaterRunning) {
            showStartState();
        }
    }

    private void openFileAsPdf() {
        buttonOpenDownload.setText(App.getInstance().getResources().getText(R.string.open));
        buttonOpenDownload.setOnClickListener(v -> {
            File pdf = downloadManager.getDownloadFile(download);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(FileProviderUtil.getUriForFile(pdf), "application/pdf");
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, null);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                App.getInstance().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                ToastUtil.show(R.string.toast_no_pdf_viewer_found);
            }
        });
    }

}
