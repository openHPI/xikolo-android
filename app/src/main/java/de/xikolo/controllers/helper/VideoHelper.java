package de.xikolo.controllers.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.managers.DownloadManager;
import de.xikolo.models.Course;
import de.xikolo.models.DownloadAsset;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.PlaybackSpeedUtil;
import de.xikolo.views.CustomFontTextView;
import de.xikolo.views.CustomSizeVideoView;
import de.xikolo.views.ExoPlayerVideoView;

public class VideoHelper {

    public static final String TAG = VideoHelper.class.getSimpleName();

    private static final int MILLISECONDS = 100;

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;

    private static final int VIDEO_STEPPING_DURATION = 10000;

    @BindView(R.id.videoView) CustomSizeVideoView videoView;
    @BindView(R.id.videoController) View videoController;
    @BindView(R.id.videoProgress) View videoProgress;
    @BindView(R.id.videoSeekBar) SeekBar seekBar;

    @BindView(R.id.btnPlay) CustomFontTextView buttonPlay;
    @BindView(R.id.btnStepForward) CustomFontTextView buttonStepForward;
    @BindView(R.id.btnStepBackward) CustomFontTextView buttonStepBackward;
    @BindView(R.id.btnRetry) TextView buttonRetry;

    @BindView(R.id.currentTime) TextView textCurrentTime;
    @BindView(R.id.totalTime) TextView textTotalTime;

    @BindView(R.id.hdSwitch) CustomFontTextView textHdSwitch;
    @BindView(R.id.playbackSpeed) TextView textPlaybackSpeed;

    @BindView(R.id.offlineHint) View viewOfflineHint;
    @BindView(R.id.videoWarning) View viewVideoWarning;
    @BindView(R.id.videoWarningText) TextView textVideoWarning;

    private DownloadManager downloadManager;

    private Activity activity;

    private View videoContainer;

    private ControllerListener controllerListener;

    private Runnable seekBarUpdater;

    private Handler handler = new MessageHandler(this);

    private boolean seekBarUpdaterIsRunning = false;

    private boolean userIsSeeking = false;

    private boolean isPlaying = true;

    private PlaybackSpeedUtil currentPlaybackSpeed = PlaybackSpeedUtil.x10;

    private Course course;
    private Section module;
    private Item item;
    private Video video;

    private enum VideoMode {
        SD, HD
    }

    private VideoMode videoMode;

