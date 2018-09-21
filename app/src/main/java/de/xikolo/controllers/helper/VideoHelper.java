package de.xikolo.controllers.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import de.xikolo.models.VideoSubtitles;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.PlaybackSpeedUtil;
import de.xikolo.views.CustomFontTextView;
import de.xikolo.views.CustomSizeVideoView;

public class VideoHelper {

    public static final String TAG = VideoHelper.class.getSimpleName();

    private static final int MILLISECONDS = 100;

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;

    private static final int VIDEO_STEPPING_DURATION = 10000;

    @BindView(R.id.videoView) CustomSizeVideoView videoView;
    @BindView(R.id.videoController) View videoController;
    @BindView(R.id.videoControls) View videoControls;
    @BindView(R.id.videoProgress) View videoProgress;
    @BindView(R.id.videoSeekBar) SeekBar seekBar;
    @BindView(R.id.videoOverlay) View videoOverlay;

    @BindView(R.id.settingsContainer) LinearLayout settingsContainer;
    @BindView(R.id.buttonSettings) TextView settingsButton;

    @BindView(R.id.btnPlay) CustomFontTextView buttonPlay;
    @BindView(R.id.btnStepForward) CustomFontTextView buttonStepForward;
    @BindView(R.id.btnStepBackward) CustomFontTextView buttonStepBackward;
    @BindView(R.id.btnRetry) TextView buttonRetry;

    @BindView(R.id.currentTime) TextView textCurrentTime;
    @BindView(R.id.totalTime) TextView textTotalTime;

    @BindView(R.id.offlineHint) View viewOfflineHint;
    @BindView(R.id.videoWarning) View viewVideoWarning;
    @BindView(R.id.videoWarningText) TextView textVideoWarning;

    private DownloadManager downloadManager;

    private Activity activity;

    private ApplicationPreferences applicationPreferences = new ApplicationPreferences();

    private View videoContainer;

    private ControllerListener controllerListener;

    private Runnable seekBarUpdater;

    private Handler handler = new MessageHandler(this);

    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private boolean seekBarUpdaterIsRunning = false;

    private boolean userIsSeeking = false;

    private Course course;
    private Section module;
    private Item item;
    private Video video;

    private VideoSettingsHelper videoSettingsHelper;
    private boolean settingsOpen = false;

    public VideoHelper(FragmentActivity activity, View videoContainer) {
        this.activity = activity;
        this.videoContainer = videoContainer;

        ButterKnife.bind(this, activity);

        this.videoContainer.setOnTouchListener((v, event) -> {
            if (settingsOpen) {
                hideSettings();
            }
            show();
            v.performClick();
            return false;
        });

        downloadManager = new DownloadManager(activity);

        setupView();
    }

