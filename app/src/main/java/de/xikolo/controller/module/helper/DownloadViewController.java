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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

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

    private String uri;

    private Runnable progressBarUpdater;
    private boolean progressBarUpdaterRunning = false;

    @SuppressWarnings("SetTextI18n")
    public DownloadViewController(final FragmentActivity activity, final DownloadModel.DownloadFileType type, final Course course, final Module module, final Item<VideoItemDetail> item) {
        this.type = type;
        this.course = course;
        this.module = module;
        this.item = item;

        this.downloadModel = new DownloadModel(GlobalApplication.getInstance().getJobManager(), activity);

        LayoutInflater inflater = LayoutInflater.from(GlobalApplication.getInstance());
        layout = inflater.inflate(R.layout.container_download, null);

        textFileSize = (TextView) layout.findViewById(R.id.textFileSize);
        textFileName = (TextView) layout.findViewById(R.id.textFileName);

        final AppPreferences appPreferences = GlobalApplication.getInstance().getPreferencesFactory().getAppPreferences();

        viewDownloadStart = layout.findViewById(R.id.downloadStartContainer);
        buttonDownloadStart = (IconButton) layout.findViewById(R.id.buttonDownloadStart);
        buttonDownloadStart.setOnClickListener(new View.OnClickListener() {
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

        viewDownloadRunning = layout.findViewById(R.id.downloadRunningContainer);
        progressBarDownload = (ProgressBar) layout.findViewById(R.id.progressDownload);
        buttonDownloadCancel = (TextView) layout.findViewById(R.id.buttonDownloadCancel);
        buttonDownloadCancel.setOnClickListener(new View.OnClickListener() {
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
                uri = item.detail.slides_url;
                textFileName.setText(GlobalApplication.getInstance().getText(R.string.slides_as_pdf));
                buttonDownloadStart.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                openFileAsPdf();
                break;
            case TRANSCRIPT:
                uri = item.detail.transcript_url;
                textFileName.setText(GlobalApplication.getInstance().getText(R.string.transcript_as_pdf));
                buttonDownloadStart.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                openFileAsPdf();
                break;
            case VIDEO_HD:
                uri = item.detail.stream.hd_url;
                textFileName.setText(GlobalApplication.getInstance().getText(R.string.video_hd_as_mp4));
                buttonDownloadStart.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                buttonOpenDownload.setVisibility(View.GONE);
                break;
            case VIDEO_SD:
                uri = item.detail.stream.sd_url;
                textFileName.setText(GlobalApplication.getInstance().getText(R.string.video_sd_as_mp4));
                buttonDownloadStart.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                buttonOpenDownload.setVisibility(View.GONE);
                break;
        }

        if (uri == null) {
            layout.setVisibility(View.GONE);
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
                                progressBarDownload.setIndeterminate(false);
                                progressBarDownload.setProgress((int) (dl.bytesDownloadedSoFar * 100 / dl.totalSizeBytes));
                                textFileSize.setText(FileUtil.getFormattedFileSize(dl.bytesDownloadedSoFar) + " / "
                                        + FileUtil.getFormattedFileSize(dl.totalSizeBytes));
                            }
                        }
                    });
                }

                if (progressBarUpdaterRunning) {
                    progressBarDownload.postDelayed(this, MILLISECONDS);
                } else {
                    textFileSize.setText(FileUtil.getFormattedFileSize(downloadModel.getDownloadFileSize(type, course, module, item)));
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

        if (uri != null) {
            downloadModel.getRemoteDownloadFileSize(new Result<Long>() {
                @Override
                protected void onSuccess(Long result, DataSource dataSource) {
                    if (result > 0) {
                        textFileSize.setText(FileUtil.getFormattedFileSize(result));
                    } else {
                        textFileSize.setText("--");
                    }
                }
            }, uri);
        }

        progressBarDownload.setProgress(0);
        progressBarDownload.setIndeterminate(true);
        progressBarUpdaterRunning = false;
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

        textFileSize.setText(FileUtil.getFormattedFileSize(downloadModel.getDownloadFileSize(type, course, module, item)));

        progressBarUpdaterRunning = false;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadCompletedEvent(DownloadCompletedEvent event) {
        if (event.getDownload().localUri.contains(item.id)
                && DownloadModel.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri) == type) {
            showEndState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadStartedEvent(DownloadStartedEvent event) {
        if (event.getUrl().equals(uri) && !progressBarUpdaterRunning) {
            showRunningState();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadDeletedEvent(DownloadDeletedEvent event) {
        if (event.getItem().id.equals(item.id) && progressBarUpdaterRunning) {
            showStartState();
        }
    }

    private void openFileAsPdf() {
        buttonOpenDownload.setText(GlobalApplication.getInstance().getResources().getText(R.string.open));
        buttonOpenDownload.setOnClickListener(new View.OnClickListener() {
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
