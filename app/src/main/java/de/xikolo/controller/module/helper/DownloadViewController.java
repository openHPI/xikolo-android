package de.xikolo.controller.module.helper;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.dialogs.ConfirmDeleteDialog;
import de.xikolo.controller.dialogs.MobileDownloadDialog;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Download;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.preferences.AppPreferences;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.DownloadCompletedEvent;
import de.xikolo.model.events.DownloadDeletedEvent;
import de.xikolo.model.events.DownloadStartedEvent;
import de.xikolo.util.FileUtil;
import de.xikolo.util.NetworkUtil;
import de.xikolo.view.IconButton;

public class DownloadViewController {

    public static final String TAG = DownloadViewController.class.getSimpleName();
    private static final int MILLISECONDS = 250;


    private DownloadModel.DownloadFileType type;

    private Course course;
    private Module module;
    private Item<VideoItemDetail> item;

    private DownloadModel downloadModel;

    private View view;
    private TextView fileNameText;
    private TextView fileSizeText;
    private View downloadStartContainer;
    private IconButton downloadStartButton;
    private View downloadRunningContainer;
    private TextView downloadCancelButton;
    private ProgressBar downloadProgress;
    private View downloadEndContainer;
    private Button downloadOpenButton;
    private Button downloadDeleteButton;

    private String uri;

    private Runnable progressBarUpdater;
    private boolean progressBarUpdaterRunning = false;

    private FragmentActivity activity;