    private void setupView() {
        videoView.setKeepScreenOn(true);
        videoView.setOnPreparedListener(() -> {
            hideProgress();
            viewVideoWarning.setVisibility(View.GONE);
            seekBar.setMax(getDuration());
            show();

            textTotalTime.setText(getTimeString(getDuration()));
            textCurrentTime.setText(getTimeString(0));

            buttonStepForward.setVisibility(View.VISIBLE);
            buttonStepBackward.setVisibility(View.VISIBLE);

            seekTo(video.progress);

            videoView.setPlaybackSpeed(videoSettingsHelper.getCurrentSpeed().getSpeed());

            play();

            new Thread(seekBarUpdater).start();
        });

        videoView.setOnBufferUpdateListener(percent -> seekBar.setSecondaryProgress((int) (seekBar.getMax() * (percent / 100.))));

        videoView.setOnCompletionListener(() -> {
            pause();
            buttonPlay.setText(activity.getString(R.string.icon_reload));
            buttonStepForward.setVisibility(View.GONE);
            buttonStepBackward.setVisibility(View.GONE);
            show();
        });

        videoView.setOnErrorListener(e -> {
            saveCurrentPosition();
            viewVideoWarning.setVisibility(View.VISIBLE);
            textVideoWarning.setText(activity.getString(R.string.error_plain));

            return true;
        });

        seekBarUpdater = new Runnable() {
            @Override
            public void run() {
                seekBarUpdaterIsRunning = true;
                activity.runOnUiThread(() -> {
                    if (!userIsSeeking) {
                        seekBar.setProgress(getCurrentPosition());
                        textCurrentTime.setText(getTimeString(getCurrentPosition()));
                    }
                });
                if (getCurrentPosition() < getDuration()) {
                    seekBar.postDelayed(this, MILLISECONDS);
                } else {
                    seekBarUpdaterIsRunning = false;
                }
            }
        };

        buttonPlay.setOnClickListener(v -> {
            show();
            if (videoView.isPlaying()) {
                pause();

                LanalyticsUtil.trackVideoPause(item.id,
                    course.id, module.id,
                    getCurrentPosition(),
                    videoSettingsHelper.getCurrentSpeed().getSpeed(),
                    activity.getResources().getConfiguration().orientation,
                    getCurrentQualityString(),
                    getSourceString());
            } else {
                if (getCurrentPosition() >= getDuration()) {
                    // 'replay' button was pressed
                    buttonStepForward.setVisibility(View.VISIBLE);
                    buttonStepBackward.setVisibility(View.VISIBLE);
                    seekTo(0);
                }

                play();

                LanalyticsUtil.trackVideoPlay(item.id,
                    course.id, module.id,
                    getCurrentPosition(),
                    videoSettingsHelper.getCurrentSpeed().getSpeed(),
                    activity.getResources().getConfiguration().orientation,
                    getCurrentQualityString(),
                    getSourceString());
            }
        });

        settingsButton.setOnClickListener(v -> showSettings(videoSettingsHelper.buildSettingsView()));

        buttonStepForward.setOnClickListener(v -> {
            show();
            stepForward();
        });

        buttonStepBackward.setOnClickListener(v -> {
            show();
            stepBackward();
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
                    videoSettingsHelper.getCurrentSpeed().getSpeed(),
                    activity.getResources().getConfiguration().orientation,
                    getCurrentQualityString(),
                    getSourceString());

                userIsSeeking = false;
                seekTo(progress);
            }
        });

        buttonRetry.setOnClickListener(v -> updateVideo());

        bottomSheetBehavior = BottomSheetBehavior.from(settingsContainer);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        settingsOpen = false;
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        videoControls.setVisibility(View.GONE);
                        settingsOpen = true;
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                slideOffset = (slideOffset + 1) / 2f; // 0 if HIDDEN, 1 if EXPANDED
                videoOverlay.setAlpha(slideOffset * 0.75f);
                videoControls.setAlpha(1 - slideOffset);
            }
        });
    }

    public PlaybackSpeedUtil getCurrentPlaybackSpeed() {
        return videoSettingsHelper.getCurrentSpeed();
    }

    public int getCurrentPosition() {
        return (int) videoView.getCurrentPosition();
    }

    public int getDuration() {
        return (int) videoView.getDuration();
    }

    public void play() {
        buttonPlay.setText(activity.getString(R.string.icon_pause));
        videoView.start();
        if (!seekBarUpdaterIsRunning) {
            new Thread(seekBarUpdater).start();
        }
    }

    public void pause() {
        buttonPlay.setText(activity.getString(R.string.icon_play));
        videoView.pause();
        saveCurrentPosition();
    }

    private void seekTo(int progress) {
        videoView.seekTo(progress);
        textCurrentTime.setText(getTimeString(progress));
        seekBar.setProgress(progress);
        if (!seekBarUpdaterIsRunning) {
            new Thread(seekBarUpdater).start();
        }
    }

    private void stepForward() {
        seekTo(
            Math.min(
                getCurrentPosition() + VIDEO_STEPPING_DURATION,
                getDuration()
            )
        );
    }

    private void stepBackward() {
        seekTo(
            Math.max(
                getCurrentPosition() - VIDEO_STEPPING_DURATION,
                0)
        );
    }

    private void showSettings(View view) {
        settingsContainer.removeAllViews();
        settingsContainer.addView(view);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideSettings() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void showProgress() {
        videoProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        videoProgress.setVisibility(View.GONE);
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void show(int timeout) {
        videoControls.setVisibility(View.VISIBLE);
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
            videoControls.setVisibility(View.GONE);
            if (controllerListener != null) {
                controllerListener.onControllerHide();
            }
        } else {
            show();
        }
    }

    public boolean handleBackPress() {
        if (settingsOpen) {
            hideSettings();
            return false;
        }
        pause();
        videoView.release();
        return true;
    }

    public void setupVideo(Course course, Section module, Item item, Video video) {
        this.course = course;
        this.module = module;
        this.item = item;
        this.video = video;

        this.videoSettingsHelper = new VideoSettingsHelper(
            activity,
            video.subtitles,
            new VideoSettingsHelper.OnSettingsChangeListener() {
                @Override
                public void onSubtitleChanged(@Nullable VideoSubtitles old, @Nullable VideoSubtitles videoSubtitles) {
                    if (old != videoSubtitles) {
                        saveCurrentPosition();
                        showProgress();
                        updateVideo();
                    }

                    hideSettings();
                }

                @Override
                public void onQualityChanged(@NotNull VideoSettingsHelper.VideoMode old, @NotNull VideoSettingsHelper.VideoMode videoMode) {
                    if (old != videoMode) {
                        String oldSourceString = getSourceString();

                        saveCurrentPosition();
                        showProgress();
                        updateVideo();

                        LanalyticsUtil.trackVideoChangeQuality(item.id,
                            course.id, module.id,
                            getCurrentPosition(),
                            videoSettingsHelper.getCurrentSpeed().getSpeed(),
                            activity.getResources().getConfiguration().orientation,
                            getQualityString(old),
                            getCurrentQualityString(),
                            oldSourceString,
                            getSourceString());
                    }

                    hideSettings();
                }

                @Override
                public void onPlaybackSpeedChanged(@NotNull PlaybackSpeedUtil old, @NotNull PlaybackSpeedUtil speed) {
                    if (old != speed) {
                        videoView.setPlaybackSpeed(speed.getSpeed());

                        applicationPreferences.setVideoPlaybackSpeed(speed);

                        LanalyticsUtil.trackVideoChangeSpeed(item.id,
                            course.id, module.id,
                            getCurrentPosition(),
                            old.getSpeed(),
                            speed.getSpeed(),
                            activity.getResources().getConfiguration().orientation,
                            getCurrentQualityString(),
                            getSourceString());
                    }

                    hideSettings();
                }
            },
            new VideoSettingsHelper.OnSettingsClickListener() {
                @Override
                public void onSubtitleClick() {
                    showSettings(videoSettingsHelper.buildSubtitleView());
                }

                @Override
                public void onPlaybackSpeedClick() {
                    showSettings(videoSettingsHelper.buildPlaybackSpeedView());
                }

                @Override
                public void onQualityClick() {
                    showSettings(videoSettingsHelper.buildQualityView());
                }
            }
        );

        int connectivityStatus = NetworkUtil.getConnectivityStatus();

        if (videoDownloadPresent(new DownloadAsset.Course.Item.VideoHD(item, video))) { // hd video download available
            videoSettingsHelper.setCurrentQuality(VideoSettingsHelper.VideoMode.HD);
        } else if (videoDownloadPresent(new DownloadAsset.Course.Item.VideoSD(item, video))) { // sd video download available
            videoSettingsHelper.setCurrentQuality(VideoSettingsHelper.VideoMode.SD);
        } else if (connectivityStatus == NetworkUtil.TYPE_WIFI || !applicationPreferences.isVideoQualityLimitedOnMobile()) {
            videoSettingsHelper.setCurrentQuality(VideoSettingsHelper.VideoMode.HD);
        } else {
            videoSettingsHelper.setCurrentQuality(VideoSettingsHelper.VideoMode.SD);
        }

        videoSettingsHelper.setCurrentSpeed(applicationPreferences.getVideoPlaybackSpeed());

        VideoSubtitles videoSubtitles = null;
        for (VideoSubtitles v : video.subtitles) {
            if (v.language.equals(applicationPreferences.getVideoSubtitlesLanguage())) {
                videoSubtitles = v;
            }
        }
        videoSettingsHelper.setCurrentVideoSubtitles(videoSubtitles);

        updateVideo();
    }

    private void updateVideo() {
        viewVideoWarning.setVisibility(View.GONE);
        viewOfflineHint.setVisibility(View.GONE);

        String stream;
        DownloadAsset.Course.Item videoAssetDownload;

        if (videoSettingsHelper.getCurrentQuality() == VideoSettingsHelper.VideoMode.HD) {
            stream = video.singleStream.hdUrl;
            videoAssetDownload = new DownloadAsset.Course.Item.VideoHD(item, video);
        } else {
            stream = video.singleStream.sdUrl;
            videoAssetDownload = new DownloadAsset.Course.Item.VideoSD(item, video);
        }

        if (videoDownloadPresent(videoAssetDownload)) {
            setLocalVideoUri(videoAssetDownload);
        } else if (NetworkUtil.isOnline()) {
            setVideoUri(stream);
        } else if (videoSettingsHelper.getCurrentQuality() == VideoSettingsHelper.VideoMode.HD) {
            videoSettingsHelper.setCurrentQuality(VideoSettingsHelper.VideoMode.SD);
            updateVideo();
        } else {
            viewVideoWarning.setVisibility(View.VISIBLE);
            textVideoWarning.setText(activity.getString(R.string.video_notification_no_offline_video));
        }

        VideoSubtitles currentSubtitles = videoSettingsHelper.getCurrentVideoSubtitles();
        if (currentSubtitles != null) {
            videoView.showSubtitles(currentSubtitles.vttUrl, currentSubtitles.language);
            applicationPreferences.setVideoSubtitlesLanguage(currentSubtitles.language);
        } else {
            videoView.removeSubtitles();
            applicationPreferences.setVideoSubtitlesLanguage(null);
        }

        videoView.setPlaybackSpeed(videoSettingsHelper.getCurrentSpeed().getSpeed());
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

    private void saveCurrentPosition() {
        saveVideoPosition(getCurrentPosition());
    }

    private void saveVideoPosition(int position) {
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

    private String getQualityString(VideoSettingsHelper.VideoMode videoMode) {
        return videoMode.name().toLowerCase();
    }

    public String getCurrentQualityString() {
        if (videoSettingsHelper != null) {
            return videoSettingsHelper.getCurrentQuality().name().toLowerCase();
        }
        return null;
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