    public VideoHelper(FragmentActivity activity, View videoContainer) {
        this.activity = activity;
        this.videoContainer = videoContainer;

        ButterKnife.bind(this, activity);

        this.videoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show();
                return false;
            }
        });

        downloadManager = new DownloadManager(activity);

        setupView();
    }

    private void setupView() {
        videoView.setKeepScreenOn(true);
        videoView.setOnPreparedListener(new ExoPlayerVideoView.OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoProgress.setVisibility(View.GONE);
                viewVideoWarning.setVisibility(View.GONE);
                seekBar.setMax(getDuration());
                show();

                textTotalTime.setText(getTimeString(getDuration()));
                textCurrentTime.setText(getTimeString(0));

                buttonStepForward.setVisibility(View.VISIBLE);
                buttonStepBackward.setVisibility(View.VISIBLE);

                seekTo(video.progress);

                setPlaybackSpeed(currentPlaybackSpeed);

                isPlaying = true;
                play();

                new Thread(seekBarUpdater).start();
            }
        });
        videoView.setOnBufferUpdateListener(new ExoPlayerVideoView.OnBufferUpdateListener() {
            @Override
            public void onBufferingUpdate(@IntRange(from = 0L, to = 100L) int percent) {
                seekBar.setSecondaryProgress((int) (seekBar.getMax() * (percent / 100.)));
            }
        });
        videoView.setOnCompletionListener(new ExoPlayerVideoView.OnCompletionListener() {
            @Override
            public void onCompletion() {
                pause();
                buttonPlay.setText(activity.getString(R.string.icon_reload));
                buttonStepForward.setVisibility(View.GONE);
                buttonStepBackward.setVisibility(View.GONE);
                show();
            }
        });
        videoView.setOnErrorListener(new ExoPlayerVideoView.OnErrorListener() {
            @Override
            public boolean onError(Exception e) {
                saveCurrentPosition();
                viewVideoWarning.setVisibility(View.VISIBLE);
                textVideoWarning.setText(activity.getString(R.string.error_plain));

                return true;
            }
        });

        seekBarUpdater = new Runnable() {
            @Override
            public void run() {
                seekBarUpdaterIsRunning = true;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!userIsSeeking) {
                            seekBar.setProgress(getCurrentPosition());
                            textCurrentTime.setText(getTimeString(getCurrentPosition()));
                        }
                    }
                });
                if (getCurrentPosition() < getDuration()) {
                    seekBar.postDelayed(this, MILLISECONDS);
                } else {
                    seekBarUpdaterIsRunning = false;
                }
            }
        };

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                if (videoView.isPlaying()) {
                    pause();

                    LanalyticsUtil.trackVideoPause(item.id,
                        course.id, module.id,
                        getCurrentPosition(),
                        currentPlaybackSpeed.getSpeed(),
                        activity.getResources().getConfiguration().orientation,
                        getQualityString(),
                        getSourceString());
                } else {
                    if (getCurrentPosition() >= getDuration()) {
                        // 'replay' button was pressed
                        seekTo(0);
                        buttonStepForward.setVisibility(View.VISIBLE);
                        buttonStepBackward.setVisibility(View.VISIBLE);
                    }

                    play();

                    LanalyticsUtil.trackVideoPlay(item.id,
                        course.id, module.id,
                        getCurrentPosition(),
                        currentPlaybackSpeed.getSpeed(),
                        activity.getResources().getConfiguration().orientation,
                        getQualityString(),
                        getSourceString());
                }
            }
        });

        buttonStepForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                stepForward();
            }
        });

        buttonStepBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                stepBackward();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    show();
                    this.progress = progress;
                    textCurrentTime.setText(getTimeString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LanalyticsUtil.trackVideoSeek(item.id,
                    course.id, module.id,
                    getCurrentPosition(),
                    progress,
                    currentPlaybackSpeed.getSpeed(),
                    activity.getResources().getConfiguration().orientation,
                    getQualityString(),
                    getSourceString());

                userIsSeeking = false;
                seekTo(progress);
            }
        });

        textHdSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldQuality = getQualityString();
                String oldSource = getSourceString();
                int position = getCurrentPosition();

                toggleHdButton();

                LanalyticsUtil.trackVideoChangeQuality(item.id,
                    course.id, module.id,
                    position,
                    currentPlaybackSpeed.getSpeed(),
                    activity.getResources().getConfiguration().orientation,
                    oldQuality,
                    getQualityString(),
                    oldSource,
                    getSourceString());
            }
        });

        textPlaybackSpeed.setVisibility(View.VISIBLE);
        textPlaybackSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackSpeedUtil oldSpeed = currentPlaybackSpeed;

                togglePlaybackSpeed();

                LanalyticsUtil.trackVideoChangeSpeed(item.id,
                    course.id, module.id,
                    getCurrentPosition(),
                    oldSpeed.getSpeed(),
                    currentPlaybackSpeed.getSpeed(),
                    activity.getResources().getConfiguration().orientation,
                    getQualityString(),
                    getSourceString());
            }
        });

        buttonRetry.setOnClickListener(v -> updateVideo(item, video));
    }

    public void play() {
        buttonPlay.setText(activity.getString(R.string.icon_pause));
        videoView.start();
        isPlaying = true;
        if (!seekBarUpdaterIsRunning) {
            new Thread(seekBarUpdater).start();
        }
    }

    public void pause() {
        buttonPlay.setText(activity.getString(R.string.icon_play));
        videoView.pause();
        isPlaying = false;
        saveCurrentPosition();
    }

    public void release() {
        pause();
        videoView.release();
    }

    public void seekTo(int progress) {
        videoView.seekTo(progress);
        textCurrentTime.setText(getTimeString(progress));
        seekBar.setProgress(progress);
        if (!seekBarUpdaterIsRunning) {
            new Thread(seekBarUpdater).start();
        }
    }

    public void stepForward() {
        seekTo(
            Math.min(
                getCurrentPosition() + VIDEO_STEPPING_DURATION,
                getDuration()
            )
        );
    }

    public void stepBackward() {
        seekTo(
            Math.max(
                getCurrentPosition() - VIDEO_STEPPING_DURATION,
                0)
        );
    }

    @TargetApi(23)
    public void togglePlaybackSpeed() {
        switch (currentPlaybackSpeed) {
            case x07:
                setPlaybackSpeed(PlaybackSpeedUtil.x10);
                break;
            case x10:
                setPlaybackSpeed(PlaybackSpeedUtil.x13);
                break;
            case x13:
                setPlaybackSpeed(PlaybackSpeedUtil.x15);
                break;
            case x15:
                setPlaybackSpeed(PlaybackSpeedUtil.x18);
                break;
            case x18:
                setPlaybackSpeed(PlaybackSpeedUtil.x20);
                break;
            case x20:
                setPlaybackSpeed(PlaybackSpeedUtil.x07);
                break;
        }
    }

    @TargetApi(23)
    public void setPlaybackSpeed(PlaybackSpeedUtil speed) {
        try {
            videoView.setPlaybackSpeed(speed.getSpeed());

            currentPlaybackSpeed = speed;
            textPlaybackSpeed.setText(speed.toString());

            if (!isPlaying) {
                pause();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void show(int timeout) {
        videoController.setVisibility(View.VISIBLE);
        if (controllerListener != null) {
            controllerListener.onControllerShow();
        }
        Message msg = handler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            handler.removeMessages(FADE_OUT);
            handler.sendMessageDelayed(msg, timeout);
        }
    }

    public void hide() {
        if (videoView.isPlaying()) {
            videoController.setVisibility(View.GONE);
            if (controllerListener != null) {
                controllerListener.onControllerHide();
            }
        } else {
            show();
        }
    }

    private void toggleHdButton() {
        if (videoMode == VideoMode.HD) {
            videoMode = VideoMode.SD;
        } else {
            videoMode = VideoMode.HD;
        }

        saveCurrentPosition();
        updateVideo(item, video);
    }

    public PlaybackSpeedUtil getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }

    public int getCurrentPosition() {
        return (int) videoView.getCurrentPosition();
    }

    public int getDuration() {
        return (int) videoView.getDuration();
    }

    public void setupVideo(Course course, Section module, Item item, Video video) {
        this.course = course;
        this.module = module;
        this.item = item;
        this.video = video;

        int connectivityStatus = NetworkUtil.getConnectivityStatus();
        ApplicationPreferences appPreferences = new ApplicationPreferences();

        if (videoDownloadPresent(new DownloadAsset.Course.Item.VideoHD(item, video))) { // hd video download available
            videoMode = VideoMode.HD;
        } else if (videoDownloadPresent(new DownloadAsset.Course.Item.VideoSD(item, video))) { // sd video download available
            videoMode = VideoMode.SD;
        } else if (connectivityStatus == NetworkUtil.TYPE_WIFI || !appPreferences.isVideoQualityLimitedOnMobile()) {
            videoMode = VideoMode.HD;
        } else {
            videoMode = VideoMode.SD;
        }

        currentPlaybackSpeed = appPreferences.getVideoPlaybackSpeed();

        updateVideo(item, video);
    }

    private void updateVideo(Item item, Video video) {
        String stream;
        DownloadAsset.Course.Item videoAssetDownload;

        viewVideoWarning.setVisibility(View.GONE);

        if (videoMode == VideoMode.HD) {
            stream = video.singleStream.hdUrl;
            videoAssetDownload = new DownloadAsset.Course.Item.VideoHD(item, video);
        } else {
            stream = video.singleStream.sdUrl;
            videoAssetDownload = new DownloadAsset.Course.Item.VideoSD(item, video);
        }

        viewOfflineHint.setVisibility(View.GONE);

        if (videoDownloadPresent(videoAssetDownload)) {
            setLocalVideoUri(videoAssetDownload);
        } else if (NetworkUtil.isOnline()) {
            setVideoUri(stream);
        } else if (videoMode == VideoMode.HD) {
            videoMode = VideoMode.SD;
            updateVideo(item, video);
        } else {
            viewVideoWarning.setVisibility(View.VISIBLE);
            textVideoWarning.setText(activity.getString(R.string.video_notification_no_offline_video));
        }

        updateHdSwitchColor();
    }

    private boolean videoDownloadPresent(DownloadAsset.Course.Item item) {
        return !downloadManager.downloadRunning(item)
            && downloadManager.downloadExists(item);
    }

    private void setLocalVideoUri(DownloadAsset.Course.Item item) {
        setVideoUri("file://" + downloadManager.getDownloadFile(item));
        viewOfflineHint.setVisibility(View.VISIBLE);
    }

    private void setVideoUri(String uri) {
        if (Config.DEBUG) {
            Log.i(TAG, "Video HOST_URL: " + uri);
        }
        videoView.setVideoURI(Uri.parse(uri));
    }

    private void updateHdSwitchColor() {
        if (videoMode == VideoMode.HD) {
            textHdSwitch.setTextColor(ContextCompat.getColor(activity, R.color.video_hd_enabled));
        } else {
            textHdSwitch.setTextColor(ContextCompat.getColor(activity, R.color.video_hd_disabled));
        }
    }

    public void saveCurrentPosition() {
        saveCurrentPosition(getCurrentPosition());
    }

    public void saveCurrentPosition(int position) {
        if (video != null) {
            video.progress = position;
        }
    }

    public View getControllerView() {
        return videoController;
    }

    public View getVideoContainer() {
        return videoContainer;
    }

    private String getTimeString(int millis) {
        return String.format(Locale.US, "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public String getSourceString() {
        if (viewOfflineHint != null) {
            return viewOfflineHint.getVisibility() == View.VISIBLE ? "offline" : "online";
        } else return null;
    }

    public String getQualityString() {
        return videoMode.name().toLowerCase();
    }

    public void setControllerListener(ControllerListener listener) {
        this.controllerListener = listener;
    }

    public interface ControllerListener {

        void onControllerShow();

        void onControllerHide();

    }

    private static class MessageHandler extends Handler {
        VideoHelper mController;

        MessageHandler(VideoHelper controller) {
            mController = controller;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    mController.hide();
                    break;
            }
        }
    }

}