    public DownloadViewController(final FragmentActivity activity, final DownloadModel.DownloadFileType type, final Course course, final Module module, final Item<VideoItemDetail> item) {
        this.type = type;
        this.course = course;
        this.module = module;
        this.item = item;
        this.activity = activity;

        this.downloadModel = new DownloadModel(GlobalApplication.getInstance().getJobManager(), activity);

        LayoutInflater inflater = LayoutInflater.from(GlobalApplication.getInstance());
        view = inflater.inflate(R.layout.container_download, null);

        fileSizeText = (TextView) view.findViewById(R.id.textFileSize);
        fileNameText = (TextView) view.findViewById(R.id.textFileName);

        final AppPreferences appPreferences = GlobalApplication.getInstance().getPreferencesFactory().getAppPreferences();

        downloadStartContainer = view.findViewById(R.id.downloadStartContainer);
        downloadStartButton = (IconButton) view.findViewById(R.id.buttonDownloadStart);
        downloadStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                    if (NetworkUtil.getConnectivityStatus(activity) == NetworkUtil.TYPE_MOBILE &&
                            appPreferences.isDownloadNetworkLimitedOnMobile()) {
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

        downloadRunningContainer = view.findViewById(R.id.downloadRunningContainer);
        downloadProgress = (ProgressBar) view.findViewById(R.id.progressDownload);
        downloadCancelButton = (TextView) view.findViewById(R.id.buttonDownloadCancel);
        downloadCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadModel.cancelDownload(
                        DownloadViewController.this.type,
                        DownloadViewController.this.course,
                        DownloadViewController.this.module,
                        DownloadViewController.this.item);

                showStartState();
            }
        });

        downloadEndContainer = view.findViewById(R.id.downloadEndContainer);
        downloadOpenButton = (Button) view.findViewById(R.id.buttonDownloadOpen);
        downloadDeleteButton = (Button) view.findViewById(R.id.buttonDownloadDelete);
        downloadDeleteButton.setOnClickListener(new View.OnClickListener() {
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
                uri = item.detail.slides_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.slides_as_pdf));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                openFileAsPdf();
                break;
            case TRANSCRIPT:
                uri = item.detail.transcript_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.transcript_as_pdf));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                openFileAsPdf();
                break;
            case VIDEO_HD:
                uri = item.detail.stream.hd_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.video_hd_as_mp4));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                downloadOpenButton.setVisibility(View.GONE);
                break;
            case VIDEO_SD:
                uri = item.detail.stream.sd_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.video_sd_as_mp4));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                downloadOpenButton.setVisibility(View.GONE);
                break;
        }

        if (uri == null) {
            view.setVisibility(View.GONE);
        }

        EventBus.getDefault().register(this);

        progressBarUpdater = new Runnable() {
            @Override
            public void run() {
                final Download dl = downloadModel.getDownload(type, course, module, item);

                if (dl != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBarUpdaterRunning) {
                                downloadProgress.setIndeterminate(false);
                                downloadProgress.setProgress((int) (dl.bytesDownloadedSoFar * 100 / dl.totalSizeBytes));
                                fileSizeText.setText(FileUtil.getFormattedFileSize(dl.bytesDownloadedSoFar) + " / "
                                        + FileUtil.getFormattedFileSize(dl.totalSizeBytes));
                            }
                        }
                    });
                }

                if (progressBarUpdaterRunning) {
                    downloadProgress.postDelayed(this, MILLISECONDS);
                } else {
                    fileSizeText.setText(FileUtil.getFormattedFileSize(downloadModel.getDownloadFileSize(type, course, module, item)));
                }
            }
        };

        if (downloadModel.downloadRunning(type, course, module, item)) {
            showRunningState();
        } else if (downloadModel.downloadExists(type, course, module, item)) {
            showEndState();
        } else {
            showStartState();
        }

    }

    private void deleteFile() {
        if (downloadModel.cancelDownload(
                DownloadViewController.this.type,
                DownloadViewController.this.course,
                DownloadViewController.this.module,
                DownloadViewController.this.item)) {
            showStartState();
        }
    }

    private void startDownload() {
        long status = downloadModel.startDownload(uri,
                DownloadViewController.this.type,
                DownloadViewController.this.course,
                DownloadViewController.this.module,
                DownloadViewController.this.item);
        if (status != 0) {
            showRunningState();
        }
    }

    public View getView() {
        return view;
    }

    private void showStartState() {
        if (downloadStartContainer != null) {
            downloadStartContainer.setVisibility(View.VISIBLE);
        }
        if (downloadRunningContainer != null) {
            downloadRunningContainer.setVisibility(View.INVISIBLE);
        }
        if (downloadEndContainer != null) {
            downloadEndContainer.setVisibility(View.INVISIBLE);
        }

        if (uri != null) {
            downloadModel.getRemoteDownloadFileSize(new Result<Long>() {
                @Override
                protected void onSuccess(Long result, DataSource dataSource) {
                    fileSizeText.setText(FileUtil.getFormattedFileSize(result));
                }
            }, uri);
        }

        downloadProgress.setProgress(0);
        downloadProgress.setIndeterminate(true);
        progressBarUpdaterRunning = false;
    }

    private void showRunningState() {
        if (downloadStartContainer != null) {
            downloadStartContainer.setVisibility(View.INVISIBLE);
        }
        if (downloadRunningContainer != null) {
            downloadRunningContainer.setVisibility(View.VISIBLE);
        }
        if (downloadEndContainer != null) {
            downloadEndContainer.setVisibility(View.INVISIBLE);
        }

        progressBarUpdaterRunning = true;
        new Thread(progressBarUpdater).start();
    }

    private void showEndState() {
        if (downloadStartContainer != null) {
            downloadStartContainer.setVisibility(View.INVISIBLE);
        }
        if (downloadRunningContainer != null) {
            downloadRunningContainer.setVisibility(View.INVISIBLE);
        }
        if (downloadEndContainer != null) {
            downloadEndContainer.setVisibility(View.VISIBLE);
        }

        fileSizeText.setText(FileUtil.getFormattedFileSize(downloadModel.getDownloadFileSize(type, course, module, item)));

        progressBarUpdaterRunning = false;
    }

    public void onEventMainThread(DownloadCompletedEvent event) {
        if (event.getDownload().localUri.contains(item.id)
                && DownloadModel.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri) == type) {
//            String suffix = DownloadModel.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri).getFileSuffix();
            showEndState();
        }
    }

    public void onEventMainThread(DownloadStartedEvent event) {
        if (event.getUrl().equals(uri) && !progressBarUpdaterRunning) {
            showRunningState();
        }
    }

    public void onEventMainThread(DownloadDeletedEvent event) {
        if (event.getItem().id.equals(item.id) && progressBarUpdaterRunning) {
            showStartState();
        }
    }

    public void openFileAsPdf() {
        downloadOpenButton.setText(GlobalApplication.getInstance().getResources().getText(R.string.open));
        downloadOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File pdf = downloadModel.getDownloadFile(type, course, module, item);
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(Uri.fromFile(pdf), "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                Intent intent = Intent.createChooser(target, null);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    GlobalApplication.getInstance().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }
            }
        });
    }

}
