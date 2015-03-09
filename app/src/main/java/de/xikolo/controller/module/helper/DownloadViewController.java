package de.xikolo.controller.module.helper;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Download;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.DownloadCompletedEvent;
import de.xikolo.util.FileSizeUtil;
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

    public DownloadViewController(final DownloadModel.DownloadFileType type, final Course course, final Module module, final Item<VideoItemDetail> item) {
        this.type = type;
        this.course = course;
        this.module = module;
        this.item = item;

        this.downloadModel = new DownloadModel(GlobalApplication.getInstance(), GlobalApplication.getInstance().getJobManager());

        LayoutInflater inflater = LayoutInflater.from(GlobalApplication.getInstance());
        view = inflater.inflate(R.layout.container_download, null);

        fileSizeText = (TextView) view.findViewById(R.id.textFileSize);
        fileNameText = (TextView) view.findViewById(R.id.textFileName);

        downloadStartContainer = view.findViewById(R.id.downloadStartContainer);
        downloadStartButton = (IconButton) view.findViewById(R.id.buttonDownloadStart);
        downloadStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadModel.startDownload(uri,
                        DownloadViewController.this.type,
                        DownloadViewController.this.course,
                        DownloadViewController.this.module,
                        DownloadViewController.this.item);

                showRunningState();
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
                downloadModel.cancelDownload(
                        DownloadViewController.this.type,
                        DownloadViewController.this.course,
                        DownloadViewController.this.module,
                        DownloadViewController.this.item);

                showStartState();
            }
        });

        switch (type) {
            case SLIDES:
                uri = item.detail.slides_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.slides_as_pdf));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                break;
            case TRANSCRIPT:
                uri = item.detail.transcript_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.transcript_as_pdf));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                break;
            case VIDEO_HD:
                uri = item.detail.stream.hd_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.video_hd_as_mp4));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                break;
            case VIDEO_SD:
                uri = item.detail.stream.sd_url;
                fileNameText.setText(GlobalApplication.getInstance().getText(R.string.video_sd_as_mp4));
                downloadStartButton.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
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
                                fileSizeText.setText(FileSizeUtil.getFormattedFileSize(dl.bytesDownloadedSoFar) + " / "
                                        + FileSizeUtil.getFormattedFileSize(dl.totalSizeBytes));
                            }
                        }
                    });
                }

                if (progressBarUpdaterRunning) {
                    downloadProgress.postDelayed(this, MILLISECONDS);
                } else {
                    fileSizeText.setText(FileSizeUtil.getFormattedFileSize(downloadModel.getDownloadFileSize(type, course, module, item)));
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
                    fileSizeText.setText(FileSizeUtil.getFormattedFileSize(result));
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

        fileSizeText.setText(FileSizeUtil.getFormattedFileSize(downloadModel.getDownloadFileSize(type, course, module, item)));

        progressBarUpdaterRunning = false;
    }

    public void onEventMainThread(DownloadCompletedEvent event) {
        if (event.getDownload().localUri.contains(item.id)
                && DownloadModel.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri) == type) {
//            String suffix = DownloadModel.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri).getFileSuffix();
            showEndState();
        }
    }

}
