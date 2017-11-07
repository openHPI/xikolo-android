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
import de.xikolo.events.DownloadCompletedEvent;
import de.xikolo.events.DownloadDeletedEvent;
import de.xikolo.events.DownloadStartedEvent;
import de.xikolo.managers.DownloadManager;
import de.xikolo.models.Course;
import de.xikolo.models.Download;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.DownloadUtil;
import de.xikolo.utils.FileProviderUtil;
import de.xikolo.utils.FileUtil;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.views.IconButton;

public class DownloadViewController {

    public static final String TAG = DownloadViewController.class.getSimpleName();
    private static final int MILLISECONDS = 250;

    private DownloadUtil.VideoAssetType type;

    private FragmentActivity activity;

    private Course course;
    private Section section;
    private Item item;
    private Video video;

    private DownloadManager downloadManager;

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
    public DownloadViewController(FragmentActivity a, DownloadUtil.VideoAssetType t, Course c, final Section s, Item i, Video v) {
        activity = a;
        type = t;
        course = c;
        section = s;
        item = i;
        video = v;

        this.downloadManager = new DownloadManager(a);

        LayoutInflater inflater = LayoutInflater.from(App.getInstance());
        layout = inflater.inflate(R.layout.container_download, null);

        textFileSize = (TextView) layout.findViewById(R.id.textFileSize);
        textFileName = (TextView) layout.findViewById(R.id.textFileName);

        final ApplicationPreferences appPreferences = new ApplicationPreferences();

        viewDownloadStart = layout.findViewById(R.id.downloadStartContainer);
        buttonDownloadStart = (IconButton) layout.findViewById(R.id.buttonDownloadStart);
        buttonDownloadStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isOnline()) {
                    if (NetworkUtil.getConnectivityStatus() == NetworkUtil.TYPE_MOBILE && appPreferences.isDownloadNetworkLimitedOnMobile()) {
                        MobileDownloadDialog dialog = MobileDownloadDialog.getInstance();
                        dialog.setMobileDownloadDialogListener(new MobileDownloadDialog.MobileDownloadDialogListener() {
                            @Override
                            public void onDialogPositiveClick(DialogFragment dialog) {
                                appPreferences.setIsDownloadNetworkLimitedOnMobile(false);
                                startDownload();
                            }
                        });
                        dialog.show(activity.getSupportFragmentManager(), MobileDownloadDialog.TAG);
                    } else {
                        startDownload();
                    }
                } else {
                    NetworkUtil.showNoConnectionToast();
                }
            }
        });

        viewDownloadRunning = layout.findViewById(R.id.downloadRunningContainer);
        progressBarDownload = (ProgressBar) layout.findViewById(R.id.progressDownload);
        buttonDownloadCancel = (TextView) layout.findViewById(R.id.buttonDownloadCancel);
        buttonDownloadCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.cancelItemAssetDownload(
                        DownloadViewController.this.item.id,
                        DownloadViewController.this.type
                );

                showStartState();
            }
        });

        viewDownloadEnd = layout.findViewById(R.id.downloadEndContainer);
        buttonOpenDownload = (Button) layout.findViewById(R.id.buttonDownloadOpen);
        buttonDeleteDownload = (Button) layout.findViewById(R.id.buttonDownloadDelete);
        buttonDeleteDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appPreferences.confirmBeforeDeleting()) {
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
            }
        });

        switch (type) {
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

        if (url == null) {
            layout.setVisibility(View.GONE);
        }

        EventBus.getDefault().register(this);

        progressBarUpdater = new Runnable() {
            @Override
            public void run() {
                final Download dl = downloadManager.getDownload(item.id, type);

                if (dl != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });
                }

                if (progressBarUpdaterRunning) {
                    progressBarDownload.postDelayed(this, MILLISECONDS);
                }
            }
        };

        if (downloadManager.downloadRunning(item.id, type)) {
            showRunningState();
        } else if (downloadManager.downloadExists(item.id, type)) {
            showEndState();
        } else {
            showStartState();
        }

    }

    private void deleteFile() {
        downloadManager.cancelItemAssetDownload(
                DownloadViewController.this.item.id,
                DownloadViewController.this.type
        );
        showStartState();
    }

    private void startDownload() {
        downloadManager.startItemAssetDownload(
                DownloadViewController.this.item.id,
                DownloadViewController.this.type
        );
        showRunningState();
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
        if (event.itemId.equals(item.id) && event.type == type) {
            showEndState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadStartedEvent(DownloadStartedEvent event) {
        if (event.itemId.equals(item.id) && event.type == type && !progressBarUpdaterRunning) {
            showRunningState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadDeletedEvent(DownloadDeletedEvent event) {
        if (event.itemId.equals(item.id) && event.type == type && progressBarUpdaterRunning) {
            showStartState();
        }
    }

    private void openFileAsPdf() {
        buttonOpenDownload.setText(App.getInstance().getResources().getText(R.string.open));
        buttonOpenDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File pdf = downloadManager.getDownloadFile(item.id, type);
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(FileProviderUtil.getUriForFile(pdf), "application/pdf");
                target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent intent = Intent.createChooser(target, null);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    App.getInstance().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }
            }
        });
    }

}
